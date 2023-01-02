package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

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

import ugh.exceptions.PreferencesException;

public class PrefsTest {
    private Prefs prefs;

    private static Document docError;
    private static NodeList upperChildListError;
    private static Node upperChildError;
    private static NodeList blocksListError; // blocks prepared for error tests

    private static Document docFunctionality;
    private static NodeList upperChildListFunctionality;
    private static Node upperChildFunctionality;
    private static NodeList blocksListFunctionality; // blocks prepared for functionality tests

    @BeforeClass
    public static void setUpBeforeAll() throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Do not validate xml file.
        factory.setValidating(false);
        // Namespace does not matter.
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        docError = builder.parse(new File("src/test/resources/rulesetForPrefsErrorTest.xml"));
        upperChildListError = docError.getElementsByTagName("Preferences");
        upperChildError = upperChildListError.item(0);
        blocksListError = upperChildError.getChildNodes();

        docFunctionality = builder.parse(new File("src/test/resources/rulesetForPrefsFunctionalityTest.xml"));
        upperChildListFunctionality = docFunctionality.getElementsByTagName("Preferences");
        upperChildFunctionality = upperChildListFunctionality.item(0);
        blocksListFunctionality = upperChildFunctionality.getChildNodes();
    }

    @Before
    public void setUpBeforeEach() {
        prefs = new Prefs();
    }

    /* Tests for the constructor */
    @Test
    public void testConstructor() {
        assertNotNull(prefs.getAllDocStructTypes());
        assertEquals(0, prefs.getAllDocStructTypes().size());
        assertNotNull(prefs.getAllMetadataTypes());
        assertEquals(0, prefs.getAllMetadataTypes().size());
    }

    /* Tests for the method loadPrefs(String) */
    @Test
    public void testLoadPrefsGivenNull() {
        // better handle the NullPointerException 
        assertThrows(Exception.class, () -> prefs.loadPrefs(null));
    }

    @Test
    public void testLoadPrefsGivenEmptyString() {
        assertThrows(PreferencesException.class, () -> prefs.loadPrefs(""));
    }

    @Test
    public void testLoadPrefsGivenUnexistingName() {
        assertThrows(PreferencesException.class, () -> prefs.loadPrefs("unexisting.xml"));
    }

    @Ignore("This test actually passes. But there is a logical bug in the source codes. Check the comments below.")
    @Test
    public void testLoadPrefsGivenValidFileWithInvalidContents1() {
        // On L170 of Prefs.java, upperChildlist will never be null.
        // Therefore running this test will give us "No upper child in preference file" as the error message.
        // The checking logic should be changed to "if (upperChildlist.getLength() == 0)"
        assertThrows(PreferencesException.class, () -> prefs.loadPrefs("src/test/resources/nodeTest.xml"));
    }

    @Ignore("The logic in the method cannot pass this test. Check the comments below.")
    @Test
    public void testLoadPrefsGivenValidFileWithInvalidContents2() throws PreferencesException {
        // On L179 of Prefs.java, upperChild will not be null as expected.
        // Therefore running this test will throw no exception. 
        // The checking logic should be modified (no idea how yet).
        assertThrows(PreferencesException.class, () -> prefs.loadPrefs("src/test/resources/rulesetFake.xml"));
    }

    @Test
    public void testLoadPrefsGivenValidFileWithValidContents() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertTrue(prefs.getAllMetadataTypes().size() > 0);
        assertTrue(prefs.getAllDocStructTypes().size() > 0);
    }

    /* Tests for the method parseDocStrctType(Node) */
    @Test
    public void testParseDocStrctTypeGivenInvalidInputs() {
        validateBlocksDocStrctTypeTest(blocksListError);
    }

    @Test
    public void testParseDocStrctTypeGivenValidInputs() {
        validateBlocksDocStrctTypeTest(blocksListFunctionality);
    }

    private void validateBlocksDocStrctTypeTest(NodeList blocksList) {
        Node blockNode = null;
        NodeList dstTestList = null;
        NodeList dstList = null;
        Node dstNode = null;

        // get the NodeList of <DocStrctTypeTest> blocks
        for (int i = 0; i < blocksList.getLength(); ++i) {
            blockNode = blocksList.item(i);
            if (blockNode.getNodeType() != Node.ELEMENT_NODE || !blockNode.getNodeName().equals("DocStrctTypeTest")) {
                continue;
            }
            dstTestList = blockNode.getChildNodes();
            // for every <DocStrctTypeTest> block, get its <DocStrctType> NodeList
            for (int j = 0; j < dstTestList.getLength(); ++j) {
                dstNode = dstTestList.item(j);
                if (dstNode.getNodeType() != Node.ELEMENT_NODE || !dstNode.getNodeName().equals("DocStrctType")) {
                    continue;
                }
                dstList = dstNode.getChildNodes();
                // for every <DocStrctType> node, get its <Name> node
                for (int k = 0; k < dstList.getLength(); ++k) {
                    Node node = dstList.item(k);
                    if (node.getNodeName().equals("Name")) {
                        assertNotNull(node.getChildNodes());
                        // On L321 textnodes will never be null, hence the check is redundant and the Error p004 will never be triggered
                    }
                }

                DocStructType dst = prefs.parseDocStrctType(dstNode);
                if (blocksList == blocksListError) {
                    // all <DocStrctType> nodes in blocksListError are designed to be problematic, so each one should trigger a null to return
                    assertNull(dst);
                } else if (blocksList == blocksListFunctionality) {
                    assertNotNull(dst);
                }
            }
        }
    }

    /* Tests for the method parseMetadataType(Node) */
    @Test
    public void testParseMetadataTypeGivenInvalidInputs() {
        validateBlocksMetadataTypeTest(blocksListError);
    }

    @Test
    public void testParseMetadataTypeGivenValidInputs() {
        validateBlocksMetadataTypeTest(blocksListFunctionality);
    }

    private void validateBlocksMetadataTypeTest(NodeList blocksList) {
        Node blockNode = null;
        NodeList mdtTestList = null;
        NodeList mdtList = null;
        Node mdtNode = null;

        // get the NodeList of <MetadataTypeTest> blocks
        for (int i = 0; i < blocksList.getLength(); ++i) {
            blockNode = blocksList.item(i);
            if (blockNode.getNodeType() != Node.ELEMENT_NODE || !blockNode.getNodeName().equals("MetadataTypeTest")) {
                continue;
            }
            mdtTestList = blockNode.getChildNodes();
            // for every <MetadataTypeTest> block, get its <MetadataType> NodeList
            for (int j = 0; j < mdtTestList.getLength(); ++j) {
                mdtNode = mdtTestList.item(j);
                if (mdtNode.getNodeType() != Node.ELEMENT_NODE || !mdtNode.getNodeName().equals("MetadataType")) {
                    continue;
                }
                mdtList = mdtNode.getChildNodes();
                // for every <MetadataType> node, get its <Name> node
                for (int k = 0; k < mdtList.getLength(); ++k) {
                    Node node = mdtList.item(k);
                    if (node.getNodeName().equals("Name")) {
                        assertNotNull(node.getChildNodes()); // On L629 textnodes will never be null, hence the check is redundant
                    }
                }

                MetadataType mdt = prefs.parseMetadataType(mdtNode);
                if (blocksList == blocksListError) {
                    // all <MetadataType> nodes in blocksListError are designed to be problematic, so each one should trigger a null to return
                    assertNull(mdt);
                } else if (blocksList == blocksListFunctionality) {
                    assertNotNull(mdt);
                    assertTrue(mdt.isCorporate || mdt.isPerson || mdt.isIdentifier);

                    Node ndNode = mdtNode.getAttributes().getNamedItem("normdata");
                    if (ndNode != null && ndNode.getNodeValue().equals("true")) {
                        assertTrue(mdt.isAllowNormdata());
                    } else {
                        assertFalse(mdt.isAllowNormdata());
                    }

                    Node npNode = mdtNode.getAttributes().getNamedItem("namepart");
                    if (npNode != null && npNode.getNodeValue().equals("true")) {
                        assertTrue(mdt.isAllowNameParts());
                    } else {
                        assertFalse(mdt.isAllowNameParts());
                    }
                }
            }
        }
    }

    /* Tests for the method parseMetadataGroup(Node) */

    /* Tests for the method getDocStrctTypeByName(String) */
    @Test
    public void testGetDocStrctTypeByNameGivenNull() {
        assertNull(prefs.getDocStrctTypeByName(null));
    }

    @Test
    public void testGetDocStrctTypeByNameGivenEmptyString() {

    }

    /* Tests for the method getAllAnchorDocStructTypes() */

}




