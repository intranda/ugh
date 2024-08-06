package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class MetadataTypeTest {
    private MetadataType mdt;
    private HashMap<String, String> hashMap;

    @Before
    public void setUp() {
        mdt = new MetadataType();
        hashMap = new HashMap<>();
    }

    /* Test for Constructor */
    @Test
    public void testConstructor() {
        assertNull(mdt.getAllLanguages());
        assertFalse(mdt.getIsPerson());
        assertFalse(mdt.isCorporate());
        assertFalse(mdt.isIdentifier());
        assertFalse(mdt.isAllowNameParts());
        assertFalse(mdt.isAllowNormdata());
        assertNotEquals("", mdt.getName());
        assertNull(mdt.getName());
        assertNotEquals(new String(), mdt.getNum());
        assertEquals("1o", mdt.getNum());
        assertEquals(new String(), mdt.getValidationExpression());
        assertNotNull(mdt.getValidationExpression());
        assertNull(mdt.getValidationErrorMessages());
    }

    /* Tests for the method addLanguage(String, String) */
    @Test
    public void testAddLanguageWithoutSettingAllLanguagesFirst() {
        mdt.addLanguage("de", "Deutsch");
        assertTrue(mdt.getAllLanguages().containsKey("de"));
    }

    @Test
    public void testAddLanguageGivenTwoNullAsParameters() {
        mdt.setAllLanguages(hashMap);
        assertFalse(mdt.getAllLanguages().containsKey(null));
        assertFalse(mdt.getAllLanguages().containsValue(null));
        assertEquals(0, mdt.getAllLanguages().size());
        mdt.addLanguage(null, null);
        assertEquals(hashMap, mdt.getAllLanguages());
        assertFalse(mdt.getAllLanguages().containsKey(null));
        assertFalse(mdt.getAllLanguages().containsValue(null));
        assertEquals(0, mdt.getAllLanguages().size());
    }

    @Test
    public void testAddLanguageGivenNullAsFirstParameter() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage(null, "Deutsch");
        assertFalse(mdt.getAllLanguages().containsKey(null));
        assertFalse(mdt.getAllLanguages().containsValue("Deutsch"));
        assertNull(mdt.getAllLanguages().get(null));
    }

    @Test
    public void testAddLanguageGivenNullAsSecondParameter() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", null);
        assertTrue(mdt.getAllLanguages().containsKey("de"));
        assertTrue(mdt.getAllLanguages().containsValue(null));
        assertNull(mdt.getAllLanguages().get("de"));
    }

    @Test
    public void testAddLanguageGivenNullAsSecondParameterTwice() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", null);
        mdt.addLanguage("en", null);
        assertEquals(2, mdt.getAllLanguages().size());
        assertTrue(mdt.getAllLanguages().containsKey("de"));
        assertTrue(mdt.getAllLanguages().containsKey("en"));
        assertTrue(mdt.getAllLanguages().containsValue(null));
    }

    @Test
    public void testAddLanguageGivenEmptyStringAsFirstParameter() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("", "Deutsch");
        assertTrue(mdt.getAllLanguages().containsKey(new String()));
        assertTrue(mdt.getAllLanguages().containsValue("Deutsch"));
    }

    @Test
    public void testAddLanguageGivenEmptyStringAsSecondParameter() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", new String());
        assertTrue(mdt.getAllLanguages().containsKey("de"));
        assertTrue(mdt.getAllLanguages().containsValue(""));
    }

    @Test
    public void testAddLanguageGivenEmptyStringAsFirstParameterTwice() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage(new String(), "Deutsch");
        mdt.addLanguage("", "English");
        assertTrue(mdt.getAllLanguages().containsValue("English"));
        assertFalse(mdt.getAllLanguages().containsValue("Deutsch"));
        assertEquals(1, mdt.getAllLanguages().size());
    }

    /* Tests for the method setNum(String) */
    @Test
    public void testSetNumGivenLegalInput() {
        String[] legalInputs = new String[] { "1m", "1o", "+", "*" };
        for (String input : legalInputs) {
            mdt.setNum(input);
            assertEquals(input, mdt.getNum());
        }
    }

    @Test
    public void testSetNumGivenNull() {
        mdt.setNum(null); // should we allow null?
        assertEquals("1o", mdt.getNum());
    }

    @Test
    public void testSetNumGivenEmptyString() {
        mdt.setNum(""); // nothing will be changed since "" is illegal
        assertEquals("1o", mdt.getNum());
        assertNotEquals(new String(), mdt.getNum());
    }

    @Test
    public void testSetNumGivenOnceLegalThenOnceIllegal() {
        mdt.setNum("+");
        mdt.setNum("illegal");
        assertEquals("+", mdt.getNum());
    }

    @Test
    public void testSetNumGivenOnceLegalThenOnceIllegalThenOnceLegal() {
        mdt.setNum("+");
        mdt.setNum("illegal");
        mdt.setNum("1o");
        assertEquals("1o", mdt.getNum());
    }

    @Test
    public void testSetNumGivenOnceIllegalThenOnceLegalThenOnceIllegal() {
        mdt.setNum("illegal");
        assertEquals("1o", mdt.getNum());
        mdt.setNum("1m");
        assertNotNull(mdt.getNum());
        mdt.setNum("");
        assertEquals("1m", mdt.getNum());
    }

    /* Tests for the method copy() */
    @Test
    public void testCopyGivenUninitializedOrigin() {
        MetadataType mdt2 = mdt.copy();
        assertNull(mdt2.getAllLanguages());
        assertFalse(mdt2.getIsPerson());
        assertFalse(mdt2.isCorporate());
        assertFalse(mdt2.isIdentifier());
        assertFalse(mdt2.isAllowNameParts());
        assertFalse(mdt2.isAllowNormdata());
        assertNotEquals("", mdt2.getName());
        assertNull(mdt2.getName());
        assertNotEquals(new String(), mdt2.getNum());
        assertEquals("1o", mdt2.getNum());
        assertEquals(new String(), mdt2.getValidationExpression());
        assertNotNull(mdt2.getValidationExpression());
        assertNull(mdt2.getValidationErrorMessages());
    }

    @Test
    public void testCopyGivenVaryingOrigin() {
        // Expected behavior: if you change the type, all objects based on the type are affected. - Robert
        mdt.setAllLanguages(hashMap);
        MetadataType mdt2 = mdt.copy();
        assertNotNull(mdt2.getAllLanguages());
        mdt.addLanguage("de", "Deutsch");
        assertTrue(mdt2.getAllLanguages().containsKey("de"));
        mdt2.addLanguage("de", "deutsch");
        assertEquals("deutsch", mdt.getNameByLanguage("de"));
    }

    /* Tests for the method getNameByLanguage(String) */
    @Test
    public void testGetNameByLanguageGivenUninitializedLanguageMap() {
        assertNull(mdt.getNameByLanguage(null));
        assertNull(mdt.getNameByLanguage("de"));
    }

    @Test
    public void testGetNameByLanguageGivenNullAsKeyAndValue() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage(null, null);
        assertEquals(0, mdt.getAllLanguages().size());
        assertNull(mdt.getNameByLanguage(null));
    }

    @Test
    public void testGetNameByLanguageGivenNullAsValue() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", null);
        assertEquals(1, mdt.getAllLanguages().size());
        assertNull(mdt.getNameByLanguage("de"));
    }

    @Test
    public void testGetNameByLanguageGivenNullAsKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage(null, "Deutsch");
        assertEquals(0, mdt.getAllLanguages().size());
        assertNull(mdt.getNameByLanguage(null));
    }

    @Test
    public void testGetNameByLanguageGivenUnknownKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", "Deutsch");
        assertNull(mdt.getNameByLanguage("en"));
    }

    @Test
    public void testGetNameByLanguageGivenEmptyStringAsKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("", "Deutsch");
        assertEquals("Deutsch", mdt.getNameByLanguage(new String()));
    }

    @Test
    public void testGetNameByLanguageGivenKnownKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", "Deutsch");
        assertEquals("Deutsch", mdt.getNameByLanguage("de"));
    }

    /* Tests for the method changeLanguageByName(String, String) */
    @Test
    public void testChangeLanguageByNameGivenUninitializedLanguageMap() {
        mdt.changeLanguageByName("de", "Deutsch");
        assertTrue(mdt.getAllLanguages().containsKey("de"));
    }

    @Test
    public void testChangeLanguageByNameGivenNullAsFirstParameter() {
        mdt.setAllLanguages(hashMap);
        mdt.changeLanguageByName(null, "Deutsch");
        assertFalse(mdt.getAllLanguages().containsKey(null));
    }

    @Test
    public void testChangeLanguageByNameGivenNullAsSecondParameter() {
        mdt.setAllLanguages(hashMap);
        mdt.changeLanguageByName("de", null);
        assertTrue(mdt.getAllLanguages().containsKey("de"));
        assertTrue(mdt.getAllLanguages().containsValue(null));
    }

    @Test
    public void testChangeLanguageByNameGivenNullAsSecondParameterTwice() {
        mdt.setAllLanguages(hashMap);
        mdt.changeLanguageByName("de", null);
        mdt.changeLanguageByName("de", null);
        assertEquals(1, mdt.getAllLanguages().size());
        assertTrue(mdt.getAllLanguages().containsKey("de"));
        assertTrue(mdt.getAllLanguages().containsValue(null));
    }

    /* Tests for the method removeLanguage(String) */
    @Test
    public void testRemoveLanguageGivenUninitializedLanguageMap() {
        assertFalse(mdt.removeLanguage("de"));
    }

    @Test
    public void testRemoveLanguageGivenEmptyLanguageMap() {
        mdt.setAllLanguages(hashMap);
        assertFalse(mdt.removeLanguage("de"));
        assertFalse(mdt.removeLanguage(null));
        assertFalse(mdt.removeLanguage(""));
    }

    @Test
    public void testRemoveLanguageGivenNullAsKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", "Deutsch");
        mdt.addLanguage("en", "English");
        assertFalse(mdt.removeLanguage(null));
    }

    @Test
    public void testRemoveLanguageAppliedTwice() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", "Deutsch");
        mdt.addLanguage("en", "English");
        assertTrue(mdt.removeLanguage("de"));
        assertFalse(mdt.getAllLanguages().containsKey("de"));
        assertFalse(mdt.removeLanguage("de"));
    }

    /* Tests for the method getLanguage(String) */
    @Test
    public void testGetLanguageGivenUninitializedLanguageMap() {
        assertNull(mdt.getLanguage("de"));
    }

    @Test
    public void testGetLanguageGivenEmptyLanguageMap() {
        mdt.setAllLanguages(hashMap);
        assertNull(mdt.getLanguage("de"));
        assertNull(mdt.getLanguage(null));
        assertNull(mdt.getLanguage(""));
    }

    @Test
    public void testGetLanguageGivenNullAsKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", "Deutsch");
        assertNull(mdt.getLanguage(null));
    }

    /* Tests for the method equals(MetadataType) */
    @Test
    public void testEqualsToItself() {
        assertEquals(mdt, mdt);
    }

    @Test
    public void testEqualsGivenTwoBrandNewMDTs() {
        MetadataType mdt2 = new MetadataType();
        assertNotSame(mdt, mdt2); // in the sense that they are located differently
        assertEquals(mdt2, mdt); // but according to our rewritten equals(MetadataType) method, they are equal
    }

    @Test
    public void testEqualsGivenOneNull() {
        assertNotEquals(mdt, null);
    }

    @Test
    public void testEqualsGivenOneWithNameNull() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("some name");
        assertNotEquals(mdt2, mdt);
        assertNotEquals(mdt, mdt2);
    }

    @Test
    public void testEqualsGivenBothWithEmptyNames() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("");
        mdt.setName(new String());
        assertEquals(mdt2, mdt);
        assertEquals(mdt, mdt2);
    }

    @Test
    public void testEqualsGivenDifferentNames() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("name2");
        mdt.setName("name1");
        assertNotEquals(mdt, mdt2);
    }

    @Test
    public void testEqualsGivenDifferentIsPerson() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setIsPerson(true);
        assertNotEquals(mdt, mdt2);
    }

    @Test
    public void testEqualsGivenDifferentIsIdentifier() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setIdentifier(true);
        assertNotEquals(mdt, mdt2);
    }

    @Test
    public void testEqualsGivenEqualNameAndIsPersonAndIsIdentifier() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("name");
        mdt.setName("name");
        mdt2.setIsPerson(true);
        mdt.setIsPerson(true);
        mdt2.setIdentifier(true);
        mdt.setIdentifier(true);
        assertEquals(mdt, mdt2);
    }

    @Test
    public void testEqualsTogetherWithCopy() {
        MetadataType mdt2 = mdt.copy();
        assertEquals(mdt, mdt2);
        mdt.setIdentifier(true);
        assertNotEquals(mdt, mdt2);
        mdt2.setIdentifier(true);
        assertEquals(mdt2, mdt);
    }

}
