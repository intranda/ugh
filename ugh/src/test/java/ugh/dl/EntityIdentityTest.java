package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * Metadata, Person, Corporate and MetadataGroup are entities, not values: they are mutable, they know their parent, and a document may legitimately
 * hold two of them that carry exactly the same data. Two such objects are therefore not interchangeable - which one of them a caller passes to
 * removeMetadata, removePerson, removeCorporate, removeMetadataGroup or changeMetadata decides which occurrence is affected.
 * </p>
 * <p>
 * These tests pin that contract down for the case where the data of two occurrences is identical, because that is the only case in which value based
 * equality and object identity disagree.
 * </p>
 */
public class EntityIdentityTest {

    private Prefs prefs;
    private DocStruct monograph;

    @Before
    public void setUp() throws Exception {
        prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");
        monograph = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
    }

    private Metadata description(String value) throws Exception {
        Metadata md = new Metadata(prefs.getMetadataTypeByName("Description"));
        md.setValue(value);
        return md;
    }

    private Person contributor(String firstname, String lastname) throws Exception {
        Person person = new Person(prefs.getMetadataTypeByName("Contributor"));
        person.setFirstname(firstname);
        person.setLastname(lastname);
        return person;
    }

    private Corporate corporation(String mainName) throws Exception {
        Corporate corporate = new Corporate(prefs.getMetadataTypeByName("Corporation"));
        corporate.setMainName(mainName);
        return corporate;
    }

    private MetadataGroup publisherGroup() throws Exception {
        return new MetadataGroup(prefs.getMetadataGroupTypeByName("PublisherGroup"));
    }

    /* removal has to affect the occurrence the caller passed */

    @Test
    public void testRemoveMetadataRemovesTheGivenObject() throws Exception {
        Metadata first = description("same value");
        Metadata second = description("same value");
        monograph.addMetadata(first);
        monograph.addMetadata(second);

        monograph.removeMetadata(second, true);

        assertEquals(1, monograph.getAllMetadata().size());
        assertSame(first, monograph.getAllMetadata().get(0));
    }

    /**
     * The document and the removed object have to agree about the removal: everything left in the document knows the document as its parent, and
     * the removed occurrence does not. Detaching one occurrence while dropping another one from the list leaves both in an inconsistent state.
     */
    @Test
    public void testRemoveMetadataDetachesOnlyTheGivenObject() throws Exception {
        Metadata first = description("same value");
        Metadata second = description("same value");
        monograph.addMetadata(first);
        monograph.addMetadata(second);

        monograph.removeMetadata(second, true);

        assertNull("the removed occurrence is detached", second.getParent());
        for (Metadata remaining : monograph.getAllMetadata()) {
            assertSame("metadata in the document knows the document as its parent", monograph, remaining.getParent());
        }
    }

    @Test
    public void testRemovePersonRemovesTheGivenObject() throws Exception {
        Person first = contributor("Jane", "Doe");
        Person second = contributor("Jane", "Doe");
        monograph.addPerson(first);
        monograph.addPerson(second);

        monograph.removePerson(second, true);

        assertEquals(1, monograph.getAllPersons().size());
        assertSame(first, monograph.getAllPersons().get(0));
    }

    @Test
    public void testRemoveCorporateRemovesTheGivenObject() throws Exception {
        Corporate first = corporation("Acme");
        Corporate second = corporation("Acme");
        monograph.addCorporate(first);
        monograph.addCorporate(second);

        monograph.removeCorporate(second, true);

        assertEquals(1, monograph.getAllCorporates().size());
        assertSame(first, monograph.getAllCorporates().get(0));
    }

    /**
     * This one passes today, but only by accident: removeMetadataGroup detaches the group before it looks it up, and MetadataGroup compares its
     * parent, so the detached group no longer matches its siblings. It is kept as a guard for that path.
     */
    @Test
    public void testRemoveMetadataGroupRemovesTheGivenObject() throws Exception {
        MetadataGroup first = publisherGroup();
        MetadataGroup second = publisherGroup();
        monograph.addMetadataGroup(first);
        monograph.addMetadataGroup(second);

        monograph.removeMetadataGroup(second, true);

        assertEquals(1, monograph.getAllMetadataGroups().size());
        assertSame(first, monograph.getAllMetadataGroups().get(0));
    }

    /**
     * MetadataGroup holds its metadata the same way a DocStruct does, so removing from a group has to behave the same.
     */
    @Test
    public void testRemoveMetadataFromGroupRemovesTheGivenObject() throws Exception {
        MetadataGroup group = publisherGroup();
        Metadata first = new Metadata(prefs.getMetadataTypeByName("PlaceOfPublication"));
        first.setValue("same value");
        Metadata second = new Metadata(prefs.getMetadataTypeByName("PlaceOfPublication"));
        second.setValue("same value");
        group.addMetadata(first);
        group.addMetadata(second);

        group.removeMetadata(second, true);

        assertEquals(1, group.getMetadataList().size());
        assertSame(first, group.getMetadataList().get(0));
    }

    /**
     * changeMetadata exchanges one occurrence for another, so it has to exchange the one it was given, at its own position.
     */
    @Test
    public void testChangeMetadataReplacesTheGivenObject() throws Exception {
        Metadata first = description("same value");
        Metadata second = description("same value");
        monograph.addMetadata(first);
        monograph.addMetadata(second);
        Metadata replacement = description("replacement");

        assertTrue(monograph.changeMetadata(second, replacement));

        assertEquals(2, monograph.getAllMetadata().size());
        assertSame(first, monograph.getAllMetadata().get(0));
        assertSame(replacement, monograph.getAllMetadata().get(1));
    }

    /**
     * changeMetadataGroup has the same job for groups, and unlike removeMetadataGroup it does not detach the group first, so nothing hides the
     * lookup by content there.
     */
    @Test
    public void testChangeMetadataGroupReplacesTheGivenObject() throws Exception {
        MetadataGroup parent = new MetadataGroup(prefs.getMetadataGroupTypeByName("TestGroup"));
        MetadataGroup first = new MetadataGroup(prefs.getMetadataGroupTypeByName("UnusedGroup"));
        MetadataGroup second = new MetadataGroup(prefs.getMetadataGroupTypeByName("UnusedGroup"));
        parent.addMetadataGroup(first);
        parent.addMetadataGroup(second);
        MetadataGroup replacement = new MetadataGroup(prefs.getMetadataGroupTypeByName("LocationGroup"));

        parent.changeMetadataGroup(second, replacement);

        assertEquals(2, parent.getAllMetadataGroups().size());
        assertSame(first, parent.getAllMetadataGroups().get(0));
        assertSame(replacement, parent.getAllMetadataGroups().get(1));
    }

    /* the contract those removals rest on */

    @Test
    public void testEqualContentDoesNotMakeDistinctOccurrencesEqual() throws Exception {
        assertNotEquals("two metadata occurrences of one document are distinct", description("same value"), description("same value"));
        assertNotEquals("two person occurrences of one document are distinct", contributor("Jane", "Doe"), contributor("Jane", "Doe"));
        assertNotEquals("two corporate occurrences of one document are distinct", corporation("Acme"), corporation("Acme"));
        assertNotEquals("two group occurrences of one document are distinct", publisherGroup(), publisherGroup());
    }

    /**
     * Whatever equality means for these classes, equal objects have to agree on their hash code. Metadata hashes its authority value without
     * comparing it, and MetadataGroup hashes its corporates without comparing them, so both used to report equal objects with different hash codes.
     */
    @Test
    public void testEqualObjectsAgreeOnTheirHashCode() throws Exception {
        Metadata withAuthority = description("same value");
        withAuthority.setAuthorityFile("id1", "uri1", "value1");
        Metadata withOtherAuthority = description("same value");
        withOtherAuthority.setAuthorityFile("id2", "uri2", "value2");

        if (withAuthority.equals(withOtherAuthority)) {
            assertEquals(withAuthority.hashCode(), withOtherAuthority.hashCode());
        }

        MetadataGroup withCorporate = publisherGroup();
        Corporate publisher = new Corporate(prefs.getMetadataTypeByName("PublisherCorporate"));
        publisher.setMainName("Acme");
        withCorporate.addCorporate(publisher);
        MetadataGroup withoutCorporate = publisherGroup();

        if (withCorporate.equals(withoutCorporate)) {
            assertEquals(withCorporate.hashCode(), withoutCorporate.hashCode());
        }
    }
}
