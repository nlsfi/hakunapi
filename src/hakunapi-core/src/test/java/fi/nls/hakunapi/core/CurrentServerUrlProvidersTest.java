package fi.nls.hakunapi.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class CurrentServerUrlProvidersTest {

    @Test
    public void testStatic() {
        String pattern = "http://www.domain.com/features";
        String expected = pattern;
        assertEquals(expected, CurrentServerUrlProviders.from(pattern).get(__ -> null));
    }

    @Test
    public void testDynamic() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Forwarded-Host", "www.domain.com");

        String pattern = "https://${X-Forwarded-Host}/features";
        String expected = "https://www.domain.com/features";
        assertEquals(expected, CurrentServerUrlProviders.from(pattern).get(headers::get));
    }

    @Test
    public void testStartsWithDynamic() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Forwarded-Proto", "https");
        headers.put("X-Forwarded-Host", "www.domain.com");

        String pattern = "${X-Forwarded-Proto}://${X-Forwarded-Host}/features";
        String expected = "https://www.domain.com/features";
        assertEquals(expected, CurrentServerUrlProviders.from(pattern).get(headers::get));
    }

    @Test
    public void testEndsWithDynamic() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Forwarded-Host", "www.domain.com");
        headers.put("X-Forwarded-Path", "not-features");

        String pattern = "https://${X-Forwarded-Host}/${X-Forwarded-Path}";
        String expected = "https://www.domain.com/not-features";
        assertEquals(expected, CurrentServerUrlProviders.from(pattern).get(headers::get));
    }

}
