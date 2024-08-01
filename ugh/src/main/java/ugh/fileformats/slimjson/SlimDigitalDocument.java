package ugh.fileformats.slimjson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;

@Data
public class SlimDigitalDocument {
    //for conversion back
    @JsonIgnore
    private Map<String, DocStruct> origDsMap = new HashMap<>();
    @JsonIgnore
    private Map<String, ContentFile> origContentFileMap = new HashMap<>();

    private Map<String, SlimDocStruct> dsMap = new HashMap<>();
    private Map<String, DocStructType> dsTypeMap = new HashMap<>();
    private Map<String, MetadataType> metadataTypeMap = new HashMap<>();
    private Map<String, MetadataGroupType> metadataGroupTypeMap = new HashMap<>();
    private Map<String, SlimContentFile> imagesMap = new HashMap<>();

    private String topPhysicalStructId;
    private String topLogicalStructId;
    // Contains all files, which are referenced from this digital document (e.g.
    // imagefiles, textfiles etc...).
    private SlimFileSet allImages;

    private SlimAmdSec amdSec;

    public static SlimDigitalDocument fromDigitalDocument(DigitalDocument dd, Prefs prefs) {
        SlimDigitalDocument sdd = new SlimDigitalDocument();
        SlimDocStruct topPhys = SlimDocStruct.fromDocStruct(dd.getPhysicalDocStruct(), sdd);
        SlimDocStruct topLogical = SlimDocStruct.fromDocStruct(dd.getLogicalDocStruct(), sdd);
        sdd.setTopPhysicalStructId(topPhys.getId());
        sdd.setTopLogicalStructId(topLogical.getId());
        sdd.allImages = SlimFileSet.fromFileSet(dd.getFileSet(), sdd);
        sdd.amdSec = SlimAmdSec.fromAmdSec(dd.getAmdSec(), sdd);
        for (DocStructType dst : prefs.getAllDocStructTypes()) {
            if (!sdd.dsTypeMap.containsKey(dst.getName())) {
                sdd.dsTypeMap.put(dst.getName(), dst);
            }
        }
        return sdd;
    }

    public DigitalDocument toDigitalDocument() {
        //first, add all references back to this object
        allImages.setDigitalDocument(this);
        for (SlimDocStruct sds : dsMap.values()) {
            sds.setDigitalDocument(this);
        }
        for (SlimContentFile scf : imagesMap.values()) {
            scf.setDigitalDocument(this);
        }

        DigitalDocument dd = new DigitalDocument();
        if (amdSec != null) {
            dd.setAmdSec(amdSec.toAmdSec());
        }
        dd.setFileSet(allImages.toFileSet(dd));
        dd.setLogicalDocStruct(this.dsMap.get(this.topLogicalStructId).toDocStruct(dd));
        dd.setPhysicalDocStruct(this.dsMap.get(this.topPhysicalStructId).toDocStruct(dd));
        return dd;
    }

    public void addSlimDocStruct(SlimDocStruct sds) {
        if (!this.dsMap.containsKey(sds.getId())) {
            this.dsMap.put(sds.getId(), sds);
        }
    }

    public void addMetadataType(MetadataType type) {
        if (!this.metadataTypeMap.containsKey(type.getName())) {
            this.metadataTypeMap.put(type.getName(), type);
        }

    }

    public void addDsType(DocStructType type) {
        if (!this.dsTypeMap.containsKey(type.getName())) {
            this.dsTypeMap.put(type.getName(), type);
        }
    }

    public void addMetadataGroupType(MetadataGroupType type) {
        if (!this.metadataGroupTypeMap.containsKey(type.getName())) {
            this.metadataGroupTypeMap.put(type.getName(), type);
        }
    }
}
