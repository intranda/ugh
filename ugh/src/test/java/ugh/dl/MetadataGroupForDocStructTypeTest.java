package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MetadataGroupForDocStructTypeTest {
    private MetadataGroupForDocStructType type;
    private MetadataGroupType groupType;

    @Test
    public void testConstructorGivenNull() {
        type = new MetadataGroupForDocStructType(null);
        assertNull(type.getMetadataGroup());
    }

    @Test
    public void testConstructorOnTwoObjectsCreatedFromSameMetadataGroupTypeObject() {
        groupType = new MetadataGroupType();
        type = new MetadataGroupForDocStructType(groupType);
        assertNull(type.getMetadataGroup().getName());
        type.getMetadataGroup().setName("name");
        MetadataGroupForDocStructType anotherType = new MetadataGroupForDocStructType(groupType);
        assertEquals("name", anotherType.getMetadataGroup().getName());
    }

}
