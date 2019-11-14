package ugh.fileformat.mets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.dl.VirtualFileGroup;
import ugh.fileformats.mets.MetsMods;

public class MetsModsAreaTest {

    @Test
    public void testReadPhysicalSectionInMetsFile() throws Exception {
        Prefs prefs = new Prefs();

        prefs.loadPrefs("test/resources/ruleset.xml");

        MetsMods mm = new MetsMods(prefs);
        mm.read("test/resources/meta.xml");

        assertNotNull(mm);
        DocStruct boundBook = mm.getDigitalDocument().getPhysicalDocStruct();
        DocStruct firstPage = boundBook.getAllChildren().get(0);
        assertEquals(firstPage.getDocstructType(), "div");
        DocStruct firstArea = firstPage.getAllChildren().get(0);
        assertEquals(firstArea.getDocstructType(), "area");
        for (Metadata md : firstArea.getAllMetadata()) {
            if (md.getType().getName().equals("_urn")) {
                assertEquals(md.getValue(), "some urn");
            } else if (md.getType().getName().equals("_COORDS")) {
                assertEquals(md.getValue(), "1,2,3,4");
            } else if (md.getType().getName().equals("_SHAPE")) {
                assertEquals(md.getValue(), "RECT");
            } else {
                fail("Found unexpected metadata type :" + md.getType().getName());
            }
        }
    }

    @Test
    public void testWritePhysicalDocstruct() throws Exception {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("test/resources/ruleset.xml");

        MetsMods mm = new MetsMods(prefs);
        mm.read("test/resources/meta.xml");

        DocStruct boundBook = mm.getDigitalDocument().getPhysicalDocStruct();

        DocStruct secondPage = boundBook.getAllChildren().get(1);
        DocStruct area = mm.getDigitalDocument().createDocStruct(prefs.getDocStrctTypeByName("area"));
        area.setDocstructType("area");

        secondPage.addChild(area);

        Metadata md1 = new Metadata(prefs.getMetadataTypeByName("_COORDS"));
        md1.setValue("coordinates");
        Metadata md2 = new Metadata(prefs.getMetadataTypeByName("_SHAPE"));
        md2.setValue("RECT");
        area.addMetadata(md1);
        area.addMetadata(md2);
        // save, load, compare
        mm.write("test/resources/tmp.xml");

        MetsMods mm2 = new MetsMods(prefs);
        mm2.read("test/resources/tmp.xml");
        DocStruct boundBook2 = mm2.getDigitalDocument().getPhysicalDocStruct();
        DocStruct pageFromMets = boundBook2.getAllChildren().get(1);
        DocStruct areaFromMets = pageFromMets.getAllChildren().get(0);

        Metadata coords = areaFromMets.getAllMetadataByType(prefs.getMetadataTypeByName("_COORDS")).get(0);
        Metadata shape = areaFromMets.getAllMetadataByType(prefs.getMetadataTypeByName("_SHAPE")).get(0);
        assertEquals(md1.getValue(), coords.getValue());
        assertEquals(md2.getValue(), shape.getValue());
    }

    @Test
    public void testMapLogicalToArea() throws Exception {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("test/resources/ruleset.xml");
        MetsMods mm = null;
        mm = new MetsMods(prefs);
        mm.read("test/resources/meta.xml");

        DocStruct boundBook = mm.getDigitalDocument().getPhysicalDocStruct();

        DocStruct monograph = mm.getDigitalDocument().getLogicalDocStruct();
        DocStruct chapter = mm.getDigitalDocument().createDocStruct(prefs.getDocStrctTypeByName("Chapter"));
        Metadata title = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        title.setValue("main title");
        chapter.addMetadata(title);
        monograph.addChild(chapter);
        DocStruct secondPage = boundBook.getAllChildren().get(1);
        DocStruct area = mm.getDigitalDocument().createDocStruct(prefs.getDocStrctTypeByName("area"));
        area.setDocstructType("area");

        secondPage.addChild(area);
        chapter.addReferenceTo(area, "logical_physical");

        Metadata md1 = new Metadata(prefs.getMetadataTypeByName("_COORDS"));
        md1.setValue("coordinates");
        Metadata md2 = new Metadata(prefs.getMetadataTypeByName("_SHAPE"));
        md2.setValue("RECT");
        area.addMetadata(md1);
        area.addMetadata(md2);
        // save, load, compare
        mm.write("test/resources/tmp2.xml");

        MetsMods mm2 = new MetsMods(prefs);
        mm2.read("test/resources/tmp2.xml");
        DocStruct chapterFromMets = mm2.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(1);
        assertEquals("main title", chapterFromMets.getAllMetadata().get(0).getValue());
        DocStruct linkedArea = chapterFromMets.getAllToReferences().get(0).getTarget();
        assertEquals("coordinates", linkedArea.getAllMetadataByType(prefs.getMetadataTypeByName("_COORDS")).get(0).getValue());

    }

    @Test
    public void testGetPhysicalStructureAsFlatList() throws Exception {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("test/resources/ruleset.xml");
        MetsMods mm = null;
        mm = new MetsMods(prefs);
        mm.read("test/resources/meta.xml");

        DocStruct boundBook = mm.getDigitalDocument().getPhysicalDocStruct();
        List<DocStruct> phys = boundBook.getAllChildrenAsFlatList();

        assertEquals("page", phys.get(0).getType().getName());
        assertEquals("area", phys.get(1).getType().getName());
        assertEquals("area", phys.get(2).getType().getName());
        assertEquals("page", phys.get(3).getType().getName());
    }


    @Test
    public void testWriteSeveralFileGroups() throws Exception {
        Prefs prefs = new Prefs();
        prefs.loadPrefs("test/resources/ruleset.xml");

        MetsMods mm = new MetsMods(prefs);
        mm.read("test/resources/meta.xml");

        DocStruct boundBook = mm.getDigitalDocument().getPhysicalDocStruct();

        DocStruct secondPage = boundBook.getAllChildren().get(1);
        DocStruct area = mm.getDigitalDocument().createDocStruct(prefs.getDocStrctTypeByName("area"));
        area.setDocstructType("area");

        secondPage.addChild(area);

        Metadata md1 = new Metadata(prefs.getMetadataTypeByName("_COORDS"));
        md1.setValue("coordinates");
        Metadata md2 = new Metadata(prefs.getMetadataTypeByName("_SHAPE"));
        md2.setValue("RECT");
        area.addMetadata(md1);
        area.addMetadata(md2);

        VirtualFileGroup vfg1 = new VirtualFileGroup();
        vfg1.setFileSuffix("jpg");
        vfg1.setIdSuffix("iiif");
        vfg1.setMainGroup(false);
        vfg1.setMimetype("image/jpeg");
        vfg1.setName("iiif");
        vfg1.setPathToFiles("https://example.com/viewer/");

        mm.getDigitalDocument().getFileSet().addVirtualFileGroup(vfg1);

        VirtualFileGroup vfg2 = new VirtualFileGroup();
        vfg2.setFileSuffix("tif");
        vfg2.setIdSuffix("main");
        vfg2.setMainGroup(true);
        vfg2.setMimetype("image/tiff");
        vfg2.setName("main");
        vfg2.setPathToFiles("file:///tmp/");
        mm.getDigitalDocument().getFileSet().addVirtualFileGroup(vfg2);
        mm.setWriteLocal(false);

        // save, load, compare
        mm.write("test/resources/tmp3.xml");

        MetsMods mm2 = new MetsMods(prefs);
        mm2.read("test/resources/tmp3.xml");
        DocStruct boundBook2 = mm2.getDigitalDocument().getPhysicalDocStruct();
        DocStruct pageFromMets = boundBook2.getAllChildren().get(1);
        DocStruct areaFromMets = pageFromMets.getAllChildren().get(0);

        Metadata coords = areaFromMets.getAllMetadataByType(prefs.getMetadataTypeByName("_COORDS")).get(0);
        Metadata shape = areaFromMets.getAllMetadataByType(prefs.getMetadataTypeByName("_SHAPE")).get(0);
        assertEquals(md1.getValue(), coords.getValue());
        assertEquals(md2.getValue(), shape.getValue());
    }
}
