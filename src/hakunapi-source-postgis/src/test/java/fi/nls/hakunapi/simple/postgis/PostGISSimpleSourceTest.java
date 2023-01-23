package fi.nls.hakunapi.simple.postgis;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

public class PostGISSimpleSourceTest {

    @Test
    @Ignore("Windows specific")
    public void testAbsolutePath() {
        Path relative = Paths.get("C:/foo/bar/tilanne.properties");
        assertEquals(Paths.get("C:/foo/bar/foo.properties"), Paths.get(PostGISSimpleSource.getDataSourcePath(relative, "foo")));
    }

    @Test
    public void testRelativePath() {
        Path relative = Paths.get("tilanne.properties");
        assertEquals(Paths.get("foo.properties"), Paths.get(PostGISSimpleSource.getDataSourcePath(relative, "foo")));
    }

}
