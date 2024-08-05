package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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

    @Test
    public void testLoadPrefsGivenValidFileWithInvalidContents1() {
        // On L170 of Prefs.java, upperChildlist will never be null.
        // Therefore running this test will give us "No upper child in preference file" as the error message.
        assertThrows(PreferencesException.class, () -> prefs.loadPrefs("src/test/resources/nodeTest.xml"));
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
        // prepare two MetadataTypes 'ExistingMetadata' and 'ExistingMetadata2' for prefs, which will be needed for the functionality tests
        MetadataType type = new MetadataType();
        type.setName("ExistingMetadata");
        prefs.addMetadataType(type);
        MetadataType type2 = new MetadataType();
        type2.setName("ExistingMetadata2");
        prefs.addMetadataType(type2);
        // prepare two MetadataGroupTypes 'UnusedGroup' and 'UnusedGroup2' for prefs, which will be needed for the functionality tests
        MetadataGroupType groupType = new MetadataGroupType();
        groupType.setName("UnusedGroup");
        prefs.addMetadataGroup(groupType);
        MetadataGroupType groupType2 = new MetadataGroupType();
        groupType2.setName("UnusedGroup2");
        prefs.addMetadataGroup(groupType2);

        Node blockNode = null;
        NodeList dstTestList = null;
        NodeList dstList = null;
        Node dstNode = null;

        // get the NodeList of <DocStrctTypeTest> blocks
        for (int i = 0; i < blocksList.getLength(); ++i) {
            blockNode = blocksList.item(i);
            if (blockNode.getNodeType() != Node.ELEMENT_NODE || !"DocStrctTypeTest".equals(blockNode.getNodeName())) {
                continue;
            }
            dstTestList = blockNode.getChildNodes();
            // for every <DocStrctTypeTest> block, get its <DocStrctType> NodeList
            for (int j = 0; j < dstTestList.getLength(); ++j) {
                dstNode = dstTestList.item(j);
                if (dstNode.getNodeType() != Node.ELEMENT_NODE || !"DocStrctType".equals(dstNode.getNodeName())) {
                    continue;
                }
                dstList = dstNode.getChildNodes();
                Node nameNode = null;
                // for every <DocStrctType> node, get its <Name> node
                for (int k = 0; k < dstList.getLength(); ++k) {
                    Node node = dstList.item(k);
                    if ("Name".equals(node.getNodeName())) {
                        assertNotNull(node.getChildNodes());
                        // On L321 textnodes will never be null, hence the check is redundant and the Error p004 will never be triggered
                        // Null check on L348, L394, L474, L532 are all redundant.

                        nameNode = node;
                        break;
                    }
                }

                DocStructType dst = prefs.parseDocStrctType(dstNode);
                if (blocksList == blocksListError) {
                    // all <DocStrctType> nodes in blocksListError are designed to be problematic, so each one should trigger a null to return
                    assertNull(dst);
                } else if (blocksList == blocksListFunctionality) {
                    assertNotNull(dst);
                    assertEquals(nameNode.getChildNodes().item(0).getNodeValue(), dst.getName());
                    assertEquals(2, dst.getAllLanguages().size());
                    assertEquals(1, dst.getAllAllowedDocStructTypes().size());

                    int countMetadata = 0; // count of <metadata> nodes
                    int countGroup = 0; // count of <group> nodes
                    // find all <metadata> as well as all <group> nodes of mdgt and test their num attributes
                    for (int t = 0; t < dstList.getLength(); ++t) {
                        Node node = dstList.item(t);

                        if ("metadata".equals(node.getNodeName())) {
                            Node numAtt = node.getAttributes().getNamedItem("num");
                            String num = dst.getAllMetadataTypes().get(countMetadata).getNum();
                            if (numAtt == null) {
                                assertEquals("1o", num);
                            } else {
                                assertEquals(numAtt.getNodeValue(), num);
                            }
                            ++countMetadata;
                        }

                        if ("group".equals(node.getNodeName())) {
                            Node numAtt = node.getAttributes().getNamedItem("num");
                            String num = dst.getAllMetadataGroupTypes().get(countGroup).getNum();
                            if (numAtt == null) {
                                assertEquals("1o", num);
                            } else {
                                assertEquals(numAtt.getNodeValue(), num);
                            }
                            ++countGroup;
                        }
                    }
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
            if (blockNode.getNodeType() != Node.ELEMENT_NODE || !"MetadataTypeTest".equals(blockNode.getNodeName())) {
                continue;
            }
            mdtTestList = blockNode.getChildNodes();
            // for every <MetadataTypeTest> block, get its <MetadataType> NodeList
            for (int j = 0; j < mdtTestList.getLength(); ++j) {
                mdtNode = mdtTestList.item(j);
                if (mdtNode.getNodeType() != Node.ELEMENT_NODE || !"MetadataType".equals(mdtNode.getNodeName())) {
                    continue;
                }
                mdtList = mdtNode.getChildNodes();
                Node nameNode = null;
                // for every <MetadataType> node, get its <Name> node
                for (int k = 0; k < mdtList.getLength(); ++k) {
                    Node node = mdtList.item(k);
                    if ("Name".equals(node.getNodeName())) {
                        assertNotNull(node.getChildNodes()); // On L629 textnodes will never be null, hence the check is redundant
                        // Same argument applies also for L653, L675, L686.

                        nameNode = node;
                        break;
                    }
                }

                MetadataType mdt = prefs.parseMetadataType(mdtNode);
                if (blocksList == blocksListError) {
                    // all <MetadataType> nodes in blocksListError are designed to be problematic, so each one should trigger a null to return
                    assertNull(mdt);
                } else if (blocksList == blocksListFunctionality) {
                    assertNotNull(mdt);
                    assertEquals(nameNode.getChildNodes().item(0).getNodeValue(), mdt.getName());
                    assertTrue(mdt.isCorporate || mdt.isPerson || mdt.isIdentifier);

                    Node ndNode = mdtNode.getAttributes().getNamedItem("normdata");
                    if (ndNode != null && "true".equals(ndNode.getNodeValue())) {
                        assertTrue(mdt.isAllowNormdata());
                    } else {
                        assertFalse(mdt.isAllowNormdata());
                    }

                    Node npNode = mdtNode.getAttributes().getNamedItem("namepart");
                    if (npNode != null && "true".equals(npNode.getNodeValue())) {
                        assertTrue(mdt.isAllowNameParts());
                    } else {
                        assertFalse(mdt.isAllowNameParts());
                    }
                }
            }
        }
    }

    /* Tests for the method parseMetadataGroup(Node) */
    @Test
    public void testParseMetadataGroupGivenInvalidInputs() {
        validateBlocksMetadataGroupTest(blocksListError);
    }

    @Test
    public void testParseMetadataGroupGivenValidInputs() {
        validateBlocksMetadataGroupTest(blocksListFunctionality);
    }

    private void validateBlocksMetadataGroupTest(NodeList blocksList) {
        // prepare two MetadataTypes 'ExistingMetadata' and 'ExistingMetadata2' for prefs, which will be needed for the functionality tests
        MetadataType type = new MetadataType();
        type.setName("ExistingMetadata");
        prefs.addMetadataType(type);
        MetadataType type2 = new MetadataType();
        type2.setName("ExistingMetadata2");
        prefs.addMetadataType(type2);

        Node blockNode = null;
        NodeList mdgTestList = null;
        NodeList mdgList = null;
        Node mdgNode = null;

        // get the NodeList of <GroupTest> blocks
        for (int i = 0; i < blocksList.getLength(); ++i) {
            blockNode = blocksList.item(i);
            if (blockNode.getNodeType() != Node.ELEMENT_NODE || !"GroupTest".equals(blockNode.getNodeName())) {
                continue;
            }
            mdgTestList = blockNode.getChildNodes();
            // for every <GroupTest> block, get its <Group> NodeList
            for (int j = 0; j < mdgTestList.getLength(); ++j) {
                mdgNode = mdgTestList.item(j);
                if (mdgNode.getNodeType() != Node.ELEMENT_NODE || !"Group".equals(mdgNode.getNodeName())) {
                    continue;
                }
                mdgList = mdgNode.getChildNodes();
                Node nameNode = null;
                // for every <Group> node, get its <Name> node
                for (int k = 0; k < mdgList.getLength(); ++k) {
                    Node node = mdgList.item(k);
                    if ("Name".equals(node.getNodeName())) {
                        assertNotNull(node.getChildNodes()); // On L742 textnodes will never be null, hence the check is redundant
                        // Same argument applies also for L778, L822, L844.

                        nameNode = node;
                        break;
                    }
                }

                MetadataGroupType mdgt = prefs.parseMetadataGroup(mdgNode);
                if (blocksList == blocksListError) {
                    // all <Group> nodes in blocksListError are designed to be problematic, so each one should trigger a null to return
                    assertNull(mdgt);
                } else if (blocksList == blocksListFunctionality) {
                    assertNotNull(mdgt);
                    assertEquals(nameNode.getChildNodes().item(0).getNodeValue(), mdgt.getName());
                    assertEquals(2, mdgt.getAllLanguages().size());
                    assertEquals(1, mdgt.getAllAllowedGroupTypeTypes().size());
                    assertEquals("+", mdgt.getAllAllowedGroupTypeTypes().get(0).getNumAllowed());

                    int count = 0; // count of <metadata> nodes
                    // find all <metadata> nodes of mdgt and test their num attributes
                    for (int t = 0; t < mdgList.getLength(); ++t) {
                        Node node = mdgList.item(t);

                        if ("metadata".equals(node.getNodeName())) {
                            Node numAtt = node.getAttributes().getNamedItem("num");
                            String num = mdgt.getMetadataTypeList().get(count).getNum();
                            if (numAtt == null) { // if not set explicitly, "*" will be used as default value for num
                                assertEquals("*", num);
                            } else {
                                assertEquals(numAtt.getNodeValue(), num);
                            }
                            ++count;
                        }
                    }
                }
            }
        }
    }

    /* Tests for the following methods:
     * 1. getDocStrctTypeByName(String)
     * 2. getDocStrctTypeByName(String, String)
     * */
    @Test
    public void testGetDocStrctTypeByNameGivenNull() throws PreferencesException {
        // before initialization of the field allDocStrctTypes
        assertEquals(0, prefs.getAllDocStructTypes().size());
        assertNull(prefs.getDocStrctTypeByName(null));
        assertNull(prefs.getDocStrctTypeByName(null, "en"));
        assertNull(prefs.getDocStrctTypeByName(null, null));
        assertNull(prefs.getDocStrctTypeByName("Introduction", null));

        // now initialize it and do the tests again
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertNotEquals(0, prefs.getAllDocStructTypes().size());
        assertNull(prefs.getDocStrctTypeByName(null));
        assertNull(prefs.getDocStrctTypeByName(null, "en"));
        assertNull(prefs.getDocStrctTypeByName(null, null));
        assertNull(prefs.getDocStrctTypeByName("Introduction", null));
    }

    @Test
    public void testGetDocStrctTypeByNameGivenUnexistingName() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertNull(prefs.getDocStrctTypeByName("Unexisting"));
        assertNull(prefs.getDocStrctTypeByName("Unexisting", "en"));
    }

    @Test
    public void testGetDocStrctTypeByNameGivenUnmatchingLanguage() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertNotNull(prefs.getDocStrctTypeByName("Introduction"));
        assertNotNull(prefs.getDocStrctTypeByName("Einleitung", "de"));
        assertNotNull(prefs.getDocStrctTypeByName("Introduction", "en"));
        assertNull(prefs.getDocStrctTypeByName("Introduction", "de"));

        // if the language does not exist, then null should be returned as well
        assertNull(prefs.getDocStrctTypeByName("Introduction", "Klingon"));
    }

    /* Tests for the method getAllAnchorDocStructTypes() */
    @Test
    public void testGetAllAnchorDocStructTypes() throws PreferencesException {
        // even before initialization of the field allDocStrctTypes, the call of this method does not return null
        assertNotNull(prefs.getAllAnchorDocStructTypes());
        assertEquals(0, prefs.getAllAnchorDocStructTypes().size());

        // initialize the field allDocStrctTypes
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertTrue(prefs.getAllAnchorDocStructTypes().size() > 0);
        for (DocStructType dst : prefs.getAllAnchorDocStructTypes()) {
            assertTrue(dst.isAnchor());
        }
    }

    /* Tests for the method getPreferenceNode(String) */
    @Test
    public void testGetPreferenceNodeGivenNull() throws PreferencesException {
        // before initialization of the field allFormats
        assertNull(prefs.getPreferenceNode(null));

        // initialize the field allFormats and do the test again
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertNull(prefs.getPreferenceNode(null));
    }

    @Test
    public void testGetPreferenceNodeGivenUnexistingKeys() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        String[] keys = new String[] { "java", "PicaMinus", "", "Hallelujah" };
        for (String key : keys) {
            assertNull(prefs.getPreferenceNode(key));
        }
    }

    @Test
    public void testGetPreferenceNodeGivenExistingKeys() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        String[] keys = new String[] { "PicaPlus", "Marc", "METS" };
        for (String key : keys) {
            assertNotNull(prefs.getPreferenceNode(key));
        }
    }

    /* Tests for the following methods:
     * 1. addMetadataType(MetadataType)
     * 2. getMetadataTypeByName(String)
     * 3. getMetadataTypeByName(String, String)
     * 4. getAllMetadataTypes()
     * */
    @Test
    public void testAddMetadataTypeGivenNull() throws PreferencesException {
        // before the initialization of the field allMetadataTypes
        assertFalse(prefs.addMetadataType(null));
        // initialize this field and do the test again
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertFalse(prefs.addMetadataType(null));
    }

    @Test
    public void testAddMetadataTypeGivenUnnamedMetadataType() {
        assertEquals(0, prefs.getAllMetadataTypes().size());
        // unnamed MetadataType objects should not be addable
        MetadataType mdt1 = new MetadataType();
        assertFalse(prefs.addMetadataType(mdt1));
        assertEquals(0, prefs.getAllMetadataTypes().size());
        // otherwise it would cause a NullPointerException when one wants to add another object
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("name");
        assertTrue(prefs.addMetadataType(mdt2));
        assertEquals(1, prefs.getAllMetadataTypes().size());
    }

    @Test
    public void testAddMetadataTypeGivenNamedMetadataType() {
        assertEquals(0, prefs.getAllMetadataTypes().size());
        MetadataType mdt = new MetadataType();
        mdt.setName("mdt");
        mdt.setNum("+");
        assertTrue(prefs.addMetadataType(mdt));
        assertEquals(1, prefs.getAllMetadataTypes().size());
        assertEquals("+", prefs.getMetadataTypeByName("mdt").getNum());

        // modify mdt and try to add it once more
        mdt.setNum("*");
        assertTrue(prefs.addMetadataType(mdt));
        assertEquals(1, prefs.getAllMetadataTypes().size());
        assertEquals("*", prefs.getMetadataTypeByName("mdt").getNum());

        // create a new MetadataType object with the same name 'mdt' and try to add it
        MetadataType mdt2 = new MetadataType();
        mdt2.setName("mdt");
        mdt2.setNum("1o");
        assertTrue(prefs.addMetadataType(mdt2));
        assertEquals(1, prefs.getAllMetadataTypes().size());
        assertEquals("1o", prefs.getMetadataTypeByName("mdt").getNum());
    }

    @Test
    public void testGetMetadataTypeByNameGivenNull() throws PreferencesException {
        // before initialization of the field allMetadataTypes
        assertEquals(0, prefs.getAllMetadataTypes().size());
        assertNull(prefs.getMetadataTypeByName(null));
        assertNull(prefs.getMetadataTypeByName(null, null));
        assertNull(prefs.getMetadataTypeByName(null, "en"));
        assertNull(prefs.getMetadataTypeByName("URN", null));

        // initialize the field and do the tests again
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertTrue(prefs.getAllMetadataTypes().size() > 0);
        assertNull(prefs.getMetadataTypeByName(null));
        assertNull(prefs.getMetadataTypeByName(null, null));
        assertNull(prefs.getMetadataTypeByName(null, "en"));
        assertNotNull(prefs.getMetadataTypeByName("URN", null));
    }

    @Test
    public void testGetMetadataTypeByNameGivenUnmatchingLanguage() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertTrue(prefs.getAllMetadataTypes().size() > 0);
        assertNotNull(prefs.getMetadataTypeByName("Related series", "en"));
        assertNull(prefs.getMetadataTypeByName("Related series", "Klingon"));
        assertNull(prefs.getMetadataTypeByName("Related series", null));
        assertNotNull(prefs.getMetadataTypeByName("Series relacionadas", "es"));
    }

    @Test
    public void testGetMetadataTypeByNameGivenUnexistingName() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertTrue(prefs.getAllMetadataTypes().size() > 0);
        assertNull(prefs.getMetadataTypeByName("Unexisting"));
        assertNull(prefs.getMetadataTypeByName("Unexisting", "en"));
    }

    /* Tests for the method getAllPersonTypes() */
    @Test
    public void testGetAllPersonTypes() throws PreferencesException {
        // even before initialization of the field allMetadataTypes, calling this method will not return null
        assertNotNull(prefs.getAllPersonTypes());

        // initialize this field and test the method again
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertNotNull(prefs.getAllPersonTypes());
        assertTrue(prefs.getAllPersonTypes().size() > 0);
        for (MetadataType mdt : prefs.getAllPersonTypes()) {
            assertTrue(mdt.getIsPerson());
        }
    }

    /* Tests for the following methods:
     * 1. getMetadataGroupTypeByName(String)
     * 2. addMetadataGroup(MetadataGroupType)
     * */
    @Test
    public void testGetMetadataGroupTypeByNameGivenNull() throws PreferencesException {
        // before initialization of the field allMetadataGroupTypes
        assertEquals(0, prefs.getAllMetadataTypes().size());
        assertNull(prefs.getMetadataGroupTypeByName(null));
        // initialize this field and test the method again
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertNotEquals(0, prefs.getAllMetadataTypes().size());
        assertNull(prefs.getMetadataGroupTypeByName(null));
    }

    @Test
    public void testGetMetadataGroupTypeByNameGivenUnexistingName() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        String[] names = new String[] { "Unexisting", "", " ", "not a group" };
        for (String name : names) {
            assertNull(prefs.getMetadataGroupTypeByName(name));
        }
    }

    @Test
    public void testGetMetadataGroupTypeByNameGivenExistingName() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        String[] names = new String[] { "PublisherGroup", "TestGroup", "LocationGroup", "UnusedGroup" };
        for (String name : names) {
            assertNotNull(prefs.getMetadataGroupTypeByName(name));
        }
    }

    @Test
    public void testAddMetadataGroupGivenNull() throws PreferencesException {
        // before initialization of the field allMetadataGroupTypes
        assertFalse(prefs.addMetadataGroup(null));
        // initialize this field and test the method again
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertFalse(prefs.addMetadataGroup(null));
    }

    @Test
    public void testAddMetadataGroupGivenUnnamedMetadataGroupObject() {
        MetadataGroupType mdgt = new MetadataGroupType();
        assertNull(mdgt.getName());
        assertFalse(prefs.addMetadataGroup(mdgt));
    }

    @Test
    public void testAddMetadataGroupGivenNamedMetadataGroupObject() {
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName("mdgt");
        mdgt.setNum("+");
        assertTrue(prefs.addMetadataGroup(mdgt));
        assertEquals("+", prefs.getMetadataGroupTypeByName("mdgt").getNum());

        // modify mdgt a little bit, and try to add it again
        mdgt.setNum("*");
        assertTrue(prefs.addMetadataGroup(mdgt));
        assertEquals("*", prefs.getMetadataGroupTypeByName("mdgt").getNum());

        // create another MetadataGroupType object using the same name and try to add it
        MetadataGroupType mdgt2 = new MetadataGroupType();
        mdgt2.setName("mdgt");
        mdgt2.setNum("1m");
        assertTrue(prefs.addMetadataGroup(mdgt2));
        assertEquals("1m", prefs.getMetadataGroupTypeByName("mdgt").getNum());
    }

    @Test
    public void testAllowAccessRestrictionConfiguration() throws PreferencesException {
        prefs.loadPrefs("src/test/resources/ruleset.xml");
        MetadataType metadataTypeWithoutRestriction = prefs.getMetadataTypeByName("PlaceOfPublication");
        MetadataType personTypeWithoutRestriction = prefs.getMetadataTypeByName("Editor");
        MetadataType corporateTypeWithoutRestriction = prefs.getMetadataTypeByName("CorporateOther");

        MetadataType metadataTypeWithRestriction = prefs.getMetadataTypeByName("PublicationYear");
        MetadataType personTypeWithRestriction = prefs.getMetadataTypeByName("Author");
        MetadataType corporateTypeWithRestriction = prefs.getMetadataTypeByName("Corporation");

        assertFalse(metadataTypeWithoutRestriction.isAllowAccessRestriction());
        assertFalse(personTypeWithoutRestriction.isAllowAccessRestriction());
        assertFalse(corporateTypeWithoutRestriction.isAllowAccessRestriction());
        assertTrue(metadataTypeWithRestriction.isAllowAccessRestriction());
        assertTrue(personTypeWithRestriction.isAllowAccessRestriction());
        assertTrue(corporateTypeWithRestriction.isAllowAccessRestriction());

    }

}
