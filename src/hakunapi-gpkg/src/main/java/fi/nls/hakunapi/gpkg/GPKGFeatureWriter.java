package fi.nls.hakunapi.gpkg;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.schemas.Link;

public abstract class GPKGFeatureWriter implements FeatureWriter {

    protected File dir;
    protected File file;
    protected Connection c;
    protected OutputStream out;
    protected SRIDCode srid;
    
    public GPKGFeatureWriter(File dir) {
        this.dir = dir;
    }

    @Override
    public void init(OutputStream out, SRIDCode srid) throws Exception {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException eString) {
            throw new RuntimeException("Could not init JDBC driver - driver not found");
        }
        this.out = out;
        this.srid = srid;
        this.file = File.createTempFile("temporary", ".gpkg", dir);
        this.file.deleteOnExit();
        this.c = DriverManager.getConnection("jdbc:sqlite:" + file.toPath().toString());
        GPKG.init(c);
        c.setAutoCommit(false);
    }

    @Override
    public void close() {
        try {
            // Write file to out
            try {
                c.commit();
            } catch (SQLException e) {
                // Ignore
            }
            try {
                c.close();
            } catch (SQLException e) {
                // Ignore
            }
            try {
                Files.copy(file.toPath(), out);
            } catch (IOException e) {
                // Ignore
            }
        } finally {
            file.delete();
        }
    }
    
    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        // NOP
    }

    @Override
    public void writeTimeStamp() throws Exception {
        // NOP
    }

    @Override
    public void writeLinks(List<Link> link) throws Exception {
        // NOP
    }

    @Override
    public void writeNumberReturned(int numberReturned) throws Exception {
        // NOP
    }

    @Override
    public void writeAttribute(String name, String value) throws Exception {
        writeProperty(name, value);
    }

    @Override
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
        // NOP
        // Single Geometry Per Feature
    }

    @Override
    public void writeStartObject(String name) throws Exception {
        // NOP
        // Complex properties not supported in GPKG
    }

    @Override
    public void writeCloseObject() throws Exception {
        // NOP
        // Complex properties not supported in GPKG
    }

    @Override
    public void writeStartArray(String name) throws Exception {
        // NOP
        // Complex properties not supported in GPKG
    }

    @Override
    public void writeCloseArray() throws Exception {
        // NOP
        // Complex properties not supported in GPKG
    }

}
