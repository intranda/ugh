package ugh.fileformats.slimjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

import ugh.dl.AmdSec;
import ugh.dl.Md;
import ugh.dl.Md.MdType;

public class SlimAmdSecTest {
    private static final File xmlFile = new File("src/test/resources/nodeTest.xml");
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder dBuilder;
    private static Document doc;
    private static NodeList nList;
    private static ArrayList<Md> mdList = new ArrayList<>();
    private SlimDigitalDocument sdd;
    private AmdSec sec;
    private SlimAmdSec slimSec;

    @BeforeClass
    public static void setUpBeforeAll() throws ParserConfigurationException, SAXException, IOException {
        factory = DocumentBuilderFactory.newInstance();
        dBuilder = factory.newDocumentBuilder();
        doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        nList = doc.getElementsByTagName("node");
        for (int i = 0; i < nList.getLength(); ++i) {
            Node nNode = nList.item(i);
            mdList.add(new Md(nNode, MdType.TECH_MD));
        }
    }

    @Before
    public void setUpBeforeEach() {
        sdd = new SlimDigitalDocument();
        sec = new AmdSec(mdList);
    }

    @Test
    public void testFromAmdSecGivenNullAsFirstArgument() {
        assertNull(SlimAmdSec.fromAmdSec(null, sdd));
    }

    @Test
    public void testFromAmdSecGivenNullAsSecondArgument() {
        assertNotNull(SlimAmdSec.fromAmdSec(sec, null));
    }

    @Test
    public void testFromAmdSecGivenAmdSecWithoutAnId() {
        assertNull(sec.getId());
        slimSec = SlimAmdSec.fromAmdSec(sec, sdd);
        assertNotNull(sec.getId());
        assertEquals(sec.getId(), slimSec.getId());
    }

    @Test
    public void testFromAmdSecGivenAmdWithAnId() {
        String id = "amdsec-id";
        sec.setId(id);
        slimSec = SlimAmdSec.fromAmdSec(sec, sdd);
        assertEquals(id, slimSec.getId());

        // check contents
        assertEquals(slimSec.getTechMdList().size(), sec.getTechMdList().size());
    }

    @Test
    public void testFromAmdSecToAmdSecTogether() {
        slimSec = SlimAmdSec.fromAmdSec(sec, sdd);

        AmdSec sec2 = slimSec.toAmdSec();
        // check contents
        assertEquals(sec2.getTechMdList().size(), slimSec.getTechMdList().size());
        // check id
    }

}
