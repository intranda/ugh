package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ugh.exceptions.UGHException;

public class DocStructTest {

    private Prefs prefs;

    @Before
    public void setUp() throws Exception {
        prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");
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
        c.addSubName(new NamePart("subname", "sub"));
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
        c.addSubName(new NamePart("subname", "sub"));
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

    @Test
    public void testValidationErrorPresent() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertNotNull(ds);
        assertFalse(ds.isValidationErrorPresent());
        ds.setValidationErrorPresent(true);
        assertTrue(ds.isValidationErrorPresent());

    }

    @Test
    public void testValidationMessage() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        ds.setValidationMessage("fixture");
        assertEquals("fixture", ds.getValidationMessage());
    }

    @Test
    public void testAdditionalValue() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        ds.setAdditionalValue("fixture");
        assertEquals("fixture", ds.getAdditionalValue());
    }

    @Test
    public void testOrderLabel() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        ds.setOrderLabel("fixture");
        assertEquals("fixture", ds.getOrderLabel());
    }

    @Test
    public void testLink() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        ds.setLink("fixture");
        assertEquals("fixture", ds.getLink());
    }

    @Test
    public void testChildren() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertNull(ds.getAllChildren());
        DocStruct titlePage = new DocStruct(prefs.getDocStrctTypeByName("TitlePage"));
        ds.addChild(titlePage);
        assertEquals(1, ds.getAllChildren().size());
    }

    @Test
    public void testReferenceToAnchor() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        ds.setReferenceToAnchor("fixture");
        assertEquals("fixture", ds.getReferenceToAnchor());
    }

    @Test
    public void testAllChildrenByTypeAndMetadataType() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        DocStructType type = prefs.getDocStrctTypeByName("Chapter");
        MetadataType title = prefs.getMetadataTypeByName("TitleDocMain");
        // empty, no children
        assertNull(ds.getAllChildrenByTypeAndMetadataType(type.getName(), title.getName()));
        DocStruct chapter = new DocStruct(type);
        ds.addChild(chapter);
        // empty, chidlren have no title
        assertNull(ds.getAllChildrenByTypeAndMetadataType(type.getName(), title.getName()));

        Metadata md = new Metadata(title);
        md.setValue("fixture");
        chapter.addMetadata(md);
        // not empty
        assertEquals(1, ds.getAllChildrenByTypeAndMetadataType(type.getName(), title.getName()).size());
    }

    @Test
    public void testIdentifier() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        ds.setIdentifier("fixture");
        assertEquals("fixture", ds.getIdentifier());
    }

    @Test
    public void testAllIdentifierMetadata() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataType mdt = prefs.getMetadataTypeByName("CatalogIDDigital");
        // no identifier metadata found
        assertNull(ds.getAllIdentifierMetadata());

        Metadata md = new Metadata(mdt);
        md.setValue("fixture");
        ds.addMetadata(md);
        // not empty
        assertEquals(1, ds.getAllIdentifierMetadata().size());
    }

    @Test
    public void testCopy() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        DocStructType dst = prefs.getDocStrctTypeByName("Chapter");
        MetadataType mdt = prefs.getMetadataTypeByName("CatalogIDDigital");
        DocStruct chapter = new DocStruct(dst);
        ds.addChild(chapter);

        Metadata md = new Metadata(mdt);
        md.setValue("fixture");
        ds.addMetadata(md);

        MetadataType title = prefs.getMetadataTypeByName("TitleDocMain");
        Metadata meta = new Metadata(title);
        meta.setValue("fixture");
        chapter.addMetadata(meta);

        Person p = new Person(prefs.getMetadataTypeByName("Author"));
        p.setFirstname("fixture");
        ds.addPerson(p);

        Corporate c = new Corporate(prefs.getMetadataTypeByName("CorporateCurator"));
        c.setMainName("fixture");
        ds.addCorporate(c);

        MetadataGroup mg = new MetadataGroup(prefs.getMetadataGroupTypeByName("PublisherGroup"));
        Metadata place = new Metadata(prefs.getMetadataTypeByName("PlaceOfPublication"));
        place.setValue("fixture");
        mg.addMetadata(place);
        ds.addMetadataGroup(mg);

        // copy without data
        DocStruct other = ds.copy(false, false);
        assertNotNull(other);
        assertEquals("Monograph", other.getType().getName());
        assertNull(other.getAllChildren());
        assertNull(other.getAllMetadata());

        // copy with metadata
        other = ds.copy(true, false);
        assertNotNull(other);
        assertEquals("Monograph", other.getType().getName());
        assertNull(other.getAllChildren());
        assertNotNull(other.getAllMetadata());
        assertEquals(1, other.getAllMetadata().size());

        // copy with sub elements
        other = ds.copy(false, true);
        assertNotNull(other);
        assertEquals("Monograph", other.getType().getName());
        assertNotNull(other.getAllChildren());
        assertNull(other.getAllMetadata());
        assertEquals(1, other.getAllChildren().size());

        // copy everything
        other = ds.copy(true, true);
        assertNotNull(other);
        assertEquals("Monograph", other.getType().getName());
        assertNotNull(other.getAllChildren());
        assertNotNull(other.getAllMetadata());
        assertEquals(1, other.getAllMetadata().size());
        assertEquals(1, other.getAllChildren().size());
    }

    @Test
    public void testAllReferences() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        assertNull(ds.getAllReferences(null)); //null
        assertNull(ds.getAllReferences("")); // unknown
        assertTrue(ds.getAllReferences("to").isEmpty()); // empty
        assertTrue(ds.getAllReferences("from").isEmpty());// empty

        DocStruct page = new DocStruct(prefs.getDocStrctTypeByName("page"));
        ds.addReferenceTo(page, "logical_physical");
        assertEquals(1, ds.getAllReferences("to").size());
        assertEquals(1, page.getAllReferences("from").size());
    }

    @Test
    public void testAllToReferences() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertTrue(ds.getAllToReferences().isEmpty());
        DocStruct page = new DocStruct(prefs.getDocStrctTypeByName("page"));
        ds.addReferenceTo(page, "logical_physical");
        assertEquals(1, ds.getAllToReferences().size());
    }

    @Test
    public void testAllToReferencesType() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertNull(ds.getAllToReferences("xyz"));
        DocStruct page = new DocStruct(prefs.getDocStrctTypeByName("page"));
        ds.addReferenceTo(page, "logical_physical");
        assertNull(ds.getAllToReferences("xyz"));
        assertEquals(1, ds.getAllToReferences("logical_physical").size());
    }

    @Test
    public void testAllFronReferencesType() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        DocStruct page = new DocStruct(prefs.getDocStrctTypeByName("page"));
        assertTrue(ds.getAllFromReferences().isEmpty());
        ds.addReferenceTo(page, "logical_physical");
        assertNull(page.getAllFromReferences("xyz"));
        assertEquals(1, page.getAllFromReferences("logical_physical").size());
    }

    @Test
    public void testSetAllMetadata() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        List<Metadata> mdl = new ArrayList<>();
        MetadataType mdt = prefs.getMetadataTypeByName("CatalogIDDigital");
        Metadata md = new Metadata(mdt);
        md.setValue("fixture");
        mdl.add(md);
        ds.setAllMetadata(mdl);
        assertEquals(1, ds.getAllMetadata().size());
    }

    @Test
    public void testSetAllPersons() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        List<Person> pdl = new ArrayList<>();
        Person p = new Person(prefs.getMetadataTypeByName("Author"));
        p.setFirstname("fixture");
        pdl.add(p);
        ds.setAllPersons(pdl);
        assertEquals(1, ds.getAllPersons().size());
    }

    @Test
    public void testSetAllCorporates() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        List<Corporate> cdl = new ArrayList<>();
        Corporate c = new Corporate(prefs.getMetadataTypeByName("CorporateCurator"));
        c.setMainName("fixture");
        cdl.add(c);
        ds.setAllCorporates(cdl);
        assertEquals(1, ds.getAllCorporates().size());
    }

    @Test
    public void testSetAllGroups() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        List<MetadataGroup> mgl = new ArrayList<>();
        MetadataGroup mg = new MetadataGroup(prefs.getMetadataGroupTypeByName("PublisherGroup"));
        Metadata place = new Metadata(prefs.getMetadataTypeByName("PlaceOfPublication"));
        place.setValue("fixture");
        mg.addMetadata(place);
        mgl.add(mg);
        ds.setAllMetadataGroups(mgl);
        assertEquals(1, ds.getAllMetadataGroups().size());
    }

    @Test
    public void testGetAllContentFiles() throws Exception {
        DigitalDocument dd = new DigitalDocument();

        DocStruct ds = dd.createDocStruct(prefs.getDocStrctTypeByName("Monograph"));
        dd.setLogicalDocStruct(ds);
        assertNull(ds.getAllContentFiles());

        ContentFile cf = new ContentFile();
        ds.addContentFile(cf);
        assertEquals(1, ds.getAllContentFiles().size());
    }

    @Test
    public void testHasMetadataGroupType() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertFalse(ds.hasMetadataGroupType(null));
        MetadataGroupType mgt = prefs.getMetadataGroupTypeByName("PublisherGroup");
        assertFalse(ds.hasMetadataGroupType(mgt));
        MetadataGroup mg = new MetadataGroup(prefs.getMetadataGroupTypeByName("PublisherGroup"));
        ds.addMetadataGroup(mg);
        assertTrue(ds.hasMetadataGroupType(mgt));
    }


    @Test
    public void testHasMetadataype() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertFalse(ds.hasMetadataType(null));

        MetadataType idType = prefs.getMetadataTypeByName("CatalogIDDigital");
        assertFalse(ds.hasMetadataType(idType));
        Metadata md = new Metadata(idType);
        md.setValue("fixture");
        ds.addMetadata(md);
        assertTrue(ds.hasMetadataType(idType));

        MetadataType personType = prefs.getMetadataTypeByName("Author");
        assertFalse(ds.hasMetadataType(personType));
        Person p = new Person(personType);
        p.setFirstname("fixture");
        ds.addPerson(p);
        assertTrue(ds.hasMetadataType(personType));

        MetadataType corpType = prefs.getMetadataTypeByName("Corporation");
        assertFalse(ds.hasMetadataType(corpType));
        Corporate c = new Corporate(corpType);
        c.setMainName("fixture");
        ds.addCorporate(c);
        assertTrue(ds.hasMetadataType(corpType));
    }

    @Test
    public void testGetAllContentFileReferences() throws Exception {
        DigitalDocument dd = new DigitalDocument();
        DocStruct ds = dd.createDocStruct(prefs.getDocStrctTypeByName("Monograph"));
        dd.setLogicalDocStruct(ds);
        assertNull(ds.getAllContentFiles());
        assertTrue(ds.getAllContentFileReferences().isEmpty());
        ContentFile cf = new ContentFile();
        ds.addContentFile(cf);
        assertEquals(1, ds.getAllContentFiles().size());
        assertEquals(1, ds.getAllContentFileReferences().size());
    }

    @Test
    public void testAddContentFileArea() throws Exception {
        DigitalDocument dd = new DigitalDocument();
        FileSet fs = new FileSet();
        dd.setFileSet(fs);

        DocStruct ds = dd.createDocStruct(prefs.getDocStrctTypeByName("Monograph"));
        dd.setLogicalDocStruct(ds);
        assertNull(ds.getAllContentFiles());

        ContentFile cf = new ContentFile();
        ContentFileArea cfa = new ContentFileArea();
        ds.addContentFile(cf, cfa);
        assertEquals(1, ds.getAllContentFiles().size());
    }


    @Test
    public void testRemoveContentFile() throws Exception {
        DigitalDocument dd = new DigitalDocument();
        DocStruct ds = dd.createDocStruct(prefs.getDocStrctTypeByName("Monograph"));
        dd.setLogicalDocStruct(ds);

        ContentFile cf = new ContentFile();
        ds.addContentFile(cf);
        assertEquals(1, ds.getAllContentFiles().size());
        assertTrue(ds.removeContentFile(cf));
    }



    @Test
    public void testAddReferenceFrom() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertTrue(ds.getAllToReferences().isEmpty());
        DocStruct page = new DocStruct(prefs.getDocStrctTypeByName("page"));
        assertTrue(page.getAllFromReferences().isEmpty());
        page.addReferenceFrom(ds, "logical_physical");
        assertEquals(1, ds.getAllToReferences().size());
        assertEquals(1, page.getAllFromReferences().size());
    }

    @Test
    public void testRemoveReferenceTo() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertTrue(ds.getAllToReferences().isEmpty());
        DocStruct page = new DocStruct(prefs.getDocStrctTypeByName("page"));
        ds.addReferenceTo(page, "logical_physical");
        assertEquals(1, ds.getAllToReferences().size());
        ds.removeReferenceTo(page);
        assertTrue(ds.getAllToReferences().isEmpty());
    }

    @Test
    public void testRemoveReferenceFrom() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertTrue(ds.getAllToReferences().isEmpty());
        DocStruct page = new DocStruct(prefs.getDocStrctTypeByName("page"));
        ds.addReferenceTo(page, "logical_physical");
        assertEquals(1, ds.getAllToReferences().size());
        page.removeReferenceFrom(ds);
        assertTrue(page.getAllFromReferences().isEmpty());
    }

    // TODO continue with line 1312

}
