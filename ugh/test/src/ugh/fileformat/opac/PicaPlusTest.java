package ugh.fileformat.opac;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.fileformats.opac.PicaPlus;

public class PicaPlusTest {


    @Test
    public void testReadPrefs() throws Exception {
        Prefs prefs = new Prefs();

        prefs.loadPrefs("test/resources/ruleset.xml");

        PicaPlus fixture = new PicaPlus(prefs);
        assertNotNull (fixture);
    }

    @Test
    public void testReadPicaFromNode() throws Exception {
        Prefs prefs = new Prefs();

        prefs.loadPrefs("test/resources/ruleset.xml");

        PicaPlus fixture = new PicaPlus(prefs);
        assertNotNull (fixture);
        File fXmlFile = new File("test/resources/pica.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        Node node = doc.getDocumentElement();
        fixture.read(node);

        DigitalDocument digDoc = fixture.getDigitalDocument();
        assertNotNull(digDoc);
        DocStruct logical = digDoc.getLogicalDocStruct();
        assertEquals("Monograph", logical.getType().getName());

        Metadata m = logical.getAllMetadata().get(0);;
        Corporate c = logical.getAllCorporates().get(0);
        Person p = logical.getAllPersons().get(0);

        assertEquals("CatalogIDDigital", m.getType().getName());
        assertEquals("1733284826", m.getValue());

        assertEquals("Corporation",c.getType().getName());
        assertEquals("Georg-August-Universität Göttingen", c.getMainName());
        assertEquals("Grad-verleihende Institution", c.getSubNames().get(0).getValue());
        assertEquals("Göttingen; 2014", c.getPartName());
        assertEquals("2024315-7", c.getAuthorityValue());

        assertEquals("Anja", p.getFirstname());
        assertEquals("Weingart", p.getLastname());
        assertEquals("Author", p.getRole());

    }

    @Test
    public void testReadPicaFromFile() throws Exception {
        Prefs prefs = new Prefs();

        prefs.loadPrefs("test/resources/ruleset.xml");

        PicaPlus fixture = new PicaPlus(prefs);
        assertNotNull (fixture);
        fixture.read("test/resources/pica.xml");

        DigitalDocument digDoc = fixture.getDigitalDocument();
        assertNotNull(digDoc);
        DocStruct logical = digDoc.getLogicalDocStruct();
        assertEquals("Monograph", logical.getType().getName());

        Metadata m = logical.getAllMetadata().get(0);;
        Corporate c = logical.getAllCorporates().get(0);
        Person p = logical.getAllPersons().get(0);

        assertEquals("CatalogIDDigital", m.getType().getName());
        assertEquals("1733284826", m.getValue());

        assertEquals("Corporation",c.getType().getName());
        assertEquals("Georg-August-Universität Göttingen", c.getMainName());
        assertEquals("Grad-verleihende Institution", c.getSubNames().get(0).getValue());
        assertEquals("Göttingen; 2014", c.getPartName());
        assertEquals("2024315-7", c.getAuthorityValue());

        assertEquals("Anja", p.getFirstname());
        assertEquals("Weingart", p.getLastname());
        assertEquals("Author", p.getRole());

    }
}
