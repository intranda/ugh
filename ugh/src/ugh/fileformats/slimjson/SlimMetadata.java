package ugh.fileformats.slimjson;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.Metadata;

@Data
public class SlimMetadata {
    @JsonIgnore
    private transient SlimDigitalDocument digitalDocument;

    private String MDTypeId;
    // Document structure to which this metadata type belongs to.
    private String myDocStructId;

    private String metadataValue;
    private String MetadataVQ;
    private String MetadataVQType;

    private String authorityURI;
    private String authorityID;
    private String authorityValue;

    private boolean updated = false;

    public static SlimMetadata fromMetadata(Metadata meta, SlimDigitalDocument sdd) {
        SlimMetadata sm = new SlimMetadata();
        sm.digitalDocument = sdd;
        sdd.addMetadataType(meta.getType());
        sm.MDTypeId = meta.getType().getName();
        if (meta.getDocStruct().getIdentifier() == null) {
            meta.getDocStruct().setIdentifier(UUID.randomUUID().toString());
        }
        sm.myDocStructId = meta.getDocStruct().getIdentifier();

        sm.metadataValue = meta.getValue();
        sm.MetadataVQ = meta.getValueQualifier();
        sm.MetadataVQType = meta.getValueQualifierType();

        sm.authorityURI = meta.getAuthorityURI();
        sm.authorityID = meta.getAuthorityID();
        sm.authorityValue = meta.getAuthorityValue();

        sm.updated = meta.wasUpdated();
        return sm;
    }
}
