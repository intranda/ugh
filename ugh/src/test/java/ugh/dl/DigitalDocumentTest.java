package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;

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

    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testSetLogicalDocStructGivenNull() throws TypeNotAllowedForParentException {
        // before initializing the field topLogicalStruct
        dd.setLogicalDocStruct(null);

        // initialize the field topLogicalStruct
        DocStructType dsType = new DocStructType();
        DocStruct ds = new DocStruct(dsType);
        assertFalse(ds.isLogical());
        dd.setLogicalDocStruct(ds);
        assertTrue(ds.isLogical());
        assertNotNull(dd.getLogicalDocStruct());

        // applying this method with null should not change the field topLogicalStruct
        dd.setLogicalDocStruct(null);
        assertTrue(ds.isLogical());
        assertSame(ds, dd.getLogicalDocStruct());
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

    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testSetPhysicalDocStructGivenNull() throws TypeNotAllowedForParentException {
        // before initializing the field topPhysicalStruct
        dd.setPhysicalDocStruct(null);

        // initialize the field topPhysicalStruct
        DocStructType dsType = new DocStructType();
        DocStruct ds = new DocStruct(dsType);
        assertFalse(ds.isPhysical());
        dd.setPhysicalDocStruct(ds);
        assertTrue(ds.isPhysical());
        assertNotNull(dd.getPhysicalDocStruct());
        
        // applying this method with null should not change the field topPhysicalStruct
        dd.setPhysicalDocStruct(null);
        assertTrue(ds.isPhysical());
        assertSame(ds, dd.getPhysicalDocStruct());
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

    /* Tests for the method getAllDocStructsByType(String) */

    /* Tests for the method readXStreamXml(String, Prefs) */

    /* Tests for the methods:
     * 1. sortMetadataRecursivelyAbcdefg() 
     * 2. sortMetadataRecursively(Prefs)
     */

    /* Tests for the method addContentFileFromPhysicalPage(DocStruct) */

    /* Tests for the method addAllContentFiles() */

    /* Tests for the method overrideContentFiles(List<String>) */

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

    @Ignore("The logic in the method cannot pass this test. Null check for the field amdSec needed to avoid the NullPointerException.")
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

    @Ignore("The logic in the method cannot pass this test. List copy needed to achieve encapsulation.")
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
        assertEquals(1, dd.getTechMds().size());
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
    public void testCopy() throws WriteException, PreferencesException {
        fileformat.getDigitalDocument().copyDigitalDocument();
    }

    /* Tests for the method detectMimeType(Path) */

}
