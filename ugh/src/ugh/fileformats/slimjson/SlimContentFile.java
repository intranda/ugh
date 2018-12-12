package ugh.fileformats.slimjson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.ContentFile;

@Data
public class SlimContentFile {
    @JsonIgnore
    private transient SlimDigitalDocument digitalDocument;

    private List<String> referencedDocStructs;
    private String location;
    private String mimeType;
    private String identifier;

    private boolean isRepresentative = false;

    public static SlimContentFile fromContentFile(ContentFile cf, SlimDigitalDocument sdd) {
        SlimContentFile scf = new SlimContentFile();
        scf.location = cf.getLocation();
        scf.mimeType = cf.getMimetype();
        scf.identifier = cf.getIdentifier();
        sdd.getImagesMap().put(scf.identifier, scf);
        return scf;
    }

}
