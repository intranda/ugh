package ugh.dl.slim;

import java.util.List;

import lombok.Data;

@Data
public class SlimContentFile {

    private List<String> referencedDocStructs;
    private String location;
    private String mimeType;
    private String subType;
    private String offset;
    private String offsetType;
    private String identifier;

    private boolean isRepresentative = false;
}
