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

import ugh.exceptions.TypeNotAllowedAsChildException;
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

    @Test
    public void testForceRemoveMetadataGroup() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataGroupType mgt = prefs.getMetadataGroupTypeByName("PublisherGroup");
        assertFalse(ds.hasMetadataGroupType(mgt));
        MetadataGroup mg = new MetadataGroup(prefs.getMetadataGroupTypeByName("PublisherGroup"));
        ds.addMetadataGroup(mg);
        assertTrue(ds.hasMetadataGroupType(mgt));
        ds.removeMetadataGroup(mg, true);
        assertFalse(ds.hasMetadataGroupType(mgt));
    }

    @Test
    public void testRemoveMetadataGroup() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataGroupType mgt = prefs.getMetadataGroupTypeByName("PublisherGroup");
        assertFalse(ds.hasMetadataGroupType(mgt));
        MetadataGroup mg = new MetadataGroup(prefs.getMetadataGroupTypeByName("PublisherGroup"));
        ds.addMetadataGroup(mg);
        assertTrue(ds.hasMetadataGroupType(mgt));
        ds.removeMetadataGroup(mg);
        assertFalse(ds.hasMetadataGroupType(mgt));
    }

    @Test
    public void testChangeMetadataGroup() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataGroupType mgt = prefs.getMetadataGroupTypeByName("PublisherGroup");
        MetadataGroupType mgt2 = prefs.getMetadataGroupTypeByName("LocationGroup");
        MetadataGroup mg = new MetadataGroup(mgt);
        MetadataGroup mg2 = new MetadataGroup(mgt2);

        Metadata place = new Metadata(prefs.getMetadataTypeByName("PlaceOfPublication"));
        place.setValue("fixture");
        mg.addMetadata(place);

        Metadata place2 = new Metadata(prefs.getMetadataTypeByName("City"));
        place2.setValue("fixture2");
        mg2.addMetadata(place2);

        ds.addMetadataGroup(mg);

        assertEquals("fixture", ds.getAllMetadataGroups().get(0).getMetadataList().get(0).getValue());

        ds.changeMetadataGroup(mg, mg2);

        assertEquals("fixture2", ds.getAllMetadataGroups().get(0).getMetadataList().get(0).getValue());

    }

    @Test
    public void testGetAllMetadataGroupsByType() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataGroupType mgt = prefs.getMetadataGroupTypeByName("PublisherGroup");
        MetadataGroup mg = new MetadataGroup(mgt);
        // empty as type is null
        assertTrue(ds.getAllMetadataGroupsByType(null).isEmpty());
        // empty as ds has no groups
        assertTrue(ds.getAllMetadataGroupsByType(mgt).isEmpty());

        ds.addMetadataGroup(mg);
        // not empty
        assertEquals(1, ds.getAllMetadataGroupsByType(mgt).size());

    }

    @Test
    public void testAddMetadata() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        Metadata md = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        md.setValue("fixture");
        ds.addMetadata(md);
        assertEquals("fixture", ds.getAllMetadata().get(0).getValue());
    }

    @Test
    public void testRemoveMetadata() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        Metadata md = new Metadata(prefs.getMetadataTypeByName("TitleUniform"));
        md.setValue("fixture");
        ds.addMetadata(md);
        assertEquals("fixture", ds.getAllMetadata().get(0).getValue());
        ds.removeMetadata(md);
        assertNull(ds.getAllMetadata());
    }

    @Test
    public void testChangeMetadata() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        Metadata md = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        md.setValue("main title");
        Metadata md2 = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        md2.setValue("uniform title");

        ds.addMetadata(md);
        assertEquals("main title", ds.getAllMetadata().get(0).getValue());
        ds.changeMetadata(md, md2);
        assertEquals("uniform title", ds.getAllMetadata().get(0).getValue());
    }

    @Test
    public void testAllMetadataByType() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataType mtype = prefs.getMetadataTypeByName("TitleDocMain");
        MetadataType ptype = prefs.getMetadataTypeByName("Author");
        MetadataType ctype = prefs.getMetadataTypeByName("CorporateOther");
        Metadata m = new Metadata(mtype);

        Person p = new Person(ptype);
        Corporate c = new Corporate(ctype);

        assertTrue(ds.getAllMetadataByType(null).isEmpty());
        assertTrue(ds.getAllMetadataByType(mtype).isEmpty());
        assertTrue(ds.getAllMetadataByType(ptype).isEmpty());
        assertTrue(ds.getAllMetadataByType(ctype).isEmpty());

        ds.addMetadata(m);
        ds.addPerson(p);
        ds.addCorporate(c);
        assertEquals(1, ds.getAllMetadataByType(mtype).size());
        assertEquals(1, ds.getAllMetadataByType(ptype).size());
        assertEquals(1, ds.getAllMetadataByType(ctype).size());
    }

    @Test
    public void testAllPersonsByType() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataType ptype = prefs.getMetadataTypeByName("Author");
        Person p = new Person(ptype);
        assertNull(ds.getAllPersonsByType(null));
        assertNull(ds.getAllPersonsByType(ptype));
        ds.addPerson(p);
        assertEquals(1, ds.getAllPersonsByType(ptype).size());
    }

    @Test
    public void testAllCorporatesByType() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataType ctype = prefs.getMetadataTypeByName("CorporateOther");
        Corporate c = new Corporate(ctype);
        assertNull(ds.getAllCorporatesByType(null));
        assertNull(ds.getAllCorporatesByType(ctype));
        ds.addCorporate(c);
        assertEquals(1, ds.getAllCorporatesByType(ctype).size());
    }

    @Test
    public void testAllVisibleMetadata() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataType mtype = prefs.getMetadataTypeByName("TitleDocMain");
        MetadataType invistype = prefs.getMetadataTypeByName("_digitalOrigin");
        Metadata m = new Metadata(mtype);
        Metadata invis = new Metadata(invistype);

        assertNull(ds.getAllVisibleMetadata());

        ds.addMetadata(invis);
        assertNull(ds.getAllVisibleMetadata());
        ds.addMetadata(m);
        assertEquals(1, ds.getAllVisibleMetadata().size());
    }

    @Test
    public void testDefaultDisplayMetadataGroupTypes() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        // PublisherGroup is set to DefaultDisplay="true"
        assertEquals(1, ds.getDefaultDisplayMetadataGroupTypes().size());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDisplayMetadataGroupTypes() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertEquals(1, ds.getDisplayMetadataGroupTypes().size());
    }

    @Test
    public void testGetDefaultDisplayMetadataTypes() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        // 11 metadata assignments are set to DefaultDisplay="true"
        assertEquals(11, ds.getDefaultDisplayMetadataTypes().size());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetDisplayMetadataTypes() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertEquals(11, ds.getDisplayMetadataTypes().size());
    }

    @Test
    public void testCountMDofthisType() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        MetadataType mtype = prefs.getMetadataTypeByName("TitleDocMain");
        MetadataType ptype = prefs.getMetadataTypeByName("Author");
        MetadataType ctype = prefs.getMetadataTypeByName("CorporateOther");
        MetadataGroupType mgt = prefs.getMetadataGroupTypeByName("PublisherGroup");
        MetadataGroup mg = new MetadataGroup(mgt);
        Metadata m = new Metadata(mtype);
        Person p = new Person(ptype);
        Corporate c = new Corporate(ctype);

        assertEquals(0, ds.countMDofthisType("TitleDocMain"));

        ds.addMetadata(m);
        ds.addPerson(p);
        ds.addCorporate(c);
        ds.addMetadataGroup(mg);
        assertEquals(1, ds.countMDofthisType("TitleDocMain"));
        assertEquals(1, ds.countMDofthisType("Author"));
        assertEquals(1, ds.countMDofthisType("CorporateOther"));
        assertEquals(1, ds.countMDofthisType("PublisherGroup"));
    }

    @Test
    public void testGetAddableMetadataGroupTypes() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        assertEquals(2, ds.getAddableMetadataGroupTypes().size());

        MetadataGroupType mgt2 = prefs.getMetadataGroupTypeByName("LocationGroup");
        MetadataGroup mg2 = new MetadataGroup(mgt2);
        ds.addMetadataGroup(mg2);

        assertEquals(1, ds.getAddableMetadataGroupTypes().size());
    }

    @Test
    public void testGetPossibleMetadataGroupTypes() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        assertEquals(2, ds.getAddableMetadataGroupTypes().size());

        MetadataGroupType mgt2 = prefs.getMetadataGroupTypeByName("LocationGroup");
        MetadataGroup mg2 = new MetadataGroup(mgt2);
        ds.addMetadataGroup(mg2);

        assertEquals(1, ds.getPossibleMetadataGroupTypes().size());
    }

    @Test
    public void testGetAddableMetadataTypes() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));

        assertEquals(110, ds.getAddableMetadataTypes(false).size());
        assertEquals(119, ds.getAddableMetadataTypes(true).size());

        Metadata md = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        md.setValue("fixture");
        ds.addMetadata(md);
        Person p = new Person(prefs.getMetadataTypeByName("Editor"));
        p.setLastname("fixture");
        ds.addPerson(p);

        assertEquals(109, ds.getAddableMetadataTypes(false).size());
        assertEquals(118, ds.getAddableMetadataTypes(true).size());

    }

    @Test
    public void testAddChild() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        assertFalse(ds.addChild(null));

        DocStruct sub = new DocStruct(prefs.getDocStrctTypeByName("Chapter"));
        assertTrue(ds.addChild(sub));
    }

    @Test(expected = TypeNotAllowedAsChildException.class)
    public void testAddChildException() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        ds.addChild(ds);
    }

    @Test
    public void testRemove() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        DocStruct sub = new DocStruct(prefs.getDocStrctTypeByName("Chapter"));
        assertTrue(ds.addChild(sub));

        assertTrue(ds.removeChild(sub));
    }

    @Test
    public void testMoveChild() throws Exception {
        DocStruct ds = new DocStruct(prefs.getDocStrctTypeByName("Monograph"));
        DocStruct sub = new DocStruct(prefs.getDocStrctTypeByName("Chapter"));
        DocStruct sub2 = new DocStruct(prefs.getDocStrctTypeByName("Cover"));
        assertTrue(ds.addChild(sub));
        assertTrue(ds.addChild(sub2));

        assertEquals("Chapter", ds.getAllChildren().get(0).getType().getName());
        assertEquals("Cover", ds.getAllChildren().get(1).getType().getName());

        assertFalse(ds.moveChild(sub2, -1));
        assertTrue(ds.moveChild(sub2, 0));

        assertEquals("Cover", ds.getAllChildren().get(0).getType().getName());
        assertEquals("Chapter", ds.getAllChildren().get(1).getType().getName());
    }

    // TODO continue with line 2312
}
