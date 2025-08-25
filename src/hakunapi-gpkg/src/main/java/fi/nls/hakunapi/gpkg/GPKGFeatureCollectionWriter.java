package fi.nls.hakunapi.gpkg;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Envelope;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.gpkg.GPKGGeometryColumn.GeometryTypeName;
import fi.nls.hakunapi.gpkg.extension.RtreeIndexExtension;

public class GPKGFeatureCollectionWriter extends GPKGFeatureWriter implements FeatureCollectionWriter {

    private static final String FALLBACK_ID_FIELD = "_id";
    private static final int BATCH_LIMIT = 1024;
    private static final EnumSet<HakunaPropertyType> ALLOWED_TYPES = EnumSet.of(
            HakunaPropertyType.BOOLEAN,
            HakunaPropertyType.DATE,
            HakunaPropertyType.DOUBLE,
            HakunaPropertyType.FLOAT,
            HakunaPropertyType.INT,
            HakunaPropertyType.LONG,
            HakunaPropertyType.STRING,
            HakunaPropertyType.TIMESTAMP,
            HakunaPropertyType.TIMESTAMPTZ,
            HakunaPropertyType.UUID
    );

    private FeatureType ft;
    private String tableName;
    private Map<String, Integer> nameToSqlType;
    private Map<String, Integer> nameToIndex;
    private PreparedStatement insert;
    private Envelope envelope;
    private int batchSize;

    public GPKGFeatureCollectionWriter(File dir) {
        super(dir);
    }

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {
        this.ft = ft;
        this.tableName = layername;

        HakunaProperty id = ft.getId();
        HakunaPropertyGeometry geom = ft.getGeom();
        List<HakunaProperty> writeableProperties = ft.getProperties().stream()
                .filter(it -> it.getPropertyWriter() != HakunaPropertyWriters.HIDDEN)
                .filter(it -> ALLOWED_TYPES.contains(it.getType()))
                .collect(Collectors.toList());

        this.nameToSqlType = writeableProperties.stream()
                .collect(Collectors.toMap(p -> p.getName(), p -> getSqlType(p.getType())));
        this.nameToIndex = new HashMap<>();
        for (int i = 0; i < writeableProperties.size(); i++) {
            // 1 id, 2 geom, 3 first writable property
            nameToIndex.put(writeableProperties.get(i).getName(), i + 3);
        }

        createTable(tableName, id, geom, writeableProperties);
        this.insert = prepareInsert(layername, id, geom, writeableProperties);

        this.envelope = new Envelope();
        this.batchSize = 0;
    }

    @Override
    public void startFeature(String fid) throws Exception {
        insert.setString(1, fid);
    }

    @Override
    public void startFeature(long fid) throws Exception {
        insert.setLong(1, fid);
    }

    @Override
    public void startFeature(int fid) throws Exception {
        insert.setInt(1, fid);
    }

    @Override
    public void endFeature() throws Exception {
        insert.addBatch();
        if (++batchSize == BATCH_LIMIT) {
            insert.executeBatch();
            batchSize = 0;
        }
    }

    @Override
    public void endFeatureCollection() throws Exception {
        if (batchSize > 0) {
            insert.executeBatch();
        }
        insert.close();
        GPKGGeometryColumn geometryColumn = createGeometryColumn(ft.getGeom());
        GPKGSpatialRefSys srs = GPKGSpatialRefSystems.get(srid);
        GPKG.insertSpatialRefSys(c, srs);
        GPKGFeaturesTable tableEntry = new GPKGFeaturesTable(tableName, ft.getTitle(), ft.getDescription(), envelope, srs.getSrsId());
        GPKG.insertFeaturesTableEntry(c, tableEntry);
        GPKG.insertGeometryColumn(c, geometryColumn);
        String idColumnName = isPropertyIntegerType(ft.getId()) ? ft.getId().getName() : FALLBACK_ID_FIELD;
        RtreeIndexExtension rtree = new RtreeIndexExtension(tableName, geometryColumn.getColumn(), idColumnName);
        rtree.createExtension(c);
        c.commit();
    }
    
    private boolean isPropertyIntegerType(HakunaProperty prop) {
        return prop.getType() == HakunaPropertyType.INT || prop.getType() == HakunaPropertyType.LONG;
    }

    private GPKGGeometryColumn createGeometryColumn(HakunaPropertyGeometry geom) {
        String column = geom.getName();
        GeometryTypeName typeName = GeometryTypeName.valueOf(geom.getGeometryType().toString());
        boolean hasZ = geom.getDimension() >= 3;
        boolean hasM = geom.getDimension() >= 4;
        return new GPKGGeometryColumn(tableName, column, typeName, srid, hasZ, hasM);
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        Envelope env = geometry.boundedBy();
        envelope.expandToInclude(env);
        int headerLen = GPKGGeometry.LENGHT_XY_ENVELOPE;
        int wkbLen = geometry.getWKBLength();
        byte[] gpkgGeometry = new byte[headerLen + wkbLen];
        GPKGGeometry.write(srid, env, gpkgGeometry);
        geometry.toWKB(gpkgGeometry, headerLen);
        insert.setBytes(2, gpkgGeometry); 
    }

    @Override
    public void writeProperty(String name, String value) throws Exception {
        insert.setString(nameToIndex.get(name), value);
    }

    @Override
    public void writeProperty(String name, boolean value) throws Exception {
        insert.setBoolean(nameToIndex.get(name), value);
    }

    @Override
    public void writeProperty(String name, int value) throws Exception {
        insert.setInt(nameToIndex.get(name), value);
    }

    @Override
    public void writeProperty(String name, long value) throws Exception {
        insert.setLong(nameToIndex.get(name), value);
    }

    @Override
    public void writeProperty(String name, float value) throws Exception {
        insert.setFloat(nameToIndex.get(name), value);
    }

    @Override
    public void writeProperty(String name, double value) throws Exception {
        insert.setDouble(nameToIndex.get(name), value);
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        insert.setString(nameToIndex.get(name), value.toString());
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        insert.setString(nameToIndex.get(name), value.toString());
    }

    @Override
    public void writeNullProperty(String name) throws Exception {
        Integer i = nameToIndex.get(name);
        if (i != null) {
            int sqlType = nameToSqlType.get(name);
            insert.setNull(i, sqlType);
        }
    }

    private void createTable(String tableName, HakunaProperty id, HakunaPropertyGeometry geom, List<HakunaProperty> properties) throws SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE ").append(tableName).append(" (");
        if (isPropertyIntegerType(id)) {
            sb.append('"').append(id.getName()).append('"');
            sb.append(" INTEGER PRIMARY KEY");
        } else {
            sb.append(FALLBACK_ID_FIELD).append(" INTEGER PRIMARY KEY");
            sb.append(',');
            sb.append(id.getName()).append(' ').append(getGeoPackageDataType(id.getType())).append(" NOT NULL");
        }
        sb.append(',');
        sb.append('"').append(geom.getName()).append('"').append(' ').append(geom.getGeometryType().toString());
        if (!geom.nullable()) {
            sb.append(" NOT NULL");
        }
        for (HakunaProperty p : properties) {
            sb.append(',');
            appendColumnDefinition(sb, p);
        }
        sb.append(')');

        String createTable = sb.toString();
        GPKG.execute(c, createTable);
    }

    private void appendColumnDefinition(StringBuilder sb, HakunaProperty p) {
        sb.append('"').append(p.getName()).append('"');
        sb.append(' ');
        sb.append(getGeoPackageDataType(p.getType()));
        if (!p.nullable()) {
            sb.append(" NOT NULL");
        }
        if (p.unique()) {
            sb.append(" UNIQUE");
        }
    }

    private PreparedStatement prepareInsert(String tableName, HakunaProperty id, HakunaPropertyGeometry geom, List<HakunaProperty> properties) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" (");
        sb.append('"').append(id.getName()).append('"');
        sb.append(',').append('"').append(geom.getName()).append('"');
        for (HakunaProperty p : properties) {
            sb.append(',');
            sb.append('"');
            sb.append(p.getName());
            sb.append('"');
        }
        sb.append(") VALUES (");
        sb.append('?');
        sb.append(',').append('?');
        for (int i = 0; i < properties.size(); i++) {
            sb.append(',').append('?');
        }
        sb.append(')');
        String insert = sb.toString();
        return c.prepareStatement(insert);
    }

    private String getGeoPackageDataType(HakunaPropertyType type) {
        switch (type) {
        case BOOLEAN:
            return "BOOLEAN";
        case DATE:
            return "DATE";
        case DOUBLE:
            return "DOUBLE";
        case FLOAT:
            return "FLOAT";
        case INT:
            return "INTEGER";
        case LONG:
            return "BIGINT";
        case STRING:
            return "TEXT";
        case TIMESTAMP:
            return "DATETIME";
        case TIMESTAMPTZ:
            return "DATETIME";
        case UUID:
            return "TEXT";
        default:
            throw new IllegalArgumentException("Invalid property type");
        }
    }

    private int getSqlType(HakunaPropertyType type) {
        switch (type) {
        case BOOLEAN:
            return java.sql.Types.BOOLEAN;
        case DATE:
            return java.sql.Types.DATE;
        case DOUBLE:
            return java.sql.Types.DOUBLE;
        case FLOAT:
            return java.sql.Types.FLOAT;
        case INT:
            return java.sql.Types.INTEGER;
        case LONG:
            return java.sql.Types.BIGINT;
        case STRING:
            return java.sql.Types.VARCHAR;
        case TIMESTAMP:
            return java.sql.Types.TIMESTAMP;
        case TIMESTAMPTZ:
            return java.sql.Types.TIMESTAMP;
        case UUID:
            return java.sql.Types.OTHER;
        default:
            throw new IllegalArgumentException("Invalid property type");
        }
    }

}
