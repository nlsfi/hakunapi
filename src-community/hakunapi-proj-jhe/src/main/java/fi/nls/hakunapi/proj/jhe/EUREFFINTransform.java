package fi.nls.hakunapi.proj.jhe;

@FunctionalInterface
public interface EUREFFINTransform {

    public void transform(double x, double y, double[] out, int off);

}
