package ugh.fileformats.slimjson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import ugh.dl.AmdSec;
import ugh.dl.Md;

@Data
public class SlimAmdSec {
    private String id;
    private List<SlimMd> techMdList = new ArrayList<>();
    private boolean tempId;

    public static SlimAmdSec fromAmdSec(AmdSec amd, SlimDigitalDocument sdd) {
        if (amd == null) {
            return null;
        }
        SlimAmdSec samd = new SlimAmdSec();
        if (amd.getId() != null) {
            samd.setId(amd.getId());
        } else {
            samd.setId(UUID.randomUUID().toString());
            amd.setId(samd.id);
        }
        for (Md md : amd.getTechMdList()) {
            samd.techMdList.add(SlimMd.fromMd(md));
        }
        return samd;
    }

    public AmdSec toAmdSec() {
        ArrayList<Md> mdList = new ArrayList<>();
        for (SlimMd smd : this.techMdList) {
            mdList.add(smd.toMd());
        }
        return new AmdSec(mdList);
    }
}
