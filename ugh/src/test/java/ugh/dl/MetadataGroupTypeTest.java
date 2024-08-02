package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;

public class MetadataGroupTypeTest {
    private MetadataGroupType mdgType;

    @Before
    public void setUp() {
        mdgType = new MetadataGroupType();
    }

    /* Tests for constructor */
    @Test
    public void testConstructor() {
        assertNotNull(mdgType.getMetadataTypeList());
        assertNotNull(mdgType.getAllAllowedGroupTypeTypes());
        assertNull(mdgType.getName());
        assertNull(mdgType.getAllLanguages());
        assertNull(mdgType.getNum());
    }

    /* Tests for the methods getMetadataTypeList(), setMetadataTypeList(List<MetadataType>), setTypes(List<MetadataTypeForDocStructType>)  */
    @Test
    public void testGetMetadataTypeListAgainstModificationOfReturnedResult() {
        // The content of the object should not be affected from outside via getters.
        assertEquals(0, mdgType.getMetadataTypeList().size());
        List<MetadataType> list = mdgType.getMetadataTypeList();
        assertEquals(0, list.size());
        list.add(new MetadataType());
        assertEquals(1, list.size());
        assertEquals(0, mdgType.getMetadataTypeList().size());
    }

    @Test
    public void testSetMetadataTypeListAgainstModificationOfOriginalList() {
        // The content of the object should not be affected via setters anymore after creation.
        List<MetadataType> list = new ArrayList<>();
        list.add(new MetadataType());
        assertEquals(1, list.size());
        mdgType.setMetadataTypeList(list);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        list.add(new MetadataType());
        assertEquals(2, list.size());
        assertEquals(1, mdgType.getMetadataTypeList().size());
    }

    @Test
    public void testSetTypesGivenNullThenGetMetadataTypeList() {
        assertNotNull(mdgType.getMetadataTypeList());
        mdgType.setTypes(null);
        assertNull(mdgType.getMetadataTypeList());
    }

    @Test
    public void testSetTypesOnTwoObjectsRelatedToTheSameTypeList() {
        List<MetadataTypeForDocStructType> list = new ArrayList<>();
        mdgType.setTypes(list);
        MetadataGroupType mdgType2 = new MetadataGroupType();
        mdgType2.setTypes(list);
        assertEquals(0, mdgType.getMetadataTypeList().size());
        assertEquals(0, mdgType2.getMetadataTypeList().size());
        mdgType.addMetadataType(new MetadataType(), null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        assertEquals(1, mdgType2.getMetadataTypeList().size());
        list.add(new MetadataTypeForDocStructType(new MetadataType()));
        assertEquals(2, list.size());
        assertEquals(2, mdgType.getMetadataTypeList().size());
        assertEquals(2, mdgType2.getMetadataTypeList().size());
    }

    /* Tests for the method addMetadataType(MetadataType, String, boolean, boolean) */
    @Test(expected = IllegalArgumentException.class)
    public void testAddMetadataTypeGivenNullAsFirstArgument() {
        mdgType.addMetadataType(null, null, false, false);
    }

    @Test
    public void testAddMetadataTypeGivenSameNamedMetadataTypeObjectTwice() {
        MetadataType mdType = new MetadataType();
        mdType.setName("name");
        assertEquals(0, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(mdType, null, false, true);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        assertFalse(new MetadataTypeForDocStructType(mdgType.getMetadataTypeList().get(0)).isInvisible());
    }

    @Test
    public void testAddMetadataTypeGivenUnnamedMetadataTypeObjectTwice() {
        MetadataType mdType = new MetadataType();
        assertEquals(0, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
    }

    @Test
    public void testAddMetadataTypeGivenEquivalentButDifferentMetadataTypeObjects() {
        // the method equals(MetadataType) in class MetadataType determines if a MetadataType object is still addable
        // i.e. one can never add similar MetadataType objects twice, if there is already one, then it would be the only one
        MetadataType type1 = new MetadataType();
        MetadataType type2 = new MetadataType();
        type1.setName("name");
        type2.setName("name");
        HashMap<String, String> hash1 = new HashMap<>();
        HashMap<String, String> hash2 = new HashMap<>();
        hash1.put("de", "deutsch");
        hash2.put("de", "Deutsch");
        type1.setAllLanguages(hash1);
        type2.setAllLanguages(hash2);
        assertEquals(type1, type2);
        assertEquals(0, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(type1, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(type2, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        assertEquals("deutsch", mdgType.getMetadataTypeList().get(0).getAllLanguages().get("de"));
    }

    @Test
    public void testAddMetadataTypeAgainstModificationsOfTheAddedObjectFromOutside1() throws MetadataTypeNotAllowedException {
        // add MetadataType without setting its language list first
        MetadataType mdType = new MetadataType();
        mdType.setName("name");
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals("name", mdgType.getMetadataTypeList().get(0).getName());
        assertNull(mdgType.getMetadataTypeList().get(0).getAllLanguages());
        Metadata md = new Metadata(mdType);
        assertThrows(NullPointerException.class, () -> mdType.getAllLanguages().size());
        assertThrows(NullPointerException.class, () -> md.getType().getAllLanguages().size());
        assertThrows(NullPointerException.class, () -> mdgType.getMetadataTypeList().get(0).getAllLanguages().size());
        md.getType().setAllLanguages(new HashMap<>());
        assertEquals(0, mdType.getAllLanguages().size());
        assertEquals(0, md.getType().getAllLanguages().size());
        assertThrows(NullPointerException.class, () -> mdgType.getMetadataTypeList().get(0).getAllLanguages().size());
    }

    @Test
    public void testAddMetadataTypeAgainstModificationsOfTheAddedObjectFromOutside2() throws MetadataTypeNotAllowedException {
        // add MetadataType after setting its language list first
        MetadataType mdType = new MetadataType();
        mdType.setName("name");
        mdType.setAllLanguages(new HashMap<>());
        Metadata md = new Metadata(mdType);
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals(0, mdType.getAllLanguages().size());
        assertEquals(0, md.getType().getAllLanguages().size());
        assertEquals(0, mdgType.getMetadataTypeList().get(0).getAllLanguages().size());

        // this is the wanted behavior. If you change the type, all instances using this type are affected
        md.getType().addLanguage("de", "Deutsch");
        assertEquals(1, mdType.getAllLanguages().size());
        assertEquals(1, md.getType().getAllLanguages().size());
        assertEquals(1, mdgType.getMetadataTypeList().get(0).getAllLanguages().size());
    }

    /* Tests for the method removeMetadataType(MetadataType) */
    @Test
    public void testRemoveMetadataTypeGivenNull() {
        int length = mdgType.getMetadataTypeList().size();
        mdgType.removeMetadataType(null);
        assertEquals(length, mdgType.getMetadataTypeList().size());
    }

    @Test
    public void testRemoveMetadataTypeGivenUnexistingElement() {
        MetadataType typeAdded = new MetadataType();
        typeAdded.setName("added");
        mdgType.addMetadataType(typeAdded, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        MetadataType type = new MetadataType();
        type.setName("type");
        assertNotEquals(type, typeAdded);
        mdgType.removeMetadataType(type);
        assertEquals(1, mdgType.getMetadataTypeList().size());
    }

    @Test
    public void testRemoveMetadataTypeGivenExistingElement() {
        assertEquals(0, mdgType.getMetadataTypeList().size());
        MetadataType typeAdded = new MetadataType();
        typeAdded.setName("added");
        mdgType.addMetadataType(typeAdded, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        mdgType.removeMetadataType(typeAdded);
        assertEquals(0, mdgType.getMetadataTypeList().size());
    }

    @Test
    public void testRemoveMetadataTypeGivenEquivalentButDifferentElements() {
        // the method equals(MetadataType) in class MetadataType determines if a MetadataType object is removable
        // i.e. one can remove a MetadataType object via one sufficiently similar to the one in the group
        MetadataType typeAdded = new MetadataType();
        typeAdded.setName("name");
        typeAdded.setAllowNameParts(true);
        MetadataType typeSimilar = new MetadataType();
        typeSimilar.setName("name");
        typeSimilar.setAllowNameParts(false);
        assertEquals(typeAdded, typeSimilar);
        assertEquals(0, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(typeAdded, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        mdgType.removeMetadataType(typeSimilar);
        assertEquals(0, mdgType.getMetadataTypeList().size());
    }

    /* Tests for the method equals(Object) */
    @Test
    public void testEqualsToItself() {
        assertSame(mdgType, mdgType);
        assertEquals(mdgType, mdgType);
    }

    @Test
    public void testEqualsGivenNull() {
        mdgType.setName("");
        assertNotEquals(mdgType, null);
    }

    @Test
    public void testEqualsToItsCopy() {
        mdgType.setName("name");
        MetadataGroupType mdgTypeCopy = mdgType.copy();
        assertNotSame(mdgType, mdgTypeCopy);
        assertEquals(mdgType, mdgTypeCopy);
    }

    @Test
    public void testEqualsGivenExtendedObject() {
        mdgType.setName("name");
        ExtendedMetadataGroupType extendedType = new ExtendedMetadataGroupType("name");
        assertEquals(mdgType, extendedType);
        assertEquals(extendedType, mdgType);
    }

    // class needed for the test case above
    private class ExtendedMetadataGroupType extends MetadataGroupType {
        private static final long serialVersionUID = 630033786970054284L;

        public ExtendedMetadataGroupType(String name) {
            super();
            super.setName(name);
        }

        public ExtendedMetadataGroupType() {
            new ExtendedMetadataGroupType("");
        }
    }

    /* Tests for the following methods:
     * getAllLanguages()
     * setAllLanguages(HashMap<String,String>)
     * getLanguage(String)
     * getNameByLanguage(String)
     * addLanguage(String, String)
     */
    @Test
    public void testAddLanguageGivenUninitializedFieldAllLanguages() {
        assertNull(mdgType.getAllLanguages());
        mdgType.addLanguage("de", "Deutsch");
        assertEquals(1, mdgType.getAllLanguages().size());
    }

    @Test
    public void testAddLanguageTogetherWithModificationsOnTheResultOfGetAllLanguages() {
        HashMap<String, String> hashMap = new HashMap<>();
        mdgType.setAllLanguages(hashMap);
        mdgType.addLanguage("de", "Deutsch");
        assertEquals(1, mdgType.getAllLanguages().size());
        assertEquals("Deutsch", mdgType.getNameByLanguage("de"));
        mdgType.addLanguage("de", "deutsch"); // should make no change according to the logic of the method addLanguage(String, String)
        assertEquals(1, mdgType.getAllLanguages().size());
        assertEquals("Deutsch", mdgType.getNameByLanguage("de"));
        Map<String, String> languages = mdgType.getAllLanguages();
        languages.put("de", "deutsch"); // but one can still modify the value from outside, which is DANGEROUS !
        assertEquals(1, mdgType.getAllLanguages().size());
        assertEquals("deutsch", mdgType.getNameByLanguage("de"));
    }

    @Test
    public void testGetLanguageGivenNull() {
        mdgType.setAllLanguages(new HashMap<>());
        mdgType.addLanguage("de", "Deutsch");
        assertNull(mdgType.getLanguage(null));
    }

    @Test
    public void testGetLanguageGivenUninitializedFieldAllLanguages() {
        assertNull(mdgType.getLanguage("de"));
    }

    @Test
    public void testGetLanguageGivenUnexistingLanguage() {
        mdgType.setAllLanguages(new HashMap<>());
        mdgType.addLanguage("de", "Deutsch");
        assertNull(mdgType.getLanguage("en"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLanguageGivenNullAsFirstArgument() {
        mdgType.addLanguage(null, "null");
    }

    @Test
    public void testAddLanguageGivenNullAsSecondArgument() {
        // Null should be allowed as value. - Robert
        mdgType.setAllLanguages(new HashMap<>());
        mdgType.addLanguage("de", null);
        assertNull(mdgType.getNameByLanguage("de"));
    }

    /* Tests for the method getNumberOfMetadataType(PrefsType) */
    @Test
    public void testGetNumberOfMetadataTypeGivenEmptyMetadataTypeListAndNullAsArgument() {
        assertEquals("0", mdgType.getNumberOfMetadataType(null));
    }

    @Test
    public void testGetNumberOfMetadataTypeGivenUnemptyMetadataTypeListOfUnnamedMetadataTypeObjectsAndNullAsArgument() {
        MetadataType type = new MetadataType();
        mdgType.addMetadataType(type, null, false, false);
        assertEquals("0", mdgType.getNumberOfMetadataType(null));
    }

    @Test
    public void testGetNumberOfMetadataTypeGivenUnemptyMetadataTypeListOfNamedMetadataTypeObjectsAndNullAsArgument() {
        MetadataType type = new MetadataType();
        type.setName("name");
        mdgType.addMetadataType(type, null, false, false);
        assertEquals("0", mdgType.getNumberOfMetadataType(null));
    }

    @Test
    public void testGetNumberOfMetadataTypeGivenNamelessMetadataTypeObjectAsArgument() {
        MetadataType type = new MetadataType();
        type.setName("name");
        mdgType.addMetadataType(type, null, false, false);
        assertEquals("0", mdgType.getNumberOfMetadataType(new MetadataType()));
    }

    @Test
    public void testGetNumberOfMetadataTypeGivenNamedUnexistingMetadataTypeObjectAsArgument() {
        MetadataType type = new MetadataType();
        type.setName("name");
        mdgType.addMetadataType(type, null, false, false);
        MetadataType anotherType = new MetadataType();
        anotherType.setName("another name");
        assertEquals("0", mdgType.getNumberOfMetadataType(anotherType));
    }

    @Test
    public void testGetNumberOfMetadataTypeGivenNormalInput() throws PreferencesException {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");
        mdgType = prefs.getMetadataGroupTypeByName("PublisherGroup");
        MetadataType type1 = new MetadataType();
        MetadataType type2 = new MetadataType();
        MetadataType type3 = new MetadataType();
        MetadataType type4 = new MetadataType();
        MetadataType type5 = new MetadataType();
        type1.setName("PublisherPerson");
        type2.setName("PlaceOfPublication");
        type3.setName("PublicationYear");
        type4.setName("PublisherCorporate");
        type5.setName("PublisherName");
        // that is the correct behaviour, "*" is default if nothing is set. - Robert
        assertEquals("*", mdgType.getNumberOfMetadataType(type1)); // It seems that num will be defaulted to "*" if not set manually, but I haven't found out how so. - Zehong
        // two findings until now:
        // 1. in ugh.dl.Prefs from Line 367 to 374, "1" seems to be taken as default value for num if it is not set manually.
        // 2. in ugh.dl.DocStruct around Line 1518, however, "*" would be used as default value instead, which also happens to be the expected behavior.
        assertEquals("+", mdgType.getNumberOfMetadataType(type2));
        assertEquals("1m", mdgType.getNumberOfMetadataType(type3));
        assertEquals("1o", mdgType.getNumberOfMetadataType(type4));
        assertEquals("0", mdgType.getNumberOfMetadataType(type5));
    }

    /* Tests for the method copy() */
    @Test
    public void testCopy() throws PreferencesException {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");
        mdgType = prefs.getMetadataGroupTypeByName("PublisherGroup");
        MetadataGroupType mdgType2 = mdgType.copy();
        assertNotSame(mdgType2, mdgType);
        assertEquals(mdgType2, mdgType);
        assertEquals(mdgType2.getNum(), mdgType.getNum());
        assertEquals(mdgType2.getName(), mdgType.getName());
        assertEquals(mdgType2.getAllLanguages(), mdgType.getAllLanguages());
        assertSame(mdgType2.getAllLanguages(), mdgType.getAllLanguages()); // The copy and the original share the same HashMap allLanguages
        assertNotSame(mdgType2.getAllAllowedGroupTypeTypes(), mdgType.getAllAllowedGroupTypeTypes());
        assertNotSame(mdgType2.getMetadataTypeList(), mdgType.getMetadataTypeList());
        assertEquals(mdgType2.getAllAllowedGroupTypeTypes().size(), mdgType.getAllAllowedGroupTypeTypes().size());
        assertEquals(mdgType2.getMetadataTypeList().size(), mdgType.getMetadataTypeList().size());
        Iterator<AllowedMetadataGroupType> allGroupsIterator2 = mdgType2.getAllAllowedGroupTypeTypes().iterator();
        for (AllowedMetadataGroupType type1 : mdgType.getAllAllowedGroupTypeTypes()) {
            AllowedMetadataGroupType type2 = allGroupsIterator2.next();
            assertEquals(type1.getGroupName(), type2.getGroupName());
            assertEquals(type1.getNumAllowed(), type2.getNumAllowed());
            assertEquals(type1.isDefaultDisplay(), type2.isDefaultDisplay());
            assertEquals(type1.isHidden(), type2.isHidden());
        }
        Iterator<MetadataType> mdTypeListIterator2 = mdgType2.getMetadataTypeList().iterator();
        for (MetadataType element : mdgType.getMetadataTypeList()) {
            assertEquals(element, mdTypeListIterator2.next());
        }
    }

    /* Tests for the method getAllDefaultDisplayMetadataTypes() */
    @Test
    public void testGetAllDefaultDisplayMetadataTypesWhenUninitialized() {
        assertNotNull(mdgType.getAllDefaultDisplayMetadataTypes());
        assertEquals(0, mdgType.getAllAllowedGroupTypeTypes().size());
    }

    @Test
    public void testGetAllDefaultDisplayMetadataTypesUnderCommonScenario() throws PreferencesException {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");
        mdgType = prefs.getMetadataGroupTypeByName("PublisherGroup");
        assertEquals(3, mdgType.getAllDefaultDisplayMetadataTypes().size());
    }

    /* Tests for the following methods:
     *  addGroupTypeAsChild(String, String, boolean, boolean)
     *  removeGroupTypeAsChild(String)
     *  getAllAllowedGroupTypeTypes()
     *  getAllowedMetadataGroupTypeByName(String)
     */
    @Test
    public void testAddGroupTypeAsChildGivenSameObjectTwice() {
        assertEquals(0, mdgType.getAllAllowedGroupTypeTypes().size());
        mdgType.addGroupTypeAsChild("group", null, false, false);
        assertEquals(1, mdgType.getAllAllowedGroupTypeTypes().size());
        mdgType.addGroupTypeAsChild("group", "*", false, false);
        assertEquals(1, mdgType.getAllAllowedGroupTypeTypes().size());
        assertNull(mdgType.getAllAllowedGroupTypeTypes().get(0).getNumAllowed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddGroupTypeAsChildGivenNull() {
        mdgType.addGroupTypeAsChild(null, null, false, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddGroupTypeAsChildGivenEmptyGroupName() {
        mdgType.addGroupTypeAsChild("", null, false, false);
    }

    @Test
    public void testRemoveGroupTypeAsChildGivenNull() {
        mdgType.addGroupTypeAsChild("group", null, false, false);
        assertEquals(1, mdgType.getAllAllowedGroupTypeTypes().size());
        mdgType.removeGroupTypeAsChild(null);
        assertEquals(1, mdgType.getAllAllowedGroupTypeTypes().size());
    }

    @Test
    public void testRemoveGroupTypeAsChildGivenUnexistingGroupType() {
        mdgType.addGroupTypeAsChild("group", null, false, false);
        assertEquals(1, mdgType.getAllAllowedGroupTypeTypes().size());
        mdgType.removeGroupTypeAsChild("another group");
        assertEquals(1, mdgType.getAllAllowedGroupTypeTypes().size());
    }

    @Test
    public void testRemoveGroupTypeAsChildGivenExistingGroupType() {
        mdgType.addGroupTypeAsChild("group", null, false, false);
        assertEquals(1, mdgType.getAllAllowedGroupTypeTypes().size());
        mdgType.removeGroupTypeAsChild("group");
        assertEquals(0, mdgType.getAllAllowedGroupTypeTypes().size());
    }

    @Test
    public void testGetAllowedMetadataGroupTypeByNameGivenNull() {
        assertNull(mdgType.getAllowedMetadataGroupTypeByName(null));
        mdgType.addGroupTypeAsChild("group", null, false, false);
        assertNull(mdgType.getAllowedMetadataGroupTypeByName(null));
    }
}
