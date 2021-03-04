package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class MetadataGroupTest {

    private Prefs prefs;
    private MetadataGroupType groupType;

    @Before
    public void setUp() throws Exception {
        prefs = new Prefs();
        prefs.loadPrefs("test/resources/ruleset.xml");
        groupType = prefs.getMetadataGroupTypeByName("PublisherGroup");
    }

    @Test
    public void testConstructor() throws Exception {

        MetadataGroup group = new MetadataGroup(groupType);
        assertNotNull(group);
        assertEquals("PublisherGroup", group.getType().getName());
    }

    @Test
    public void testDocStruct() throws Exception {
        MetadataGroup group = new MetadataGroup(groupType);
        assertNotNull(group);
        assertEquals("PublisherGroup", group.getType().getName());

        DocStruct monograph = new DigitalDocument().createDocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertNull(monograph.getAllMetadataGroups());
        monograph.addMetadataGroup(group);
        assertEquals(1, monograph.getAllMetadataGroups().size());
        assertEquals("Monograph", group.getDocStruct().getType().getName());
    }

    @Test
    public void testType() throws Exception {
        MetadataGroup group = new MetadataGroup(groupType);
        group.setType(groupType);
        assertEquals(groupType, group.getType());
    }

    @Test
    public void testTAddMetadata1() throws Exception {
        MetadataGroup fixture = new MetadataGroup(groupType);

        // first case, no explicit limitation configured, adding is possible
        Person p = new Person(prefs.getMetadataTypeByName("PublisherPerson"));
        p.setFirstname("firstname");
        p.setLastname("lastname");
        assertEquals(1, fixture.getPersonList().size());
        fixture.addPerson(p);
        assertEquals(2, fixture.getPersonList().size());
    }

    @Test
    public void testTAddMetadata2() throws Exception {
        // second case, at least one or unlimited, adding is possible
        MetadataGroup fixture = new MetadataGroup(groupType);
        Metadata place = new Metadata(prefs.getMetadataTypeByName("PlaceOfPublication"));
        place.setValue("fixture");
        assertEquals(1, fixture.getMetadataByType("PlaceOfPublication").size());
        fixture.addMetadata(place);
        assertEquals(2, fixture.getMetadataByType("PlaceOfPublication").size());
    }

    @Test(expected = MetadataTypeNotAllowedException.class)
    public void testTAddMetadata3() throws Exception {
        MetadataGroup fixture = new MetadataGroup(groupType);
        // Third case, exact one field is allowed, adding a new one throws exception
        Metadata year = new Metadata(prefs.getMetadataTypeByName("PublicationYear"));
        year.setValue("fixture");
        assertEquals(1, fixture.getMetadataByType("PublicationYear").size());
        fixture.addMetadata(year);
    }

    @Test(expected = MetadataTypeNotAllowedException.class)
    public void testTAddMetadata4() throws Exception {
        MetadataGroup fixture = new MetadataGroup(groupType);
        // fourth case, not more then one field is allowed, adding a new one throws exception
        Corporate c = new Corporate(prefs.getMetadataTypeByName("PublisherCorporate"));
        c .setMainName("fixture");
        assertEquals(1, fixture.getCorporateByType("PublisherCorporate").size());
        fixture.addCorporate(c);
    }

}
