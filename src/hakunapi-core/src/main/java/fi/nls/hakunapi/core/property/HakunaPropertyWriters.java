package fi.nls.hakunapi.core.property;

import java.time.ZoneOffset;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.ObjectArrayValueContainer;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.util.DToA;

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
        return getSimplePropertyWriter(name, type, true);
    }

    /**
     * The writer for one property, with the null check resolved here instead of
     * per row: a property declared non-nullable gets a variant that never calls
     * {@link ValueProvider#isNull(int)}. For the primitive types the value is
     * read through the {@code getPrimitive*} accessors, so a source that can
     * hand out a primitive never has to box it.
     */
    public static HakunaPropertyWriter getSimplePropertyWriter(String name, HakunaPropertyType type, boolean nullable) {
        switch (type) {
        case BOOLEAN:
            return nullable
                    ? (vp, i, writer) -> {
                        if (vp.isNull(i)) {
                            writer.writeNullProperty(name);
                        } else {
                            writer.writeProperty(name, vp.getPrimitiveBoolean(i));
                        }
                    }
                    : (vp, i, writer) -> writer.writeProperty(name, vp.getPrimitiveBoolean(i));
        case INT:
            return nullable
                    ? (vp, i, writer) -> {
                        if (vp.isNull(i)) {
                            writer.writeNullProperty(name);
                        } else {
                            writer.writeProperty(name, vp.getPrimitiveInt(i));
                        }
                    }
                    : (vp, i, writer) -> writer.writeProperty(name, vp.getPrimitiveInt(i));
        case LONG:
            return nullable
                    ? (vp, i, writer) -> {
                        if (vp.isNull(i)) {
                            writer.writeNullProperty(name);
                        } else {
                            writer.writeProperty(name, vp.getPrimitiveLong(i));
                        }
                    }
                    : (vp, i, writer) -> writer.writeProperty(name, vp.getPrimitiveLong(i));
        case DOUBLE:
            return nullable
                    ? (vp, i, writer) -> {
                        if (vp.isNull(i)) {
                            writer.writeNullProperty(name);
                        } else {
                            writer.writeProperty(name, vp.getPrimitiveDouble(i));
                        }
                    }
                    : (vp, i, writer) -> writer.writeProperty(name, vp.getPrimitiveDouble(i));
        case FLOAT:
            return nullable
                    ? (vp, i, writer) -> {
                        if (vp.isNull(i)) {
                            writer.writeNullProperty(name);
                        } else {
                            writer.writeProperty(name, vp.getPrimitiveFloat(i));
                        }
                    }
                    : (vp, i, writer) -> writer.writeProperty(name, vp.getPrimitiveFloat(i));
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
            return (vp, i, writer) -> writeStartFeature(ft, layerName, writer, vp.getPrimitiveInt(i));
        case LONG:
            return (vp, i, writer) -> writeStartFeature(ft, layerName, writer, vp.getPrimitiveLong(i));
        case STRING:
        case UUID:
            return (vp, i, writer) -> writeStartFeature(ft, layerName, writer, vp.getObject(i).toString());
        case DOUBLE:
            return (vp, i, writer) -> writeStartFeature(ft, layerName, writer, doubleAsID(vp.getPrimitiveDouble(i)));
        default:
            throw new IllegalArgumentException("Invalid type for id property");
        }
    }

    protected static String doubleAsID(double d) {
        byte[] b = new byte[24];
        int len = DToA.dtoa(d, b, 0, 0, 8);
        return new String(b, 0, len);
    }

    private static void writeStartFeature(FeatureType ft, String layerName, FeatureWriter writer, int fid) throws Exception {
        if (writer instanceof FeatureCollectionWriter) {
            ((FeatureCollectionWriter) writer).startFeature(fid);
        } else {
            ((SingleFeatureWriter) writer).startFeature(ft, layerName, fid);
        }
    }

    private static void writeStartFeature(FeatureType ft, String layerName, FeatureWriter writer, long fid) throws Exception {
        if (writer instanceof FeatureCollectionWriter) {
            ((FeatureCollectionWriter) writer).startFeature(fid);
        } else {
            ((SingleFeatureWriter) writer).startFeature(ft, layerName, fid);
        }
    }

    private static void writeStartFeature(FeatureType ft, String layerName, FeatureWriter writer, String fid) throws Exception {
        if (writer instanceof FeatureCollectionWriter) {
            ((FeatureCollectionWriter) writer).startFeature(fid);
        } else {
            ((SingleFeatureWriter) writer).startFeature(ft, layerName, fid);
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
