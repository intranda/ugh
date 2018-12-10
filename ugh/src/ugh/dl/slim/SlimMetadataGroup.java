package ugh.dl.slim;

import java.util.List;

import lombok.Data;
import ugh.dl.Person;

@Data
public class SlimMetadataGroup {
    protected String mDGroupTypeId;
    // Document structure to which this metadata type belongs to.
    protected String myDocStructId;

    private List<SlimMetadata> metadataList;
    private List<Person> personList;

}
