package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RomanNumeralTest {
    private RomanNumeral numeral;
    private static final int[] integers = new int[] { 3, 4, 5, 8, 9,
            10, 13, 14, 16, 19,
            20, 40, 57, 69, 73,
            99, 127, 234, 449, 499,
            649, 777, 789, 999, 3999 };
    private static final String[] modernRom = new String[] { "III", "IV", "V", "VIII", "IX",
            "X", "XIII", "XIV", "XVI", "XIX",
            "XX", "XL", "LVII", "LXIX", "LXXIII",
            "XCIX", "CXXVII", "CCXXXIV", "CDXLIX", "CDXCIX",
            "DCXLIX", "DCCLXXVII", "DCCLXXXIX", "CMXCIX", "MMMCMXCIX" };
    private static final String[] archaicRom = new String[] { "III", "IIII", "V", "VIII", "VIIII",
            "X", "XIII", "XIIII", "XVI", "XVIIII",
            "XX", "XXXX", "LVII", "LXVIIII", "LXXIII",
            "LXXXXVIIII", "CXXVII", "CCXXXIIII", "CCCCXXXXVIIII", "CCCCLXXXXVIIII",
            "DCXXXXVIIII", "DCCLXXVII", "DCCLXXXVIIII", "DCCCCLXXXXVIIII", "MMMDCCCCLXXXXVIIII" };

    private static final int[] bigIntegers = new int[] { 4000, 4049, 4449, 4949, 4999 };
    private static final String[] bigModernRom = new String[] { "MMMM", "MMMMXLIX", "MMMMCDXLIX", "MMMMCMXLIX", "MMMMCMXCIX" };
    private static final String[] bigArchaicRom = new String[] { "MMMM", "MMMMXXXXVIIII", "MMMMCCCCXXXXVIIII", "MMMMDCCCCXXXXVIIII",
            "MMMMDCCCCLXXXXVIIII" };

    @Test
    public void testConstructorDefault() {
        numeral = new RomanNumeral();
        assertEquals(1, numeral.intValue());
        assertFalse(numeral.isArchaic());
        assertTrue(numeral.isModern());
        assertEquals("I", numeral.getNumber());
    }

    @Test
    public void testConstructorForInt() {
        for (int i = 0; i < integers.length; ++i) {
            numeral = new RomanNumeral(integers[i]);
            assertEquals(modernRom[i], numeral.getNumber());
        }
    }

    @Test
    public void testConstructorForBigInt() {
        for (int i = 0; i < bigIntegers.length; ++i) {
            numeral = new RomanNumeral(bigIntegers[i]);
            assertEquals(bigModernRom[i], numeral.getNumber());
        }
    }

    @Test
    public void testConstructorForString() {
        for (int i = 0; i < modernRom.length; ++i) {
            numeral = new RomanNumeral(modernRom[i]);
            assertEquals(integers[i], numeral.intValue());
        }
    }

    @Test
    public void testIsArchaicIsModern() {
        numeral = new RomanNumeral();
        assertEquals(numeral.isArchaic(), !numeral.isModern());
        numeral.setStyleArchaic();
        assertFalse(numeral.isModern());
        numeral.setStyleModern();
        assertTrue(numeral.isModern());
        assertFalse(numeral.isArchaic());
    }

    @Test
    public void testEquals() {
        RomanNumeral numeral1;
        RomanNumeral numeral2;
        RomanNumeral numeral3;
        for (int i = 0; i < integers.length; ++i) {
            numeral1 = new RomanNumeral(integers[i]);
            numeral2 = new RomanNumeral(modernRom[i]);
            numeral3 = new RomanNumeral(archaicRom[i]);
            assertEquals(numeral1, numeral2);
            assertEquals(numeral2, numeral3);
        }
    }

    @Test
    public void testSetValueGivenBigNumbers() {
        numeral = new RomanNumeral();
        assertThrows(NumberFormatException.class, () -> numeral.setValue(5000));
    }

    @Test
    public void testSetValueGivenZero() {
        numeral = new RomanNumeral();
        assertThrows(NumberFormatException.class, () -> numeral.setValue(0));
    }

    @Test
    public void testSetValueGivenNegativeNumbers() {
        numeral = new RomanNumeral();
        assertThrows(NumberFormatException.class, () -> numeral.setValue(-1));
    }

    @Test
    public void testSetValueGivenStyleArchaic() {
        numeral = new RomanNumeral();
        numeral.setStyleArchaic();
        for (int i = 0; i < integers.length; ++i) {
            numeral.setValue(integers[i]);
            assertEquals(archaicRom[i], numeral.getNumber());
        }
        for (int i = 0; i < bigIntegers.length; ++i) {
            numeral.setValue(bigIntegers[i]);
            assertEquals(bigArchaicRom[i], numeral.getNumber());
        }
    }
}
