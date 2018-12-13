package ugh.fileformats.slimjson;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.Reference;

@Data
public class SlimReference {
    @JsonIgnore
    private transient SlimDigitalDocument digitalDocument;

    private String type;
    private String sourceDsId;
    private String targetDsId;

    public static SlimReference fromReference(Reference ref, SlimDigitalDocument sdd) {
        SlimReference sr = new SlimReference();
        sr.digitalDocument = sdd;
        sr.type = ref.getType();
        if (ref.getSource().getIdentifier() == null) {
            ref.getSource().setIdentifier(UUID.randomUUID().toString());
        }
        sr.sourceDsId = ref.getSource().getIdentifier();
        if (ref.getTarget().getIdentifier() == null) {
            ref.getTarget().setIdentifier(UUID.randomUUID().toString());
        }
        sr.targetDsId = ref.getTarget().getIdentifier();
        return sr;
    }
}
