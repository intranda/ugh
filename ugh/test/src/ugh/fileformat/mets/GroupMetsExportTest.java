package ugh.fileformat.mets;

import static org.junit.Assert.assertEquals;
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

import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Prefs;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;

public class GroupMetsExportTest {

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
    public void testGroupInGroup() throws Exception {

        // create group with metadata, person, corporate
        MetadataGroup publisherGroup = new MetadataGroup(prefs.getMetadataGroupTypeByName("PublisherGroup"));

        Metadata metadata = new Metadata(prefs.getMetadataTypeByName("PlaceOfPublication"));
        metadata.setValue("Place");
        metadata.setAutorityFile("111", "url", "http://example.com/111");
        publisherGroup.addMetadata(metadata);
        Metadata metadata2 = new Metadata(prefs.getMetadataTypeByName("PublicationYear"));
        metadata2.setValue("666");
        publisherGroup.addMetadata(metadata2);
        MetadataGroup other = new MetadataGroup(prefs.getMetadataGroupTypeByName("LocationGroup"));
        Metadata city = new Metadata(prefs.getMetadataTypeByName("City"));
        city.setValue("city");
        other.addMetadata(city);
        publisherGroup.addMetadataGroup(other);

        fileformat.getDigitalDocument().getLogicalDocStruct().addMetadataGroup(publisherGroup);

        Path metadataFile = Paths.get(exportFolder.toString(), "meta.xml");
        fileformat.write(metadataFile.toString());

        assertTrue(Files.exists(metadataFile));

        // read internal format
        Fileformat fileformat2 = new MetsMods(prefs);
        fileformat2.read(metadataFile.toString());
        DocStruct mono = fileformat2.getDigitalDocument().getLogicalDocStruct();

        // group is still here
        MetadataGroup mdg = mono.getAllMetadataGroups().get(0);

        MetadataGroup mdg2 = mdg.getAllMetadataGroups().get(0);

        assertEquals("LocationGroup", mdg2.getType().getName());
        assertEquals("City", mdg2.getMetadataList().get(0).getType().getName());
        assertEquals("city", mdg2.getMetadataList().get(0).getValue());

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

        Element placeTerm = originInfo.getChildren("place", modsNamespace).get(0).getChild("placeTerm", modsNamespace);
        Element dateIssued = originInfo.getChild("dateIssued", modsNamespace);

        assertEquals("Place", placeTerm.getText());
        assertEquals("http://example.com/111", placeTerm.getAttributeValue("valueURI"));
        assertEquals("666", dateIssued.getValue());

        Element subGroupPlaceTerm = originInfo.getChildren("place", modsNamespace).get(1).getChild("placeTerm", modsNamespace);
        assertEquals("city", subGroupPlaceTerm.getValue());

    }

}
