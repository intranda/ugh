package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        assertEquals(namePart, namePart);
    }

    @Test
    public void testEqualsGivenNull() {
        namePart = new NamePart();
        assertNotEquals(namePart, null);
    }

    @Test
    public void testEqualsGivenOtherObjectClass() {
        namePart = new NamePart();
        NamePart2 namePart2 = new NamePart2();
        assertNotEquals(namePart, namePart2);
        assertNotEquals(namePart, namePart2);
    }

    @Test
    public void testEqualsGivenUninitalizedObjects() {
        namePart = new NamePart();
        NamePart anotherPart = new NamePart(null, null);
        assertEquals(namePart, anotherPart);
    }

    @Test
    public void testEqualsGivenOneInitializedEmptyObject() {
        namePart = new NamePart("", new String());
        NamePart anotherPart = new NamePart();
        assertNotEquals(namePart, anotherPart);
        assertNotEquals(anotherPart, namePart);
    }

    @Test
    public void testEqualsGivenOneInitializedObject() {
        namePart = new NamePart("type", "value");
        NamePart anotherPart = new NamePart();
        assertNotEquals(namePart, anotherPart);
        assertNotEquals(anotherPart, namePart);
        anotherPart.setType("type");
        anotherPart.setValue("value");
        assertEquals(namePart, anotherPart);
        assertEquals(anotherPart, namePart);
    }

    @Test
    public void testEqualsGivenObjectifiedParameter() {
        namePart = new NamePart("type", "value");
        NamePart anotherPart = new NamePart("type", "value");
        Object anotherObject = anotherPart;
        assertEquals(namePart, anotherObject);
        assertEquals(anotherObject, namePart);
    }

    private class NamePart2 extends NamePart {

        private static final long serialVersionUID = -861371056416644295L;
        // intentionally left blank, since we just need its class name for testing the method equals(Object)
    }

}
