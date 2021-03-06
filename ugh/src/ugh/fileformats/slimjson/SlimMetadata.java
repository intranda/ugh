package ugh.fileformats.slimjson;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.extern.log4j.Log4j;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.exceptions.MetadataTypeNotAllowedException;

@Data
@Log4j
public class SlimMetadata {
    @JsonIgnore
    private transient SlimDigitalDocument digitalDocument;

    private String mdTypeId;
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
        sm.mdTypeId = meta.getType().getName();
        if (meta.getParent().getIdentifier() == null) {
            meta.getParent().setIdentifier(UUID.randomUUID().toString());
        }
        sm.myDocStructId = meta.getParent().getIdentifier();

        sm.metadataValue = meta.getValue();
        sm.MetadataVQ = meta.getValueQualifier();
        sm.MetadataVQType = meta.getValueQualifierType();

        sm.authorityURI = meta.getAuthorityURI();
        sm.authorityID = meta.getAuthorityID();
        sm.authorityValue = meta.getAuthorityValue();

        sm.updated = meta.wasUpdated();
        return sm;
    }

    public Metadata toMetadata(DigitalDocument dd) {
        Metadata sm;
        try {
            sm = new Metadata(digitalDocument.getMetadataTypeMap().get(this.mdTypeId));
            DocStruct ds = digitalDocument.getOrigDsMap().get(this.myDocStructId);
            if (ds == null) {
                ds = digitalDocument.getDsMap().get(this.myDocStructId).toDocStruct(dd);
            }
            sm.setParent(ds);

            sm.setValue(this.metadataValue);
            sm.setValueQualifier(this.MetadataVQ, this.MetadataVQType);

            sm.setAuthorityID(this.authorityID);
            sm.setAuthorityURI(this.authorityURI);
            sm.setAuthorityValue(this.authorityValue);

            sm.wasUpdated(this.updated);
            return sm;
        } catch (MetadataTypeNotAllowedException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
        return null;
    }
}
