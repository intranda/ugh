package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class VirtualFileGroupTest {

    private VirtualFileGroup vfg;

    @Before
    public void setUp() {
        vfg = new VirtualFileGroup();
    }

    /* Tests for constructors */
    @Test
    public void testConstructorWithNoParameter() {
        assertEquals("", vfg.getName());
        assertEquals("", vfg.getPathToFiles());
        assertEquals("", vfg.getMimetype());
        assertEquals("", vfg.getFileSuffix());
        assertEquals("", vfg.getIdSuffix());
        assertNotNull(vfg.getContentFiles());
        assertEquals(0, vfg.getContentFiles().size());
        assertFalse(vfg.isMainGroup());
        assertFalse(vfg.isIgnoreConfiguredMimetypeAndSuffix());
        assertNull(vfg.getFileExtensionsToIgnore());
    }

    @Test
    public void testConstructorWithParametersIncludingNull() {
        vfg = new VirtualFileGroup("name", null, "mimetype", "fileSuffix");
        assertNotNull(vfg.getPathToFiles());
    }

    /* Tests for setters */
    @Test
    public void testSetNameGivenNull() {
        assertNotNull(vfg.getName());
        vfg.setName(null);
        assertNotNull(vfg.getName());
    }

    @Test
    public void testSetPathToFilesGivenNull() {
        assertNotNull(vfg.getPathToFiles());
        vfg.setPathToFiles(null);
        assertNotNull(vfg.getPathToFiles());
    }

    @Test
    public void testSetMimetypeGivenNull() {
        assertNotNull(vfg.getMimetype());
        vfg.setMimetype(null);
        assertNotNull(vfg.getMimetype());
    }

    @Test
    public void testSetFileSuffixGivenNull() {
        assertNotNull(vfg.getFileSuffix());
        vfg.setFileSuffix(null);
        assertNotNull(vfg.getFileSuffix());
    }

    @Test
    public void testSetFileSuffixGivenStringWithoutHeadingPoint() {
        String suffix = "suffix";
        vfg.setFileSuffix(suffix);
        assertEquals("suffix", vfg.getFileSuffix());
    }

    @Test
    public void testSetFileSuffixGivenStringHeadedByOnePoint() {
        String suffix = ".suffix";
        vfg.setFileSuffix(suffix);
        assertEquals("suffix", vfg.getFileSuffix());
    }

    @Test
    public void testSetFileSuffixGivenStringHeadedByTwoPoints() {
        // I don't think this is needed. Removing the first dot from file extension is useful, because some might
        // enter 'tif' and others '.tif' as extension. But '..tif' is clearly a wrong user input and can be fixed by the user. - Robert
        String suffix = "..suffix";
        vfg.setFileSuffix(suffix);
        assertEquals("suffix", vfg.getFileSuffix());
    }

    @Test
    public void testSetIdSuffixGivenNull() {
        assertNotNull(vfg.getIdSuffix());
        vfg.setIdSuffix(null);
        assertNotNull(vfg.getIdSuffix());
    }

    /* Tests for the following methods:
     * allowAllFiles()
     * restrictFiles()
     * addContentFile(ContentFile)
     * addContentFiles(Collection<ContentFile>)
     * removeContentFile(ContentFile)
     * removeContentFiles(Collection<ContentFile>)
     * contains(ContentFile)
     */
    @Test
    public void testAllowAllFilesAndRestrictFiles() {
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.restrictFiles();
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.allowAllFiles();
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
    }

    @Test
    public void testAddContentFileGivenNull1() {
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.addContentFile(null);
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
    }

    @Test
    public void testAddContentFileGivenNull2() {
        vfg.restrictFiles();
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        assertEquals(0, vfg.getContentFiles().size());
        vfg.addContentFile(null);
        assertEquals(0, vfg.getContentFiles().size());
    }

    @Test
    public void testAddContentFilesGivenNull1() {
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.addContentFiles(null);
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
    }

    @Test
    public void testAddContentFilesGivenNull2() {
        vfg.restrictFiles();
        assertEquals(0, vfg.getContentFiles().size());
        vfg.addContentFiles(null);
        assertEquals(0, vfg.getContentFiles().size());
    }

    @Test
    public void testAddContentFilesGivenNormalInput() {
        ContentFile cf1 = new ContentFile();
        ContentFile cf2 = new ContentFile();
        List<ContentFile> files = new ArrayList<>();
        files.add(cf1);
        files.add(cf2);
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.addContentFiles(files);
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        assertEquals(2, vfg.getContentFiles().size());
    }

    @Test
    public void testRemoveContentFileGivenNull() {
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.removeContentFile(null);
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.restrictFiles();
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.removeContentFile(null);
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
    }

    @Test
    public void testRemoveContentFileGivenUnexistingObject() {
        ContentFile cf = new ContentFile();
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.removeContentFile(cf);
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.restrictFiles();
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.removeContentFile(cf);
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
    }

    @Test
    public void testRemoveContentFileGivenEquivalentObject() {
        ContentFile cf1 = new ContentFile();
        ContentFile cf2 = new ContentFile();
        assertTrue(cf1.equals(cf2));
        assertNotEquals(cf1, cf2);
        vfg.addContentFile(cf1);
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        assertEquals(1, vfg.getContentFiles().size());
        vfg.removeContentFile(cf2);
        assertEquals(1, vfg.getContentFiles().size()); // the method will remove exactly the same object in the sense of address, i.e. == is used to compare instead of equals()
    }

    @Test
    public void testRemoveContentFileGivenSameObject() {
        ContentFile cf = new ContentFile();
        vfg.addContentFile(cf);
        assertEquals(1, vfg.getContentFiles().size());
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.removeContentFile(cf);
        assertEquals(0, vfg.getContentFiles().size());
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
    }

    @Test
    public void testRemoveContentFilesGivenNull() {
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.removeContentFiles(null);
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.restrictFiles();
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        vfg.removeContentFile(null);
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
    }

    @Test
    public void testRemoveContentFilesGivenMixedCollection() {
        ContentFile cf1 = new ContentFile();
        ContentFile cf2 = new ContentFile();
        ContentFile cf3 = new ContentFile();
        List<ContentFile> files12 = new ArrayList<>();
        List<ContentFile> files23 = new ArrayList<>();
        files12.add(cf1);
        files12.add(cf2);
        files23.add(cf2);
        files23.add(cf3);
        vfg.addContentFiles(files12);
        assertEquals(2, vfg.getContentFiles().size());
        assertTrue(vfg.contains(cf1));
        assertTrue(vfg.contains(cf2));
        vfg.removeContentFiles(files23);
        assertEquals(1, vfg.getContentFiles().size());
        assertTrue(vfg.contains(cf1));
        assertFalse(vfg.contains(cf2));
    }

    @Test
    public void testContainsGivenNullWhenAllAllowed() {
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        assertFalse(vfg.contains(null));
    }

    @Test
    public void testContainsGivenNullWhenRestricted() {
        vfg.restrictFiles();
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        assertFalse(vfg.contains(null));
    }

    @Test
    public void testContainsGivenAnyContentFile() {
        ContentFile cf = new ContentFile();
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        assertTrue(vfg.contains(cf));
        vfg.restrictFiles();
        assertNotSame(vfg.ALL_FILES, vfg.getContentFiles());
        assertFalse(vfg.contains(cf));
        vfg.allowAllFiles();
        assertSame(vfg.ALL_FILES, vfg.getContentFiles());
        assertTrue(vfg.contains(cf));
    }

}


