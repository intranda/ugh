package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DocStructTypeTest {

    private DocStructType dsType;

    @Before
    public void setUp() {
        this.dsType = new DocStructType();
    }

    /* Tests for the constructor */
    @Test
    public void testConstructor() {
        // this.allChildrenTypes
        assertNotNull(dsType.getAllAllowedDocStructTypes());
        assertEquals(0, dsType.getAllAllowedDocStructTypes().size());
        // this.allMetadataTypes
        assertNotNull(dsType.getAllMetadataTypes());
        assertEquals(0, dsType.getAllMetadataTypes().size());
        // this.allMetadataGroups

        // this.allLanguages
        assertNotNull(dsType.getAllLanguages());
        assertEquals(0, dsType.getAllLanguages().size());

        // default fields
        assertNull(dsType.getName());
        assertFalse(dsType.isAnchor());
        assertTrue(dsType.hasFileSet());
        assertFalse(dsType.isTopmost());
    }

    /* Tests for setters and getters */
    @Ignore("Should we allow null to be set as name?")
    @Test
    public void testSetNameGivenNull() {

    }

    @Ignore("Should we allow empty string to be set as name?")
    @Test
    public void testSetNameGivenEmptyString() {

    }

    @Test
    public void testTheFieldHasfileset() {
        // there are two methods to get the value of hasfileset
        assertTrue(dsType.hasFileSet());
        assertTrue(dsType.isHasfileset());
        // there are also two methods to set the value of hasfileset
        dsType.setHasfileset(false);
        assertFalse(dsType.hasFileSet());
        dsType.setHasFileSet(true);
        assertTrue(dsType.isHasfileset());
    }

    @Test
    public void testTheFieldTopmost() {
        // there are two methods to get the value of topmost, one of which is deprecated
        assertFalse(dsType.isTopmost());
        // there are also two methods to set the value of topmost, one of which is also deprecated
        dsType.setTopmost(true);
        assertTrue(dsType.isTopmost());
    }

    @Test
    public void testTheFieldIsanchor() {
        assertFalse(dsType.isAnchor());
        dsType.isAnchor(true);
        assertTrue(dsType.isAnchor());
    }

    @Test
    public void testTheFieldAllLanguages() {
        HashMap<String, String> in = new HashMap<>();
        dsType.setAllLanguages(in);
        assertSame(in, dsType.getAllLanguages());
    }

    /* Tests for the following methods:
     * 1. addLanguage(String, String) 
     * 2. getNameByLanguage(String)
     * 3. changeLanguageByName(String, String)
     * 4. removeLanguage(String)
     * */
    @Ignore("The logic in the method cannot pass this test. Null check for the first argument needed.")
    @Test
    public void testAddLanguageGivenNullAsKey() {
        // JavaDoc of this method should be updated!
        // null should not be used as key
        assertEquals(0, dsType.getAllLanguages().size());
        dsType.addLanguage(null, "Deutsch");
        assertEquals(0, dsType.getAllLanguages().size());
        assertFalse(dsType.getAllLanguages().containsKey(null));
    }

    @Test
    public void testAddLanguageGivenEmptyStringAsKey() {
        // "" should be allowed as key
        assertEquals(0, dsType.getAllLanguages().size());
        dsType.addLanguage("", "Deutsch");
        assertEquals(1, dsType.getAllLanguages().size());
        assertTrue(dsType.getAllLanguages().containsKey(""));
        assertEquals("Deutsch", dsType.getNameByLanguage(""));
    }

    @Test
    public void testAddLanguageGivenNullAsValue() {
        // null should be allowed as value
        assertEquals(0, dsType.getAllLanguages().size());
        dsType.addLanguage("de", null);
        assertEquals(1, dsType.getAllLanguages().size());
        assertTrue(dsType.getAllLanguages().containsValue(null));
        assertNull(dsType.getNameByLanguage("de"));
    }

    @Test
    public void testAddLanguageGivenEmptyStringAsValue() {
        // "" should be allowed as value
        assertEquals(0, dsType.getAllLanguages().size());
        dsType.addLanguage("de", "");
        assertEquals(1, dsType.getAllLanguages().size());
        assertTrue(dsType.getAllLanguages().containsValue(""));
        assertEquals("", dsType.getNameByLanguage("de"));
    }

    @Ignore("Applying addLanguage twice with the same key behaves like updating, but should it be so?")
    @Test
    public void testAddLanguageGivenSameKeyButDifferentValueTwice() {
        // According to the logic in the method changeLanguageByName(String, String), 
        // this updating behavior might not be expected. 
        dsType.addLanguage("de", "deutsch");
        assertEquals("deutsch", dsType.getNameByLanguage("de"));
        dsType.addLanguage("de", "Deutsch");
        assertEquals("Deutsch", dsType.getNameByLanguage("de"));
    }

    @Test
    public void testGetNameByLanguageGivenUnavailableLanguage() {
        assertFalse(dsType.getAllLanguages().containsKey("en"));
        assertNull(dsType.getNameByLanguage("en"));
    }

    @Ignore("Should we return null instead when the translation is empty?")
    @Test
    public void testGetNameByLanguageGivenEmptyTranslation() {
        dsType.addLanguage("de", "");
        assertNotNull(dsType.getNameByLanguage("de"));
    }
    
    @Ignore("The logic in the method cannot pass this test. Null check for the first parameter needed.")
    @Test
    public void testChangeLanguageByNameGivenNullAsKey() {
        // null should not be allowed as key
        dsType.addLanguage("de", "Deutsch");
        assertEquals(1, dsType.getAllLanguages().size());
        dsType.changeLanguageByName(null, "deutsch");
        assertEquals(1, dsType.getAllLanguages().size());
        assertFalse(dsType.getAllLanguages().containsKey(null));
    }

    @Test
    public void testChangeLanguageByNameGivenEmptyStringAsKey() {
        // "" should be allowed as key
        dsType.addLanguage("", "deutsch");
        assertEquals(1, dsType.getAllLanguages().size());
        assertEquals("deutsch", dsType.getNameByLanguage(""));
        dsType.changeLanguageByName("", "Deutsch");
        assertEquals(1, dsType.getAllLanguages().size());
        assertEquals("Deutsch", dsType.getNameByLanguage(""));
    }

    @Test
    public void testChangeLanguageByNameGivenNullAsValue() {
        // null should be allowed as value
        dsType.addLanguage("de", "deutsch");
        assertEquals(1, dsType.getAllLanguages().size());
        assertNotNull(dsType.getNameByLanguage("de"));
        dsType.changeLanguageByName("de", null);
        assertEquals(1, dsType.getAllLanguages().size());
        assertNull(dsType.getNameByLanguage("de"));
    }

    @Test
    public void testChangeLanguageByNameGivenEmptyStringAsValue() {
        // "" should be allowed as value
        dsType.addLanguage("de", "deutsch");
        assertEquals(1, dsType.getAllLanguages().size());
        assertNotEquals("", dsType.getNameByLanguage("de"));
        dsType.changeLanguageByName("de", "");
        assertEquals(1, dsType.getAllLanguages().size());
        assertEquals("", dsType.getNameByLanguage("de"));
    }

    @Ignore("The logic in the method cannot pass this test. Existence check needed. See the comments below.")
    @Test
    public void testChangeLanguageByNameGivenUnavailableKey() {
        // If we could add a new language by applying this method with an unavailable key, 
        // then the method addLanguage(String) would be redundant and the codes would be more unpredictable.
        dsType.addLanguage("de", "deutsch");
        assertEquals(1, dsType.getAllLanguages().size());
        assertFalse(dsType.getAllLanguages().containsKey("en"));
        dsType.changeLanguageByName("en", "english");
        assertEquals(1, dsType.getAllLanguages().size());
        assertFalse(dsType.getAllLanguages().containsKey("en"));
    }

    @Test
    public void testRemoveLanguageGivenNull() {
        // since null is not allowed as key, trying to remove it should always return false
        assertFalse(dsType.removeLanguage(null));
    }

    @Test
    public void testRemoveLanguageGivenEmptyString() {
        // since "" is allowed as key, trying to remove it should return true when it exists as a key
        assertFalse(dsType.getAllLanguages().containsKey(""));
        assertFalse(dsType.removeLanguage(""));
        dsType.addLanguage("", "deutsch");
        assertTrue(dsType.getAllLanguages().containsKey(""));
        assertTrue(dsType.removeLanguage(""));
        assertFalse(dsType.getAllLanguages().containsKey(""));
    }

    /* Tests for the following methods:
     * 1. setAllMetadataTypes(List<MetadataType>) 
     * 2. getAllMetadataTypes()
     * 3. getAllDefaultDisplayMetadataTypes()
     * */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testSetAllMetadataTypesGivenNull() {
        // null should be regarded as empty list
        assertEquals(0, dsType.getAllMetadataTypes().size());
        dsType.setAllMetadataTypes(null);
        assertEquals(0, dsType.getAllMetadataTypes().size());
        // hence if we already have a non-empty list of allMetadataTypes
        ArrayList<MetadataType> newList = new ArrayList<>();
        newList.add(new MetadataType());
        dsType.setAllMetadataTypes(newList);
        assertEquals(1, dsType.getAllMetadataTypes().size());
        // then we should reset the allMetadataTypes to empty by applying this method with null again
        dsType.setAllMetadataTypes(null);
        assertEquals(0, dsType.getAllMetadataTypes().size());
    }

    @Ignore("The logic in the method cannot pass this test. Reinitialization of the field allMetadataTypes needed.")
    @Test
    public void testSetAllMetadataTypesGivenEmptyList() {
        assertEquals(0, dsType.getAllMetadataTypes().size());
        ArrayList<MetadataType> emptyList = new ArrayList<>();
        dsType.setAllMetadataTypes(emptyList);
        assertEquals(0, dsType.getAllMetadataTypes().size());
        // if we already have a non-empty list of allMetadataTypes
        ArrayList<MetadataType> newList = new ArrayList<>();
        newList.add(new MetadataType());
        dsType.setAllMetadataTypes(newList);
        assertEquals(1, dsType.getAllMetadataTypes().size());
        // then we should reset the allMetadataTypes to empty by applying this method with emptyList again
        dsType.setAllMetadataTypes(emptyList);
        assertEquals(0, dsType.getAllMetadataTypes().size());
    }

    @Ignore("The logic in the method cannot pass this test. Reinitialization of the field allMetadataTypes needed.")
    @Test
    public void testSetAllMetadataTypesGivenSameListTwice() {
        ArrayList<MetadataType> newList = new ArrayList<>();
        newList.add(new MetadataType());
        assertEquals(0, dsType.getAllMetadataTypes().size());
        dsType.setAllMetadataTypes(newList);
        assertEquals(1, dsType.getAllMetadataTypes().size());
        dsType.setAllMetadataTypes(newList);
        assertEquals(1, dsType.getAllMetadataTypes().size());
    }

    @Test
    public void testSetGetAllMetadataTypesAgainstModificationFromOutside() {
        ArrayList<MetadataType> newList = new ArrayList<>();
        newList.add(new MetadataType());
        assertEquals(1, newList.size());
        dsType.setAllMetadataTypes(newList);
        assertEquals(1, dsType.getAllMetadataTypes().size());
        // modify newList from outside, and that should have no influence on dsType
        newList.add(new MetadataType());
        assertEquals(2, newList.size());
        assertEquals(1, dsType.getAllMetadataTypes().size());
        // modify the result of getAllMetadataTypes from outside, and that should have no influence on dsType
        dsType.getAllMetadataTypes().add(new MetadataType());
        assertEquals(1, dsType.getAllMetadataTypes().size());
    }

    @Ignore("Is this method useful at all? I have no idea how to modify the field defaultDisplay. See the comments below.")
    @Test
    public void testGetAllDefaultDisplayMetadataTypesAgainstModificationFromOutside() {
        ArrayList<MetadataType> newList = new ArrayList<>();
        MetadataType mdt1 = new MetadataType();
        mdt1.setName("MetadataType 1");
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("MetadataType 2");
        newList.add(mdt1);
        newList.add(mdt2);
        // the method setAllMetadataTypes creates a MetadataTypeForDocStructType object based on every incoming MetadataType object
        // and the method getAllMetadataTypes retrieves the MetadataType object out of every MetadataTypeForDocStructType object
        dsType.setAllMetadataTypes(newList);
        assertEquals(2, dsType.getAllMetadataTypes().size());
        assertEquals("MetadataType 1", dsType.getAllMetadataTypes().get(0).getName());
        // since false is the default value for defaultDisplay whenever we create a MetadataTypeForDocStructType object based on
        // a MetadataType object, a call of the method getAllDefaultDisplayMetadataTypes should theoretically give us nothing unless
        // we can somehow manage to modify some values of the field defaultDisplay
        assertFalse(new MetadataTypeForDocStructType(mdt1).isDefaultdisplay());
        assertEquals(0, dsType.getAllDefaultDisplayMetadataTypes().size());
        // since the MetadataType objects retrieved by the method getAllMetadataTypes are still connected to those
        // MetadataTypeForDocStructType objects, so theoretically we can still modify the value of the field defaultDisplay
        // if only we can somehow manage to cast

    }

    /* Tests for the method getNumberOfMetadataType(PrefsType) */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testGetNumberOfMetadataTypeGivenNull() {
        // allMetadataTypes is still empty
        assertEquals(0, dsType.getAllMetadataTypes().size());
        assertEquals("0", dsType.getNumberOfMetadataType(null));
        // set allMetadataTypes to a nonempty list
        ArrayList<MetadataType> newList = new ArrayList<>();
        newList.add(new MetadataType());
        dsType.setAllMetadataTypes(newList);
        assertEquals(1, dsType.getAllMetadataTypes().size());
        assertEquals("0", dsType.getNumberOfMetadataType(null));
    }

    @Ignore("The logic in the method cannot pass this test. Instead of '0' there is always a null by default returned.")
    @Test
    public void testGetNumberOfMetadataTypeGivenNormalInput() {
        ArrayList<MetadataType> newList = new ArrayList<>();
        String[] nums = new String[] { "1o", "1m", "+", "*" };
        for (String num : nums) {
            MetadataType mdt = new MetadataType();
            mdt.setName("normal_" + num);
            mdt.setNum(num);
            newList.add(mdt);
        }
        String[] specialNums = new String[] { null, "0", "Bazinga!" };
        for (String num : specialNums) {
            MetadataType mdt = new MetadataType();
            mdt.setName("special_" + num);
            mdt.setNum(num);
            newList.add(mdt);
        }
        dsType.setAllMetadataTypes(newList);
        List<MetadataType> types = dsType.getAllMetadataTypes();
        assertEquals(nums.length + specialNums.length, types.size());
        for (int i = 0; i < nums.length; ++i) {
            assertEquals(nums[i], types.get(i).getNum());
        }
        for (int j = 0; j < specialNums.length; ++j) {
            assertEquals("0", types.get(j + nums.length).getNum());
        }
    }

    /* Tests for the following methods:
     * 1. addMetadataType(MetadataType, String)
     * 2. addMetadataType(MetadataType, String, boolean, boolean) [ Will Be Omitted Since It Is Almost The Same As 1. ]
     * 3. removeMetadataType(MetadataType)
     * 4. removeMetadataType(String)
     * 5. getMetadataTypeByType(PrefsType)
     * 6. isMetadataTypeAlreadyAvailable(PrefsType) [ PRIVATE ]
     *  */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddMetadataTypeGivenNullAsFirstArgument() {
        assertEquals(0, dsType.getAllMetadataTypes().size());
        dsType.addMetadataType(null, "num");
        assertEquals(0, dsType.getAllMetadataTypes().size());
    }

    @Test
    public void testAddMetadataTypeGivenNullAsSecondArgument() {
        assertEquals(0, dsType.getAllMetadataTypes().size());
        dsType.addMetadataType(new MetadataType(), null);
        assertEquals(1, dsType.getAllMetadataTypes().size());
    }

    @Test
    public void testAddMetadataTypeGivenSameNamedMetadataTypeObjectTwice() {
        MetadataType mdt = new MetadataType();
        mdt.setName("name");
        mdt.setNum("*");
        assertEquals(0, dsType.getAllMetadataTypes().size());
        assertNotNull(dsType.addMetadataType(mdt, "1o"));
        // "1o" will be set as num to the MetadataTypeForDocStructType object created from the incoming MetadataType object mdt
        // the num of the MetadataType object will not be affected
        assertEquals(1, dsType.getAllMetadataTypes().size());
        assertEquals("*", dsType.getAllMetadataTypes().get(0).getNum());
        assertNull(dsType.addMetadataType(mdt, "+"));
        // changing the second argument should not change the fact that nothing will be created and added again
        // i.e. the num of the MetadataTypeForDocStructType object based on the incoming MetadataType object should remain unchanged
        // but I have not found a way to achieve this. - Zehong
        assertEquals(1, dsType.getAllMetadataTypes().size());
        assertEquals("*", dsType.getAllMetadataTypes().get(0).getNum());
    }

    @Ignore("The logic in the private method isMetadataTypeAlreadyAvailable(PrefsType) cannot pass this test. Modification needed.")
    @Test
    public void testAddMetadataTypeGivenSameUnnamedMetadataTypeObjectTwice() {
        MetadataType mdt = new MetadataType();
        assertEquals(0, dsType.getAllMetadataTypes().size());
        dsType.addMetadataType(mdt, "num");
        assertEquals(1, dsType.getAllMetadataTypes().size());
        dsType.addMetadataType(mdt, "num");
        assertEquals(1, dsType.getAllMetadataTypes().size());
    }

    @Test
    public void testAddMetadataTypeAgainstModificationFromOutside() {
        MetadataType mdt = new MetadataType();
        mdt.setName("name");
        mdt.setNum("*");
        assertEquals(0, dsType.getAllMetadataTypes().size());
        assertNotNull(dsType.addMetadataType(mdt, "1o"));
        assertEquals(1, dsType.getAllMetadataTypes().size());
        assertEquals("*", dsType.getAllMetadataTypes().get(0).getNum());
        // changing num of the original MetadataType object will not affect dsType
        mdt.setNum("+");
        assertEquals("*", dsType.getAllMetadataTypes().get(0).getNum());
        // but we can still modify the MetadataType object of our dsType from outside
        dsType.getAllMetadataTypes().get(0).setNum("+");
        assertEquals("+", dsType.getAllMetadataTypes().get(0).getNum());
    }
    
    @Test
    public void testRemoveMetadataTypeGivenNull() {
        // since calling the method removeMetadataType with null is ambiguous
        // no specific test is needed here anymore
    }

    @Test
    public void testRemoveMetadataTypeGivenUnnamedMetadataType() {
        MetadataType mdt = new MetadataType();
        assertEquals(0, dsType.getAllMetadataTypes().size());
        assertNotSame(mdt, dsType.addMetadataType(mdt, "1o"));
        assertEquals(1, dsType.getAllMetadataTypes().size());
        assertTrue(dsType.removeMetadataType(mdt));
        assertEquals(0, dsType.getAllMetadataTypes().size());
        // try to remove it again
        assertFalse(dsType.removeMetadataType(mdt));
    }

    @Ignore("This test actually passes. But what is the point to prepare an always-false-returning method?")
    @Test
    public void testRemoveMetadataTypeGivenAnyString() {
        MetadataType mdt = new MetadataType();
        mdt.setName("name");
        dsType.addMetadataType(mdt, "+");
        assertEquals("name", dsType.getMetadataTypeByType(mdt).getName());
        String[] args = new String[] { "", new String(), mdt.getName(), "anything else" };
        for (String arg: args) {
            assertFalse(dsType.removeMetadataType(arg));
        }
    }

    @Test
    public void testGetMetadataTypeByTypeGivenNull() {
        assertNull(dsType.getMetadataTypeByType(null));
    }

    @Ignore("The logic in the method cannot pass this test. Comparison logic needs modification to avoid the NullPointerException.")
    @Test
    public void testGetMetadataTypeByTypeGivenExistingUnnamedMetadataType() {
        MetadataType mdt = new MetadataType();
        dsType.addMetadataType(mdt, "*");
        assertNotNull(dsType.getMetadataTypeByType(mdt));
    }

    @Test
    public void testGetMetadataTypeByTypeGivenUnexistingNamedMetadataType() {
        MetadataType mdt = new MetadataType();
        mdt.setName("name");
        assertNull(dsType.getMetadataTypeByType(mdt));
    }

    @Test
    public void testGetMetadataTypeByTypeGivenExistingNamedMetadataType() {
        MetadataType mdt = new MetadataType();
        mdt.setName("name");
        dsType.addMetadataType(mdt, "+");
        assertNotNull(dsType.getMetadataTypeByType(mdt));
        // now remove mdt and try to get it again
        dsType.removeMetadataType(mdt);
        assertNull(dsType.getMetadataTypeByType(mdt));
    }

    /* Tests for the following methods:
     * 1. addDocStructTypeAsChild(String)
     * 2. removeDocStructTypeAsChild(String)
     * 3. getAllAllowedDocStructTypes()
     *  */

}

