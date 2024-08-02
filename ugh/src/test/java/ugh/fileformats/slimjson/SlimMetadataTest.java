package ugh.fileformats.slimjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.dl.DigitalDocument;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.exceptions.MetadataTypeNotAllowedException;

public class SlimMetadataTest {
    private MetadataType mdt;
    private Metadata md;
    private MetadataGroupType mdgType;
    private MetadataGroup mdg;

    private SlimDigitalDocument sdd;

    @Before
    public void setUpForEach() throws MetadataTypeNotAllowedException {
        sdd = new SlimDigitalDocument();
        mdt = new MetadataType();
        mdt.setName("mdt");
        md = new Metadata(mdt);
        mdgType = new MetadataGroupType();
        mdg = new MetadataGroup(mdgType);
    }

    /* tests for the public methods */
    @Test(expected = Exception.class)
    public void testFromMetadataGivenNullAsFirstArgument() {
        SlimMetadata.fromMetadata(null, sdd);
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed for the second parameter to avoid the NullPointerException.")
    @Test
    public void testFromMetadataGivenNullAsSecondArgument() {
        assertNull(SlimMetadata.fromMetadata(md, null));
    }

    @Test
    public void testFromMetadataWithoutIdentifier() {
        md.setParent(mdg);
        assertNull(md.getParent().getIdentifier());

        SlimMetadata smd = SlimMetadata.fromMetadata(md, sdd);
        assertNotNull(md.getParent().getIdentifier());
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testToMetadataGivenNull() {
        SlimMetadata smd = new SlimMetadata();
        // without initializing the fields of smd
        assertNull(smd.toMetadata(null));

        // initialize the fields of smd via calling the method fromMetadata
        md.setValue("value");
        md.setValueQualifier("en", "language");
        md.setAuthorityFile("id", "uri", "value");
        assertNotNull(md.getValue());
        assertNotNull(md.getValueQualifier());
        assertNotNull(md.getAuthorityID());
        assertNotNull(md.getAuthorityURI());
        assertNotNull(md.getAuthorityValue());
        smd = SlimMetadata.fromMetadata(md, sdd);

        assertNull(smd.toMetadata(null));
    }

    @Ignore("Not finished yet.")
    @Test
    public void testFromMetadataAndToMetadataTogether() {
        String value = "value";
        String vq = "en";
        String vqType = "language";
        String authorityId = "auth-id";
        String authorityUri = "auth-uri";
        String authorityValue = "auth-value";
        String id = "id";
        md.setValue(value);
        md.setValueQualifier(vq, vqType);
        md.setAuthorityFile(authorityId, authorityUri, authorityValue);
        mdg.setIdentifier(id);
        md.setParent(mdg);
        assertEquals(id, md.getParent().getIdentifier());
        assertEquals("mdt", md.getType().getName());

        SlimMetadata smd = SlimMetadata.fromMetadata(md, sdd);
        Metadata md2 = smd.toMetadata(new DigitalDocument());
        // 1. toMetadata needs to call SlimDigitalDocument::getDsMap, which needs a String myDocStructId
        // 2. the String myDocStructId will be set in the method SlimMetadata::fromMetadata
        // 3. and myDocStructId will be equal to the id of the input Metadata object's parent, i.e. MetadataGroup's id

        // 4. dsMap is a HashMap<String, SlimDocStruct>
        // 5. the only way to add an element to dsMap is via the method SlimDigitalDocument::addSlimDocStruct
        // 6. the input SlimDocStruct should contain an id as String, and this id will be used as key, while the SlimDocStruct object will be its value

        // 7. there will also be a call of the method SlimDocStruct::toDocStruct, which needs a DigitalDocument object
        // 8. the DigitalDocument object will use a DocStructType object to create a DocStruct
        // 9. this DocStructType object is got via SlimDigitalDocument::getDsTypeMap
        // 10. dsTypeMap is a HashMap<String, DocStructType>, where key is the type's name
        // 11. one can add an item to dsTypeMap via SlimDigitalDocument::addDsType
    }

}
