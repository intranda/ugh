package ugh.fileformats.slimjson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Person;

@Data
public class SlimMetadataGroup {
    @JsonIgnore
    private transient SlimDigitalDocument digitalDocument;

    protected String mDGroupTypeId;
    // Document structure to which this metadata type belongs to.
    protected String myDocStructId;

    private List<SlimMetadata> metadataList = new ArrayList<>();
    private List<Person> personList = new ArrayList<>();

    public static SlimMetadataGroup fromMetadataGroup(MetadataGroup mg, SlimDigitalDocument sdd) {
        SlimMetadataGroup smg = new SlimMetadataGroup();
        smg.digitalDocument = sdd;
        smg.myDocStructId = mg.getDocStruct().getIdentifier();
        //add metadata
        for (Metadata meta : mg.getMetadataList()) {
            smg.metadataList.add(SlimMetadata.fromMetadata(meta, sdd));
        }
        //add persons
        smg.personList = mg.getPersonList();
        return null;
    }

}
