package ugh.fileformats.slimjson;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStructType;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;

@Data
public class SlimDigitalDocument {
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

    public static SlimDigitalDocument fromDigitalDocument(DigitalDocument dd) {
        SlimDigitalDocument sdd = new SlimDigitalDocument();
        SlimDocStruct topPhys = SlimDocStruct.fromDocStruct(dd.getPhysicalDocStruct(), sdd);
        SlimDocStruct topLogical = SlimDocStruct.fromDocStruct(dd.getLogicalDocStruct(), sdd);
        sdd.setTopPhysicalStructId(topPhys.getId());
        sdd.setTopLogicalStructId(topLogical.getId());
        sdd.allImages = SlimFileSet.fromFileSet(dd.getFileSet(), sdd);
        return sdd;
    }

    public DigitalDocument toDigitalDocument() {
        return null;
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
}
