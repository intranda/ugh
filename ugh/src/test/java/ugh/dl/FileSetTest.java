package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class FileSetTest {

    private FileSet fs;

    @Before
    public void setUp() {
        fs = new FileSet();
    }

    /* Tests for the constructor */
    @Test
    public void testConstructor() {
        assertNotNull(fs.getAllFiles());
        assertNotNull(fs.getAllMetadata());
        assertNotNull(fs.getVirtualFileGroups());
        // There is still no way to fetch the field removedMetadata.
    }

    /* Tests for the method addFile(ContentFile) */
    @Test
    public void testAddFileGivenNull() {
        assertEquals(0, fs.getAllFiles().size());
        assertTrue(fs.addFile(null));
        assertEquals(0, fs.getAllFiles().size());
    }

    @Test
    public void testAddFileGivenSameFileTwice() {
        assertEquals(0, fs.getAllFiles().size());
        ContentFile cf = new ContentFile();
        fs.addFile(cf);
        assertEquals(1, fs.getAllFiles().size());
        fs.addFile(cf);
        assertEquals(1, fs.getAllFiles().size());
    }

    /* Tests for the method removeFile(ContentFile) */
    @Test
    public void testRemoveFileGivenNull() {
        assertTrue(fs.removeFile(null));
        fs.addFile(new ContentFile());
        assertTrue(fs.removeFile(null));
    }

    @Test
    public void testRemoveFileGivenUnexistingFile() {
        ContentFile cf1 = new ContentFile();
        cf1.setIdentifier("Id1");
        ContentFile cf2 = new ContentFile();
        cf1.setIdentifier("Id2");
        fs.addFile(cf1);
        assertEquals(1, fs.getAllFiles().size());
        fs.removeFile(cf2);
        assertEquals(1, fs.getAllFiles().size());
    }

    @Test
    public void testRemoveFileGivenExistingFile() {
        ContentFile cf = new ContentFile();
        fs.addFile(cf);
        assertEquals(1, fs.getAllFiles().size());
        fs.removeFile(cf);
        assertEquals(0, fs.getAllFiles().size());
    }

    /* Tests for the method addMetadata(Metadata) */
    @Test
    public void testAddMetadataGivenNull() {
        assertTrue(fs.addMetadata(null));
        assertEquals(0, fs.getAllMetadata().size());
    }

    @Test
    public void testAddMetadataGivenSameOneTwice() throws MetadataTypeNotAllowedException {
        MetadataType mdType = new MetadataType();
        Metadata md = new Metadata(mdType);
        assertTrue(fs.addMetadata(md));
        assertEquals(1, fs.getAllMetadata().size());
        assertTrue(fs.addMetadata(md));
        assertEquals(2, fs.getAllMetadata().size());
    }

    /* Tests for the method removeMetadata(Metadata) */
    @Test
    public void testRemoveMetadataGivenNull() throws MetadataTypeNotAllowedException {
        assertTrue(fs.removeMetadata(null));
        fs.addMetadata(new Metadata(new MetadataType()));
        assertTrue(fs.removeMetadata(null));
    }

    @Test
    public void testRemoveMetadataGivenExistingObject() throws MetadataTypeNotAllowedException {
        MetadataType mdType = new MetadataType();
        Metadata md = new Metadata(mdType);
        fs.addMetadata(md);
        assertTrue(fs.getAllMetadata().contains(md));
        fs.removeMetadata(md);
        assertFalse(fs.getAllMetadata().contains(md));
    }

    /* Tests for the method addVirtualFileGroup(VirtualFileGroup) */
    @Test(expected = IllegalArgumentException.class)
    public void testAddVirtualFileGroupGivenNull() {
        fs.addVirtualFileGroup(null);
    }

    @Test
    public void testAddVirtualFileGroupGivenSameObjectTwice() {
        VirtualFileGroup vfg = new VirtualFileGroup();
        fs.addVirtualFileGroup(vfg);
        assertEquals(1, fs.getVirtualFileGroups().size());
        fs.addVirtualFileGroup(vfg);
        assertEquals(2, fs.getVirtualFileGroups().size());
    }

    /* Tests for the method removeVirtualFileGroup(VirtualFileGroup) */
    @Test
    public void testRemoveVirtualFileGroupGivenNull() {
        VirtualFileGroup vfg = new VirtualFileGroup();
        fs.addVirtualFileGroup(vfg);
        assertEquals(1, fs.getVirtualFileGroups().size());
        fs.removeVirtualFileGroup(null);
        assertEquals(1, fs.getVirtualFileGroups().size());
    }

    @Test
    public void testRemoveVirtualFileGroupGivenNormalInput() {
        VirtualFileGroup vfg1 = new VirtualFileGroup();
        VirtualFileGroup vfg2 = new VirtualFileGroup();
        fs.addVirtualFileGroup(vfg1);
        assertTrue(fs.getVirtualFileGroups().contains(vfg1));
        assertFalse(fs.getVirtualFileGroups().contains(vfg2));
        fs.removeVirtualFileGroup(vfg2);
        assertTrue(fs.getVirtualFileGroups().contains(vfg1));
        fs.removeVirtualFileGroup(vfg1);
        assertFalse(fs.getVirtualFileGroups().contains(vfg1));
    }

}
