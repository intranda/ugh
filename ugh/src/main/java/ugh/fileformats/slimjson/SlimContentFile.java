package ugh.fileformats.slimjson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;

@Data
public class SlimContentFile {
    @JsonIgnore
    // This `transient` is required for GSON to work properly (circular references would lead to StackOverflow)
    private transient SlimDigitalDocument digitalDocument;

    private List<String> referencedDocStructs = new ArrayList<>();
    private String location;
    private String mimeType;
    private String identifier;

    private boolean isRepresentative = false;

    public static SlimContentFile fromContentFile(ContentFile cf, SlimDigitalDocument sdd) {
        SlimContentFile scf = new SlimContentFile();
        scf.digitalDocument = sdd;
        scf.location = cf.getLocation();
        scf.mimeType = cf.getMimetype();
        if (cf.getIdentifier() != null) {
            scf.identifier = cf.getIdentifier();
        } else {
            scf.identifier = UUID.randomUUID().toString();
            cf.setIdentifier(scf.identifier);
        }
        sdd.getImagesMap().put(scf.identifier, scf);
        if (cf.getReferencedDocStructs() != null) {
            for (DocStruct ds : cf.getReferencedDocStructs()) {
                if (ds.getIdentifier() == null) {
                    ds.setIdentifier(UUID.randomUUID().toString());
                }
                scf.referencedDocStructs.add(ds.getIdentifier());
            }
        }
        return scf;
    }

    public ContentFile toContentFile(DigitalDocument dd) {
        ContentFile cf = digitalDocument.getOrigContentFileMap().get(this.identifier);
        if (cf == null) {
            cf = new ContentFile();
            cf.setLocation(location);
            cf.setMimetype(mimeType);
            cf.setIdentifier(identifier);

            digitalDocument.getOrigContentFileMap().put(this.identifier, cf);
        }
        return cf;
    }

}
