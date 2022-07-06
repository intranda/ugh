package ugh.dl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ugh.exceptions.PreferencesException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;

public class DigitalDocumentTest {

    private Prefs prefs;

    private Fileformat fileformat;
    
    @Before
    public void setUp() throws Exception {
        prefs = new Prefs();
        prefs.loadPrefs("src/test/resources/ruleset.xml");

        fileformat = new MetsMods(prefs);
        fileformat.read("src/test/resources/meta.xml");

    }
    
    @Test
    public void testCopy() throws WriteException, PreferencesException {
        fileformat.getDigitalDocument().copyDigitalDocument();
    }

}
