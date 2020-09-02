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
        corp.addSubName("sub1");
        corp.addSubName("sub2");
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
        assertEquals("sub1", fixture.getSubNames().get(0));
        assertEquals("sub2", fixture.getSubNames().get(1));
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
        corp.addSubName("first sub name");
        corp.addSubName("additional sub name");
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

}
