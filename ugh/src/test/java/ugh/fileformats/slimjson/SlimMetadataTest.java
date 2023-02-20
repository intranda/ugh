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
    @Ignore("The logic in the method cannot pass this test. Null check needed for the first parameter to avoid the NullPointerException.")
    @Test
    public void testFromMetadataGivenNullAsFirstArgument() {
        assertNull(SlimMetadata.fromMetadata(null, sdd));
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed for the second parameter to avoid the NullPointerException.")
    @Test
    public void testFromMetadataGivenNullAsSecondArgument() throws MetadataTypeNotAllowedException {
        assertNull(SlimMetadata.fromMetadata(md, null));
    }

    @Test
    public void testFromMetadataWithoutIdentifier() throws MetadataTypeNotAllowedException {
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
        md.setAutorityFile("id", "uri", "value");
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
        md.setAutorityFile(authorityId, authorityUri, authorityValue);
        mdg.setIdentifier(id);
        md.setParent(mdg);
        assertEquals(id, md.getParent().getIdentifier());
        assertEquals("mdt", md.getType().getName());

        SlimMetadata smd = SlimMetadata.fromMetadata(md, sdd);
        Metadata md2 = smd.toMetadata(new DigitalDocument());

    }

}
