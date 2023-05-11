package ugh.fileformats.slimjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import ugh.dl.AmdSec;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.FileSet;
import ugh.dl.Md;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
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
        // SlimDocStruct objects are distinguished by their ids
        SlimDocStruct sds = new SlimDocStruct();
        String id = "id";
        sds.setId(id);
        assertEquals(0, sdd.getDsMap().size());
        sdd.addSlimDocStruct(sds);
        assertEquals(1, sdd.getDsMap().size());
        sdd.addSlimDocStruct(sds);
        assertEquals(1, sdd.getDsMap().size());
        assertSame(sds, sdd.getDsMap().get(id));
    }

    @Test
    public void testAddSlimDocStructGivenUnexistingObject() {
        SlimDocStruct sds1 = new SlimDocStruct();
        SlimDocStruct sds2 = new SlimDocStruct();
        String id1 = "1";
        String id2 = "2";
        sds1.setId(id1);
        sds2.setId(id2);
        assertEquals(0, sdd.getDsMap().size());
        sdd.addSlimDocStruct(sds1);
        assertEquals(1, sdd.getDsMap().size());
        sdd.addSlimDocStruct(sds2);
        assertEquals(2, sdd.getDsMap().size());
        assertSame(sds1, sdd.getDsMap().get(id1));
        assertNotSame(sds1, sdd.getDsMap().get(id2));
        assertSame(sds2, sdd.getDsMap().get(id2));
    }

    /* tests for the method addMetadataType */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddMetadataTypeGivenNull() {
        sdd.addMetadataType(null);
    }

    @Test
    public void testAddMetadataTypeGivenExistingObject() {
        // MetadataType objects are distinguished by their names
        MetadataType mdt = new MetadataType();
        String name = "name";
        mdt.setName(name);
        assertEquals(0, sdd.getMetadataTypeMap().size());
        sdd.addMetadataType(mdt);
        assertEquals(1, sdd.getMetadataTypeMap().size());
        sdd.addMetadataType(mdt);
        assertEquals(1, sdd.getMetadataTypeMap().size());
        assertSame(mdt, sdd.getMetadataTypeMap().get(name));
    }

    @Test
    public void testAddMetadataTypeGivenUnexistingObject() {
        String name1 = "1";
        String name2 = "2";
        MetadataType mdt1 = new MetadataType();
        MetadataType mdt2 = new MetadataType();
        mdt1.setName(name1);
        mdt2.setName(name2);
        assertEquals(0, sdd.getMetadataTypeMap().size());
        sdd.addMetadataType(mdt1);
        assertEquals(1, sdd.getMetadataTypeMap().size());
        sdd.addMetadataType(mdt2);
        assertEquals(2, sdd.getMetadataTypeMap().size());
        assertSame(mdt1, sdd.getMetadataTypeMap().get(name1));
        assertNotSame(mdt1, sdd.getMetadataTypeMap().get(name2));
        assertSame(mdt2, sdd.getMetadataTypeMap().get(name2));
    }

    /* tests for the method addDsType */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddDsTypeGivenNull() {
        sdd.addDsType(null);
    }

    @Test
    public void testAddDsTypeGivenExistingObject() {
        // DocStructType objects are distinguished by their names
        String name = "dst";
        DocStructType dst = new DocStructType();
        dst.setName(name);
        assertEquals(0, sdd.getDsTypeMap().size());
        sdd.addDsType(dst);
        assertEquals(1, sdd.getDsTypeMap().size());
        sdd.addDsType(dst);
        assertEquals(1, sdd.getDsTypeMap().size());
        assertSame(dst, sdd.getDsTypeMap().get(name));
    }

    @Test
    public void testAddDsTypeGivenUnexistingObject() {
        String name1 = "1";
        String name2 = "2";
        DocStructType dst1 = new DocStructType();
        dst1.setName(name1);
        DocStructType dst2 = new DocStructType();
        dst2.setName(name2);
        assertEquals(0, sdd.getDsTypeMap().size());
        sdd.addDsType(dst1);
        assertEquals(1, sdd.getDsTypeMap().size());
        sdd.addDsType(dst2);
        assertEquals(2, sdd.getDsTypeMap().size());
        assertSame(dst1, sdd.getDsTypeMap().get(name1));
        assertNotSame(dst1, sdd.getDsTypeMap().get(name2));
        assertSame(dst2, sdd.getDsTypeMap().get(name2));
    }

    /* tests for the method addMetadataGroupType */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddMetadataGroupTypeGivenNull() {
        sdd.addMetadataGroupType(null);
    }

    @Test
    public void testAddMetadataGroupTypeGivenExistingObject() {
        // MetadataGroupType objects are distinguished by their names
        String name = "name";
        MetadataGroupType mdgt = new MetadataGroupType();
        mdgt.setName(name);
        assertEquals(0, sdd.getMetadataGroupTypeMap().size());
        sdd.addMetadataGroupType(mdgt);
        assertEquals(1, sdd.getMetadataGroupTypeMap().size());
        sdd.addMetadataGroupType(mdgt);
        assertEquals(1, sdd.getMetadataGroupTypeMap().size());
        assertSame(mdgt, sdd.getMetadataGroupTypeMap().get(name));
    }

    @Test
    public void testAddMetadataGroupTypeGivenUnexistingObject() {
        String name1 = "1";
        String name2 = "2";
        MetadataGroupType mdgt1 = new MetadataGroupType();
        MetadataGroupType mdgt2 = new MetadataGroupType();
        mdgt1.setName(name1);
        mdgt2.setName(name2);
        assertEquals(0, sdd.getMetadataGroupTypeMap().size());
        sdd.addMetadataGroupType(mdgt1);
        assertEquals(1, sdd.getMetadataGroupTypeMap().size());
        sdd.addMetadataGroupType(mdgt2);
        assertEquals(2, sdd.getMetadataGroupTypeMap().size());
        assertSame(mdgt1, sdd.getMetadataGroupTypeMap().get(name1));
        assertNotSame(mdgt1, sdd.getMetadataGroupTypeMap().get(name2));
        assertSame(mdgt2, sdd.getMetadataGroupTypeMap().get(name2));
    }

    /* tests for the method fromDigitalDocument */
    @Ignore("The logic in the method cannot pass this test. Null check needed for the first argument to avoid the NullPointerException.")
    @Test
    public void testFromDigitalDocumentGivenNullAsFirstArgument() {
        Prefs prefs = new Prefs();
        assertNotNull(prefs.getAllDocStructTypes());
        SlimDigitalDocument.fromDigitalDocument(null, prefs);
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed for the second argument to avoid the NullPointerException.")
    @Test
    public void testFromDigitalDocumentGivenNullAsSecondArgument() {
        DigitalDocument dd = prepareDigitalDocument();

        assertNotNull(dd.getPhysicalDocStruct());
        assertNotNull(dd.getLogicalDocStruct());
        assertNotNull(dd.getAmdSec());
        assertNotNull(dd.getFileSet());

        SlimDigitalDocument.fromDigitalDocument(dd, null);
    }

    @Test
    public void testFromDigitalDocumentGivenValidInput() {
        // prepare DigitalDocument
        DigitalDocument dd = prepareDigitalDocument();

        assertNotNull(dd.getPhysicalDocStruct());
        assertNotNull(dd.getLogicalDocStruct());
        assertNotNull(dd.getAmdSec());
        assertNotNull(dd.getFileSet());

        // prepare Prefs
        Prefs prefs = new Prefs();
        assertNotNull(prefs.getAllDocStructTypes());

        // test the method
        sdd = SlimDigitalDocument.fromDigitalDocument(dd, prefs);
        assertNotNull(sdd);
    }

    /* tests for the method toDigitalDocument */
    @Test
    public void testToDigitalDocument() {
        // prepare DigitalDocument
        DigitalDocument dd = prepareDigitalDocument();

        // prepare Prefs
        Prefs prefs = new Prefs();

        // prepare SlimDigitalDocument
        sdd = SlimDigitalDocument.fromDigitalDocument(dd, prefs);

        String logicalId = sdd.getTopLogicalStructId();
        String physicalId = sdd.getTopPhysicalStructId();

        DigitalDocument dd2 = sdd.toDigitalDocument();
        assertNotNull(dd2);

        // ids of DocStruct objects should be equal
        DocStruct dsLogical = dd2.getLogicalDocStruct();
        DocStruct dsPhysical = dd2.getPhysicalDocStruct();
        assertEquals(logicalId, dsLogical.getIdentifier());
        assertEquals(physicalId, dsPhysical.getIdentifier());

        // ids of AmdSec objects are NOT equal, FEATURE?
        SlimAmdSec slimAmdSec = sdd.getAmdSec();
        String sasId = slimAmdSec.getId();
        AmdSec amdSec = dd2.getAmdSec();
        String asId = amdSec.getId();
        assertNotEquals(sasId, asId);

        // lengths of Md lists should be equal
        List<SlimMd> smdList = slimAmdSec.getTechMdList();
        List<Md> mdList = amdSec.getTechMdList();
        assertEquals(smdList.size(), mdList.size());

        // lists of allImages should be equal in sizes
        SlimFileSet slimFileSet = sdd.getAllImages();
        FileSet fileSet = dd2.getFileSet();
        assertEquals(slimFileSet.getAllImages().size(), fileSet.getAllFiles().size());
    }

    /* private functions needed in the tests */
    private DigitalDocument prepareDigitalDocument() {
        DigitalDocument dd = new DigitalDocument();
        DocStructType dstLogical = new DocStructType();
        dstLogical.setName("logical");
        DocStructType dstPhysical = new DocStructType();
        dstPhysical.setName("physical");
        DocStruct dsLogical = new DocStruct();
        dsLogical.setType(dstLogical);
        DocStruct dsPhysical = new DocStruct();
        dsPhysical.setType(dstPhysical);
        dd.setLogicalDocStruct(dsLogical);
        dd.setPhysicalDocStruct(dsPhysical);

        ArrayList<Md> techMdList = new ArrayList<>();
        AmdSec amdSec = new AmdSec(techMdList);
        dd.setAmdSec(amdSec);

        FileSet fileSet = new FileSet();
        dd.setFileSet(fileSet);

        return dd;
    }

}
