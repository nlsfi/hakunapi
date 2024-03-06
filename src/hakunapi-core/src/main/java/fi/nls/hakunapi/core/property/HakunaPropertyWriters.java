package fi.nls.hakunapi.core.property;

import java.time.ZoneOffset;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.ObjectArrayValueContainer;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.ValueProvider;

public final class HakunaPropertyWriters {

    public static final HakunaPropertyWriter HIDDEN = (vp, i, writer) -> { };

    public static HakunaPropertyWriter getHiddenPropertyWriter() {
        return HIDDEN;
    }

    /* Not implemented
    public static HakunaPropertyWriter getStructPropertyWriter(String name, List<HakunaProperty> properties) {
        return (vp, i, writer) -> {
            if (vp.isNull(i)) {
                writer.writeNullProperty(name);
            } else {
                ValueContainer v = (ValueContainer) vp.getObject(i);
                writer.writeStartObject(name);
                i = 0;
                for (HakunaProperty prop : properties) {
                    prop.write(v, i++, writer);
                }
                writer.writeCloseObject();
            }
            return i + 1;
        };
    }
    */

    public static HakunaPropertyWriter getArrayPropertyWriter(String name, HakunaPropertyWriter wrapped) {
        return (vp, i, writer) -> {
            if (vp.isNull(i)) {
                writer.writeNullProperty(name);
            } else {
                Object[] arr = (Object[]) vp.getArray(i);
                writer.writeStartArray(name);
                ValueProvider tmp = ObjectArrayValueContainer.wrap(arr);
                int j = 0;
                int n = tmp.size();
                while (j < n) {
                    wrapped.write(tmp, j++, writer);
                }
                writer.writeCloseArray();
            }
        };
    }

    public static HakunaPropertyWriter getSimplePropertyWriter(String name, HakunaPropertyType type) {
        switch (type) {
        case BOOLEAN:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getBoolean(i));
                }
            };
        case INT:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getInt(i));
                }
            };
        case LONG:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getLong(i));
                }
            };
        case DOUBLE:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getDouble(i));
                }
            };
        case FLOAT:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getFloat(i));
                }
            };
        case DATE:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getLocalDate(i));
                }
            };
        case TIMESTAMP:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getLocalDateTime(i).toInstant(ZoneOffset.UTC));
                }
            };
        case TIMESTAMPTZ:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getInstant(i));
                }
            };
        case STRING:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getString(i));
                }
            };
        case UUID:
            return (vp, i, writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeProperty(name, vp.getUUID(i).toString());
                }
            };
        case JSON:
            return (vp,i,writer) -> {
                if (vp.isNull(i)) {
                    writer.writeNullProperty(name);
                } else {
                    writer.writeJsonProperty(name, vp.getJSON(i));                        
                }                
            };
        default:
            throw new IllegalArgumentException();
        }
    }

    public static HakunaPropertyWriter getIdPropertyWriter(FeatureType ft, String layerName, String name, HakunaPropertyType type) {
        switch (type) {
        case INT:
            return (vp, i, writer) -> {
                writeStartFeature(ft, layerName, writer, type, vp.getInt(i));
            };
        case LONG:
            return (vp, i, writer) -> {
                writeStartFeature(ft, layerName, writer, type, vp.getLong(i));
            };
        case STRING:
        case UUID:
            return (vp, i, writer) -> {
                writeStartFeature(ft, layerName, writer, type, vp.getObject(i).toString());
            };
        case DOUBLE:
            return (vp, i, writer) -> {
                writeStartFeature(ft, layerName, writer, type, vp.getObject(i).toString());
            };
        default:
            throw new IllegalArgumentException("Invalid type for id property");
        }
    }
    
    private static void writeStartFeature(FeatureType ft, String layerName, FeatureWriter writer, HakunaPropertyType type, Object value) throws Exception {
        if (writer instanceof FeatureCollectionWriter) {
            FeatureCollectionWriter fcWriter = (FeatureCollectionWriter) writer;
            if (type == HakunaPropertyType.INT) {
                int fid = ((Number) value).intValue();
                fcWriter.startFeature(fid);
            } else if (type == HakunaPropertyType.LONG) {
                long fid = ((Number) value).longValue();
                fcWriter.startFeature(fid);
            } else {
                fcWriter.startFeature(value.toString());
            }
        } else {
            SingleFeatureWriter singleWriter = (SingleFeatureWriter) writer;
            if (type == HakunaPropertyType.INT) {
                int fid = ((Number) value).intValue();
                singleWriter.startFeature(ft, layerName, fid);
            } else if (type == HakunaPropertyType.LONG) {
                long fid = ((Number) value).longValue();
                singleWriter.startFeature(ft, layerName, fid);
            } else {
                singleWriter.startFeature(ft, layerName, value.toString());
            }
        }
    }

    public static HakunaPropertyWriter getGeometryPropertyWriter(String name, boolean isDefault) {
        if (isDefault) {
            return (vp, i, w) -> {
                if (vp.isNull(i)) {
                    w.writeGeometry(name, null);
                } else {
                    w.writeGeometry(name, vp.getHakunaGeometry(i));
                }
            };
        } else {
            return (vp, i, w) -> {
                if (vp.isNull(i)) {
                    w.writeNullProperty(name);
                } else {
                    w.writeProperty(name, vp.getHakunaGeometry(i));
                }
            };
        }
    }

}
