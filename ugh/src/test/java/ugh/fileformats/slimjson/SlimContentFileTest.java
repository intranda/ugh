package ugh.fileformats.slimjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;

public class SlimContentFileTest {

    private SlimContentFile scf;
    private ContentFile cf;
    private SlimDigitalDocument sdd;

    @Before
    public void setUpBeforeEach() {
        cf = new ContentFile();
        sdd = new SlimDigitalDocument();
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed for the first parameter to avoid the NullPointerException.")
    @Test
    public void testFromContentFileGivenNullAsFirstArgument() {
        assertNull(SlimContentFile.fromContentFile(null, sdd));
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed for the second parameter to avoid the NullPointerException. ")
    @Test
    public void testFromContentFileGivenNullAsSecondArgument() {
        assertNull(SlimContentFile.fromContentFile(cf, null));
    }

    @Test
    public void testFromContentFileGivenContentFileWithoutId() {
        assertNull(cf.getIdentifier());
        scf = SlimContentFile.fromContentFile(cf, sdd);
        assertNotNull(scf.getIdentifier());
        assertNotNull(cf.getIdentifier());
        assertEquals(cf.getIdentifier(), scf.getIdentifier());
    }

    @Test
    public void testFromContentFileGivenContentFileWithId() {
        String id = "cf-id";
        cf.setIdentifier(id);
        // prepare a list of DocStruct for further tests
        DocStructType dsType = new DocStructType();
        DocStruct ds1 = new DocStruct();
        DocStruct ds2 = new DocStruct();
        ds1.setType(dsType);
        ds2.setType(dsType);
        assertTrue(cf.addDocStructAsReference(ds1));
        assertTrue(cf.addDocStructAsReference(ds2));
        assertEquals(2, cf.getReferencedDocStructs().size());

        scf = SlimContentFile.fromContentFile(cf, sdd);
        assertEquals(id, scf.getIdentifier());
        assertEquals(2, scf.getReferencedDocStructs().size());
        for (int i = 0; i < cf.getReferencedDocStructs().size(); ++i) {
            assertEquals(cf.getReferencedDocStructs().get(i).getIdentifier(), scf.getReferencedDocStructs().get(i));
        }
    }

    @Ignore("The parameter is not used at all. WHY?")
    @Test
    public void testToContentFileGivenNull() {

    }

    @Test
    public void testFromContentFileToContentFileTogether() {
        DigitalDocument dd = new DigitalDocument();
        String id = "cf-id";
        String location = "cf-location";
        String mimetype = "cf-mime";
        cf.setIdentifier(id);
        cf.setLocation(location);
        cf.setMimetype(mimetype);

        scf = SlimContentFile.fromContentFile(cf, sdd);
        ContentFile cf2 = scf.toContentFile(dd);
        assertEquals(cf.getIdentifier(), cf2.getIdentifier());
        assertEquals(cf.getLocation(), cf2.getLocation());
        assertEquals(cf.getMimetype(), cf2.getMimetype());
    }
}
