package com.entry_level_jobs.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobFetchServiceNormalizeUrlTest {

    private String normalize(String url) throws Exception {
        JobFetchService svc = new JobFetchService(null, null, null);
        Method m = JobFetchService.class.getDeclaredMethod("normalizeUrl", String.class);
        m.setAccessible(true);
        return (String) m.invoke(svc, url);
    }

    @Test
    public void testBasicWwwAndTrailingSlash() throws Exception {
        assertEquals("https://example.com/path", normalize("https://www.Example.COM/path/"));
    }

    @Test
    public void testDefaultPortAndMultipleSlashesAndPercent() throws Exception {
        String input = "HTTP://Example.COM:80//a//B/%7Euser";
        assertEquals("http://example.com/a/b/~user", normalize(input));
    }

    @Test
    public void testNoScheme() throws Exception {
        assertEquals("http://example.com/path", normalize("example.com/path"));
    }

    @Test
    public void testQueryAndFragmentStripped() throws Exception {
        assertEquals("https://example.com/path", normalize("https://www.example.com/path?query=1#frag"));
    }

    @Test
    public void testPercentEncodingCanonicalization() throws Exception {
        // %C3%A9 is 'é'
        assertEquals("https://example.com/%C3%A9", normalize("https://example.com/%c3%a9"));
    }

    @Test
    public void testRootKeepsSlash() throws Exception {
        assertEquals("https://example.com/", normalize("https://WWW.EXAMPLE.COM/"));
    }
}

