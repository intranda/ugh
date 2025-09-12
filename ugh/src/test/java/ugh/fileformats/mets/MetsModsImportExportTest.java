package ugh.fileformats.mets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

import ugh.dl.Prefs;
import ugh.exceptions.UGHException;

public class MetsModsImportExportTest {

    @Test
    public void testSplitRegularExpressions() {
        // null value returns empty list
        String regex = null;
        assertTrue(MetsModsImportExport.splitRegularExpression(regex).isEmpty());
        // empty value returns empty list
        regex = "";
        assertTrue(MetsModsImportExport.splitRegularExpression(regex).isEmpty());

        // complete value returns two parts
        regex = "s/search/replace/g";
        List<String> parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(2, parts.size());
        assertEquals("search", parts.get(0));
        assertEquals("replace", parts.get(1));

        // without s and g modifier
        regex = "/search/replace/";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(2, parts.size());
        assertEquals("search", parts.get(0));
        assertEquals("replace", parts.get(1));

        // escaped slashes in values remain
        regex = "/sea\\/rch/replace/";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(2, parts.size());
        assertEquals("sea\\/rch", parts.get(0));
        assertEquals("replace", parts.get(1));

        // search value without replacement returns only one value
        regex = "/search/";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(1, parts.size());
        assertEquals("search", parts.get(0));

        // value without any sed/perl syntax is returned completely
        regex = "something";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(1, parts.size());
        assertEquals("something", parts.get(0));

        // empty replacement value is returned too
        regex = "/search//";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(2, parts.size());
        assertEquals("search", parts.get(0));
        assertEquals("", parts.get(1));

        // search value special characters
        regex = "/^CC BY-SA$/";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(1, parts.size());
        assertEquals("^CC BY-SA$", parts.get(0));

        regex = "^CC BY-SA$";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(1, parts.size());
        assertEquals("^CC BY-SA$", parts.get(0));

        regex = "m/(Restricted|AK_Noe_intern|AK_Tirol_intern)/";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(1, parts.size());
        assertEquals("(Restricted|AK_Noe_intern|AK_Tirol_intern)", parts.get(0));

    }

    @Test
    public void testExportVideoSections() throws UGHException, JDOMException, IOException {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");

        MetsMods mm = new MetsMods(prefs);
        mm.read("src/test/resources/video.xml");

        MetsModsImportExport exp = new MetsModsImportExport(prefs);
        exp.setDigitalDocument(mm.getDigitalDocument());

        exp.write("src/test/resources/tmp.xml");

        // open file
        Namespace metsNs = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build("src/test/resources/tmp.xml");
        Element mets = doc.getRootElement();

        // metadata was exported
        List<Element> dmdSecs = mets.getChildren("dmdSec", metsNs);
        assertEquals(5, dmdSecs.size());

        // premis block exists
        Element amd = mets.getChild("amdSec", metsNs);
        Element techMd = amd.getChild("techMD", metsNs);
        Element mdWrap = techMd.getChild("mdWrap", metsNs);
        Element xmlData = mdWrap.getChild("xmlData", metsNs);
        assertEquals("premis", xmlData.getChildren().get(0).getNamespacePrefix());

        // filegroup contains a single mp4 file
        Element fileSec = mets.getChild("fileSec", metsNs);
        Element fileGrp = fileSec.getChild("fileGrp", metsNs);
        List<Element> files = fileGrp.getChildren();
        assertEquals(1, files.size());
        assertEquals("video/mp4", files.get(0).getAttributeValue("MIMETYPE"));

        // structMap logical

        List<Element> structMaps = mets.getChildren("structMap", metsNs);
        Element log = structMaps.get(0);
        assertEquals("Video title", log.getChild("div", metsNs).getAttributeValue("LABEL"));

        Element phys = structMaps.get(1);
        Element file = phys.getChild("div", metsNs).getChild("div", metsNs);
        assertEquals("video", file.getAttributeValue("TYPE"));

        Element fptr = file.getChild("fptr", metsNs);
        assertEquals("FILE_0001", fptr.getAttributeValue("FILEID"));
        Element seq = fptr.getChild("seq", metsNs);
        List<Element> areas = seq.getChildren();
        assertEquals(4, areas.size());
        Element area = areas.get(0);

        assertEquals("00:00:00", area.getAttributeValue("BEGIN"));
        assertEquals("00:01:47", area.getAttributeValue("END"));
        assertEquals("TIME", area.getAttributeValue("BETYPE"));
        assertEquals("FILE_0001", area.getAttributeValue("FILEID"));
        assertEquals("PHYS_0002", area.getAttributeValue("ID"));

    }
}
