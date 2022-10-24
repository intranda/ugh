package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MetadataTypeForDocStructTypeTest {
    private MetadataType mdType;
    private MetadataTypeForDocStructType type;

    @Test
    public void testConstructorGivenNull() {
        type = new MetadataTypeForDocStructType(null);
        assertNull(type.getMetadataType());
    }

    @Test
    public void testConstructorOnTwoObjectsCreatedFromSameMetadataTypeObject() {
        mdType = new MetadataType();
        assertNull(mdType.getName());
        type = new MetadataTypeForDocStructType(mdType);
        type.getMetadataType().setName("mdType");
        MetadataTypeForDocStructType anotherType = new MetadataTypeForDocStructType(mdType);
        assertEquals("mdType", anotherType.getMetadataType().getName());
    }

}
