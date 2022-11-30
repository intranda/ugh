package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MdTest {

    private static final File xmlFile = new File("src/test/resources/nodeTest.xml");
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder dBuilder;
    private static Document doc;
    private static NodeList nList;

    @BeforeClass
    public static void setUp() throws ParserConfigurationException, SAXException, IOException {
        factory = DocumentBuilderFactory.newInstance();
        dBuilder = factory.newDocumentBuilder();
        doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        nList = doc.getElementsByTagName("node");
    }

    @Test
    public void testMdConstructorAndGetterSetter() {
        for (int i = 0; i < nList.getLength(); ++i) {
            Node nNode = nList.item(i);
            Md tester = new Md(nNode);
            assertEquals(nNode, tester.getContent());
            assertNull(tester.getId());
            assertNull(tester.getType());
            tester.setId(String.valueOf(i));
            assertEquals(String.valueOf(i), tester.getId());
            tester.setType("text");
            assertEquals("text", tester.getType());
        }
    }


}
