package fi.nls.hakunapi.simple.servlet.param;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class GetTilesUtilTest {

	@Test
	public void testCollectionFilterSplit() {
		
		String input = "kohdeluokka[tiesymboli]";
		
		String[] parts = input.split("[\\W]");
		
		assertNotNull(parts);
		assertTrue(parts.length==2);
		assertTrue(parts[0].equals("kohdeluokka"));
		assertTrue(parts[1].equals("tiesymboli"));
		
	}
	
	@Test
	public void testCollectionFilterSplit2() {
		
		String input = "kohdeluokka/tiesymboli";
		
		String[] parts = input.split("[\\W]");
		
		assertNotNull(parts);
		assertTrue(parts.length==2);
		assertTrue(parts[0].equals("kohdeluokka"));
		assertTrue(parts[1].equals("tiesymboli"));
		
	}
	
	@Test
	public void testFailCollectionFilterSplit() {
		
		String input = "kohdeluokkatiesymboli";
		
		String[] parts = input.split("[\\W]");
		
		assertNotNull(parts);
		assertTrue(parts.length==1);		
		
	}
}
