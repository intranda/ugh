package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ugh.dl.DigitalDocument.ListPairCheck;
import ugh.dl.DigitalDocument.PhysicalElement;
import ugh.exceptions.ContentFileNotLinkedException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.XStream;

public class DigitalDocumentTest {
    private DigitalDocument dd;

    private static Prefs prefs;
    private static Fileformat fileformat;

    private static Document doc;
    private static NodeList upperChildList;
    private static Node upperChild;

    @BeforeClass
    public static void setUpForAll() throws Exception {
        prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");

        fileformat = new MetsMods(prefs);
        fileformat.read("src/test/resources/meta.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Do not validate xml file.
        factory.setValidating(false);
        // Namespace does not matter.
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        doc = builder.parse(new File("src/test/resources/rulesetForPrefsFunctionalityTest.xml"));
        upperChildList = doc.getElementsByTagName("Preferences");
        upperChild = upperChildList.item(0);

    }

    @Before
    public void setUpForEach() {
        dd = new DigitalDocument();
    }

    /* Tests for Getters and Setters */
    @Test
    public void testInitialSettings() {
        assertNull(dd.getLogicalDocStruct());
        assertNull(dd.getPhysicalDocStruct());
        assertNull(dd.getFileSet());
        assertNull(dd.getAmdSec());
    }

    @Test(expected = NullPointerException.class)
    public void testSetLogicalDocStructGivenNull() throws TypeNotAllowedForParentException {
        dd.setLogicalDocStruct(null);
    }

    @Test
    public void testSetLogicalDocStructGivenDocStructObjects() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds1 = new DocStruct(dsType);
        DocStruct ds2 = new DocStruct(dsType);
        assertFalse(ds1.isLogical());
        assertFalse(ds2.isLogical());
        dd.setLogicalDocStruct(ds1);
        assertTrue(ds1.isLogical());
        assertSame(ds1, dd.getLogicalDocStruct());
        assertNotSame(ds2, dd.getLogicalDocStruct());
        dd.setLogicalDocStruct(ds2);
        assertFalse(ds1.isLogical());
        assertTrue(ds2.isLogical());
        assertNotSame(ds1, dd.getLogicalDocStruct());
        assertSame(ds2, dd.getLogicalDocStruct());
    }

    @Test(expected = NullPointerException.class)
    public void testSetPhysicalDocStructGivenNull() throws TypeNotAllowedForParentException {
        dd.setPhysicalDocStruct(null);
    }

    @Test
    public void testSetPhysicalDocStructGivenDocStructObject() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds1 = new DocStruct(dsType);
        DocStruct ds2 = new DocStruct(dsType);
        assertFalse(ds1.isPhysical());
        assertFalse(ds2.isPhysical());
        dd.setPhysicalDocStruct(ds1);
        assertTrue(ds1.isPhysical());
        assertFalse(ds2.isPhysical());
        assertSame(ds1, dd.getPhysicalDocStruct());
        assertNotSame(ds2, dd.getPhysicalDocStruct());
        dd.setPhysicalDocStruct(ds2);
        assertFalse(ds1.isPhysical());
        assertTrue(ds2.isPhysical());
        assertNotSame(ds1, dd.getPhysicalDocStruct());
        assertSame(ds2, dd.getPhysicalDocStruct());
    }

    @Test
    public void testSetFileSet() {
        // before initializing the field allImages
        dd.setFileSet(null);
        assertNull(dd.getFileSet());

        // initialize the field allImages
        FileSet fileSet1 = new FileSet();
        dd.setFileSet(fileSet1);
        assertNotNull(dd.getFileSet());
        assertSame(fileSet1, dd.getFileSet());

        // test the method against another FileSet object
        FileSet fileSet2 = new FileSet();
        dd.setFileSet(fileSet2);
        assertNotSame(fileSet1, dd.getFileSet());
        assertSame(fileSet2, dd.getFileSet());

        // test the method against null
        dd.setFileSet(null);
        assertNull(dd.getFileSet());
    }

    @Test
    public void testSetAmdSecGivenEmptyString() {
        assertNull(dd.getAmdSec());
        assertNull(dd.getAmdSec(""));
        dd.setAmdSec("");
        assertNotNull(dd.getAmdSec());
        assertNotNull(dd.getAmdSec(""));
    }

    @Test
    public void testSetAmdSecGivenAmdSecObject() {
        AmdSec sec1 = new AmdSec(new ArrayList<Md>());
        dd.setAmdSec(sec1);
        assertNotNull(dd.getAmdSec());
        assertNull(dd.getAmdSec().getId());
        sec1.setId("id");
        assertEquals("id", dd.getAmdSec().getId());
        assertSame(sec1, dd.getAmdSec("id"));

        // use another AmdSec object to test the method again
        AmdSec sec2 = new AmdSec(new ArrayList<Md>());
        sec2.setId("id2");
        dd.setAmdSec(sec2);
        assertEquals("id2", dd.getAmdSec().getId());
        assertNull(dd.getAmdSec("id"));
        assertSame(sec2, dd.getAmdSec());
    }

    @Test
    public void testSetAmdSecGivenUnemptyString() {
        String name = "id";
        dd.setAmdSec(name);
        assertNotNull(dd.getAmdSec());
        assertNotNull(dd.getAmdSec(name));
        assertSame(dd.getAmdSec(), dd.getAmdSec(name));
        assertEquals(name, dd.getAmdSec().getId());
    }

    /* Tests for the method createDocStruct(DocStructType) */
    @Test
    public void testCreateDocStructGivenNull() throws TypeNotAllowedForParentException {
        DocStruct ds = dd.createDocStruct(null);
        assertNull(ds.getType());
    }

    @Test
    public void testCreateDocStructGivenValidDocStructType() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds = dd.createDocStruct(dsType);
        // since there is no normal way to access to the DocStruct's field DigitalDocument, we only test that it is not null here
        assertNotNull(ds);
    }

    /* Tests for the enum PhysicalElement */
    @Test
    public void testPhysicalElementCheckPhysicalType() {
        for (String type : new String[] { "page", "audio", "video", "object" }) {
            assertTrue(PhysicalElement.checkPhysicalType(type));
        }
        for (String other : new String[] { "", " ", "_", "not a name", "BAZINGA!" }) {
            assertFalse(PhysicalElement.checkPhysicalType(other));
        }
        assertFalse(PhysicalElement.checkPhysicalType(null));
    }

    @Test
    public void testPhysicalElementGetTypeFromValue() {
        assertEquals(PhysicalElement.AUDIO, PhysicalElement.getTypeFromValue("audio"));
        assertEquals(PhysicalElement.VIDEO, PhysicalElement.getTypeFromValue("video"));
        assertEquals(PhysicalElement.OBJECT, PhysicalElement.getTypeFromValue("object"));
        for (String s : new String[] { "page", "", " ", "_", "not a name", "BAZINGA!" }) {
            assertEquals(PhysicalElement.PAGE, PhysicalElement.getTypeFromValue(s));
        }
        assertEquals(PhysicalElement.PAGE, PhysicalElement.getTypeFromValue(null));
    }

    /* Tests for the method getAllDocStructsByType(String) */
    @Test
    public void testGetAllDocStructsByTypeGivenNull() throws TypeNotAllowedForParentException {
        // before initializing the fields topPhysicalStruct and topLogicalStruct
        assertNull(dd.getPhysicalDocStruct());
        assertNull(dd.getLogicalDocStruct());
        assertNull(dd.getAllDocStructsByType(null));

        // initialize both fields
        DocStructType phyType = new DocStructType();
        DocStructType logType = new DocStructType();
        // names of DocStructType objects are important for the private method getAllDocStructsByTypePrivate(DocStruct, String)
        phyType.setName("phy");
        logType.setName("log");
        DocStruct phyDS = new DocStruct(phyType);
        DocStruct logDS = new DocStruct(logType);
        dd.setPhysicalDocStruct(phyDS);
        dd.setLogicalDocStruct(logDS);

        // test this method again
        assertNotNull(dd.getPhysicalDocStruct());
        assertNotNull(dd.getLogicalDocStruct());
        assertNull(dd.getAllDocStructsByType(null));
    }

    @Test
    public void testGetAllDocStructsByTypeGivenDifferentNames() throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
        // initialize both fields
        DocStructType phyType = new DocStructType();
        DocStructType logType0 = new DocStructType();
        DocStructType logType1 = new DocStructType();
        DocStructType logType2 = new DocStructType();
        DocStructType someType = new DocStructType();
        // names of DocStructType objects are important for the private method getAllDocStructsByTypePrivate(DocStruct, String)
        phyType.setName("phy");
        logType0.setName("logParent");
        logType1.setName("log");
        logType2.setName("log");
        someType.setName("sth");
        DocStruct phyDS0 = new DocStruct(phyType);
        DocStruct phyDS1 = new DocStruct(phyType);
        DocStruct phyDS2 = new DocStruct(phyType);
        DocStruct logDS0 = new DocStruct(logType0);
        DocStruct logDS1 = new DocStruct(logType1);
        DocStruct logDS2 = new DocStruct(logType2);
        DocStruct logDS3 = new DocStruct(logType1);
        DocStruct someDS = new DocStruct(someType);

        // to make DocStruct objects addable as child, one has to make the according DocStructType objects addable
        phyType.addDocStructTypeAsChild(phyType);
        phyType.addDocStructTypeAsChild(someType);
        logType0.addDocStructTypeAsChild(logType1);
        logType0.addDocStructTypeAsChild(logType2);
        logType0.addDocStructTypeAsChild(someType);
        // add two children of type named "phy" to phyDS0
        for (DocStruct ds : new DocStruct[] { phyDS1, phyDS2 }) {
            phyDS0.addChild(ds);
        }
        // add three children of type named "log" to logDS0
        for (DocStruct ds : new DocStruct[] { logDS1, logDS2, logDS3 }) {
            logDS0.addChild(ds);
        }
        // add someDS to both phyDS0 and logDS0
        phyDS0.addChild(someDS);
        logDS0.addChild(someDS);
        // set both fields topPhysicalStruct and topLogicalStruct
        dd.setPhysicalDocStruct(phyDS0);
        dd.setLogicalDocStruct(logDS0);

        assertEquals(2, dd.getAllDocStructsByType("phy").size());
        assertEquals(3, dd.getAllDocStructsByType("log").size());
        assertEquals(2, dd.getAllDocStructsByType("sth").size());
        for (String name : new String[] { "", " ", "_", "not a name", "BAZINGA!" }) {
            assertNull(dd.getAllDocStructsByType(name));
        }
    }

    @Test
    public void testGetAllDocStructsByTypeGivenFileformat() throws PreferencesException {
        DigitalDocument doc = fileformat.getDigitalDocument();
        assertEquals(2, doc.getAllDocStructsByType("area").size());
        for (String name : new String[] { "", " ", "_", "div", "not a name", "BAZINGA!" }) {
            assertNull(doc.getAllDocStructsByType(name));
        }
    }

    /* Tests for the method readXStreamXml(String, Prefs) */
    @Test
    public void testReadXStreamXml() throws FileNotFoundException, UnsupportedEncodingException {
        dd.readXStreamXml("src/test/resources/digitalDocumentTest.xml", prefs);
        assertNotNull(dd);
        assertEquals("Monograph", dd.getLogicalDocStruct().getType().getName());
    }

    @Test(expected = FileNotFoundException.class)
    public void testReadXStreamXmlGivenUnexistingFile() throws FileNotFoundException, UnsupportedEncodingException {
        dd.readXStreamXml("src/test/resources/unexisting.xml", prefs);
    }

    public void generateXStreamXml() throws Exception {
        // prepare FileSet containing a list of VirtualFileGroups
        MetsMods mm = new MetsMods(prefs);
        mm.read("src/test/resources/meta.xml");

        XStream xstream = new XStream(prefs);
        xstream.setDigitalDocument(mm.getDigitalDocument());
        xstream.write("src/test/resources/digitalDocumentTest.xml");
    }

    /* Tests for the methods:
     * 1. sortMetadataRecursivelyAbcdefg()
     * 2. sortMetadataRecursively(Prefs)
     */
    @Test
    public void testSortMetadataRecursivelyAbcdefgGivenUninitializedLogicalOrPhysicalStruct() {
        assertNull(dd.getPhysicalDocStruct());
        assertNull(dd.getLogicalDocStruct());
        // just run this method to check if it throws any exceptions
        dd.sortMetadataRecursivelyAbcdefg();
    }

    @Test
    public void testSortMetadataRecursivelyGivenNull() throws TypeNotAllowedForParentException {
        // without initializing the fields topLogicalStruct and topPhysicalStruct
        assertNull(dd.getPhysicalDocStruct());
        assertNull(dd.getLogicalDocStruct());
        // run this method to check if it throws any exceptions
        dd.sortMetadataRecursively(null);

        // initialize both fields and check the method again
        DocStructType phyType = new DocStructType();
        DocStructType logType = new DocStructType();
        // names of DocStructType objects are important for the private method getAllDocStructsByTypePrivate(DocStruct, String)
        phyType.setName("phy");
        logType.setName("log");
        DocStruct phyDS = new DocStruct(phyType);
        DocStruct logDS = new DocStruct(logType);
        dd.setPhysicalDocStruct(phyDS);
        dd.setLogicalDocStruct(logDS);

        // as long as there is no exception thrown, we are good
        dd.sortMetadataRecursively(null);
    }

    /* Tests for the method addContentFileFromPhysicalPage(DocStruct) */
    @Test(expected = NullPointerException.class)
    public void testAddContentFileFromPhysicalPageGivenNull() {
        dd.addContentFileFromPhysicalPage(null);
    }

    @Test
    public void testAddContentFileFromPhysicalPageGivenDocStructWithContentFiles()
            throws TypeNotAllowedForParentException, MetadataTypeNotAllowedException {
        // prepare a MetadataType named physPageNumber that is normally addable
        MetadataType mdType = new MetadataType();
        mdType.setName("physPageNumber");
        Metadata md = new Metadata(mdType);
        // the field value is necessary for the private method createContentFile(DocStruct, String)
        md.setValue("7");

        // prepare a DocStructType which allows adding of the previously created mdType
        DocStructType dsType = new DocStructType();
        dsType.addMetadataType(mdType, "*");

        // create a DocStruct object ds related to our DigitalDocument object dd
        DocStruct ds = dd.createDocStruct(dsType);
        assertNull(ds.getAllContentFiles());

        // add some ContentFile object to make ds not empty any more
        ContentFile file = new ContentFile();
        ds.addContentFile(file);
        assertNotNull(ds.getAllContentFiles());
        assertEquals(1, ds.getAllContentFiles().size());
        assertNull(ds.getAllContentFiles().get(0).getMimetype());
        assertNull(ds.getAllContentFiles().get(0).getLocation());

        // check this method
        ds.addMetadata(md);

        for (String type : new String[] { "page", "audio", "video", "object" }) {
            dsType.setName(type);
            dd.addContentFileFromPhysicalPage(ds);
            assertEquals(1, ds.getAllContentFiles().size());
            // if this method addContentFileFromPhysicalPage would really add content files
            // then the private createContentFile would set both fields MimeType and Location
            assertNull(ds.getAllContentFiles().get(0).getMimetype());
            assertNull(ds.getAllContentFiles().get(0).getLocation());
        }
    }

    @Test
    public void testAddContentFileFromPhysicalPageGivenDocStructOtherThanPage()
            throws MetadataTypeNotAllowedException, TypeNotAllowedForParentException {
        // prepare a MetadataType named physPageNumber that is normally addable
        MetadataType mdType = new MetadataType();
        mdType.setName("physPageNumber");
        Metadata md = new Metadata(mdType);
        // the field value is necessary for the private method createContentFile(DocStruct, String)
        md.setValue("7");

        // prepare a DocStructType which allows adding of the previously created mdType
        DocStructType dsType = new DocStructType();
        dsType.addMetadataType(mdType, "1o");

        // create a DocStruct object ds related to our DigitalDocument object dd
        DocStruct ds = dd.createDocStruct(dsType);
        assertNull(ds.getAllContentFiles());

        // check this method
        ds.addMetadata(md);

        for (String type : new String[] { "", " ", "_", "BAZINGA!", "bottle" }) {
            dsType.setName(type);
            dd.addContentFileFromPhysicalPage(ds);
            assertNull(ds.getAllContentFiles());
        }
    }

    @Test
    public void testAddContentFileFromPhysicalPageGivenDocStructPageWithoutContentFiles()
            throws MetadataTypeNotAllowedException, TypeNotAllowedForParentException, ContentFileNotLinkedException {
        // prepare a MetadataType named physPageNumber that is normally addable
        MetadataType mdType = new MetadataType();
        mdType.setName("physPageNumber");
        Metadata md = new Metadata(mdType);
        // the field value is necessary for the private method createContentFile(DocStruct, String)
        md.setValue("7");

        // prepare a DocStructType which allow adding of the previously created mdType
        DocStructType dsType = new DocStructType();
        dsType.addMetadataType(mdType, "+");

        // create a DocStruct object ds related to our DigitalDocument object dd
        DocStruct ds = dd.createDocStruct(dsType);
        assertNull(ds.getAllContentFiles());

        // check this method
        ds.addMetadata(md);

        for (String type : new String[] { "page", "audio", "video", "object" }) {
            dsType.setName(type);
            // check if a ContentFile could be added
            dd.addContentFileFromPhysicalPage(ds);
            assertNotNull(ds.getAllContentFiles());
            // remove the ContentFile to assure the emptiness of our DocStruct object ds
            ds.removeContentFile(ds.getAllContentFiles().get(0));
            assertNull(ds.getAllContentFiles());
        }
    }

    /* Tests for the method addAllContentFiles() */
    @Test
    public void testAddAllContentFiles() throws MetadataTypeNotAllowedException, TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
        // prepare FileSet containing a list of VirtualFileGroups
        VirtualFileGroup vfg1 = new VirtualFileGroup();
        VirtualFileGroup vfg2 = new VirtualFileGroup();
        List<VirtualFileGroup> vfgList = new ArrayList<>();
        vfgList.add(vfg1);
        vfgList.add(vfg2);
        FileSet fileSet = new FileSet();
        fileSet.setVirtualFileGroups(vfgList);

        // add a Metadata object and a ContentFile object to show the changes of FileSet during the application of this method
        fileSet.addMetadata(new Metadata(new MetadataType()));
        fileSet.addFile(new ContentFile());

        dd.setFileSet(fileSet);
        assertNotNull(dd.getFileSet());
        assertNotNull(dd.getFileSet().getVirtualFileGroups());
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertNotNull(dd.getFileSet().getAllMetadata());
        assertEquals(1, dd.getFileSet().getAllMetadata().size());
        assertNotNull(dd.getFileSet().getAllFiles());
        assertEquals(1, dd.getFileSet().getAllFiles().size());

        // tests
        dd.addAllContentFiles();
        // the field virtualFileGroups should not be affected
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertSame(vfgList, dd.getFileSet().getVirtualFileGroups());
        // the fields allMetadata and allImages should be reset
        assertEquals(0, dd.getFileSet().getAllMetadata().size());
        assertEquals(0, dd.getFileSet().getAllFiles().size());

        // prepare a DocStruct for further tests
        DocStruct tp = preparePhysicalDocStruct();
        dd.setPhysicalDocStruct(tp);

        // before applying this method, there should be no content files and no references available
        DocStruct docStruct = dd.getPhysicalDocStruct().getAllChildren().get(0);
        assertEquals(0, docStruct.getAllContentFileReferences().size());
        assertNull(docStruct.getAllContentFiles());

        // apply this method and check its content files and references again
        dd.addAllContentFiles();

        List<ContentFileReference> contentFileRefs = docStruct.getAllContentFileReferences();
        List<ContentFile> contentFiles = docStruct.getAllContentFiles();

        assertNotNull(contentFiles);

        int numberOfRefs = contentFileRefs.size();
        int numberOfFiles = contentFiles.size();

        assertNotEquals(0, numberOfRefs);
        assertNotEquals(0, numberOfFiles);

        for (ContentFile contentFile : contentFiles) {
            assertTrue(contentFile.getLocation().contains(".tif"));
            assertEquals("image/tiff", contentFile.getMimetype());
        }

        // applying this method again should not result in duplicates
        dd.addAllContentFiles();
        assertEquals(numberOfRefs, dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFileReferences().size());
        assertEquals(numberOfFiles, dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFiles().size());
    }

    private DocStruct preparePhysicalDocStruct()
            throws TypeNotAllowedAsChildException, MetadataTypeNotAllowedException, TypeNotAllowedForParentException {
        MetadataType pifType = new MetadataType();
        pifType.setName("pathimagefiles");
        MetadataType repType = new MetadataType();
        repType.setName("_representative");
        MetadataType pageType = new MetadataType();
        pageType.setName("physPageNumber");

        DocStructType pagesType = new DocStructType();
        pagesType.setName("page");
        pagesType.addMetadataType(pageType, "*");
        DocStructType tpType = new DocStructType();
        tpType.setName("top");
        tpType.addDocStructTypeAsChild(pagesType);
        tpType.addMetadataType(pifType, "+");
        tpType.addMetadataType(repType, "+");

        Metadata pif = new Metadata(pifType);
        pif.setValue("/tmp/");
        Metadata rep = new Metadata(repType);
        rep.setValue("");
        Metadata page1 = new Metadata(pageType);
        page1.setValue("1");
        Metadata page2 = new Metadata(pageType);
        page2.setValue("2");
        Metadata page3 = new Metadata(pageType);
        page3.setValue("3");

        DocStruct tp = new DocStruct(tpType);
        DocStruct pages = new DocStruct(pagesType);

        for (Metadata md : new Metadata[] { pif, rep }) {
            tp.addMetadata(md);
        }
        for (Metadata page : new Metadata[] { page1, page2, page3 }) {
            pages.addMetadata(page);
        }
        tp.addChild(pages);

        // ============================ ATTENTION ============================= //
        // Only page3 will be added after applying the method DigitalDocument::addAllContentFiles
        // the reason is that for that method, the order of pages matters:
        // in the for loop over all Metadata objects of the DocStruct pages, the following will happen in order:
        // 1. page1 added
        // 2. page1 deleted, page2 added
        // 3. page2 deleted, page3 added
        // ============================ ATTENTION ============================= //

        // DigitalDocument is needed for calling the method DocStruct::addContentFile
        DigitalDocument ddPages = new DigitalDocument();
        pages.setDigitalDocument(ddPages);

        return tp;
    }

    /* Tests for the method overrideContentFiles(List<String>) */
    @Test(expected = NullPointerException.class)
    public void testOverrideContentFilesGivenNull() {
        dd.overrideContentFiles(null);
    }

    @Test
    public void testOverrideContentFilesGivenEmptyDigitalDocumentAndEmptyStringList() throws MetadataTypeNotAllowedException {
        // prepare FileSet containing a list of VirtualFileGroups
        VirtualFileGroup vfg1 = new VirtualFileGroup();
        VirtualFileGroup vfg2 = new VirtualFileGroup();
        List<VirtualFileGroup> vfgList = new ArrayList<>();
        vfgList.add(vfg1);
        vfgList.add(vfg2);
        FileSet fileSet = new FileSet();
        fileSet.setVirtualFileGroups(vfgList);

        // add a Metadata object and a ContentFile object to show the changes of FileSet during the application of this method
        fileSet.addMetadata(new Metadata(new MetadataType()));
        fileSet.addFile(new ContentFile());

        dd.setFileSet(fileSet);
        assertNotNull(dd.getFileSet());
        assertNotNull(dd.getFileSet().getVirtualFileGroups());
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertNotNull(dd.getFileSet().getAllMetadata());
        assertEquals(1, dd.getFileSet().getAllMetadata().size());
        assertNotNull(dd.getFileSet().getAllFiles());
        assertEquals(1, dd.getFileSet().getAllFiles().size());

        // tests
        List<String> images = new ArrayList<>();
        dd.overrideContentFiles(images);
        // the field virtualFileGroups should not be affected
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertSame(vfgList, dd.getFileSet().getVirtualFileGroups());
        // the fields allMetadata and allImages should be reset
        assertEquals(0, dd.getFileSet().getAllMetadata().size());
        assertEquals(0, dd.getFileSet().getAllFiles().size());
    }

    @Ignore("The logic in the method cannot pass this test. Empty list should be handled to avoid the IndexOutOfBoundsException.")
    @Test
    // TODO add check to overrideContentFiles if new image name list has the same size as the existing page list
    public void testOverrideContentFilesGivenUnemptyDigitalDocumentButEmptyStringList()
            throws MetadataTypeNotAllowedException, TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
        // prepare FileSet containing a list of VirtualFileGroups
        VirtualFileGroup vfg1 = new VirtualFileGroup();
        VirtualFileGroup vfg2 = new VirtualFileGroup();
        List<VirtualFileGroup> vfgList = new ArrayList<>();
        vfgList.add(vfg1);
        vfgList.add(vfg2);
        FileSet fileSet = new FileSet();
        fileSet.setVirtualFileGroups(vfgList);

        // add a Metadata object and a ContentFile object to show the changes of FileSet during the application of this method
        fileSet.addMetadata(new Metadata(new MetadataType()));
        fileSet.addFile(new ContentFile());

        dd.setFileSet(fileSet);
        assertNotNull(dd.getFileSet());
        assertNotNull(dd.getFileSet().getVirtualFileGroups());
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertNotNull(dd.getFileSet().getAllMetadata());
        assertEquals(1, dd.getFileSet().getAllMetadata().size());
        assertNotNull(dd.getFileSet().getAllFiles());
        assertEquals(1, dd.getFileSet().getAllFiles().size());

        // prepare contents
        DocStruct tp = preparePhysicalDocStruct();
        dd.setPhysicalDocStruct(tp);

        // tests
        List<String> images = new ArrayList<>();
        dd.overrideContentFiles(images);
        // the field virtualFileGroups should not be affected
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertSame(vfgList, dd.getFileSet().getVirtualFileGroups());
        // the fields allMetadata and allImages should be reset
        assertEquals(0, dd.getFileSet().getAllMetadata().size());
        assertEquals(0, dd.getFileSet().getAllFiles().size());
    }

    @Test
    public void testOverrideContentFilesGivenUnemptyDigitalDocumentAndUnemptyStringListWithMatchedSize()
            throws MetadataTypeNotAllowedException, TypeNotAllowedAsChildException, TypeNotAllowedForParentException {
        // prepare FileSet containing a list of VirtualFileGroups
        VirtualFileGroup vfg1 = new VirtualFileGroup();
        VirtualFileGroup vfg2 = new VirtualFileGroup();
        List<VirtualFileGroup> vfgList = new ArrayList<>();
        vfgList.add(vfg1);
        vfgList.add(vfg2);
        FileSet fileSet = new FileSet();
        fileSet.setVirtualFileGroups(vfgList);

        // add a Metadata object and a ContentFile object to show the changes of FileSet during the application of this method
        fileSet.addMetadata(new Metadata(new MetadataType()));
        fileSet.addFile(new ContentFile());

        dd.setFileSet(fileSet);
        assertNotNull(dd.getFileSet());
        assertNotNull(dd.getFileSet().getVirtualFileGroups());
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertNotNull(dd.getFileSet().getAllMetadata());
        assertEquals(1, dd.getFileSet().getAllMetadata().size());
        assertNotNull(dd.getFileSet().getAllFiles());
        assertEquals(1, dd.getFileSet().getAllFiles().size());

        // prepare contents
        DocStruct tp = preparePhysicalDocStruct();
        dd.setPhysicalDocStruct(tp);
        dd.addAllContentFiles();

        String oldLocation = dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFiles().get(0).getLocation();
        String oldMimeType = dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFiles().get(0).getMimetype();

        // tests
        List<String> images = Arrays.asList("11", "12", "13");
        dd.overrideContentFiles(images);
        // the field virtualFileGroups should not be affected
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertSame(vfgList, dd.getFileSet().getVirtualFileGroups());
        // the fields allMetadata and allImages should be reset
        assertEquals(0, dd.getFileSet().getAllMetadata().size());
        assertEquals(0, dd.getFileSet().getAllFiles().size());

        // location should be changed
        assertNotEquals(oldLocation, dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFiles().get(0).getLocation());
        // mimetype should remain the same
        assertEquals(oldMimeType, dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFiles().get(0).getMimetype());
    }

    @Ignore("The logic in the method cannot pass this test. Since the length of the input list matters, it should also be somehow checked.")
    @Test
    // TODO add check to overrideContentFiles if new image name list has the same size as the existing page list
    public void testOverrideContentFilesGivenUnemptyDigitalDocumentAndUnemptyStringListWithSmallerSize()
            throws TypeNotAllowedAsChildException, MetadataTypeNotAllowedException, TypeNotAllowedForParentException {
        // prepare FileSet containing a list of VirtualFileGroups
        VirtualFileGroup vfg1 = new VirtualFileGroup();
        VirtualFileGroup vfg2 = new VirtualFileGroup();
        List<VirtualFileGroup> vfgList = new ArrayList<>();
        vfgList.add(vfg1);
        vfgList.add(vfg2);
        FileSet fileSet = new FileSet();
        fileSet.setVirtualFileGroups(vfgList);

        // add a Metadata object and a ContentFile object to show the changes of FileSet during the application of this method
        fileSet.addMetadata(new Metadata(new MetadataType()));
        fileSet.addFile(new ContentFile());

        dd.setFileSet(fileSet);
        assertNotNull(dd.getFileSet());
        assertNotNull(dd.getFileSet().getVirtualFileGroups());
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertNotNull(dd.getFileSet().getAllMetadata());
        assertEquals(1, dd.getFileSet().getAllMetadata().size());
        assertNotNull(dd.getFileSet().getAllFiles());
        assertEquals(1, dd.getFileSet().getAllFiles().size());

        // prepare contents
        DocStruct tp = preparePhysicalDocStruct();
        dd.setPhysicalDocStruct(tp);
        dd.addAllContentFiles();

        String oldLocation = dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFiles().get(0).getLocation();
        String oldMimeType = dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFiles().get(0).getMimetype();

        // tests
        List<String> images = Arrays.asList("11", "12"); // <<<--- HERE IS THE PROBLEM, too short for our page valued "3"
        dd.overrideContentFiles(images);
        // the field virtualFileGroups should not be affected
        assertEquals(2, dd.getFileSet().getVirtualFileGroups().size());
        assertSame(vfgList, dd.getFileSet().getVirtualFileGroups());
        // the fields allMetadata and allImages should be reset
        assertEquals(0, dd.getFileSet().getAllMetadata().size());
        assertEquals(0, dd.getFileSet().getAllFiles().size());

        // location should be changed
        assertNotEquals(oldLocation, dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFiles().get(0).getLocation());
        // mimetype should remain the same
        assertEquals(oldMimeType, dd.getPhysicalDocStruct().getAllChildren().get(0).getAllContentFiles().get(0).getMimetype());
    }

    /* Tests for the methods:
     * 1. equals(DigitalDocument)
     * 2. quickPairCheck(Object, Object)
     * And the enum:
     * 3. ListPairCheck
     */
    @Test
    public void testEqualsAgainstItself() throws TypeNotAllowedForParentException {
        // given initial settings
        assertTrue(dd.equals(dd));

        // initialize the field topPhysicalStruct and test the method again
        DocStructType phyType = new DocStructType();
        // the name of DocStructType is necessary for comparing two DocStruct objects
        phyType.setName("phy");
        DocStruct phyDS = new DocStruct(phyType);
        dd.setPhysicalDocStruct(phyDS);
        assertTrue(dd.equals(dd));

        // initialize the field topLogicalStruct and test the method again
        DocStructType logType = new DocStructType();
        logType.setName("log");
        DocStruct logDS = new DocStruct(logType);
        dd.setLogicalDocStruct(logDS);
        assertTrue(dd.equals(dd));
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    // TODO remove this method. It is not an overwrite of the object equals method and its not used anywhere
    public void testEqualsAgainstNull() {
        assertFalse(dd.equals(null));
    }

    @Test
    public void testEqualsAgainstItsCopy() throws WriteException, TypeNotAllowedForParentException {
        // given initial settings
        DigitalDocument document = dd.copyDigitalDocument();
        assertTrue(dd.equals(document));

        // initialize the field topLogicalStruct and test again
        DocStructType logType = new DocStructType();
        logType.setName("log");
        DocStruct logDS = new DocStruct(logType);
        dd.setLogicalDocStruct(logDS);
        document = dd.copyDigitalDocument();
        assertTrue(dd.equals(document));

        // initialize the field topPhysicalStruct and test again
        DocStructType phyType = new DocStructType();
        phyType.setName("phy");
        DocStruct phyDS = new DocStruct(phyType);
        dd.setPhysicalDocStruct(phyDS);
        document = dd.copyDigitalDocument();
        assertTrue(dd.equals(document));
    }

    @Test
    public void testQuickPairCheckGivenBothNull() {
        assertEquals(ListPairCheck.isEqual, dd.quickPairCheck(null, null));
    }

    @Test
    public void testQuickPairCheckGivenOnlyOneNull() {
        Object obj = new Object();
        assertEquals(ListPairCheck.isNotEqual, dd.quickPairCheck(null, obj));
        assertEquals(ListPairCheck.isNotEqual, dd.quickPairCheck(obj, null));
    }

    @Test
    public void testQuickPairCheckGivenTheSameObject() {
        Object obj = new Object();
        assertEquals(ListPairCheck.needsFurtherChecking, dd.quickPairCheck(obj, obj));
    }

    @Test
    public void testEqualsGivenDifferentDigitalDocumentObjects() throws TypeNotAllowedForParentException {
        //          two DigitalDocument objects dd1 and dd2 are equal
        // <=>  dd1.topPhysicalStruct and dd2.topPhysicalStruct are both null OR equal
        //     && dd1.topLogicalStruct and dd2.topLogicalStruct are both null OR equal
        DigitalDocument document = new DigitalDocument();
        // before initialization of both fields topPhysicalStruct and topLogicalStruct
        assertTrue(dd.equals(document));

        // prepare DocStruct objects for further tests
        DocStructType phyType1 = new DocStructType();
        phyType1.setName("phy1");
        DocStructType phyType2 = new DocStructType();
        phyType2.setName("phy2");
        DocStructType logType1 = new DocStructType();
        logType1.setName("log1");
        DocStructType logType2 = new DocStructType();
        logType2.setName("log2");
        DocStruct phyDS1 = new DocStruct(phyType1);
        DocStruct phyDS2 = new DocStruct(phyType2);
        DocStruct logDS1 = new DocStruct(logType1);
        DocStruct logDS2 = new DocStruct(logType2);

        // tests
        dd.setPhysicalDocStruct(phyDS1);
        assertFalse(dd.equals(document));
        document.setPhysicalDocStruct(phyDS2);
        assertFalse(dd.equals(document));
        document.setPhysicalDocStruct(phyDS1);
        assertTrue(dd.equals(document));
        document.setLogicalDocStruct(logDS2);
        assertFalse(document.equals(dd));
        dd.setLogicalDocStruct(logDS1);
        assertFalse(document.equals(dd));
        dd.setLogicalDocStruct(logDS2);
        assertTrue(document.equals(dd));

        // differences in other fields should not affect the equivalence
        dd.setAmdSec("id");
        document.setAmdSec("id2");
        assertFalse(dd.getAmdSec().getId().equals(document.getAmdSec().getId()));
        assertTrue(dd.equals(document));

        dd.setFileSet(new FileSet());
        assertNotNull(dd.getFileSet());
        assertNull(document.getFileSet());
        assertTrue(dd.equals(document));
    }

    /* Tests for the methods:
     * 1. addTechMd(Node)
     * 2. addTechMd(Md)
     * 3. getTechMdsAsNodes()
     * 4. getTechMds()
     * 5. getTechMd(String)
     */
    @Test
    public void testAddTechMdGivenNodeObject() {
        // before initializing the field amdSec
        assertNull(dd.getAmdSec());
        dd.addTechMd(upperChild);
        assertEquals(1, dd.getAmdSec().getTechMdsAsNodes().size());
    }

    @Test
    public void testAddTechMdGivenMdObject() {
        // before initializing the field amdSec
        assertNull(dd.getAmdSec());
        Md md = new Md(upperChild);
        dd.addTechMd(md);
        assertEquals(1, dd.getAmdSec().getTechMdList().size());
    }

    @Test
    public void testGetTechMdsAsNodesWithoutInitializingTheFieldAmdSec() {
        assertNotNull(dd.getTechMdsAsNodes());
        assertEquals(0, dd.getTechMdsAsNodes().size());
    }

    @Test
    public void testGetTechMdsAsNodesAfterInitializingTheFieldAmdSec() {
        dd.setAmdSec("sec");
        assertNotNull(dd.getAmdSec());
        assertNotNull(dd.getTechMdsAsNodes());
        assertEquals(0, dd.getTechMdsAsNodes().size());

        Md md = new Md(upperChild);
        dd.addTechMd(md);
        assertEquals(1, dd.getTechMdsAsNodes().size());
        assertEquals(md.getContent(), dd.getTechMdsAsNodes().get(0));

        dd.addTechMd(upperChild);
        assertEquals(2, dd.getTechMdsAsNodes().size());
        assertEquals(upperChild, dd.getTechMdsAsNodes().get(0));
    }

    @Test
    public void testGetTechMdsAsNodesAgainstNullAddingFromOutside() {
        // initialize the field amdSec
        dd.setAmdSec("sec");

        List<Node> nodes = dd.getTechMdsAsNodes();
        assertEquals(0, nodes.size());
        nodes.add(null);
        assertEquals(1, nodes.size());
        assertEquals(0, dd.getTechMdsAsNodes().size());

        dd.addTechMd(upperChild);
        assertEquals(1, dd.getTechMdsAsNodes().size());
        nodes = dd.getTechMdsAsNodes();
        assertEquals(1, nodes.size());
        nodes.add(null);
        assertEquals(2, nodes.size());
        assertEquals(1, dd.getTechMdsAsNodes().size());
    }

    @Test
    public void testGetTechMds() {
        // before initializing the field amdSec
        assertNull(dd.getAmdSec());
        assertNotNull(dd.getTechMds());
        assertEquals(0, dd.getTechMds().size());

        dd.addTechMd(upperChild);
        assertEquals(1, dd.getTechMds().size());

        dd.addTechMd(new Md(upperChild));
        assertEquals(2, dd.getTechMds().size());
    }

    @Test
    public void testGetTechMdsAgainstNullAddingFromOutside() {
        // before initializing the field amdSec
        List<Md> mds = dd.getTechMds();
        assertEquals(0, mds.size());
        mds.add(null);
        assertEquals(1, mds.size());
        assertEquals(0, dd.getTechMds().size());

        // initialize the field amdSec with a new AmdSec object
        AmdSec sec = new AmdSec(new ArrayList<Md>());
        dd.setAmdSec(sec);
        Md md = new Md(upperChild);
        md.setId("id");
        sec.addTechMd(md);
        assertEquals(1, dd.getTechMds().size());

        // try to add null to the resulted list of getTechMds
        mds = dd.getTechMds();
        assertEquals(1, mds.size());
        mds.add(null);
        assertEquals(2, mds.size());
        assertEquals(2, dd.getTechMds().size());
    }

    @Test
    public void testGetTechMdGivenNull() {
        // before initializing the field amdSec
        assertNull(dd.getAmdSec());
        assertNull(dd.getTechMd(null));

        // initialize the field amdSec with a new AmdSec object
        AmdSec sec = new AmdSec(new ArrayList<Md>());
        dd.setAmdSec(sec);
        assertNotNull(sec.getTechMdList());
        assertNull(dd.getTechMd(null));

        // add a Md object and test this method again
        Md md = new Md(upperChild);
        assertEquals(0, sec.getTechMdList().size());
        dd.addTechMd(md);
        assertEquals(1, sec.getTechMdList().size());
        assertNull(dd.getTechMd(null));
    }

    @Test
    public void testGetTechMdGivenEmptyString() {
        // before initializing the field amdSec
        assertNull(dd.getAmdSec());
        assertNull(dd.getAmdSec(""));

        // initialize the field amdSec with a new AmdSec object
        AmdSec sec = new AmdSec(new ArrayList<Md>());
        dd.setAmdSec(sec);
        assertNotNull(sec.getTechMdList());
        assertNull(dd.getTechMd(""));

        // add a Md object and test this method again
        Md md = new Md(upperChild);
        assertEquals(0, sec.getTechMdList().size());
        dd.addTechMd(md);
        assertEquals(1, sec.getTechMdList().size());
        assertNull(dd.getTechMd(""));

        // set the id of the Md object and test this method again
        md.setId("");
        assertNotNull(dd.getTechMd(""));
        assertSame(md, dd.getTechMd(""));
    }

    @Test
    public void testGetTechMdGivenUnemptyString() {
        String[] names = new String[] { "_", " ", "not a name", "BAZINGA!" };
        // initialize the field amdSec with a new AmdSec object
        AmdSec sec = new AmdSec(new ArrayList<Md>());
        dd.setAmdSec(sec);
        assertNotNull(sec.getTechMdList());

        for (String name : names) {
            assertNull(dd.getTechMd(name));

            // add a Md object and test this method again
            Md md = new Md(upperChild);
            dd.addTechMd(md);
            assertNull(dd.getTechMd(name));

            // set the id of the Md object and test this method again
            md.setId(name);
            assertNotNull(dd.getTechMd(name));
            assertSame(md, dd.getTechMd(name));
        }
    }

    /* Tests for the method copyDigitalDocument() */
    @Test
    public void testCopyBeforeInitializingTheFieldAmdSec() throws WriteException {
        DigitalDocument doc = dd.copyDigitalDocument();
        assertTrue(dd.equals(doc));
        assertTrue(doc.equals(dd));
        assertNull(dd.getAmdSec());
        assertNull(doc.getAmdSec());
    }

    @Test
    public void testCopyAfterInitializingTheFieldAmdSec() throws WriteException {
        assertEquals(0, dd.getTechMds().size());
        Md md1 = new Md(upperChild);
        Md md2 = new Md(upperChild);
        dd.addTechMd(md1);
        dd.addTechMd(md2);
        assertEquals(2, dd.getTechMds().size());
        // make a copy of dd and check its contents
        DigitalDocument doc = dd.copyDigitalDocument();
        assertEquals(2, doc.getTechMds().size());
        assertSame(md1, doc.getTechMds().get(0));
        assertSame(md2, doc.getTechMds().get(1));
    }

    @Test
    public void testCopy() throws WriteException, PreferencesException {
        DigitalDocument docOriginal = fileformat.getDigitalDocument();
        DigitalDocument docCopy = docOriginal.copyDigitalDocument();

        // check the fields topPhysicalStruct and topLogicalStruct
        assertTrue(docOriginal.equals(docCopy));
        assertTrue(docCopy.equals(docOriginal));

        // check the field amdSec
        List<Md> mdsOriginal = docOriginal.getTechMds();
        List<Md> mdsCopy = docCopy.getTechMds();
        assertEquals(mdsOriginal.size(), mdsCopy.size());
        for (int i = 0; i < mdsOriginal.size(); ++i) {
            // copied are actually only the addresses of these Md objects
            assertSame(mdsOriginal.get(i), mdsCopy.get(i));
        }

        // check the field allImages
        FileSet setOriginal = docOriginal.getFileSet();
        FileSet setCopy = docCopy.getFileSet();
        List<ContentFile> filesOriginal = setOriginal.getAllFiles();
        List<ContentFile> filesCopy = setCopy.getAllFiles();
        List<Metadata> metadatenOriginal = setOriginal.getAllMetadata();
        List<Metadata> metadatenCopy = setCopy.getAllMetadata();
        List<VirtualFileGroup> vfgOriginal = setOriginal.getVirtualFileGroups();
        List<VirtualFileGroup> vfgCopy = setCopy.getVirtualFileGroups();
        assertEquals(filesOriginal.size(), filesCopy.size());
        assertEquals(metadatenOriginal.size(), metadatenCopy.size());
        assertEquals(vfgOriginal.size(), vfgCopy.size());
        for (int i = 0; i < filesOriginal.size(); ++i) {
            assertTrue(filesOriginal.get(i).equals(filesCopy.get(i)));
        }
        for (int i = 0; i < metadatenOriginal.size(); ++i) {
            assertTrue(metadatenOriginal.get(i).equals(metadatenCopy.get(i)));
        }
        for (int i = 0; i < vfgOriginal.size(); ++i) {
            assertEquals(vfgOriginal.get(i), vfgCopy.get(i));
        }
    }

    /* Tests for the method detectMimeType(Path) */
    @Test
    public void testDetectMimeTypeGivenNull() {
        assertEquals("", DigitalDocument.detectMimeType(null));
    }

    @Test
    public void testDetectMimeTypeGivenDirectoryPath() {
        assertEquals("", DigitalDocument.detectMimeType(Paths.get("/", "tmp")));
    }

    @Ignore("Check the comments below.")
    @Test
    public void testDetectMimeTypeGivenUnexistingPath() throws IOException {
        // prepare a map containing some known formats
        Map<String, String> typeMap = new HashMap<>();
        for (String s : new String[] { "jpg", "jpeg", "jpe" }) {
            typeMap.put(s, "image/jpeg");
        }
        for (String s : new String[] { "tiff", "tif" }) {
            typeMap.put(s, "image/tiff");
        }
        for (String s : new String[] { "jp2", "png", "gif" }) {
            typeMap.put(s, "image/" + s);
        }
        for (String s : new String[] { "pdf", "xml" }) {
            typeMap.put(s, "application/" + s);
        }
        typeMap.put("mp3", "audio/mpeg");
        typeMap.put("wav", "audio/wav");
        for (String s : new String[] { "mpeg", "mpg", "mpe" }) {
            typeMap.put(s, "video/mpeg");
        }
        for (String s : new String[] { "mp4", "mxf", "ogg", "webm" }) {
            typeMap.put(s, "video/" + s);
        }
        typeMap.put("mov", "video/quicktime");
        typeMap.put("avi", "video/x-msvideo");
        typeMap.put("txt", "text/plain");
        for (String s : new String[] { "x3d", "x3dv", "x3db" }) {
            typeMap.put(s, "model/x3d+XXX");
        }
        for (String s : new String[] { "obj", "ply", "stl", "fbx", "gltf", "glb" }) {
            typeMap.put(s, "object/" + s);
        }

        for (String key : typeMap.keySet()) {
            Path path = Paths.get("/tmp/", "test." + key);
            System.out.println(key + " : " + typeMap.get(key));
            System.out.println(Files.probeContentType(path));
            System.out.println(URLConnection.guessContentTypeFromName(path.getFileName().toString()));
            System.out.println(DigitalDocument.detectMimeType(path));
            System.out.println("=======");
        }

        /* ======= Problems found [ via Ubuntu 21.10 ] ======= */
        // ogg: Files.probeContentType -> "audio/ogg", URLConnection.guessContentTypeFromName -> "audio/ogg"
        // mxf: Files.probeContentType -> "application/mxf"
        // wav: Files.probeContentType -> "audio/x-wav", URLConnection.guessContentTypeFromName -> "audio/x-wav"
        // items desired to be mapped to "object/..." are actually mapped to "model/..." by Files.probeContentType
    }

}
