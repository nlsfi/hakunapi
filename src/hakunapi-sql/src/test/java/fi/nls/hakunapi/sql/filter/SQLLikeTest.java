package fi.nls.hakunapi.sql.filter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fi.nls.hakunapi.sql.filter.SQLLike;

public class SQLLikeTest {
    
    @Test
    public void testReplaceWildcards() {
        char wild = '*';
        char single = '?';
        char escape = '@';
        
        assertEquals("foobar", SQLLike.replaceWildcards("foobar", wild, single, escape));
        
        assertEquals("foobar%", SQLLike.replaceWildcards("foobar*", wild, single, escape));
        assertEquals("foo%bar%", SQLLike.replaceWildcards("foo*bar*", wild, single, escape));
        assertEquals("foo*bar", SQLLike.replaceWildcards("foo@*bar", wild, single, escape));
        assertEquals("foo\\%bar", SQLLike.replaceWildcards("foo%bar", wild, single, escape));
        assertEquals("foo\\%bar", SQLLike.replaceWildcards("foo@%bar", wild, single, escape));
        
        assertEquals("foobar_", SQLLike.replaceWildcards("foobar?", wild, single, escape));
        assertEquals("fo_o_bar", SQLLike.replaceWildcards("fo?o?bar", wild, single, escape));
        assertEquals("foo?bar", SQLLike.replaceWildcards("foo@?bar", wild, single, escape));
        assertEquals("foo\\_bar", SQLLike.replaceWildcards("foo_bar", wild, single, escape));
        assertEquals("foo\\_bar", SQLLike.replaceWildcards("foo@_bar", wild, single, escape));
        
        assertEquals("foobar", SQLLike.replaceWildcards("foobar@", wild, single, escape));
        assertEquals("foobar", SQLLike.replaceWildcards("foo@bar", wild, single, escape));
        assertEquals("foo@bar", SQLLike.replaceWildcards("foo@@bar", wild, single, escape));
        assertEquals("foo\\\\bar", SQLLike.replaceWildcards("foo@\\bar", wild, single, escape)); // This is actually 'foo\\bar' <=> 'foo@\bar' but Java eats backslashes
        
        wild = '%';
        single = '?';
        escape = '\\';
        
        assertEquals("foobar", SQLLike.replaceWildcards("foobar", wild, single, escape));
        
        assertEquals("foobar%", SQLLike.replaceWildcards("foobar%", wild, single, escape));
        assertEquals("foo%bar%", SQLLike.replaceWildcards("foo%bar%", wild, single, escape));
        assertEquals("foo\\%bar", SQLLike.replaceWildcards("foo\\%bar", wild, single, escape));
        
        assertEquals("foobar_", SQLLike.replaceWildcards("foobar?", wild, single, escape));
        assertEquals("fo_o_bar", SQLLike.replaceWildcards("fo?o?bar", wild, single, escape));
        assertEquals("foo?bar", SQLLike.replaceWildcards("foo\\?bar", wild, single, escape));
        assertEquals("foo\\_bar", SQLLike.replaceWildcards("foo_bar", wild, single, escape));
        assertEquals("foo\\_bar", SQLLike.replaceWildcards("foo\\_bar", wild, single, escape));
        
        assertEquals("foobar", SQLLike.replaceWildcards("foobar\\", wild, single, escape));
        assertEquals("foobar", SQLLike.replaceWildcards("foo\\bar", wild, single, escape));
        assertEquals("foo\\\\bar", SQLLike.replaceWildcards("foo\\\\bar", wild, single, escape)); // This is actually 'foo\\bar' <=> 'foo@\bar' but Java eats backslashes
    }

}
