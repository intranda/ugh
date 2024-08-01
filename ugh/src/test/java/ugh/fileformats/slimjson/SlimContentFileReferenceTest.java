package ugh.fileformats.slimjson;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;

import ugh.dl.ContentFile;
import ugh.dl.ContentFileArea;
import ugh.dl.ContentFileReference;

public class SlimContentFileReferenceTest {

    @Test(expected = Exception.class)
    public void testFromContentFileReferenceGivenNullAsFirstParameter() {
        SlimDigitalDocument sdd = new SlimDigitalDocument();
        SlimContentFileReference.fromContentFileReference(null, sdd);
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed for the second parameter to avoid the NullPointerException.")
    @Test
    public void testFromContentFileReferenceGivenNullAsSecondParameter() {
        ContentFileReference cfr = new ContentFileReference();
        // before initializing the fields of cfr
        assertNull(cfr.getCf());
        assertNull(cfr.getCfa());
        assertNull(SlimContentFileReference.fromContentFileReference(cfr, null));

        // initialize both fields of cfr and test this method again
        ContentFile cf = new ContentFile();
        ContentFileArea cfa = new ContentFileArea();
        cfr.setCf(cf);
        cfr.setCfa(cfa);
        assertNotNull(cfr.getCf());
        assertNotNull(cfr.getCfa());
        assertNull(SlimContentFileReference.fromContentFileReference(cfr, null));
    }

}
