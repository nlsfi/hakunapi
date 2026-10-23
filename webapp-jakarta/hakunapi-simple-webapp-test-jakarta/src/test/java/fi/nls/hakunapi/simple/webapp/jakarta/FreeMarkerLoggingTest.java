package fi.nls.hakunapi.simple.webapp.jakarta;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import freemarker.log.Logger;
import freemarker.log._Log4jLoggerFactory;

public class FreeMarkerLoggingTest {

    // log4j-over-slf4j ships a FileAppender stub, so FreeMarker 2.3 takes it for real Log4j 1
    // and logs through the Log4j 1 API, which the bridge forwards to SLF4J
    @Test
    public void test() throws Exception {
        assertEquals(_Log4jLoggerFactory.class, Logger.getLogger("hakunapi").getClass().getEnclosingClass());
        // Only in log4j-over-slf4j, not in log4j-1.2-api
        Class.forName("org.apache.log4j.Log4jLoggerFactory");
    }

}
