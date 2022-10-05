package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AmdSecTest {

    private static final File xmlFile = new File("src/test/resources/nodeTest.xml");
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder dBuilder;
    private static Document doc;
    private static NodeList nList;
    private static ArrayList<Md> list;

    @BeforeClass
    public static void setUp() throws ParserConfigurationException, SAXException, IOException {
        factory = DocumentBuilderFactory.newInstance();
        dBuilder = factory.newDocumentBuilder();
        doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        nList = doc.getElementsByTagName("node");

        list = new ArrayList<>();
        for (int i = 0; i < nList.getLength(); ++i) {
            Node nNode = nList.item(i);
            list.add(new Md(nNode));
        }
    }

    @Test
    public void testConstructorWithNull() {
        AmdSec tester = new AmdSec(null);
        assertNull(tester.getTechMdList());
    }

    @Test
    public void testContructorWithEmptyList() {
        AmdSec tester = new AmdSec(new ArrayList<Md>());
        assertNotNull(tester.getTechMdList());
        assertEquals(0, tester.getTechMdList().size());
    }

    @Test
    public void testConstructorWithNormalList() {
        AmdSec tester = new AmdSec(list);
        assertEquals(list, tester.getTechMdList());
    }

    @Test
    public void testAddTechMd() {
        AmdSec tester = new AmdSec(list);
        Md edon = new Md(doc.getElementsByTagName("edon").item(0));
        tester.addTechMd(edon);
        assertTrue(tester.getTechMdList().contains(edon));
    }

    @Test
    public void testGetTechMdsAsNodes() {
        ArrayList<Md> tempList = new ArrayList<>();
        for (int i = 0; i < nList.getLength(); ++i) {
            Node nNode = nList.item(i);
            tempList.add(new Md(nNode));
        }
        AmdSec tester = new AmdSec(tempList);
        List<Node> result = tester.getTechMdsAsNodes();
        assertNotNull(result);
        assertEquals(tempList.size(), result.size());
        assertEquals(tempList.get(0).getContent(), result.get(0));
    }

    @Test
    public void testGetTechMdsAsNodesGivenNullTechMdList() {
        AmdSec tester = new AmdSec(null);
        List<Node> result = tester.getTechMdsAsNodes();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetTechMdsAsNodesGivenEmptyTechMdList() {
        AmdSec tester = new AmdSec(new ArrayList<Md>());
        List<Node> result = tester.getTechMdsAsNodes();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

}

