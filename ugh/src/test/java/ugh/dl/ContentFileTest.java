package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedForParentException;

public class ContentFileTest {

    private static final File xmlFile = new File("src/test/resources/nodeTest.xml");
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder dBuilder;
    private static Document doc;
    private static NodeList nList;

    private ContentFile cf;

    @BeforeClass
    public static void setUpBeforeAll() throws ParserConfigurationException, SAXException, IOException {
        factory = DocumentBuilderFactory.newDefaultInstance();
        dBuilder = factory.newDocumentBuilder();
        doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        nList = doc.getElementsByTagName("node");
    }

    @Before
    public void setUp() {
        cf = new ContentFile();
    }

    /* Tests for the constructor */
    @Test
    public void testConstructor() {
        assertNull(cf.getAllMetadata());
        assertNull(cf.getReferencedDocStructs());
        assertNull(cf.getLocation());
        assertNull(cf.getMimetype());
        assertNull(cf.getIdentifier());
        assertNull(cf.getTechMds());
        assertNotNull(cf.getUuidMap());
        assertFalse(cf.isRepresentative());
    }

    /* Tests for the method addMetadata(Metadata) */
    @Test(expected = NullPointerException.class)
    public void testAddMetadataBeforeInitialization() throws MetadataTypeNotAllowedException {
        MetadataType mdType = new MetadataType();
        Metadata md = new Metadata(mdType);
        assertTrue(cf.addMetadata(md));
    }

    @Test(expected = NullPointerException.class)
    public void testAddMetadataGivenNull() {
        // Null should be avoided
        assertTrue(cf.addMetadata(null));
    }

    /* Tests for the method removeMetadata(Metadata) */
    @Test(expected = NullPointerException.class)
    public void testRemoveMetadataBeforeInitialization() throws MetadataTypeNotAllowedException {
        MetadataType mdType = new MetadataType();
        Metadata md = new Metadata(mdType);
        assertFalse(cf.removeMetadata(md));
    }

    @Ignore("There is no way to initialize the fields allMetadata and removedMetadata, hence no test can be designed.")
    @Test
    public void testRemoveMetadataGivenNull() {
        // Null should not be added into the removedMetadata list, since that would be nonsense
        // Return value should rather be True OR False?
    }

    @Ignore("There is no way to initialize the fields allMetadata and removedMetadata, hence no test can be designed.")
    @Test
    public void testRemoveMetadataGivenUnexistingMetadataObject() {
        // Unexisting Metadata object should not be added into the removedMetadata list, since it is not removed at all
        // Return value should rather be True OR False?
    }

    /* Tests for the method addDocStructAsReference(DocStruct) */
    @Test
    public void testAddDocStructAsReferenceBeforeInitialization() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds = new DocStruct(dsType);
        assertNull(cf.getReferencedDocStructs());
        cf.addDocStructAsReference(ds);
        assertNotNull(cf.getReferencedDocStructs());
        assertEquals(1, cf.getReferencedDocStructs().size());
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed.") // TODO add null check
    @Test
    public void testAddDocStructAsReferenceGivenNull() {
        assertNull(cf.getReferencedDocStructs());
        cf.addDocStructAsReference(null);
        assertNotNull(cf.getReferencedDocStructs());
        assertEquals(0, cf.getReferencedDocStructs().size());
    }

    @Ignore("The logic in the method cannot pass this test. Better to avoid adding same object again.") // TODO fix it
    @Test
    public void testAddDocStructAsReferenceGivenSameObjectTwice() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds = new DocStruct(dsType);
        assertTrue(cf.addDocStructAsReference(ds));
        assertEquals(1, cf.getReferencedDocStructs().size());
        assertFalse(cf.addDocStructAsReference(ds));
        assertEquals(1, cf.getReferencedDocStructs().size());
    }

    /* Tests for the method removeDocStructAsReference(DocStruct) */
    @Test
    public void testRemoveDocStructAsReferenceBeforeInitialization() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds = new DocStruct(dsType);
        assertFalse(cf.removeDocStructAsReference(ds));
    }

    @Test
    public void testRemoveDocStructAsReferenceGivenNull() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds = new DocStruct(dsType);
        assertTrue(cf.addDocStructAsReference(ds));
        assertEquals(1, cf.getReferencedDocStructs().size());
        assertTrue(cf.removeDocStructAsReference(null));
        assertEquals(1, cf.getReferencedDocStructs().size());
    }

    @Test
    public void testRemoveDocStructAsReferenceGivenEquivalentButDifferentObject() throws TypeNotAllowedForParentException {
        DocStructType dsType1 = new DocStructType();
        dsType1.setName("name");
        dsType1.setHasfileset(true);
        DocStructType dsType2 = new DocStructType();
        dsType2.setName("name");
        dsType2.setHasfileset(false);
        assertTrue(dsType1.equals(dsType2));
        DocStruct ds1 = new DocStruct(dsType1);
        DocStruct ds2 = new DocStruct(dsType2);
        assertNotSame(ds1, ds2);
        assertTrue(ds1.equals(ds2));
        assertTrue(cf.addDocStructAsReference(ds1));
        assertEquals(1, cf.getReferencedDocStructs().size());
        assertTrue(cf.removeDocStructAsReference(ds2));
        assertEquals(1, cf.getReferencedDocStructs().size()); // this method removes exactly the same object in the sense of address, i.e. == is used to compare instead of equals()
    }

    @Test
    public void testRemoveDocStructAsReferenceGivenUnexistingObject() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds = new DocStruct(dsType);
        assertTrue(cf.addDocStructAsReference(ds));
        assertEquals(1, cf.getReferencedDocStructs().size());
        assertTrue(cf.removeDocStructAsReference(new DocStruct(new DocStructType())));
        assertEquals(1, cf.getReferencedDocStructs().size());
    }

    @Test
    public void testRemoveDocStructAsReferenceGivenExistingObject() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds = new DocStruct(dsType);
        assertTrue(cf.addDocStructAsReference(ds));
        assertEquals(1, cf.getReferencedDocStructs().size());
        assertTrue(cf.removeDocStructAsReference(ds));
        assertEquals(0, cf.getReferencedDocStructs().size());
    }

    /* Tests for the method equals(ContentFile) */
    // Everything from L277 till the end of the method cannot be tested, since they are unreachable thanks to the uninitializable field allMetadata. - Zehong
    @Test
    public void testEqualsToItself() {
        assertTrue(cf.equals(cf));
    }

    @Test
    public void testEqualsGivenNull() {
        assertFalse(cf.equals(null));
    }

    @Test
    public void testEqualsGivenContentFileObjectsWithDifferentReferencedDocStructs() throws TypeNotAllowedForParentException {
        DocStructType dsType = new DocStructType();
        DocStruct ds1 = new DocStruct(dsType);
        DocStruct ds2 = new DocStruct(dsType);
        assertNotEquals(ds1, ds2);
        cf.addDocStructAsReference(ds1);
        ContentFile cf2 = new ContentFile();
        cf2.addDocStructAsReference(ds2);
        assertTrue(cf.equals(cf2));
    }

    /* Tests for the methods getTechMds(), addTechMd(Md), setTechMds(List<Md>) */
    @Test
    public void testAddTechMdBeforeInitialization() throws ParserConfigurationException, SAXException, IOException {
        assertNull(cf.getTechMds());
        Node node = nList.item(0);
        Md md = new Md(node);
        cf.addTechMd(md);
        assertNotNull(cf.getTechMds());
        assertEquals(1, cf.getTechMds().size());
        assertEquals(md, cf.getTechMds().get(0));
    }

    @Test
    public void testAddTechMdGivenNullBeforeInitialization() {
        assertNull(cf.getTechMds());
        cf.addTechMd(null);
        assertNotNull(cf.getTechMds());
        assertEquals(0, cf.getTechMds().size());
    }

    @Test
    public void testAddTechMdGivenNullAfterInitialization() {
        assertNull(cf.getTechMds());
        Node node = nList.item(0);
        Md md = new Md(node);
        cf.addTechMd(md);
        assertEquals(1, cf.getTechMds().size());
        cf.addTechMd(null);
        assertEquals(1, cf.getTechMds().size());
    }

    @Test
    public void testSetTechMdsGivenNullAfterInitialization() {
        Node node = nList.item(0);
        Md md = new Md(node);
        cf.addTechMd(md);
        assertNotNull(cf.getTechMds());
        cf.setTechMds(null);
        assertNotNull(cf.getTechMds());
    }

    @Ignore("This test actually passes in order to show the importance of ENCAPSULATION !!!") // TODO fix it
    @Test
    public void testAddTechMdTogetherWithModificationsOnTheResultOfGetterThenApplySetter() {
        // according to the design of the method addTechMd(Md), null can not be added
        Node node = nList.item(0);
        Md md = new Md(node);
        cf.addTechMd(md);
        assertEquals(1, cf.getTechMds().size());
        cf.addTechMd(null);
        assertEquals(1, cf.getTechMds().size());
        assertFalse(cf.getTechMds().contains(null));
        // however, we can still achieve that in the following way

        // TODO don't allow this, maybe change the setter and remove all null elements from the new list
        List<Md> techMdList = cf.getTechMds();
        techMdList.add(null);
        cf.setTechMds(techMdList);
        assertEquals(2, cf.getTechMds().size());
        assertTrue(cf.getTechMds().contains(null));
    }

    /* Tests for the methods getUuidMap(), addUUID(String, String), getUUID(String) */
    @Test
    public void testAddUUIDGivenNullAsFirstArgument() {
        cf.addUUID(null, "uuid");
        assertEquals(1, cf.getUuidMap().size());
    }

    @Test
    public void testAddUUIDGivenNullAsSecondArgument() {
        cf.addUUID("type", null);
        assertEquals(1, cf.getUuidMap().size());
    }

    @Test
    public void testAddUUIDTwiceGivenSameFirstButDifferentSecondArguments() {
        cf.addUUID("type", "1");
        assertEquals(1, cf.getUuidMap().size());
        assertEquals("1", cf.getUUID("type"));
        cf.addUUID("type", "2");
        assertEquals(1, cf.getUuidMap().size());
        assertNotEquals("1", cf.getUUID("type"));
        assertEquals("2", cf.getUUID("type"));
    }

}
