package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class MetadataGroupTest {

    private Prefs prefs;
    private MetadataGroupType groupType;
    private MetadataGroup fixture;

    @Before
    public void setUp() throws Exception {
        prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");
        groupType = prefs.getMetadataGroupTypeByName("PublisherGroup"); // ATTENTION: groupType.getNum() == null
        fixture = new MetadataGroup(groupType);
    }

    /* Tests for the constructor */
    @Test
    public void testConstructor() throws Exception {
        MetadataGroup group = new MetadataGroup(groupType);
        assertNotNull(group);
        assertEquals("PublisherGroup", group.getType().getName());
    }

    @Test
    public void testConstructorGivenNull() {
        assertThrows(MetadataTypeNotAllowedException.class, () -> new MetadataGroup(null));
    }

    @Test
    public void testConstructorGivenNewMetadataGroupTypeObject() throws MetadataTypeNotAllowedException {
        MetadataGroup group = new MetadataGroup(new MetadataGroupType());
        assertNotNull(group);
        assertNotNull(group.getType().getMetadataTypeList());
        assertNull(group.getType().getName());
    }

    /* I think the following test should be moved to DocStructTest. - Zehong  */
    @Test
    public void testDocStruct() throws Exception {
        MetadataGroup group = new MetadataGroup(groupType);
        assertNotNull(group);
        assertEquals("PublisherGroup", group.getType().getName());

        DocStruct monograph = new DigitalDocument().createDocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertNull(monograph.getAllMetadataGroups());
        monograph.addMetadataGroup(group);
        assertEquals(1, monograph.getAllMetadataGroups().size());
        assertEquals("Monograph", group.getParent().getType().getName());
    }

    /* Tests for setType() and getType() */
    @Test
    public void testType() throws Exception {
        MetadataGroup group = new MetadataGroup(groupType);
        group.setType(groupType);
        assertEquals(groupType, group.getType());
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed.")
    @Test
    public void testSetTypeGivenNull() throws MetadataTypeNotAllowedException {
        MetadataGroup group = new MetadataGroup(groupType);
        assertThrows(MetadataTypeNotAllowedException.class, () -> group.setType(null));
    }

    @Test
    public void testSetTypeGivenNewMetadataMetadataGroupTypeObject() throws MetadataTypeNotAllowedException {
        MetadataGroup group = new MetadataGroup(groupType);
        group.setType(new MetadataGroupType());
        assertNotNull(group.getType());
        assertNotSame(groupType, group.getType());
    }

    /* Four general tests for the methods addMetadata, addPerson and addCorporate */
    @Test
    public void testAddMetadata1() throws Exception {
        // first case, no explicit limitation configured, adding is possible
        Person p = new Person(prefs.getMetadataTypeByName("PublisherPerson"));
        p.setFirstname("firstname");
        p.setLastname("lastname");
        assertEquals(0, fixture.getPersonList().size());
        fixture.addPerson(p);
        assertEquals(1, fixture.getPersonList().size());
    }

    @Test
    public void testAddMetadata2() throws Exception {
        // second case, at least one or unlimited, adding is possible
        Metadata place = new Metadata(prefs.getMetadataTypeByName("PlaceOfPublication"));
        place.setValue("fixture");
        assertEquals(0, fixture.getMetadataByType("PlaceOfPublication").size());
        fixture.addMetadata(place);
        assertEquals(1, fixture.getMetadataByType("PlaceOfPublication").size());
    }

    @Test(expected = MetadataTypeNotAllowedException.class)
    public void testAddMetadata3() throws Exception {
        // Third case, exact one field is allowed, adding a second one throws exception
        Metadata year = new Metadata(prefs.getMetadataTypeByName("PublicationYear"));
        year.setValue("fixture");
        fixture.addMetadata(year);
        assertEquals(1, fixture.getMetadataByType("PublicationYear").size());
        year = new Metadata(prefs.getMetadataTypeByName("PublicationYear"));
        year.setValue("fixture");
        fixture.addMetadata(year);
    }

    @Test(expected = MetadataTypeNotAllowedException.class)
    public void testTAddMetadata4() throws Exception {
        // fourth case, not more then one field is allowed, adding a new one throws exception
        Corporate c = new Corporate(prefs.getMetadataTypeByName("PublisherCorporate"));
        c.setMainName("fixture");
        fixture.addCorporate(c);
        assertEquals(1, fixture.getCorporateByType("PublisherCorporate").size());

        Corporate c2 = new Corporate(prefs.getMetadataTypeByName("PublisherCorporate"));
        c2.setMainName("fixture");
        fixture.addCorporate(c2);
    }

    /* Tests for the method addMetadata(Metadata) */
    @Ignore("The logic in the method cannot pass this test. Null check needed.")
    @Test
    public void testAddMetadataGivenNull() {
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addMetadata(null));
    }

    @Test
    public void testAddMetadataGivenNewMetadataObjectCreatedFromUnnamedMetadataType() throws MetadataTypeNotAllowedException {
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addMetadata(new Metadata(new MetadataType())));
    }

    @Test
    public void testAddMetadataGivenNewPersonObjectCreatedFromUnnamedMetadataType() {
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addMetadata(new Person(new MetadataType())));
    }

    @Test
    public void testAddMetadataGivenNewCorporateObjectCreatedFromUnnamedMetadataType() {
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addMetadata(new Corporate(new MetadataType())));
    }

    @Test
    public void testAddMetadataGivenNewPersonObjectCreatedFromUnaddableMetadataType() {
        MetadataType type = new MetadataType();
        type.setName("unaddable");
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addMetadata(new Person(type)));
    }

    @Test
    public void testAddMetadataGivenNewCorporateObjectCreatedFromUnaddableMetadataType() {
        MetadataType type = new MetadataType();
        type.setName("unaddable");
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addMetadata(new Corporate(type)));
    }

    @Ignore("The logic of the method cannot pass this test. Class check needed.")
    @Test
    public void testAddMetadataGivenPersonObject() throws MetadataTypeNotAllowedException {
        assertEquals(0, fixture.getMetadataList().size());
        assertEquals(0, fixture.getPersonList().size());
        MetadataType type = new MetadataType();
        // addable metadata types: [ PublisherPerson, PlaceOfPublication, PublicationYear, PublisherCorporate ]
        type.setName("PublisherPerson");
        fixture.addMetadata(new Person(type));
        assertEquals(0, fixture.getMetadataList().size());
        assertEquals(1, fixture.getPersonList().size());
    }

    @Ignore("The logic of the method cannot pass this test. Class check needed.")
    @Test
    public void testAddMetadataGivenCorporateObject() throws MetadataTypeNotAllowedException {
        assertEquals(0, fixture.getMetadataList().size());
        assertEquals(0, fixture.getPersonList().size());
        MetadataType type = new MetadataType();
        type.setName("PublisherCorporate");
        fixture.addMetadata(new Corporate(type));
        assertEquals(0, fixture.getMetadataList().size());
        assertEquals(1, fixture.getCorporateList().size());
    }

    /* Tests for the method addPerson(Person) */
    @Test
    public void testAddPersonGivenNull() {
        assertThrows(NullPointerException.class, () -> fixture.addPerson(null));
    }

    @Ignore("The logic of the method cannot pass this test. Type check needed before creating Person objects. Better move this test to PersonTest.")
    @Test
    public void testAddPersonGivenPersonObjectBasedOnMetadataTypeCorporate() {
        MetadataType type = new MetadataType();
        type.setName("PublisherCorporate");
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addPerson(new Person(type)));
    }

    @Test
    public void testAddPersonGivenSameObjectTwice() throws MetadataTypeNotAllowedException {
        MetadataType type = prefs.getMetadataTypeByName("PublisherPerson");
        Person person = new Person(type);
        fixture.addPerson(person);
        fixture.addPerson(person);
        assertEquals(2, fixture.getPersonList().size());
    }

    @Test
    public void testAddPersonGivenPersonObjectsWithSameContent() throws MetadataTypeNotAllowedException {
        MetadataType type1 = prefs.getMetadataTypeByName("PublisherPerson");
        MetadataType type2 = type1.copy();
        MetadataType type3 = new MetadataType();
        type3.setName("PublisherPerson");
        fixture.addPerson(new Person(type1));
        fixture.addPerson(new Person(type2));
        fixture.addPerson(new Person(type3));
        assertEquals(3, fixture.getPersonList().size());
    }

    @Test
    public void testAddPersonGivenObjectExtendedFromPerson() throws MetadataTypeNotAllowedException {
        MetadataType type = new MetadataType();
        type.setName("PublisherPerson");
        Person person = new Person(type);
        ExtendedPerson exPerson = new ExtendedPerson(type);
        fixture.addPerson(person);
        fixture.addPerson(exPerson);
        assertEquals(2, fixture.getPersonList().size());
        assertNotEquals("This is the ExtendedPerson", fixture.getPersonList().get(0).toString());
        assertEquals("This is the ExtendedPerson", fixture.getPersonList().get(1).toString());
    }

    // class needed for the test case above
    private class ExtendedPerson extends Person {
        public ExtendedPerson(MetadataType type) throws MetadataTypeNotAllowedException {
            super(type);
        }

        @Override
        public String toString() {
            return "This is the ExtendedPerson";
        }
    }

    /* Tests for the method addCorporate(Corporate) */
    @Test
    public void testAddCorporateGivenNull() {
        assertThrows(NullPointerException.class, () -> fixture.addCorporate(null));
    }

    @Ignore("The logic of the method cannot pass this test. Type check needed before creating Corporate objects. Better move this test to CorporateTest.")
    @Test
    public void testAddCorporateGivenCorporateObjectBasedOnMetadataTypePerson() {
        MetadataType type = new MetadataType();
        type.setName("PublisherPerson");
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addCorporate(new Corporate(type)));
    }

    @Ignore("The logic in the method cannot pass this test. No suggestions available. Same logic of addPerson passed the same test. Feature OR Bug?")
    @Test
    public void testAddCorporateGivenSameObjectTwice() throws MetadataTypeNotAllowedException {
        MetadataType type = prefs.getMetadataTypeByName("PublisherCorporate");
        Corporate corporate = new Corporate(type);
        fixture.addCorporate(corporate);
        fixture.addCorporate(corporate);
        assertEquals(2, fixture.getCorporateList().size());
    }

    @Ignore("The logic in the method cannot pass this test. No suggestions available. Same logic of addPerson passed the same test. Feature OR Bug?")
    @Test
    public void testAddCorporateGivenCorporateObjectsWithSameContent() throws MetadataTypeNotAllowedException {
        MetadataType type1 = prefs.getMetadataTypeByName("PublisherCorporate");
        MetadataType type2 = type1.copy();
        MetadataType type3 = new MetadataType();
        type3.setName("PublisherCorporate");
        Corporate corporate1 = new Corporate(type1);
        Corporate corporate2 = new Corporate(type2);
        Corporate corporate3 = new Corporate(type3);
        fixture.addCorporate(corporate1);
        fixture.addCorporate(corporate2);
        fixture.addCorporate(corporate3);
        assertEquals(3, fixture.getCorporateList().size());
    }

    @Ignore("The logic in the method cannot pass this test. No suggestions available. Same logic of addPerson passed the same test. Feature OR Bug?")
    @Test
    public void testAddCorporateGivenObjectExtendedFromCorporate() throws MetadataTypeNotAllowedException {
        MetadataType type = prefs.getMetadataTypeByName("PublisherCorporate");
        Corporate corporate = new Corporate(type);
        ExtendedCorporate exCorporate = new ExtendedCorporate(type);
        fixture.addCorporate(corporate);
        fixture.addCorporate(new Corporate(type));
        assertEquals(2, fixture.getCorporateList().size());
        assertNotEquals("This is the ExtendedCorporate", fixture.getCorporateList().get(0).toString());
        assertEquals("This is the ExtendedCorporate", fixture.getCorporateList().get(1).toString());
    }

    // class needed for the test case above
    private class ExtendedCorporate extends Corporate {
        public ExtendedCorporate(MetadataType type) throws MetadataTypeNotAllowedException {
            super(type);
        }

        @Override
        public String toString() {
            return "This is the ExtendedCorporate";
        }
    }

    /* Tests for the method addMetadataGroup(MetadataGroup) */
    @Test
    public void testAddMetadataGroupAllowed() throws Exception {
        assertEquals(0, fixture.getAllMetadataGroups().size());
        MetadataGroup other = new MetadataGroup(prefs.getMetadataGroupTypeByName("LocationGroup"));
        fixture.addMetadataGroup(other);
        assertEquals(1, fixture.getAllMetadataGroups().size());
    }

    @Test(expected = MetadataTypeNotAllowedException.class)
    public void testAddMoreMetadataGroupsThenAllowed() throws Exception {
        assertEquals(0, fixture.getAllMetadataGroups().size());
        MetadataGroup other = new MetadataGroup(prefs.getMetadataGroupTypeByName("LocationGroup"));
        fixture.addMetadataGroup(other);
        assertEquals(1, fixture.getAllMetadataGroups().size());

        MetadataGroup other2 = new MetadataGroup(prefs.getMetadataGroupTypeByName("LocationGroup"));
        fixture.addMetadataGroup(other2);
    }

    @Test(expected = MetadataTypeNotAllowedException.class)
    public void testAddMetadataGroupNotAllowed() throws Exception {
        assertEquals(0, fixture.getAllMetadataGroups().size());
        MetadataGroup other = new MetadataGroup(prefs.getMetadataGroupTypeByName("UnusedGroup"));
        fixture.addMetadataGroup(other);
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed.")
    @Test
    public void testAddMetadataGroupGivenNull() {
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addMetadataGroup(null));
    }

    @Test
    public void testAddMetadataGroupGivenInternalMetadataTypeTwice() throws MetadataTypeNotAllowedException {
        MetadataGroupType type = prefs.getMetadataGroupTypeByName("LocationGroup");
        type.setName("_internal"); // the leading _ is the mark of an internal MetadataType
        MetadataGroup mdGroup = new MetadataGroup(type);
        assertTrue(fixture.addMetadataGroup(mdGroup));
        assertEquals(1, fixture.getAllMetadataGroups().size());
        assertTrue(fixture.addMetadataGroup(mdGroup));
        assertEquals(2, fixture.getAllMetadataGroups().size());
    }

    @Test
    public void testAddMetadataGroupGivenSameObjectTwice1() throws MetadataTypeNotAllowedException {
        MetadataGroupType type = prefs.getMetadataGroupTypeByName("LocationGroup");
        MetadataGroup mdGroup = new MetadataGroup(type);
        // "LocationGroup" is the only one allowed to be added to fixture.
        // And it has "1o" as value of the field numAllowed.
        assertTrue(fixture.addMetadataGroup(mdGroup));
        assertEquals(1, fixture.getAllMetadataGroups().size());
        assertEquals("1o", fixture.getType().getAllowedMetadataGroupTypeByName(type.getName()).getNumAllowed());
        assertThrows(MetadataTypeNotAllowedException.class, () -> fixture.addMetadataGroup(mdGroup));
    }

    @Test
    public void testAddMetadataGroupGivenSameObjectTwice2() throws MetadataTypeNotAllowedException {
        MetadataGroupType type = prefs.getMetadataGroupTypeByName("TestGroup");
        MetadataGroup testGroup = new MetadataGroup(type);
        assertEquals(2, testGroup.getType().getAllAllowedGroupTypeTypes().size());

        MetadataGroupType type1 = prefs.getMetadataGroupTypeByName("LocationGroup");
        MetadataGroup mdGroup1 = new MetadataGroup(type1);
        assertEquals("+", testGroup.getType().getAllowedMetadataGroupTypeByName(mdGroup1.getType().getName()).getNumAllowed());
        assertEquals(0, testGroup.getAllMetadataGroups().size());
        assertTrue(testGroup.addMetadataGroup(mdGroup1));
        assertTrue(testGroup.addMetadataGroup(mdGroup1));
        assertEquals(2, testGroup.getAllMetadataGroups().size());

        MetadataGroupType type2 = prefs.getMetadataGroupTypeByName("UnusedGroup");
        MetadataGroup mdGroup2 = new MetadataGroup(type2);
        assertEquals("*", testGroup.getType().getAllowedMetadataGroupTypeByName(mdGroup2.getType().getName()).getNumAllowed());
        assertTrue(testGroup.addMetadataGroup(mdGroup2));
        assertTrue(testGroup.addMetadataGroup(mdGroup2));
        assertEquals(4, testGroup.getAllMetadataGroups().size());
    }

    /* Tests for the method removeMetadataGroup(MetadataGroup, boolean) */
    @Test
    public void testRemoveMetadataGroup() throws Exception {
        assertEquals(0, fixture.getAllMetadataGroups().size());
        MetadataGroup other = new MetadataGroup(prefs.getMetadataGroupTypeByName("LocationGroup"));
        fixture.addMetadataGroup(other);
        assertEquals(1, fixture.getAllMetadataGroups().size());

        fixture.removeMetadataGroup(other, false);
        assertEquals(0, fixture.getAllMetadataGroups().size());
    }

    @Test
    public void testRemoveNonExistingMetadataGroup() throws Exception {
        assertEquals(0, fixture.getAllMetadataGroups().size());
        MetadataGroup other = new MetadataGroup(prefs.getMetadataGroupTypeByName("UnusedGroup"));

        assertFalse(fixture.removeMetadataGroup(other, false));
        assertEquals(0, fixture.getAllMetadataGroups().size());
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed. Avoid NullPointerException whenever possible.")
    @Test
    public void testRemoveMetadataGroupGivenNullAsFirstArgument() {
        assertFalse(fixture.removeMetadataGroup(null, false));
    }

    @Test
    public void testRemoveMetadataGroupGivenNullAllowedMetadataGroupType() throws MetadataTypeNotAllowedException {
        MetadataGroupType mdgType = new MetadataGroupType();
        mdgType.setName("NullAllowed");
        MetadataGroup testGroup = new MetadataGroup(mdgType);
        AllowedMetadataGroupType type = fixture.getType().getAllowedMetadataGroupTypeByName(testGroup.getType().getName());
        assertNull(type);
        assertFalse(fixture.removeMetadataGroup(testGroup, false));
    }

    /* Tests for the method changeMetadataGroup(MetadataGroup, MetadataGroup) */
    @Ignore("The logic in the method cannot pass this test. Null check needed. Avoid NullPointerException whenever possible.")
    @Test
    public void testChangeMetadataGroupGivenNullAsFirstArgument() throws MetadataTypeNotAllowedException {
        MetadataGroupType type = prefs.getMetadataGroupTypeByName("LocationGroup");
        MetadataGroup group = new MetadataGroup(type);
        assertThrows(IllegalArgumentException.class, () -> fixture.changeMetadataGroup(null, group));
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed. Avoid NullPointerException whenever possible.")
    @Test
    public void testChangeMetadataGroupGivenNullAsSecondArgument() throws MetadataTypeNotAllowedException {
        MetadataGroupType type = prefs.getMetadataGroupTypeByName("LocationGroup");
        MetadataGroup group = new MetadataGroup(type);
        fixture.addMetadataGroup(group);
        assertThrows(IllegalArgumentException.class, () -> fixture.changeMetadataGroup(group, null));
    }

    /* Tests for the method getAddableMetadataGroupTypes() */
    @Test
    public void testGetAddableMetadataGroupTypes() throws Exception {
        List<String> availableGroups = fixture.getAddableMetadataGroupTypes();
        assertEquals("LocationGroup", availableGroups.get(0));

        assertEquals(0, fixture.getAllMetadataGroups().size());
        MetadataGroup other = new MetadataGroup(prefs.getMetadataGroupTypeByName("LocationGroup"));
        fixture.addMetadataGroup(other);
        assertEquals(1, fixture.getAllMetadataGroups().size());

        availableGroups = fixture.getAddableMetadataGroupTypes();
        assertNull(availableGroups);
    }

    /* Tests for the method equals(Object) */
    @Test
    public void testEqualsToItself() {
        assertTrue(fixture.equals(fixture));
    }

    @Test
    public void testEqualsGivenNull() {
        assertFalse(fixture.equals(null));
    }

    @Test
    public void testEqualsGivenObjectsWithDifferentIdentifiers() throws MetadataTypeNotAllowedException {
        // equals(Object) method only compares the following fields:
        // [ metadataGroupType, metadataList, personList, parent ]
        MetadataGroupType type1 = prefs.getMetadataGroupTypeByName("TestGroup");
        MetadataGroupType type2 = prefs.getMetadataGroupTypeByName("TestGroup");
        assertTrue(type1.equals(type2));
        MetadataGroup group1 = new MetadataGroup(type1);
        MetadataGroup group2 = new MetadataGroup(type2);
        group1.setIdentifier("1");
        group2.setIdentifier("2");
        assertNotSame(group1, group2);
        assertEquals(group1, group2);
        assertTrue(group1.equals(group2));
    }

    @Test
    public void testEqualsGivenObjectsWithDifferentCorporateList() throws MetadataTypeNotAllowedException {
        MetadataGroupType anotherType = prefs.getMetadataGroupTypeByName("PublisherGroup");
        assertTrue(fixture.getType().equals(anotherType));
        MetadataGroup anotherGroup = new MetadataGroup(anotherType);
        MetadataType mdType = prefs.getMetadataTypeByName("PublisherPerson");
        anotherGroup.addCorporate(new Corporate(mdType));
        assertNotSame(fixture, anotherGroup);
        assertTrue(anotherGroup.equals(fixture));
    }

    @Test
    public void testEqualsGivenObjectOfAnExtendedClass() throws MetadataTypeNotAllowedException {
        assertFalse(fixture.equals(new ExtendedMetadataGroup(groupType)));
    }

    // class needed for the test case above
    private class ExtendedMetadataGroup extends MetadataGroup {
        public ExtendedMetadataGroup(MetadataGroupType type) throws MetadataTypeNotAllowedException {
            super(type);
        }
    }

    /* Tests for the method countMDofthisType(String) */
    @Test
    public void testCountMDofthisTypeGivenNull() {
        assertEquals(0, fixture.countMDofthisType(null));
    }

    @Test
    public void testCountMDofthisTypeGivenEmptyString() {
        assertEquals(0, fixture.countMDofthisType(""));
    }

    @Test
    public void testCountMDofthisTypeGivenNormalInput() throws MetadataTypeNotAllowedException {
        assertEquals(0, fixture.countMDofthisType("PublisherCorporate"));
        MetadataType type1 = prefs.getMetadataTypeByName("PublisherCorporate");
        Corporate corporate = new Corporate(type1);
        fixture.addCorporate(corporate);
        assertEquals(1, fixture.countMDofthisType("PublisherCorporate"));
        MetadataType type2 = prefs.getMetadataTypeByName("PublisherPerson");
        Person person = new Person(type2);
        fixture.addPerson(person);
        assertEquals(1, fixture.countMDofthisType("PublisherPerson"));
        assertEquals(1, fixture.countMDofthisType("PublisherCorporate"));
        assertEquals(0, fixture.countMDofthisType("SomethingElse"));
    }

    /* Tests for the method isMetadataTypeBeRemoved(PrefsType) */
    @Ignore("The logic in the method cannot pass this test. Null check needed. Avoid NullPointerException whenever possible.")
    @Test
    public void testIsMetadataTypeBeRemovedGivenNull() {
        assertFalse(fixture.isMetadataTypeBeRemoved(null));
    }
    
    @Test
    public void testIsMetadataTypeBeRemovedGivenNormalInput() throws MetadataTypeNotAllowedException {
        MetadataType type1 = prefs.getMetadataTypeByName("PlaceOfPublication"); // num == "+"
        MetadataType type2 = prefs.getMetadataTypeByName("PublicationYear"); // num == "1m"
        MetadataType type3 = prefs.getMetadataTypeByName("PublisherCorporate"); // num == "1o"

        assertEquals(0, fixture.countMDofthisType("PlaceOfPublication"));
        assertTrue(fixture.isMetadataTypeBeRemoved(type1));
        fixture.addMetadata(new Metadata(type1));
        assertEquals(1, fixture.countMDofthisType("PlaceOfPublication"));
        assertFalse(fixture.isMetadataTypeBeRemoved(type1));
        fixture.addMetadata(new Metadata(type1));
        assertEquals(2, fixture.countMDofthisType("PlaceOfPublication"));
        assertTrue(fixture.isMetadataTypeBeRemoved(type1));
        Metadata md1 = fixture.getMetadataList().get(0);
        fixture.removeMetadata(md1, false);
        assertEquals(1, fixture.countMDofthisType("PlaceOfPublication"));
        assertFalse(fixture.isMetadataTypeBeRemoved(type1));
        Metadata md2 = fixture.getMetadataList().get(0);
        fixture.removeMetadata(md2, true);
        assertEquals(0, fixture.countMDofthisType("PlaceOfPublication"));
        assertTrue(fixture.isMetadataTypeBeRemoved(type1));

        assertEquals(0, fixture.countMDofthisType("PublicationYear"));
        assertTrue(fixture.isMetadataTypeBeRemoved(type2));
        fixture.addMetadata(new Metadata(type2));
        assertEquals(1, fixture.countMDofthisType("PublicationYear"));
        assertFalse(fixture.isMetadataTypeBeRemoved(type2));
        Metadata md3 = fixture.getMetadataList().get(0);
        fixture.removeMetadata(md3, true);
        assertEquals(0, fixture.countMDofthisType("PublicationYear"));
        assertTrue(fixture.isMetadataTypeBeRemoved(type2));

        assertEquals(0, fixture.countMDofthisType("PublisherCorporate"));
        assertTrue(fixture.isMetadataTypeBeRemoved(type3));
        fixture.addCorporate(new Corporate(type3));
        assertEquals(1, fixture.countMDofthisType("PublisherCorporate"));
        assertTrue(fixture.isMetadataTypeBeRemoved(type3));
    }

}

