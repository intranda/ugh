package ugh.dl.slim;

import lombok.Data;

@Data
public class SlimReference {
    private String type;
    private String sourceDsId;
    private String targetDsId;

    private transient SlimDocStruct root;
}
