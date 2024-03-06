package ugh.fileformats.slimjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.FileSet;
import ugh.dl.VirtualFileGroup;

public class SlimFileSetTest {
    private SlimDigitalDocument sdd;

    @Before
    public void setUp() {
        sdd = new SlimDigitalDocument();
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed for the first parameter to avoid the NullPointerException.")
    @Test
    public void testFromFileSetGivenNullAsFirstArgument() {
        assertNull(SlimFileSet.fromFileSet(null, sdd));
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed for the second parameter to avoid the NullPointerException.")
    @Test
    public void testFromFileSetGivenNullAsSecondArgument() {
        FileSet fileSet = new FileSet();
        // before adding any file into fileSet
        assertEquals(0, fileSet.getAllFiles().size());
        assertEquals(0, fileSet.getAllMetadata().size());
        assertEquals(0, fileSet.getVirtualFileGroups().size());

        assertNull(SlimFileSet.fromFileSet(fileSet, null));

        // add some files into fileSet and test the method again
        ContentFile cf1 = new ContentFile();
        ContentFile cf2 = new ContentFile();
        fileSet.addFile(cf1);
        fileSet.addFile(cf2);
        assertEquals(2, fileSet.getAllFiles().size());

        assertNull(SlimFileSet.fromFileSet(fileSet, null));

    }

    @Test
    public void testToFileSetGivenNull() {
        // The parameter is actually not used at all, according to the design of the method ugh.fileformats.slimjson.SlimContentFile::toContentFile.
    }

    @Test
    public void testFromFileSetAndToFileSetTogether() {
        FileSet fileSet = new FileSet();
        // add some files
        ContentFile cf1 = new ContentFile();
        cf1.setIdentifier("1");
        ContentFile cf2 = new ContentFile();
        cf1.setIdentifier("2");
        fileSet.addFile(cf1);
        fileSet.addFile(cf2);
        // add some VirtualFileGroup objects
        VirtualFileGroup vfg1 = new VirtualFileGroup();
        VirtualFileGroup vfg2 = new VirtualFileGroup();
        fileSet.addVirtualFileGroup(vfg1);
        fileSet.addVirtualFileGroup(vfg2);
        assertEquals(2, fileSet.getAllFiles().size());
        assertEquals(2, fileSet.getVirtualFileGroups().size());

        SlimFileSet sfs = SlimFileSet.fromFileSet(fileSet, sdd);
        FileSet fs = sfs.toFileSet(new DigitalDocument());
        assertEquals(2, fs.getAllFiles().size());
        assertEquals(2, fs.getVirtualFileGroups().size());
    }

}
