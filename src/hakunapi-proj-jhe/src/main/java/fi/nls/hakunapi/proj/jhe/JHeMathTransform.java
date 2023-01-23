package fi.nls.hakunapi.proj.jhe;

@FunctionalInterface
public interface JHeMathTransform {

    public void transform(double[] src, int srcOff, double[] dst, int dstOff, int n);

}
