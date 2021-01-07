package ugh.fileformat.mets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ugh.dl.Corporate;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;

public class CorporateMetsExportTest {

    private Prefs prefs;

    private Fileformat fileformat;

    private static final Namespace metsNamespace = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
    private static final Namespace modsNamespace = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File exportFolder;

    @Before
    public void setUp() throws Exception {
        prefs = new Prefs();
        prefs.loadPrefs("test/resources/ruleset.xml");

        fileformat = new MetsMods(prefs);
        fileformat.read("test/resources/meta.xml");

        exportFolder = folder.newFolder("fixture");
        exportFolder.mkdirs();
    }

    @Test
    public void testAddCorporateAndReadAndWriteInternalFile() throws UGHException {
        // add corporate
        Corporate corp = new Corporate(prefs.getMetadataTypeByName("Corporation"));
        corp.setMainName("main");
        corp.addSubName(new NamePart("subname","sub1"));
        corp.addSubName(new NamePart("subname","sub2"));
        corp.setPartName("part");
        corp.setAutorityFile("1234", "url", "http://example.com/1234");

        fileformat.getDigitalDocument().getLogicalDocStruct().addCorporate(corp);

        // save mets file with corporate
        Path metadataFile = Paths.get(exportFolder.toString(), "meta.xml");
        fileformat.write(metadataFile.toString());

        assertTrue(Files.exists(metadataFile));
        // read mets file again

        Fileformat fileformat2 = new MetsMods(prefs);
        fileformat2.read(metadataFile.toString());

        // get corporate from new mets
        DocStruct mono = fileformat2.getDigitalDocument().getLogicalDocStruct();
        Corporate fixture = mono.getAllCorporates().get(0);
        assertEquals("main", fixture.getMainName());
        assertEquals("sub1", fixture.getSubNames().get(0).getValue());
        assertEquals("sub2", fixture.getSubNames().get(1).getValue());
        assertEquals("part", fixture.getPartName());

        assertEquals("1234", fixture.getAuthorityID());
        assertEquals("url", fixture.getAuthorityURI());
        assertEquals("http://example.com/1234", fixture.getAuthorityValue());
    }

    @Test
    public void testAddCorporateAndSaveExternalFile() throws Exception {

        // add corporate
        Corporate corp = new Corporate(prefs.getMetadataTypeByName("Corporation"));
        corp.setMainName("main name");
        corp.addSubName(new NamePart("subname","first sub name"));
        corp.addSubName(new NamePart("subname","additional sub name"));
        corp.setPartName("part name");
        corp.setAutorityFile("1234", "url", "http://example.com/1234");

        fileformat.getDigitalDocument().getLogicalDocStruct().addCorporate(corp);

        MetsModsImportExport metsModsExport = new MetsModsImportExport(prefs);

        metsModsExport.setDigitalDocument(fileformat.getDigitalDocument());

        Path metadataFile = Paths.get(exportFolder.toString(), "meta.xml");
        metsModsExport.write(metadataFile.toString());

        assertTrue(Files.exists(metadataFile));

        // open exported filewith jdom, check corporate
        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build(metadataFile.toFile());

        Element mets = doc.getRootElement();
        Element dmdSec = mets.getChild("dmdSec", metsNamespace);
        Element mods = dmdSec.getChild("mdWrap", metsNamespace).getChild("xmlData", metsNamespace).getChild("mods", modsNamespace);

        List<Element> nameList = mods.getChildren("name", modsNamespace);
        Element corporate = null;
        for (Element name : nameList) {
            if (name.getAttributeValue("type").equals("corporate")) {
                corporate = name;
            }
        }
        assertNotNull(corporate);
        assertEquals("http://example.com/1234", corporate.getAttributeValue("valueURI"));
        // roleTerm
        assertEquals("aut", corporate.getChild("role", modsNamespace).getChildText("roleTerm", modsNamespace));
        List<Element> nameParts = corporate.getChildren("namePart", modsNamespace);

        assertEquals("main name", nameParts.get(0).getText());
        assertEquals("first sub name", nameParts.get(1).getText());
        assertEquals("additional sub name", nameParts.get(2).getText());
        assertEquals("part name", nameParts.get(3).getText());

    }

    @Test
    public void testCorporateInGroup() throws Exception {

        // create group with metadata, person, corporate
        MetadataGroup publisherGroup = new MetadataGroup(prefs.getMetadataGroupTypeByName("PublisherGroup"));

        for (Metadata md :publisherGroup.getMetadataList()) {
            if (md.getType().getName().equals("PlaceOfPublication")) {
                md.setValue("Place");
                md.setAutorityFile("111", "url", "http://example.com/111");
            } else {
                md.setValue("666");
            }
        }
        for (Person p : publisherGroup.getPersonList()) {
            p.setFirstname("Firstname");
            p.setLastname("Lastname");
            p.setAutorityFile("ABC", "url", "http://example.com/ABC");
        }
        for (Corporate c : publisherGroup.getCorporateList()) {
            c.setMainName("Main name");
            c.addSubName(new NamePart("subname","Sub name"));
            c.setPartName("Part name");
            c.setAutorityFile("1234", "url", "http://example.com/1234");
        }
        fileformat.getDigitalDocument().getLogicalDocStruct().addMetadataGroup(publisherGroup);

        // save it as internal format

        Path metadataFile = Paths.get(exportFolder.toString(), "meta.xml");
        fileformat.write(metadataFile.toString());

        assertTrue(Files.exists(metadataFile));

        // read internal format
        Fileformat fileformat2 = new MetsMods(prefs);
        fileformat2.read(metadataFile.toString());
        DocStruct mono = fileformat2.getDigitalDocument().getLogicalDocStruct();

        // group is still here
        MetadataGroup mdg = mono.getAllMetadataGroups().get(0);

        for (Metadata md :mdg.getMetadataList()) {
            if (md.getType().getName().equals("PlaceOfPublication")) {
                assertEquals("Place", md.getValue());
            } else {
                assertEquals("666", md.getValue());
            }
        }
        for (Person p : mdg.getPersonList()) {
            assertEquals("Firstname", p.getFirstname());
            assertEquals("Lastname", p.getLastname());
        }
        for (Corporate c : mdg.getCorporateList()) {
            assertEquals("Main name", c.getMainName());
            assertEquals("Sub name", c.getSubNames().get(0).getValue());
            assertEquals("Part name", c.getPartName());
            assertEquals("http://example.com/1234", c.getAuthorityValue());
        }

        // export as external format
        MetsModsImportExport metsModsExport = new MetsModsImportExport(prefs);
        metsModsExport.setDigitalDocument(fileformat2.getDigitalDocument());

        metadataFile = Paths.get(exportFolder.toString(), "meta_export.xml");
        metsModsExport.write(metadataFile.toString());

        assertTrue(Files.exists(metadataFile));

        // read exported file
        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build(metadataFile.toFile());

        Element mets = doc.getRootElement();
        Element dmdSec = mets.getChild("dmdSec", metsNamespace);
        Element mods = dmdSec.getChild("mdWrap", metsNamespace).getChild("xmlData", metsNamespace).getChild("mods", modsNamespace);
        List<Element> originInfoList = mods.getChildren("originInfo", modsNamespace);

        // group elements are still here
        Element originInfo = originInfoList.get(1);
        List<Element> publisher = originInfo.getChildren("publisher", modsNamespace);

        Element placeTerm = originInfo.getChild("place", modsNamespace).getChild("placeTerm", modsNamespace);
        Element dateIssued = originInfo.getChild("dateIssued", modsNamespace);
        Element publisherPerson = publisher.get(0).getChild("name", modsNamespace);
        Element publisherCorporate = publisher.get(1).getChild("name", modsNamespace);

        assertEquals("Place", placeTerm.getText());
        assertEquals("http://example.com/111",placeTerm.getAttributeValue("valueURI"));
        assertEquals("666", dateIssued.getValue());

        assertEquals("personal", publisherPerson.getAttributeValue("type"));
        assertEquals("dte", publisherPerson.getChild("role", modsNamespace).getChildText("roleTerm", modsNamespace));
        List<Element> nameParts = publisherPerson.getChildren("namePart", modsNamespace);
        assertEquals("Firstname", nameParts.get(0).getText());
        assertEquals("given", nameParts.get(0).getAttributeValue("type"));
        assertEquals("Lastname", nameParts.get(1).getText());
        assertEquals("family", nameParts.get(1).getAttributeValue("type"));
        assertEquals("Lastname, Firstname", publisherPerson.getChildText("displayForm", modsNamespace));

        assertEquals("corporate", publisherCorporate.getAttributeValue("type"));
        assertEquals("aut", publisherCorporate.getChild("role", modsNamespace).getChildText("roleTerm", modsNamespace));
        nameParts = publisherCorporate.getChildren("namePart", modsNamespace);
        assertEquals("Main name", nameParts.get(0).getText());
        assertEquals("Sub name", nameParts.get(1).getText());
        assertEquals("Part name", nameParts.get(2).getText());

    }

}
