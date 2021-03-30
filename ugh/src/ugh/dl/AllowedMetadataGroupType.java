package ugh.dl;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AllowedMetadataGroupType {

    private String groupName;
    private String numAllowed;
    private boolean defaultDisplay;
    private boolean hidden;


}
