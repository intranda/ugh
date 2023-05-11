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
        // TODO: yes
    }

    @Ignore("Should we allow empty string to be set as name?")
    @Test
    public void testSetNameGivenEmptyString() {
        // TODO: yes
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

    @Test
    public void testGetNameByLanguageGivenEmptyTranslation() {
        dsType.addLanguage("de", "");
        assertEquals("", dsType.getNameByLanguage("de"));
    }

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

    @Test
    public void testChangeLanguageByNameGivenUnavailableKey() {
        dsType.addLanguage("de", "deutsch");
        assertEquals(1, dsType.getAllLanguages().size());
        assertFalse(dsType.getAllLanguages().containsKey("en"));
        dsType.changeLanguageByName("en", "english");
        assertEquals(2, dsType.getAllLanguages().size());
        assertTrue(dsType.getAllLanguages().containsKey("en"));
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

    @Test
    public void testSetAllMetadataTypesGivenNull() {
        // null should be regarded as empty list
        assertEquals(0, dsType.getAllMetadataTypes().size());
        dsType.setAllMetadataTypes(null);
        assertEquals(0, dsType.getAllMetadataTypes().size());
    }

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
    }

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
        // we could somehow manage to modify some values of the field defaultDisplay
        assertFalse(new MetadataTypeForDocStructType(mdt1).isDefaultdisplay());
        assertEquals(0, dsType.getAllDefaultDisplayMetadataTypes().size());
        // until now I have found only way to add a MetadataTypeForDocStructType object whose field defaultDisplay is set to true,
        // that is to use the method addMetadataType(MetadataType, String, boolean, boolean)
        // for those already added into the list allMetadataTypes, I have found no way to modify their field defaultDisplay
        MetadataType mdt3 = new MetadataType();
        mdt3.setName("MetadataType 3");
        assertNotNull(dsType.addMetadataType(mdt3, null, true, false));
        assertEquals(1, dsType.getAllDefaultDisplayMetadataTypes().size());
        assertTrue(mdt3.equals(dsType.getAllDefaultDisplayMetadataTypes().get(0)));
    }

    /* Tests for the method getNumberOfMetadataType(PrefsType) */

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
            assertEquals("1o", types.get(j + nums.length).getNum());
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

    @Test
    public void testAddMetadataTypeGivenSameUnnamedMetadataTypeObjectTwice() {
        MetadataType mdt = new MetadataType();
        mdt.setName("fixture");
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

    @Test
    public void testGetMetadataTypeByTypeGivenNull() {
        assertNull(dsType.getMetadataTypeByType(null));
    }

    @Test
    public void testGetMetadataTypeByTypeGivenExistingUnnamedMetadataType() {
        MetadataType mdt = new MetadataType();
        mdt.setName("fixture");
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
     * 2. addDocStructTypeAsChild(DocStructType)
     * 3. removeDocStructTypeAsChild(String)
     * 4. removeDocStructTypeAsChild(DocStructType)
     * 5. getAllAllowedDocStructTypes()
     *  */

    @Test
    public void testAddDocStructTypeAsChildGivenNull() {
        // First of all, nothing to do with the following test, but I think the first checking condition in Line 611 is redundant. - Zehong

        // To the method design:
        // 1. "null" is not addable, which also makes sense, and that is assured via the trick that applying this method on null would be ambiguous.
        // 2. In this way, unnamed DocStructType would also be non-addable.

        // Hence there is no need to test anything here.
    }

    @Test
    public void testAddDocStructTypeAsChildGivenEmptyString() {
        // I have no idea if that is a good idea to allow empty string as name for a DocStructType object, but let's assume yes:
        assertTrue(dsType.addDocStructTypeAsChild(""));
    }

    @Test
    public void testAddDocStructTypeAsChildGivenSameStringTwice() {
        // The same name should not be added twice:
        assertEquals(0, dsType.getAllAllowedDocStructTypes().size());
        assertTrue(dsType.addDocStructTypeAsChild("name"));
        assertEquals(1, dsType.getAllAllowedDocStructTypes().size());
        assertFalse(dsType.addDocStructTypeAsChild("name"));
        assertEquals(1, dsType.getAllAllowedDocStructTypes().size());
    }

    @Test
    public void testAddDocStructTypeAsChildGivenTwoDocStructTypeObjectsWithTheSameName() {
        // According to the checking logic in Line 611, the names in the list allChildrenTypes will all be unique: therefore we still have
        // to assure that every DocStructType is uniquely named.
        DocStructType child1 = new DocStructType();
        child1.setName("child");
        child1.isAnchor(false);
        DocStructType child2 = new DocStructType();
        child2.setName("child");
        child2.isAnchor(true);
        assertNotSame(child1.isAnchor(), child2.isAnchor());
        // once child1 is added into dsType.allChildrenTypes, child2 will not be addable anymore
        assertTrue(dsType.addDocStructTypeAsChild(child1));
        assertFalse(dsType.addDocStructTypeAsChild(child2));
    }

    @Test
    public void testAddDocStructTypeAsChildGivenANameStringAndADocStructTypeObjectOfThatName() {
        String name1 = "name1";
        String name2 = "name2";
        DocStructType child1 = new DocStructType();
        child1.setName(name1);
        DocStructType child2 = new DocStructType();
        child2.setName(name2);
        // 1. first apply the method on name1, then check if child1 is still addable:
        assertTrue(dsType.addDocStructTypeAsChild(name1));
        assertFalse(dsType.addDocStructTypeAsChild(child1));
        // 2. first apply the method on child2, then check if name2 is still addable:
        assertTrue(dsType.addDocStructTypeAsChild(child2));
        assertFalse(dsType.addDocStructTypeAsChild(name2));
    }

    @Test
    public void testRemoveDocStructTypeAsChildGivenNull() {
        // To the method design:
        // 1. "null" is an illegal argument, since that would make the call ambiguous.
        // 2. In this way, unnamed DocStructType would also be non-removable.

        // Hence there is no need to test anything here.
    }

    @Test
    public void testRemoveDocStructTypeAsChildGivenEmptyString() {
        // It should work like a normal string:
        assertFalse(dsType.removeDocStructTypeAsChild(""));
        assertEquals(0, dsType.getAllAllowedDocStructTypes().size());
        assertTrue(dsType.addDocStructTypeAsChild(""));
        assertEquals(1, dsType.getAllAllowedDocStructTypes().size());
        assertTrue(dsType.removeDocStructTypeAsChild(""));
        assertEquals(0, dsType.getAllAllowedDocStructTypes().size());
    }

    @Test
    public void testRemoveDocStructTypeAsChildGivenSameStringTwice() {
        String name = "name";
        assertTrue(dsType.addDocStructTypeAsChild(name));
        assertTrue(dsType.removeDocStructTypeAsChild(name));
        assertFalse(dsType.removeDocStructTypeAsChild(name));
    }

    @Test
    public void testRemoveDocStructTypeAsChildGivenMixedStringNameAndSoNamedDocStructTypeObjects() {
        String name = "name";
        DocStructType child1 = new DocStructType();
        child1.setName(name);
        child1.isAnchor(false);
        DocStructType child2 = new DocStructType();
        child2.setName(name);
        child2.isAnchor(true);
        // 1. first add using name, then remove using child1, finally try to remove using child2
        assertTrue(dsType.addDocStructTypeAsChild(name));
        assertTrue(dsType.removeDocStructTypeAsChild(child1));
        assertFalse(dsType.removeDocStructTypeAsChild(child2));

        // 2. first add using child1, then remove using child2, finally try to remove using name
        assertTrue(dsType.addDocStructTypeAsChild(child1));
        assertTrue(dsType.removeDocStructTypeAsChild(child2));
        assertFalse(dsType.removeDocStructTypeAsChild(name));

        // 3. first add using child2, then remove using name, finally try to remove using child1
        assertTrue(dsType.addDocStructTypeAsChild(child2));
        assertTrue(dsType.removeDocStructTypeAsChild(name));
        assertFalse(dsType.removeDocStructTypeAsChild(child1));
    }

    @Test
    public void testGetAllAllowedDocStructTypesAgainstModificationFromOutside() {
        // According to the design of the method addDocStructTypeAsChild, "null" and unnamed DocStructType objects are not addable,
        // however, we can still achieve that with help of the method getAllAllowedDocStructTypes in the following way:
        assertEquals(0, dsType.getAllAllowedDocStructTypes().size());
        dsType.getAllAllowedDocStructTypes().add(null);
        assertEquals(1, dsType.getAllAllowedDocStructTypes().size());
        // Since it is also syntactically assured that the method removeDocStructTypeAsChild cannot be applied on "null" nor unnamed
        // DocStructType objects, this modification from outside would cause a memory leak, unless we are aware of the cause and call:
        dsType.getAllAllowedDocStructTypes().remove(null);
        assertEquals(0, dsType.getAllAllowedDocStructTypes().size());
        // The design of this method would also allow the entrance of redundant elements into allChildrenTypes, which is not allowed using
        // the method addDocStructTypeAsChild. There would be a problem if you removed some DocStructType from the allChildrenTypes
        //  without awareness that there is still a copy of it in the list resulted from some dummy modification from outside via this method:
        dsType.addDocStructTypeAsChild("name");
        assertFalse(dsType.addDocStructTypeAsChild("name"));
        dsType.getAllAllowedDocStructTypes().add("name");
        assertEquals(2, dsType.getAllAllowedDocStructTypes().size());
        assertTrue(dsType.removeDocStructTypeAsChild("name"));
        assertEquals(1, dsType.getAllAllowedDocStructTypes().size());
        assertTrue(dsType.getAllAllowedDocStructTypes().contains("name"));
        // Hence in this way one has to remove it twice:
        assertTrue(dsType.removeDocStructTypeAsChild("name"));
        assertEquals(0, dsType.getAllAllowedDocStructTypes().size());
    }

    /* Tests for the method toString() */

    @Test
    public void testToStringGivenUnnamedDocStructType() {
        assertEquals(null, dsType.toString());
    }

    /* Tests for the following methods:
     * 1. setAllMetadataGroups(List<MetadataGroupType>)
     * 2. getAllMetadataGroupTypes()
     * 3. getAllDefaultDisplayMetadataGroups()
     *  */

    @Test
    public void testSetAllMetadataGroupsGivenNull() {
        // null should be regarded as empty list
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        dsType.setAllMetadataGroups(null);
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        // hence if we already have a non-empty list allMetadataGroups
        ArrayList<MetadataGroupType> mdgTypes = new ArrayList<>();
        mdgTypes.add(new MetadataGroupType());
        dsType.setAllMetadataGroups(mdgTypes);
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // then applying the method setAllMetadataGroups again with null should clear the contents of allMetadataGroups
        dsType.setAllMetadataGroups(null);
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
    }

    @Test
    public void testSetAllMetadataGroupsGivenEmptyList() {
        ArrayList<MetadataGroupType> emptyList = new ArrayList<>();
        ArrayList<MetadataGroupType> mdgTypes = new ArrayList<>();
        mdgTypes.add(new MetadataGroupType());

        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        dsType.setAllMetadataGroups(emptyList);
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        // if we already have a non-empty list allMetadataGroups
        dsType.setAllMetadataGroups(mdgTypes);
        assertEquals(mdgTypes.size(), dsType.getAllMetadataGroupTypes().size());
        // then applying the method setAllMetadataGroups again with emptyList should clear the contents of allMetadataGroups
        dsType.setAllMetadataGroups(emptyList);
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
    }

    @Test
    public void testSetAllMetadataGroupsGivenSameListTwice() {
        ArrayList<MetadataGroupType> mdgTypes = new ArrayList<>();
        mdgTypes.add(new MetadataGroupType());

        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        dsType.setAllMetadataGroups(mdgTypes);
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // applying the method setAllMetadataGroups again with mdgTypes should not change the contents of allMetadataGroups
        dsType.setAllMetadataGroups(mdgTypes);
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
    }

    @Test
    public void testSetGetAllMetadataGroupsAgainstModificationFromOutside() {
        ArrayList<MetadataGroupType> mdgTypes = new ArrayList<>();
        mdgTypes.add(new MetadataGroupType());
        assertEquals(1, mdgTypes.size());
        dsType.setAllMetadataGroups(mdgTypes);
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // modification of the original list mdgTypes should not affect the field dsType.allMetadataGroups
        mdgTypes.add(new MetadataGroupType());
        assertEquals(2, mdgTypes.size());
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // modification of the returned value of getAllMetadataGroupTypes() should not affect the field dsType.allMetadataGroups
        List<MetadataGroupType> returned = dsType.getAllMetadataGroupTypes();
        assertEquals(1, returned.size());
        returned.add(new MetadataGroupType());
        assertEquals(2, returned.size());
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
    }

    @Test
    public void testGetAllDefaultDisplayMetadataGroupsAgainstModificationFromOutside() {
        ArrayList<MetadataGroupType> mdgTypes = new ArrayList<>();
        MetadataGroupType mdgType1 = new MetadataGroupType();
        mdgType1.setName("MetadataGroupType 1");
        MetadataGroupType mdgType2 = new MetadataGroupType();
        mdgType2.setName("MetadataGroupType 2");
        mdgTypes.add(mdgType1);
        mdgTypes.add(mdgType2);
        // the method setAllMetadataGroups(List<MetadataGroupType>) will construct a MetadataGroupForDocStructType object based
        // on every incoming MetadataGroupType object, and by default the field defaultdisplay will all be set to false
        dsType.setAllMetadataGroups(mdgTypes);
        assertEquals(mdgTypes.size(), dsType.getAllMetadataGroupTypes().size());
        assertEquals(0, dsType.getAllDefaultDisplayMetadataGroups().size());
        // until now I have only found one way to add a MetadataGroupForDocStructType object whose defaultdisplay field is set to true
        // into the list allMetadataGroups, and that is via the method addMetadataGroup(MetadataGroupType, String, boolean, boolean)
        MetadataGroupType mdgType3 = new MetadataGroupType();
        mdgType3.setName("MetadataGroupType 3");
        dsType.addMetadataGroup(mdgType3, null, true, false);
        assertEquals(1, dsType.getAllDefaultDisplayMetadataGroups().size());
        assertTrue(mdgType3.equals(dsType.getAllDefaultDisplayMetadataGroups().get(0)));
        // for those already added into the list allMetadataGroups, I haven't found out any way to modify their field defaultdisplay
    }

    /* Tests for the method getNumberOfMetadataGroups(MetadataGroupType) */

    @Test
    public void testGetNumberOfMetadataGroupsGivenNull() {
        // the list allMetadataGroups is still empty
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        assertEquals("0", dsType.getNumberOfMetadataGroups(null));
        // make it non-empty
        ArrayList<MetadataGroupType> mdgTypes = new ArrayList<>();
        MetadataGroupType mdgType = new MetadataGroupType();
        mdgType.setName("name");
        mdgType.setNum("*");
        mdgTypes.add(mdgType);
        dsType.setAllMetadataGroups(mdgTypes);
        // "0" should be returned by default
        assertEquals("0", dsType.getNumberOfMetadataGroups(null));
    }

    @Test
    public void testGetNumberOfMetadataGroupsGivenNormalInput() {
        ArrayList<MetadataGroupType> mdgTypes = new ArrayList<>();
        String[] nums = new String[] { "1o", "1m", "*", "+" };
        for (String num : nums) {
            MetadataGroupType mdgType = new MetadataGroupType();
            mdgType.setName("normal " + num);
            mdgType.setNum(num);
            mdgTypes.add(mdgType);
        }
        String[] specialNums = new String[] { null, "0", "Hakuna Matata!" };
        for (String num : specialNums) {
            MetadataGroupType mdgType = new MetadataGroupType();
            mdgType.setName("special " + num);
            try {
                mdgType.setNum(num);
            } catch (Exception e) {
                // intentionally left blank since we only want to catch the NullPointerException caused by the special num "null"
            }
            mdgTypes.add(mdgType);
        }
        dsType.setAllMetadataGroups(mdgTypes);
        List<MetadataGroupType> types = dsType.getAllMetadataGroupTypes();
        for (int i = 0; i < nums.length; ++i) {
            assertEquals(nums[i], types.get(i).getNum());
        }
        for (int j = 0; j < specialNums.length; ++j) {
            assertEquals("1o", types.get(nums.length + j).getNum());
        }
    }

    /* Tests for the following methods:
     * 1. isMDTGroupAllowed(MetadataGroupType)
     * 2. addMetadataGroup(MetadataGroupType, String)
     * 3. addMetadataGroup(MetadataGroupType, String, boolean, boolean) [ Will Be Omitted Since It Is Almost The Same As 2. ]
     * 4. getMetadataGroupByGroup(MetadataGroupType)
     * 5. removeMetadataGroup(MetadataGroupType)
     * 6. isMetadataGroupAlreadyAvailable(MetadataGroupType) [ PRIVATE ] [ It Is Actually The Same As 1. ]
     *  */

    @Test
    public void testIsMDTGroupAllowedGivenNull() {
        assertFalse(dsType.isMDTGroupAllowed(null));
    }

    @Test
    public void testIsMDTGroupAllowedGivenAvailableUnnamedInput() {
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName("fixture");
        assertNotNull(dsType.addMetadataGroup(mdgt, null));
        assertTrue(dsType.isMDTGroupAllowed(mdgt)); // Should we allow the existence of unnamed MetadataGroupType at all?
    }

    @Test
    public void testIsMDTGroupAllowedGivenNamedInput() {
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName("mdgt");
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        // test the method while mdgt is still unavailable
        assertFalse(dsType.isMDTGroupAllowed(mdgt));
        // now add mdgt into the list allMetadataGroups and test the method again
        assertNotNull(dsType.addMetadataGroup(mdgt, null));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        assertTrue(dsType.isMDTGroupAllowed(mdgt));
    }

    @Test
    public void testAddMetadataGroupGivenNullAsFirstArgument() {
        // null as MetadataGroupType should not be addable
        assertNull(dsType.addMetadataGroup(null, "num"));

    }

    @Test
    public void testAddMetadataGroupGivenNullAsSecondArgument() {
        // null should be allowed as the second argument
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName("name");
        mdgt.setNum("*");
        assertTrue(mdgt.equals(dsType.addMetadataGroup(mdgt, null)));
        // and this "null" as inNumber is for the MetadataGroupForDocStructType object based on our mdgt
        // hence the num value of mdgt will not be affected
        assertEquals("*", dsType.getAllMetadataGroupTypes().get(0).getNum());
    }

    @Test
    public void testAddMetadataGroupGivenSameMetadataGroupTypeObjectThreeTimes() {
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName("name");
        mdgt.setNum("*");
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        // try to add mdgt for the first time
        assertNotNull(dsType.addMetadataGroup(mdgt, null));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // modify its num and try it again
        mdgt.setNum("+");
        assertNull(dsType.addMetadataGroup(mdgt, null));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // modify its name and try it again
        mdgt.setName("name2");
        assertNotNull(dsType.addMetadataGroup(mdgt, null));
        assertEquals(2, dsType.getAllMetadataGroupTypes().size());
    }

    @Test
    public void testAddMetadataGroupGivenTwoMetadataGroupTypeObjectsWithTheSameName() {
        MetadataGroupType mdgt1 = new MetadataGroupType();
        mdgt1.setName("name");
        mdgt1.setNum("*");
        MetadataGroupType mdgt2 = new MetadataGroupType();
        mdgt2.setName("name");
        mdgt2.setNum("+");
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        // try to add mdgt1 into the list
        assertEquals(mdgt2, (dsType.addMetadataGroup(mdgt1, null)));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // try to add mdgt2 into the list, and it should not succeed
        assertNull(dsType.addMetadataGroup(mdgt2, null));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        assertEquals("*", dsType.getAllMetadataGroupTypes().get(0).getNum());
    }

    @Test
    public void testGetMetadataGroupByGroupGivenNull() {
        // while the list allMetadataGroups is still empty
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        assertNull(dsType.getMetadataGroupByGroup(null));
        // now make the list non-empty
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName("name");
        assertNotNull(dsType.addMetadataGroup(mdgt, null));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // and try to test the method on null again
        assertNull(dsType.getMetadataGroupByGroup(null));
    }

    @Test
    public void testGetMetadataGroupByGroupGivenAvailableNamedGroupItself() {
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName("name");
        assertNotNull(dsType.addMetadataGroup(mdgt, null));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        assertNotNull(dsType.getMetadataGroupByGroup(mdgt));

    }

    @Test
    public void testGetMetadataGroupByGroupGivenAvailableGroupOfTheSameName() {
        String name = "name";
        MetadataGroupType mdgt1 = new MetadataGroupType();
        MetadataGroupType mdgt2 = new MetadataGroupType();
        mdgt1.setName(name);
        mdgt1.setNum("*");
        mdgt2.setName(name);
        mdgt2.setNum("+");
        // add mdgt1 into the list allMetadataGroups
        dsType.addMetadataGroup(mdgt1, null);
        // try to retrieve it using mdgt2
        MetadataGroupType mdgt = dsType.getMetadataGroupByGroup(mdgt2);
        assertEquals("*", mdgt.getNum());
    }

    @Test
    public void testRemoveMetadataGroupGivenNull() {
        // while the list allMetadataGroups is still empty
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        assertFalse(dsType.removeMetadataGroup(null));
        // now make the list non-empty
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName("name");
        assertNotNull(dsType.addMetadataGroup(mdgt, null));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // and try this method again with null
        assertFalse(dsType.removeMetadataGroup(null));
    }

    @Test
    public void testRemoveMetadataGroupGivenSameMetadataGroupTypeObjectTwice() {
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName("name");
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        // add mdgt once
        assertNotNull(dsType.addMetadataGroup(mdgt, null));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // try to remove mdgt twice
        assertTrue(dsType.removeMetadataGroup(mdgt));
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        assertFalse(dsType.removeMetadataGroup(mdgt));
    }

    @Test
    public void testRemoveMetadataGroupGivenTwoMetadataGroupTypeObjectsOfTheSameName() {
        String name = "name";
        MetadataGroupType mdgt1 = new MetadataGroupType();
        MetadataGroupType mdgt2 = new MetadataGroupType();
        mdgt1.setName(name);
        mdgt1.setNum("*");
        mdgt2.setName(name);
        mdgt2.setNum("+");
        // add mdgt1 into the list allMetadataGroups
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
        assertNotNull(dsType.addMetadataGroup(mdgt1, null));
        assertEquals(1, dsType.getAllMetadataGroupTypes().size());
        // try to remove mdgt2
        assertTrue(dsType.removeMetadataGroup(mdgt2));
        assertEquals(0, dsType.getAllMetadataGroupTypes().size());
    }
}

