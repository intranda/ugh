package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import ugh.exceptions.UGHException;

public class DocStructTest {

    private Prefs prefs;

    @Before
    public void setUp() throws Exception {
        prefs = new Prefs();
        prefs.loadPrefs("test/resources/ruleset.xml");
    }

    @Test
    public void testMetadata() throws UGHException {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        assertNotNull(ds);
        assertEquals("Monograph", ds.getType().getName());
        assertNull(ds.getAllMetadata());

        Metadata md = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        md.setValue("fixture");
        ds.addMetadata(md);

        assertNotNull(ds.getAllMetadata());
        assertEquals(1, ds.getAllMetadata().size());
        assertEquals("fixture", ds.getAllMetadata().get(0).getValue());

        ds.removeMetadata(md, true);
        assertNull(ds.getAllMetadata());
    }

    @Test
    public void testPerson() throws UGHException {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        assertNotNull(ds);
        assertEquals("Monograph", ds.getType().getName());
        assertNull(ds.getAllPersons());

        Person p = new Person(prefs.getMetadataTypeByName("Author"));
        p.setFirstname("firstname");
        p.setLastname("lastname");
        ds.addPerson(p);

        assertNotNull(ds.getAllPersons());
        assertEquals(1, ds.getAllPersons().size());
        assertEquals("lastname", ds.getAllPersons().get(0).getLastname());

        ds.removePerson(p, true);
        assertNull(ds.getAllPersons());
    }

    @Test
    public void testCorporate() throws UGHException {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        assertNotNull(ds);
        assertEquals("Monograph", ds.getType().getName());
        assertNull(ds.getAllCorporates());

        Corporate c = new Corporate(prefs.getMetadataTypeByName("Corporation"));
        c.setMainName("main");
        c.addSubName(new NamePart("subname","sub"));
        c.setPartName("part");
        ds.addCorporate(c);

        assertNotNull(ds.getAllCorporates());
        assertEquals(1, ds.getAllCorporates().size());
        assertEquals("main", ds.getAllCorporates().get(0).getMainName());

        ds.removeCorporate(c, true);
        assertNull(ds.getAllCorporates());
    }

    @Test
    public void testAllMetadataTypes() throws UGHException {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        assertNotNull(ds);
        assertEquals("Monograph", ds.getType().getName());
        assertNull(ds.getAllCorporates());
        assertNull(ds.getAllPersons());
        assertNull(ds.getAllMetadata());

        Corporate c = new Corporate(prefs.getMetadataTypeByName("Corporation"));
        c.setMainName("main");
        c.addSubName(new NamePart("subname","sub"));
        c.setPartName("part");
        ds.addCorporate(c);

        Person p = new Person(prefs.getMetadataTypeByName("Author"));
        p.setFirstname("firstname");
        p.setLastname("lastname");
        ds.addPerson(p);

        Metadata md = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        md.setValue("fixture");
        ds.addMetadata(md);

        assertNotNull(ds.getAllMetadata());
        assertEquals(1, ds.getAllMetadata().size());
        assertEquals("fixture", ds.getAllMetadata().get(0).getValue());


        assertNotNull(ds.getAllPersons());
        assertEquals(1, ds.getAllPersons().size());
        assertEquals("lastname", ds.getAllPersons().get(0).getLastname());

        assertNotNull(ds.getAllCorporates());
        assertEquals(1, ds.getAllCorporates().size());
        assertEquals("main", ds.getAllCorporates().get(0).getMainName());
    }

}
