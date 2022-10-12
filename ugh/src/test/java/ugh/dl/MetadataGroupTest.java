package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

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
        groupType = prefs.getMetadataGroupTypeByName("PublisherGroup");
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

}
