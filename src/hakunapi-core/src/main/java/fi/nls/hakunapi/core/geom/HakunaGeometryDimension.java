package fi.nls.hakunapi.core.geom;

public enum HakunaGeometryDimension {

    XY(false, false),
	XYZ(true, false),
	// XYM(false, true), Not currently implemented
	XYZM(true, true),
	;
	
	public final boolean z;
    public final boolean m;
    
    private HakunaGeometryDimension(boolean z, boolean m) {
        this.z = z;
        this.m = m;
    }
    
    public final boolean hasZ() {
        return z;
    }
    
    public final boolean hasM() {
        return m;
    }
}
