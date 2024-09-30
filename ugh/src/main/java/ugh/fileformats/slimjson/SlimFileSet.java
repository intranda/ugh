package ugh.fileformats.slimjson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.FileSet;
import ugh.dl.VirtualFileGroup;

@Data
public class SlimFileSet {
    @JsonIgnore
    // This `transient` is required for GSON to work properly (circular references would lead to StackOverflow)
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

    public FileSet toFileSet(DigitalDocument dd) {
        FileSet fs = new FileSet();
        for (String imId : allImages) {
            fs.addFile(digitalDocument.getImagesMap().get(imId).toContentFile(dd));
        }
        for (VirtualFileGroup theFilegroup : virtualFileGroups) {
            fs.addVirtualFileGroup(theFilegroup);
        }
        return fs;
    }
}
