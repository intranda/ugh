package ugh.fileformats.slimjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.fileformats.mets.MetsMods;

public class SlimDigitalDocumentTest {
    private SlimDigitalDocument sdd;

    Path meta = Paths.get("src/test/resources/meta.xml");
    Path ruleset = Paths.get("src/test/resources/ruleset.xml");
    
    @Before
    public void setUpBeforeEach() {
        sdd = new SlimDigitalDocument();
    }

    @Test
    public void testSerializeDigitalDocument() throws PreferencesException, ReadException, JsonProcessingException {
        Prefs prefs = new Prefs();
        prefs.loadPrefs(ruleset.toAbsolutePath().toString());
        
        MetsMods metsDoc = new MetsMods(prefs);
        metsDoc.read(meta.toAbsolutePath().toString());
        
        SlimDigitalDocument slimDoc = SlimDigitalDocument.fromDigitalDocument(metsDoc.getDigitalDocument(), prefs);
        assertFalse(slimDoc.getDsMap().isEmpty());
        
        DigitalDocument convDoc = slimDoc.toDigitalDocument();
        
        assertEquals(convDoc.getLogicalDocStruct().getType(), metsDoc.getDigitalDocument().getLogicalDocStruct().getType());
        
        assertEquals(convDoc.getPhysicalDocStruct().getAllChildren().size(), metsDoc.getDigitalDocument().getPhysicalDocStruct().getAllChildren().size());
    
        DocStruct origMainDoc = metsDoc.getDigitalDocument().getLogicalDocStruct();
        DocStruct convMainDoc = convDoc.getLogicalDocStruct();
        
        assertEquals(origMainDoc.getAllMetadata().size(), convMainDoc.getAllMetadata().size());
        
        for (int i = 0; i < convMainDoc.getAllMetadata().size(); i++) {
            Metadata origMd = origMainDoc.getAllMetadata().get(i);
            Metadata convMd = convMainDoc.getAllMetadata().get(i);
            
            assertEquals(origMd.getType(), convMd.getType());
            assertEquals(origMd.getAuthorityID(), convMd.getAuthorityID());
            assertEquals(origMd.getValue(), convMd.getValue());

        }
        
        for (int i = 0; i < convMainDoc.getAllPersons().size(); i++) {
            Person origMd = origMainDoc.getAllPersons().get(i);
            Person convMd = convMainDoc.getAllPersons().get(i);
            
            assertEquals(origMd.getType(), convMd.getType());
            assertEquals(origMd.getAuthorityID(), convMd.getAuthorityID());
            assertEquals(origMd.getFirstname(), convMd.getFirstname());
            assertEquals(origMd.getLastname(), convMd.getLastname());
            assertEquals(origMd.getDisplayname(), convMd.getDisplayname());

        }
    }

    /* tests for the method addSlimDocStruct */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddSlimDocStructGivenNull() {
        sdd.addSlimDocStruct(null);
    }

    @Test
    public void testAddSlimDocStructGivenExistingObject() {

    }

    @Test
    public void testAddSlimDocStructGivenUnexistingObject() {

    }

    /* tests for the method addMetadataType */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddMetadataTypeGivenNull() {
        sdd.addMetadataType(null);
    }

    @Test
    public void testAddMetadataTypeGivenExistingObject() {

    }

    @Test
    public void testAddMetadataTypeGivenUnexistingObject() {

    }

    /* tests for the method addDsType */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddDsTypeGivenNull() {
        sdd.addDsType(null);
    }

    @Test
    public void testAddDsTypeGivenExistingObject() {

    }

    @Test
    public void testAddDsTypeGivenUnexistingObject() {

    }

    /* tests for the method addMetadataGroupType */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddMetadataGroupTypeGivenNull() {
        sdd.addMetadataGroupType(null);
    }

    @Test
    public void testAddMetadataGroupTypeGivenExistingObject() {

    }

    @Test
    public void testAddMetadataGroupTypeGivenUnexistingObject() {

    }

    /* tests for the method fromDigitalDocument */
    @Test
    public void testFromDigitalDocumentGivenNullAsFirstArgument() {

    }

    @Test
    public void testFromDigitalDocumentGivenNullAsSecondArgument() {

    }

    /* tests for the method toDigitalDocument */

}
