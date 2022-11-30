package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ugh.exceptions.ContentFileAreaTypeUnknownException;

public class ContentFileAreaTest {
    private ContentFileArea testArea;
    private ContentFileArea anotherArea;

    @Before
    public void setUp() {
        testArea = new ContentFileArea();
        anotherArea = new ContentFileArea();
    }

    @Test
    public void testSetType() throws ContentFileAreaTypeUnknownException {
        String[] types = new String[] { "coordinates", "byteoffset", "smtpe", "xmlid" };
        for (String type : types) {
            testArea.setType(type);
            assertEquals(type, testArea.getType());
        }
        assertThrows(ContentFileAreaTypeUnknownException.class, () -> testArea.setType("unknown"));
    }

    @Test
    public void testDefaultValues() {
        assertNull(testArea.getType());
        assertNull(testArea.getFrom());
        assertNull(testArea.getTo());
    }

    // Tests for the method equals(ContentFileArea)
    @Test
    public void testEqualsGivenBothNullValues() {
        assertTrue(testArea.equals(anotherArea));
    }

    @Test
    public void testEqualsGivenOneNullType() throws ContentFileAreaTypeUnknownException {
        anotherArea.setType("xmlid");
        assertFalse(testArea.equals(anotherArea));
        assertFalse(anotherArea.equals(testArea));
    }

    @Test
    public void testEqualsGivenOneNullFrom() {
        anotherArea.setFrom("from");
        assertFalse(testArea.equals(anotherArea));
    }

    @Test
    public void testEqualsGivenOneEmptyFrom() {
        anotherArea.setFrom("");
        assertFalse(anotherArea.equals(testArea));
    }

    @Test
    public void testEqualsGivenOneNullTo() {
        anotherArea.setTo("to");
        assertFalse(anotherArea.equals(testArea));
    }

    @Test
    public void testEqualsGivenOneEmptyTo() {
        anotherArea.setTo(new String());
        assertFalse(testArea.equals(anotherArea));
    }

    @Test
    public void testEqualsGivenTypeButEmptyFromTo() throws ContentFileAreaTypeUnknownException {
        testArea.setType("xmlid");
        testArea.setFrom("");
        testArea.setTo(new String());
        anotherArea.setType("smtpe");
        anotherArea.setFrom(new String());
        anotherArea.setTo("");
        assertFalse(testArea.equals(anotherArea));
        anotherArea.setType("xmlid");
        assertTrue(testArea.equals(anotherArea));
    }

    @Test
    public void testEqualsGivenNormalValues() throws ContentFileAreaTypeUnknownException {
        testArea.setType("xmlid");
        anotherArea.setType("xmlid");
        testArea.setFrom("from");
        anotherArea.setFrom("another from");
        testArea.setTo("to");
        anotherArea.setTo("to");
        assertFalse(testArea.equals(anotherArea));
        anotherArea.setFrom("from");
        assertTrue(testArea.equals(anotherArea));
    }



}
