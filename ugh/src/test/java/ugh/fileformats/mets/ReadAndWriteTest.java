package ugh.fileformats.mets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ugh.dl.Corporate;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;

public class ReadAndWriteTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Prefs prefs;
    private MetsMods mm;

    @Before
    public void setUp() throws Exception {
        prefs = new Prefs();

        prefs.loadPrefs("src/test/resources/ruleset.xml");

        mm = new MetsMods(prefs);
        mm.read("src/test/resources/meta.xml");
    }

    @Test
    public void testAllowAccessRestrictMetadata() throws Exception {
        Metadata publicationYear = null;

        DocStruct logical = mm.getDigitalDocument().getLogicalDocStruct();
        for (Metadata md : logical.getAllMetadata()) {
            if ("PublicationYear".equals(md.getType().getName())) {
                publicationYear = md;
                break;
            }
        }
        // add access restriction to metadata
        assertNotNull(publicationYear);
        assertTrue(publicationYear.getType().isAllowAccessRestriction());
        assertFalse(publicationYear.isAccessRestrict());
        publicationYear.setAccessRestrict(true);

        // save metadata
        File exportFile = folder.newFile();
        mm.write(exportFile.toString());

        // read it again
        mm.read(exportFile.toString());
        Metadata publicationYearAfterSave = null;
        logical = mm.getDigitalDocument().getLogicalDocStruct();
        for (Metadata md : logical.getAllMetadata()) {
            if ("PublicationYear".equals(md.getType().getName())) {
                publicationYearAfterSave = md;
                break;
            }
        }
        // access restriction is still set
        assertNotNull(publicationYearAfterSave);
        assertTrue(publicationYearAfterSave.isAccessRestrict());
    }

    @Test
    public void testAllowAccessRestrictMetadataNotAllowed() throws Exception {
        Metadata place = null;

        DocStruct logical = mm.getDigitalDocument().getLogicalDocStruct();
        for (Metadata md : logical.getAllMetadata()) {
            if ("PlaceOfPublication".equals(md.getType().getName())) {
                place = md;
                break;
            }
        }
        // access restriction is not allowed
        assertNotNull(place);
        assertFalse(place.getType().isAllowAccessRestriction());
        assertFalse(place.isAccessRestrict());
        // try to add access restriction to metadata
        place.setAccessRestrict(true);

        // save metadata
        File exportFile = folder.newFile();
        mm.write(exportFile.toString());

        // read it again
        mm.read(exportFile.toString());
        Metadata placeAfterSave = null;
        logical = mm.getDigitalDocument().getLogicalDocStruct();
        for (Metadata md : logical.getAllMetadata()) {
            if ("PlaceOfPublication".equals(md.getType().getName())) {
                placeAfterSave = md;
                break;
            }
        }
        // access restriction is not set
        assertNotNull(placeAfterSave);
        assertFalse(placeAfterSave.isAccessRestrict());
    }

    @Test
    public void testAllowAccessRestrictPerson() throws Exception {

        MetadataType personTypeWithoutRestriction = prefs.getMetadataTypeByName("Editor");
        MetadataType personTypeWithRestriction = prefs.getMetadataTypeByName("Author");

        Person pWithout = new Person(personTypeWithoutRestriction);
        pWithout.setLastname("last");
        // try to set access restriction
        pWithout.setAccessRestrict(true);

        Person pWith = new Person(personTypeWithRestriction);
        pWith.setLastname("last");
        // try to set access restriction
        pWith.setAccessRestrict(true);

        mm.getDigitalDocument().getLogicalDocStruct().addPerson(pWithout);
        mm.getDigitalDocument().getLogicalDocStruct().addPerson(pWith);

        // save metadata
        File exportFile = folder.newFile();
        mm.write(exportFile.toString());

        // read it again
        mm.read(exportFile.toString());
        Person personWithAccessRestrictionAfterSave = null;
        Person personWithoutAccessRestrictionAfterSave = null;
        for (Person p : mm.getDigitalDocument().getLogicalDocStruct().getAllPersons()) {
            if (personTypeWithRestriction.getName().equals(p.getType().getName()) && "last".equals(p.getLastname())) {
                personWithAccessRestrictionAfterSave = p;
            } else if (personTypeWithoutRestriction.getName().equals(p.getType().getName()) && "last".equals(p.getLastname())) {
                personWithoutAccessRestrictionAfterSave = p;
            }
        }

        // access restriction is false, change was not saved
        assertNotNull(personWithoutAccessRestrictionAfterSave);
        assertFalse(personWithoutAccessRestrictionAfterSave.isAccessRestrict());

        // access restriction is true
        assertNotNull(personWithAccessRestrictionAfterSave);
        assertTrue(personWithAccessRestrictionAfterSave.isAccessRestrict());
    }

    @Test
    public void testAllowAccessRestrictCorporate() throws Exception {

        MetadataType corporateTypeWithoutRestriction = prefs.getMetadataTypeByName("CorporateOther");
        MetadataType corporateTypeWithRestriction = prefs.getMetadataTypeByName("Corporation");

        Corporate without = new Corporate(corporateTypeWithoutRestriction);
        without.setMainName("main");
        // try to set access restriction
        without.setAccessRestrict(true);

        Corporate with = new Corporate(corporateTypeWithRestriction);
        with.setMainName("main");
        // try to set access restriction
        with.setAccessRestrict(true);

        mm.getDigitalDocument().getLogicalDocStruct().addCorporate(without);
        mm.getDigitalDocument().getLogicalDocStruct().addCorporate(with);

        // save metadata
        File exportFile = folder.newFile();
        mm.write(exportFile.toString());

        // read it again
        mm.read(exportFile.toString());

        without = null;
        with = null;
        for (Corporate p : mm.getDigitalDocument().getLogicalDocStruct().getAllCorporates()) {
            if (corporateTypeWithRestriction.getName().equals(p.getType().getName())) {
                with = p;
            } else if (corporateTypeWithoutRestriction.getName().equals(p.getType().getName())) {
                without = p;
            }
        }

        // access restriction is false, change was not saved
        assertNotNull(without);
        assertFalse(without.isAccessRestrict());

        // access restriction is true
        assertNotNull(with);
        assertTrue(with.isAccessRestrict());
    }

    @Test
    public void testExportAccessRestrictMetadata() throws Exception {
        Metadata publicationYear = null;

        DocStruct logical = mm.getDigitalDocument().getLogicalDocStruct();
        for (Metadata md : logical.getAllMetadata()) {
            if ("PublicationYear".equals(md.getType().getName())) {
                publicationYear = md;
                break;
            }
        }
        publicationYear.setAccessRestrict(true);
        File exportFile = folder.newFile();

        MetsModsImportExport mmio = new MetsModsImportExport(prefs);
        mmio.setDigitalDocument(mm.getDigitalDocument());
        mmio.write(exportFile.toString());

        // open exported file

        Namespace mets = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
        Namespace mods = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(exportFile);

        XPathFactory xpathFactory = XPathFactory.instance();
        XPathExpression<Element> expr = xpathFactory.compile("//mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:originInfo/mods:dateIssued",
                Filters.element(), null, mets, mods);

        Element element = expr.evaluateFirst(doc);
        assertNotNull(element);
        assertEquals("no", element.getAttributeValue("shareable"));
    }

    @Test
    public void testExportAccessRestrictPerson() throws Exception {
        Person author = null;

        DocStruct logical = mm.getDigitalDocument().getLogicalDocStruct();
        for (Person md : logical.getAllPersons()) {
            if ("Author".equals(md.getType().getName())) {
                author = md;
                break;
            }
        }
        author.setAccessRestrict(true);
        File exportFile = folder.newFile();

        MetsModsImportExport mmio = new MetsModsImportExport(prefs);
        mmio.setDigitalDocument(mm.getDigitalDocument());
        mmio.write(exportFile.toString());

        // open exported file

        Namespace mets = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
        Namespace mods = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(exportFile);

        XPathFactory xpathFactory = XPathFactory.instance();
        XPathExpression<Element> expr = xpathFactory.compile("//mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:name[@type='personal']",
                Filters.element(), null, mets, mods);

        Element element = expr.evaluateFirst(doc);
        assertNotNull(element);
        assertEquals("true", element.getAttributeValue("accessRestrict"));
    }

    @Test
    public void testExportAccessRestrictCorporate() throws Exception {
        Corporate c = new Corporate(prefs.getMetadataTypeByName("Corporation"));
        c.setMainName("main");
        c.setAccessRestrict(true);

        DocStruct logical = mm.getDigitalDocument().getLogicalDocStruct();
        logical.addCorporate(c);

        File exportFile = folder.newFile();

        MetsModsImportExport mmio = new MetsModsImportExport(prefs);
        mmio.setDigitalDocument(mm.getDigitalDocument());
        mmio.write(exportFile.toString());

        // open exported file

        Namespace mets = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
        Namespace mods = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(exportFile);

        XPathFactory xpathFactory = XPathFactory.instance();
        XPathExpression<Element> expr = xpathFactory.compile("//mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:name[@type='corporate']",
                Filters.element(), null, mets, mods);

        Element element = expr.evaluateFirst(doc);
        assertNotNull(element);
        assertEquals("true", element.getAttributeValue("accessRestrict"));
    }
}
