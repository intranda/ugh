package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Random;

import org.junit.Test;

public class NamePartTest {
    private NamePart namePart;

    /* Tests for Constructors */
    @Test
    public void testConstructorDefault() {
        namePart = new NamePart();
        assertNull(namePart.getType());
        assertNull(namePart.getValue());
    }

    @Test
    public void testConstructorWithNull() {
        namePart = new NamePart(null, null);
        assertNull(namePart.getType());
        assertNull(namePart.getValue());
    }

    @Test
    public void testConstructorWithEmptyStrings() {
        namePart = new NamePart("", new String());
        assertNotNull(namePart.getType());
        assertEquals(new String(), namePart.getType());
        assertNotNull(namePart.getValue());
        assertEquals("", namePart.getValue());
    }

    /* Test for the method hashCode() */
    @Test
    public void testHashCode() {
        final int sampleNum = 17;
        String[] types = new String[sampleNum];
        int[] typesHashCodes = new int[sampleNum];
        String[] values = new String[sampleNum];
        int[] valuesHashCodes = new int[sampleNum];
        for (int i = 0; i < sampleNum; ++i) {
            // generate random strings for our test
            byte[] array = new byte[7]; // the string length is bounded by 7
            new Random().nextBytes(array);
            String generatedString = new String(array, Charset.forName("UTF-8"));
            types[i] = generatedString;
            typesHashCodes[i] = generatedString.hashCode();
            values[i] = generatedString.toUpperCase();
            valuesHashCodes[i] = values[i].hashCode();
        }
        
        // type == null
        for (int i = 0; i < sampleNum; ++i) {
            namePart = new NamePart(null, values[i]);
            assertEquals(31 * 31 + valuesHashCodes[i], namePart.hashCode());
        }

        // value == null
        for (int i = 0; i < sampleNum; ++i) {
            namePart = new NamePart(types[i], null);
            assertEquals(31 * (31 + typesHashCodes[i]), namePart.hashCode());
        }

        // type != null && value != null
        for (int i = 0; i < sampleNum; ++i) {
            namePart = new NamePart(types[i], values[i]);
            assertEquals(31 * (31 + typesHashCodes[i]) + valuesHashCodes[i], namePart.hashCode());
        }
    }

    /* Tests for the method equals(Object) */
    @Test
    public void testEqualsToItself() {
        namePart = new NamePart();
        assertTrue(namePart.equals(namePart));
    }

    @Test
    public void testEqualsGivenNull() {
        namePart = new NamePart();
        assertFalse(namePart.equals(null));
    }

    @Test
    public void testEqualsGivenOtherObjectClass() {
        namePart = new NamePart();
        NamePart2 namePart2 = new NamePart2();
        assertFalse(namePart.equals(namePart2));
        assertFalse(namePart.equals((NamePart) namePart2));
    }

    @Test
    public void testEqualsGivenUninitalizedObjects() {
        namePart = new NamePart();
        NamePart anotherPart = new NamePart(null, null);
        assertTrue(namePart.equals(anotherPart));
    }

    @Test
    public void testEqualsGivenOneInitializedEmptyObject() {
        namePart = new NamePart("", new String());
        NamePart anotherPart = new NamePart();
        assertFalse(namePart.equals(anotherPart));
        assertFalse(anotherPart.equals(namePart));
    }

    @Test
    public void testEqualsGivenOneInitializedObject() {
        namePart = new NamePart("type", "value");
        NamePart anotherPart = new NamePart();
        assertFalse(namePart.equals(anotherPart));
        assertFalse(anotherPart.equals(namePart));
        anotherPart.setType("type");
        anotherPart.setValue("value");
        assertTrue(namePart.equals(anotherPart));
        assertTrue(anotherPart.equals(namePart));
    }

    @Test
    public void testEqualsGivenObjectifiedParameter() {
        namePart = new NamePart("type", "value");
        NamePart anotherPart = new NamePart("type", "value");
        Object anotherObject = (Object) anotherPart;
        assertTrue(namePart.equals(anotherObject));
        assertTrue(anotherObject.equals(namePart));
    }

    private class NamePart2 extends NamePart {
        // intentionally left blank, since we just need its class name for testing the method equals(Object)
    }


}
