package ugh.fileformats.slimjson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Person;
import ugh.exceptions.MetadataTypeNotAllowedException;

@Data
@Log4j2
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
        smg.mDGroupTypeId = mg.getType().getName();
        sdd.addMetadataGroupType(mg.getType());
        if (mg.getParent().getIdentifier() == null) {
            mg.getParent().setIdentifier(UUID.randomUUID().toString());
        }
        smg.myDocStructId = mg.getParent().getIdentifier();
        //add metadata
        for (Metadata meta : mg.getMetadataList()) {
            smg.metadataList.add(SlimMetadata.fromMetadata(meta, sdd));
        }
        //add persons
        smg.personList = mg.getPersonList();
        return smg;
    }

    public MetadataGroup toMetadataGroup(DigitalDocument dd) {
        try {
            MetadataGroup mg = new MetadataGroup(digitalDocument.getMetadataGroupTypeMap().get(this.mDGroupTypeId));
            DocStruct ds = digitalDocument.getOrigDsMap().get(this.myDocStructId);
            if (ds == null) {
                ds = digitalDocument.getDsMap().get(this.myDocStructId).toDocStruct(dd);
            }
            mg.setParent(ds);
            //add metadata
            for (SlimMetadata meta : this.getMetadataList()) {
                mg.getMetadataList().add(meta.toMetadata(dd));
            }
            //add persons
            mg.setPersonList(this.personList);
            return mg;
        } catch (MetadataTypeNotAllowedException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
        return null;
    }

}
