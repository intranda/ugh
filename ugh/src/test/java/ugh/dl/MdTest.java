package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ugh.dl.Md.MdType;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;

public class MdTest {

    private static final File xmlFile = new File("src/test/resources/nodeTest.xml");
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder dBuilder;
    private static Document doc;
    private static NodeList nList;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

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
            Md tester = new Md(nNode, MdType.TECH_MD);
            assertNull(tester.getId());
            tester.setId(String.valueOf(i));
            assertEquals(String.valueOf(i), tester.getId());
            assertEquals("techMD", tester.getType().toString());
        }
    }

    @Test
    public void testJdomConversion() throws JDOMException {
        Element rootElement = getJdomContent("text");
        Md md = new Md(rootElement, MdType.SOURCE_MD);
        Node convertedContent = md.getContent();
        assertEquals("mets:techMD", convertedContent.getNodeName());

    }

    private Element getJdomContent(String value) {
        Namespace ns = Namespace.getNamespace("example", "https://example.org/namespace");
        Element rootElement = new Element("root", ns);
        Element sub = new Element("sub", ns);
        sub.setText(value);
        rootElement.addContent(sub);
        return rootElement;
    }

    @Test
    public void testIdGeneration() {
        Node nNode = nList.item(0);
        assertEquals("node", nNode.getNodeName());
        Md md = new Md(nNode, MdType.RIGHTS_MD);
        // id is not set
        assertNull(md.getId());
        // id is set by hand
        md.setId("id1");
        assertEquals("id1", md.getId());
        md.generateId();
        // id is still manual set
        assertEquals("id1", md.getId());

        // now id is not set, automatic generation works
        md.setId("");
        md.generateId();
        assertTrue(StringUtils.isNotBlank(md.getId()));
        assertTrue(md.getId().startsWith("AMD_"));
    }

    @Test
    public void testOverwriteContent() throws JDOMException {
        Node nNode = nList.item(0);
        Md md = new Md(nNode, MdType.DIGIPROV_MD);
        assertEquals("mets:techMD", md.getContent().getNodeName());
        assertTrue(md.getContent().getTextContent().trim().startsWith("11"));
        // now overwrite the content
        md.setContent(getJdomContent("text"));
        // it still has a wrapper element
        assertEquals("mets:techMD", md.getContent().getNodeName());
        // but different content
        assertTrue(md.getContent().getTextContent().trim().startsWith("text"));
    }

    @Test
    public void exportTest() throws Exception {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");

        MetsMods mm = new MetsMods(prefs);
        mm.read("src/test/resources/meta.xml");
        DocStruct boundBook = mm.getDigitalDocument().getPhysicalDocStruct();

        int counter = 1;
        for (DocStruct page : boundBook.getAllChildren()) {
            Md md = new Md(getJdomContent("page content " + counter++), MdType.TECH_MD);
            md.generateId();
            mm.getDigitalDocument().addTechMd(md);
            page.setAdmId(md.getId());
        }

        // save file
        File exportFile = folder.newFile();
        mm.write(exportFile.toString());

        // load file again, MD sections are still available
        mm.read(exportFile.toString());
        boundBook = mm.getDigitalDocument().getPhysicalDocStruct();
        counter = 1;
        for (DocStruct page : boundBook.getAllChildren()) {
            String id = page.getAdmId();
            Md md = mm.getDigitalDocument().getTechMd(id);
            assertEquals("page content " + counter++, md.getContent().getTextContent());
        }
    }

    @Test
    public void testExportMets() throws Exception {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");

        MetsMods mm = new MetsMods(prefs);
        mm.read("src/test/resources/meta.xml");
        DocStruct boundBook = mm.getDigitalDocument().getPhysicalDocStruct();

        int counter = 1;
        for (DocStruct page : boundBook.getAllChildren()) {
            Md md = new Md(getJdomContent("page content " + counter++), MdType.TECH_MD);
            md.generateId();
            mm.getDigitalDocument().addTechMd(md);
            page.setAdmId(md.getId());
        }

        // save file
        File exportFile = folder.newFile();

        MetsModsImportExport mmio = new MetsModsImportExport(prefs);
        mmio.setDigitalDocument(mm.getDigitalDocument());
        mmio.write(exportFile.toString());

        Namespace mets = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
        Namespace mods = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
        SAXBuilder builder = new SAXBuilder();
        org.jdom2.Document doc = builder.build(exportFile);

        XPathFactory xpathFactory = XPathFactory.instance();
        XPathExpression<Element> expr = xpathFactory.compile("//mets:amdSec/mets:techMD",
                Filters.element(), null, mets, mods);
        List<Element> elements = expr.evaluate(doc);
        assertEquals(18, elements.size());

        Element techMD = elements.get(17);
        assertEquals("techMD", techMD.getName());
        Element mdWrap = techMD.getChildren().get(0);
        assertEquals("mdWrap", mdWrap.getName());
        Element xmlData = mdWrap.getChildren().get(0);
        assertEquals("xmlData", xmlData.getName());
        String textcontent = xmlData.getChildren().get(0).getChildren().get(0).getText();
        assertEquals("page content 18", textcontent);

    }
}
