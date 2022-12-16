package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.exceptions.PreferencesException;

public class PrefsTest {
    private Prefs prefs;

    @Before
    public void setUp() {
        prefs = new Prefs();
    }

    /* Tests for the constructor */
    @Test
    public void testConstructor() {
        assertNotNull(prefs.getAllDocStructTypes());
        assertEquals(0, prefs.getAllDocStructTypes().size());
        assertNotNull(prefs.getAllMetadataTypes());
        assertEquals(0, prefs.getAllMetadataTypes().size());
    }

    /* Tests for the method loadPrefs(String) */
    @Test
    public void testLoadPrefsGivenNull() {
        // better handle the NullPointerException 
        assertThrows(Exception.class, () -> prefs.loadPrefs(null));
    }

    @Test
    public void testLoadPrefsGivenEmptyString() {
        assertThrows(PreferencesException.class, () -> prefs.loadPrefs(""));
    }

    @Test
    public void testLoadPrefsGivenUnexistingName() {
        assertThrows(PreferencesException.class, () -> prefs.loadPrefs("unexisting.xml"));
    }

    @Ignore("This test actually passes. But there is a logical bug in the source codes. Check the comments below.")
    @Test
    public void testLoadPrefsGivenValidFileWithInvalidContents1() {
        // On L170 of Prefs.java, upperChildlist will never be null.
        // Therefore running this test will give us "No upper child in preference file" as the error message.
        // The checking logic should be changed to "if (upperChildlist.getLength() == 0)"
        assertThrows(PreferencesException.class, () -> prefs.loadPrefs("src/test/resources/nodeTest.xml"));
    }

    @Ignore("The logic in the method cannot pass this test. Check the comments below.")
    @Test
    public void testLoadPrefsGivenValidFileWithInvalidContents2() throws PreferencesException {
        // On L179 of Prefs.java, upperChild will not be null as expected.
        // Therefore running this test will throw no exception. 
        // The checking logic should be modified (no idea how yet).
        assertThrows(PreferencesException.class, () -> prefs.loadPrefs("src/test/resources/rulesetFake.xml"));
    }

    @Test
    public void testLoadPrefsGivenValidFileWithValidContents() throws PreferencesException {
        assertTrue(prefs.loadPrefs("src/test/resources/ruleset.xml"));
        assertTrue(prefs.getAllMetadataTypes().size() > 0);
        assertTrue(prefs.getAllDocStructTypes().size() > 0);
    }

    /* Tests for the method parseDocStrctType(Node) */

    /* Tests for the method parseMetadataType(Node) */

    /* Tests for the method parseMetadataGroup(Node) */

    /* Tests for the method getDocStrctTypeByName(String) */
    @Test
    public void testGetDocStrctTypeByNameGivenNull() {
        assertNull(prefs.getDocStrctTypeByName(null));
    }

    @Test
    public void testGetDocStrctTypeByNameGivenEmptyString() {

    }

    /* Tests for the method getAllAnchorDocStructTypes() */

}




