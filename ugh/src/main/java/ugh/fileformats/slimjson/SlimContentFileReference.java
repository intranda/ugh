package ugh.fileformats.slimjson;

import lombok.Data;
import ugh.dl.ContentFileArea;
import ugh.dl.ContentFileReference;

@Data
public class SlimContentFileReference {
    private ContentFileArea area;
    private SlimContentFile file;

    public static SlimContentFileReference fromContentFileReference(ContentFileReference cfr, SlimDigitalDocument sdd) {
        SlimContentFileReference scfr = new SlimContentFileReference();
        scfr.area = cfr.getCfa();
        SlimContentFile scf = sdd.getImagesMap().get(cfr.getCf().getIdentifier());
        if (scf == null) {
            scf = SlimContentFile.fromContentFile(cfr.getCf(), sdd);
        }
        scfr.setFile(scf);
        return scfr;
    }
}
