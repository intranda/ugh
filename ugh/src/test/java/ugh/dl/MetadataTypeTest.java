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
import org.junit.Ignore;
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
        assertNull(mdt.getNum());
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
        assertTrue(mdt.getAllLanguages().containsKey(null));
        assertTrue(mdt.getAllLanguages().containsValue(null));
        assertEquals(1, mdt.getAllLanguages().size());
        mdt.addLanguage(null, null); // add it once more
        assertEquals(1, mdt.getAllLanguages().size());
    }

    @Test
    public void testAddLanguageGivenNullAsFirstParameter() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage(null, "Deutsch");
        assertTrue(mdt.getAllLanguages().containsKey(null));
        assertTrue(mdt.getAllLanguages().containsValue("Deutsch"));
    }

    @Test
    public void testAddLanguageGivenNullAsSecondParameter() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", null);
        assertTrue(mdt.getAllLanguages().containsKey("de"));
        assertTrue(mdt.getAllLanguages().containsValue(null));
    }

    @Test
    public void testAddLanguageGivenNullAsFirstParameterTwice() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage(null, "Deutsch");
        mdt.addLanguage(null, "English");
        assertEquals(1, mdt.getAllLanguages().size());
        assertTrue(mdt.getAllLanguages().containsKey(null));
        assertTrue(mdt.getAllLanguages().containsValue("English"));
        assertFalse(mdt.getAllLanguages().containsValue("Deutsch"));
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

    @Ignore("The logic in the method cannot pass this test. Null check needed.")
    @Test
    public void testSetNumGivenNull() {
        // TODO fix check in setNum
        mdt.setNum(null);
        assertNull(mdt.getNum());
    }

    @Test
    public void testSetNumGivenEmptyString() {
        mdt.setNum(""); // nothing will be changed since "" is illegal
        assertNull(mdt.getNum());
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
        assertNull(mdt.getNum());
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
        assertNull(mdt2.getNum());
        assertEquals(new String(), mdt2.getValidationExpression());
        assertNotNull(mdt2.getValidationExpression());
        assertNull(mdt2.getValidationErrorMessages());
    }

    @Ignore("The logic in the method cannot pass this test. Deep copy needed.")
    @Test
    // TODO this is the expected behavior. If you change the type, all objects based on the type are affected. Change test
    public void testCopyGivenVaryingOrigin() {
        mdt.setAllLanguages(hashMap);
        MetadataType mdt2 = mdt.copy();
        assertNotNull(mdt2.getAllLanguages());
        mdt.addLanguage("de", "Deutsch");
        assertFalse(mdt2.getAllLanguages().containsKey("de"));
        mdt2.addLanguage("de", "deutsch");
        assertEquals("Deutsch", mdt.getNameByLanguage("de"));
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
        assertEquals(1, mdt.getAllLanguages().size());
        assertNull(mdt.getNameByLanguage(null));
    }

    @Ignore("Should we allow null as value?")
    @Test
    public void testGetNameByLanguageGivenNullAsValue() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", null);
        assertNull(mdt.getNameByLanguage("de"));
    }

    @Ignore("Should we allow null as key?")
    @Test
    public void testGetNameByLanguageGivenNullAsKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage(null, "Deutsch");
        assertEquals(1, mdt.getAllLanguages().size());
        assertEquals("Deutsch", mdt.getNameByLanguage(null));
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
        assertTrue(mdt.getAllLanguages().containsKey(null));
    }

    //@Ignore("The logic in the method cannot pass this test. Null check needed in the method removeLanguage(String).")
    @Test
    public void testChangeLanguageByNameGivenNullAsFirstParameterTwice() {
        mdt.setAllLanguages(hashMap);
        mdt.changeLanguageByName(null, "Deutsch");
        mdt.changeLanguageByName(null, "deutsch");
        assertEquals(1, mdt.getAllLanguages().size());
        assertTrue(mdt.getAllLanguages().containsValue("deutsch"));
        assertFalse(mdt.getAllLanguages().containsValue("Deutsch"));
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
    public void testRemoveLanguageGivenNullAsUnknownKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", "Deutsch");
        mdt.addLanguage("en", "English");
        assertFalse(mdt.removeLanguage(null));
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed.")
    @Test
    public void testRemoveLanguageGivenNullAsKnownKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage(null, "Deutsch");
        assertEquals(1, mdt.getAllLanguages().size());
        assertTrue(mdt.removeLanguage(null));
        assertFalse(mdt.getAllLanguages().containsKey(null));
        assertEquals(0, mdt.getAllLanguages().size());
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
    public void testGetLanguageGivenUnknownNullAsKey() {
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage("de", "Deutsch");
        assertNull(mdt.getLanguage(null));
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed.")
    @Test
    public void testGetLanguageGivenKnownNullAsKey() {
        // TODO instantiate allLanguages if null
        mdt.setAllLanguages(hashMap);
        mdt.addLanguage(null, "Deutsch");
        assertEquals("Deutsch", mdt.getLanguage(null));
    }

    /* Tests for the method equals(MetadataType) */
    @Test
    public void testEqualsToItself() {
        assertTrue(mdt.equals(mdt));
    }

    @Test
    public void testEqualsGivenTwoBrandNewMDTs() {
        MetadataType mdt2 = new MetadataType();
        assertNotEquals(mdt, mdt2); // mdt and mdt2 are different objects
        assertNotSame(mdt, mdt2); // in the sense that they are located differently
        assertTrue(mdt2.equals(mdt)); // but according to our rewritten equals(MetadataType) method, they are equal
    }

    @Test
    public void testEqualsGivenOneNull() {
        assertFalse(mdt.equals(null));
    }

    @Test
    public void testEqualsGivenOneWithNameNull() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("some name");
        assertFalse(mdt2.equals(mdt));
        assertFalse(mdt.equals(mdt2));
    }

    @Test
    public void testEqualsGivenBothWithEmptyNames() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("");
        mdt.setName(new String());
        assertTrue(mdt2.equals(mdt));
        assertTrue(mdt.equals(mdt2));
    }

    @Test
    public void testEqualsGivenDifferentNames() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("name2");
        mdt.setName("name1");
        assertFalse(mdt.equals(mdt2));
    }

    @Test
    public void testEqualsGivenDifferentIsPerson() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setIsPerson(true);
        assertFalse(mdt.equals(mdt2));
    }

    @Test
    public void testEqualsGivenDifferentIsIdentifier() {
        MetadataType mdt2 = new MetadataType();
        mdt2.setIdentifier(true);
        assertFalse(mdt.equals(mdt2));
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
        assertTrue(mdt.equals(mdt2));
    }

    @Test
    public void testEqualsTogetherWithCopy() {
        MetadataType mdt2 = mdt.copy();
        assertTrue(mdt.equals(mdt2));
        mdt.setIdentifier(true);
        assertFalse(mdt.equals(mdt2));
        mdt2.setIdentifier(true);
        assertTrue(mdt2.equals(mdt));
        mdt2.setCorporate(true); // the value of isCorporate plays no role in the comparison
        assertTrue(mdt2.equals(mdt));
    }

}

