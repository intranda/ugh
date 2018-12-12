package ugh.fileformats.slimjson;

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
        SlimDocStruct sourceDs = sdd.getDsMap().get(ref.getSource().getIdentifier());
        if (sourceDs == null) {
            sourceDs = SlimDocStruct.fromDocStruct(ref.getSource(), sdd);
            sdd.addSlimDocStruct(sourceDs);
        }
        sr.setSourceDsId(sourceDs.getId());
        SlimDocStruct targetDs = sdd.getDsMap().get(ref.getTarget().getIdentifier());
        if (targetDs == null) {
            targetDs = SlimDocStruct.fromDocStruct(ref.getTarget(), sdd);
            sdd.addSlimDocStruct(targetDs);
        }
        sr.setTargetDsId(targetDs.getId());
        return sr;
    }
}
