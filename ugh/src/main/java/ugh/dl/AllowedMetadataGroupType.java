package ugh.dl;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AllowedMetadataGroupType implements Serializable  {

    private static final long serialVersionUID = 7792862593047079899L;
    
    private String groupName;
    private String numAllowed;
    private boolean defaultDisplay;
    private boolean hidden;


}
