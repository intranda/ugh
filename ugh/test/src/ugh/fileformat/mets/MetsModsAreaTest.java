package ugh.fileformat.mets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.fileformats.mets.MetsMods;

public class MetsModsAreaTest {

    @Test
    public void testReadPhysicalSectionInMetsFile() {
        Prefs prefs = new Prefs();
        try {
            prefs.loadPrefs("test/resources/ruleset.xml");
        } catch (PreferencesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MetsMods mm = null;
        try {
            mm = new MetsMods(prefs);
            mm.read("test/resources/meta.xml");
        } catch (PreferencesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(mm);
        DocStruct boundBook = mm.getDigitalDocument().getPhysicalDocStruct();
        DocStruct firstPage = boundBook.getAllChildren().get(0);
        assertEquals(firstPage.getDocsttructType(), "div");
        DocStruct firstArea = firstPage.getAllChildren().get(0);
        assertEquals(firstArea.getDocsttructType(), "area");
        for (Metadata md : firstArea.getAllMetadata()) {
            if (md.getType().getName().equals("_urn")) {
                assertEquals(md.getValue(), "some urn");
            }else  if (md.getType().getName().equals("_COORDS")) {
                assertEquals(md.getValue(), "1,2,3,4");
            } else  if (md.getType().getName().equals("_SHAPE")) {
                assertEquals(md.getValue(), "RECT");
            } else {
                fail("Found unexpected metadata type :" + md.getType().getName());
            }
        }
    }


    @Test
    public void testWritePhysicalDocstruct() throws Exception {
        Prefs prefs = new Prefs();
        try {
            prefs.loadPrefs("test/resources/ruleset.xml");
        } catch (PreferencesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MetsMods mm = null;
        try {
            mm = new MetsMods(prefs);
            mm.read("test/resources/meta.xml");
        } catch (PreferencesException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DocStruct boundBook = mm.getDigitalDocument().getPhysicalDocStruct();

        DocStruct secondPage = boundBook.getAllChildren().get(1);
        DocStruct area = mm.getDigitalDocument().createDocStruct(prefs.getDocStrctTypeByName("area"));
        area.setDocsttructType("area");

        secondPage.addChild(area);

        Metadata md1 = new Metadata(prefs.getMetadataTypeByName("_COORDS"));
        md1.setValue("coordinates");
        Metadata md2 = new Metadata(prefs.getMetadataTypeByName("_SHAPE"));
        md2.setValue("RECT");
        area.addMetadata(md1);
        area.addMetadata(md2);
        // save, load, compare
        mm.write("test/resources/tmp.xml");

        MetsMods mm2= new MetsMods(prefs);
        mm2.read("test/resources/tmp.xml");
        DocStruct boundBook2 = mm2.getDigitalDocument().getPhysicalDocStruct();
        DocStruct pageFromMets = boundBook2.getAllChildren().get(1);
        DocStruct areaFromMets = pageFromMets.getAllChildren().get(0);

        Metadata coords = areaFromMets.getAllMetadataByType(prefs.getMetadataTypeByName("_COORDS")).get(0);
        Metadata shape = areaFromMets.getAllMetadataByType(prefs.getMetadataTypeByName("_SHAPE")).get(0);
        assertEquals(md1.getValue(), coords.getValue());
        assertEquals(md2.getValue(), shape.getValue());

    }
}
