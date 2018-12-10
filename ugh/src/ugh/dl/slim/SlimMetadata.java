package ugh.dl.slim;

import lombok.Data;

@Data
public class SlimMetadata {
    private transient SlimDocStruct rootDs;

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
}
