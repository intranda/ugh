package ugh.fileformat.mets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

public class CorporateMetsExportTest {

    private Prefs prefs;

    private Fileformat fileformat;

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
    public void testAddCorporateAndSaveExternalFile() {

    }

}
