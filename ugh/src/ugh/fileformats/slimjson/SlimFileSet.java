package ugh.fileformats.slimjson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.ContentFile;
import ugh.dl.FileSet;
import ugh.dl.VirtualFileGroup;

@Data
public class SlimFileSet {
    @JsonIgnore
    private transient SlimDigitalDocument digitalDocument;

    private List<String> allImages = new ArrayList<>();
    private List<VirtualFileGroup> virtualFileGroups;

    public static SlimFileSet fromFileSet(FileSet fileSet, SlimDigitalDocument sdd) {
        SlimFileSet sfs = new SlimFileSet();
        sfs.digitalDocument = sdd;
        for (ContentFile cf : fileSet.getAllFiles()) {
            SlimContentFile scf = sdd.getImagesMap().get(cf.getIdentifier());
            if (scf == null) {
                scf = SlimContentFile.fromContentFile(cf, sdd);
            }
            sfs.allImages.add(scf.getIdentifier());
        }
        sfs.virtualFileGroups = fileSet.getVirtualFileGroups();
        return sfs;
    }
}
