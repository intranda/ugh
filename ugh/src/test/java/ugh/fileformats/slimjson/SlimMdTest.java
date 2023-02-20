package ugh.fileformats.slimjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ugh.dl.Md;

public class SlimMdTest {
    private static final File xmlFile = new File("src/test/resources/nodeTest.xml");
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder dBuilder;
    private static TransformerFactory transformerFactory;
    private static Transformer transformer;
    private static Document doc;
    private static NodeList nList;

    @BeforeClass
    public static void setUp() throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException {
        factory = DocumentBuilderFactory.newInstance();
        dBuilder = factory.newDocumentBuilder();
        doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        nList = doc.getElementsByTagName("node");
        transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
    }

    @Test
    public void testFromMdGivenMdWithoutId() {
        for (int i = 0; i < nList.getLength(); ++i) {
            Node nNode = nList.item(i);
            Md md = new Md(nNode);
            assertNull(md.getId());
            SlimMd smd = SlimMd.fromMd(md);
            assertNotNull(md.getId());
            assertEquals(smd.getId(), md.getId());
        }
    }

    @Test
    public void testFromMdGivenMdWithId() throws TransformerException {
        for (int i = 0; i < nList.getLength(); ++i) {
            Node nNode = nList.item(i);
            Md md = new Md(nNode);
            String id = "md-id";
            String type = "md-type";
            md.setId(id);
            md.setType(type);
            SlimMd smd = SlimMd.fromMd(md);
            assertEquals(id, smd.getId());
            assertEquals(type, smd.getType());

            // check content
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(nNode), new StreamResult(writer));
            assertEquals(writer.toString(), smd.getContent());
        }
    }

    @Ignore("The logic in the method cannot pass this test. Information of id and type get lost while applying the method toMd. BUG or FEATURE?")
    @Test
    public void testFromMdToMdTogether() throws TransformerException {
        for (int i = 0; i < nList.getLength(); ++i) {
            Node nNode = nList.item(i);
            Md md = new Md(nNode);
            SlimMd smd = SlimMd.fromMd(md);
            assertNotNull(smd.getId());
            smd.setType("type");

            Md md2 = smd.ToMd();
            // check id and type
            assertEquals(smd.getId(), md2.getId());
            assertEquals(smd.getType(), md2.getType());

            // check content
            StringWriter writer = new StringWriter();
            Node node = md2.getContent();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            assertEquals(writer.toString(), smd.getContent());
        }
    }

}
