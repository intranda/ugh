package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

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

}
