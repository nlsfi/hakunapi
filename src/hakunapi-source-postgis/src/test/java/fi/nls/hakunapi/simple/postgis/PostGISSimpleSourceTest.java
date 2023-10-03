package fi.nls.hakunapi.simple.postgis;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

import fi.nls.hakunapi.core.config.HakunaConfigParser;

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

    @Test
    public void testReadingInlinedDBProperties() {
        Properties props = new Properties();
        props.put("db", "my_db");
        props.put("db.my_db.datasource.url", "localhost");
        props.put("db.my_db.datasource.port", "5432");
        props.put("db.my_db.datasource.whatever", "no-you");
        props.put("db.my_db.whatever", "yeah");

        Properties toPassToHikari = PostGISSimpleSource.getInlinedDBProperties(new HakunaConfigParser(props), "my_db");

        assertEquals(4, toPassToHikari.size());
        assertEquals("localhost", toPassToHikari.get("datasource.url"));
        assertEquals("5432", toPassToHikari.get("datasource.port"));
        assertEquals("no-you", toPassToHikari.get("datasource.whatever"));
        assertEquals("yeah", toPassToHikari.get("whatever"));
    }

    @Test
    public void testReadingExternalDBProperties() {
        String oldMyProp = System.getProperty("my.prop");
        System.setProperty("my.prop", "yeah");

        PostGISSimpleSource pg = new PostGISSimpleSource();
        Properties properties = pg.loadProperties("test.properties");
        assertEquals(4, properties.size());
        assertEquals("localhost", properties.get("datasource.url"));
        assertEquals("5432", properties.get("datasource.port"));
        assertEquals("no-you", properties.get("datasource.whatever"));
        assertEquals("yeah", properties.get("whatever"));

        if (oldMyProp == null) {
            System.clearProperty("my.prop");
        } else {
            System.setProperty("my.prop", oldMyProp);
        }
    }

}
