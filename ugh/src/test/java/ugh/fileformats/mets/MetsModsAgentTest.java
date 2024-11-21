package ugh.fileformats.mets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

import ugh.UghVersion;
import ugh.dl.ExportFileformat;
import ugh.dl.Prefs;

public class MetsModsAgentTest {

    private static final Namespace metsNamespace = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
    private static final Namespace extNamespace = Namespace.getNamespace("ext", "https://intranda.com/metsExtension");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File exportFolder;

    private Prefs prefs;
    private MetsMods mm;

    @Before
    public void setUp() throws Exception {
        UghVersion.buildDate = "2000-01-01";
        UghVersion.buildVersion = "1234";
        UghVersion.PROGRAMNAME = "fixture";
        prefs = new Prefs();

        prefs.loadPrefs("src/test/resources/ruleset.xml");

        mm = new MetsMods(prefs);
        mm.read("src/test/resources/meta.xml");

        exportFolder = folder.newFolder("fixture");
        exportFolder.mkdirs();

    }

    @Test
    public void testExportWithDefaultAgent() throws Exception {

        // export without any configuration
        ExportFileformat metsModsExport = new MetsModsImportExport(prefs);
        metsModsExport.setDigitalDocument(mm.getDigitalDocument());

        Path metadataFile = Paths.get(exportFolder.toString(), "meta.xml");
        metsModsExport.write(metadataFile.toString());

        assertTrue(Files.exists(metadataFile));

        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build(metadataFile.toFile());

        Element mets = doc.getRootElement();

        Element metsHdr = mets.getChild("metsHdr", metsNamespace);
        Element agent = metsHdr.getChild("agent", metsNamespace);

        Element name = agent.getChild("name", metsNamespace);
        Element note = agent.getChild("note", metsNamespace);

        assertEquals("fixture - 1234 - 2000-01-01", name.getText());
        assertNull(note);

    }

    @Test
    public void testExportWithConfiguredAgent() throws Exception {

        // export without any configuration
        ExportFileformat metsModsExport = new MetsModsImportExport(prefs);
        metsModsExport.setDigitalDocument(mm.getDigitalDocument());

        metsModsExport.setSoftwareName("Software name");
        metsModsExport.setSoftwareVersion("Version number");
        metsModsExport.setInstanceName("Instance name");
        metsModsExport.setClientName("Client name");

        Path metadataFile = Paths.get(exportFolder.toString(), "meta.xml");
        metsModsExport.write(metadataFile.toString());

        assertTrue(Files.exists(metadataFile));

        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build(metadataFile.toFile());

        Element mets = doc.getRootElement();

        Element metsHdr = mets.getChild("metsHdr", metsNamespace);
        Element agent = metsHdr.getChild("agent", metsNamespace);

        Element name = agent.getChild("name", metsNamespace);
        List<Element> note = agent.getChildren("note", metsNamespace);

        assertEquals("Software name", name.getText());
        assertNotNull(note);
        assertEquals(5, note.size());

        // first note: git hash
        assertEquals("hash", note.get(0).getAttributeValue("type", extNamespace));
        // second note: build date
        assertEquals("builddate", note.get(1).getAttributeValue("type", extNamespace));
        // third note: version
        assertEquals("version", note.get(2).getAttributeValue("type", extNamespace));
        assertEquals("Version number", note.get(2).getText());

        // fourth note: instance name
        assertEquals("instance", note.get(3).getAttributeValue("type", extNamespace));
        assertEquals("Instance name", note.get(3).getText());

        // Last note: client name
        assertEquals("client", note.get(4).getAttributeValue("type", extNamespace));
        assertEquals("Client name", note.get(4).getText());

    }
}
