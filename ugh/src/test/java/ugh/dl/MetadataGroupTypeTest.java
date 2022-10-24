package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class MetadataGroupTypeTest {
    private MetadataGroupType mdgType;

    @Before
    public void setUp() {
        mdgType = new MetadataGroupType();
    }

    /* Tests for constructor */
    @Test
    public void testConstructor() {
        assertNotNull(mdgType.getMetadataTypeList());
        assertNotNull(mdgType.getAllAllowedGroupTypeTypes());
        assertNull(mdgType.getName());
        assertNull(mdgType.getAllLanguages());
        assertNull(mdgType.getNum());
    }

    /* Tests for the methods getMetadataTypeList(), setMetadataTypeList(List<MetadataType>), setTypes(List<MetadataTypeForDocStructType>)  */
    @Test
    public void testGetMetadataTypeListAgainstModificationOfReturnedResult() {
        // The content of the object should not be affected from outside via getters.
        assertEquals(0, mdgType.getMetadataTypeList().size());
        List<MetadataType> list = mdgType.getMetadataTypeList();
        assertEquals(0, list.size());
        list.add(new MetadataType());
        assertEquals(1, list.size());
        assertEquals(0, mdgType.getMetadataTypeList().size());
    }

    @Test
    public void testSetMetadataTypeListAgainstModificationOfOriginalList() {
        // The content of the object should not be affected via setters anymore after creation.
        List<MetadataType> list = new ArrayList<>();
        list.add(new MetadataType());
        assertEquals(1, list.size());
        mdgType.setMetadataTypeList(list);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        list.add(new MetadataType());
        assertEquals(2, list.size());
        assertEquals(1, mdgType.getMetadataTypeList().size());
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testSetTypesGivenNullThenGetMetadataTypeList() {
        assertNotNull(mdgType.getMetadataTypeList());
        mdgType.setTypes(null);
        assertNull(mdgType.getMetadataTypeList());
    }

    @Test
    public void testSetTypesOnTwoObjectsRelatedToTheSameTypeList() {
        List<MetadataTypeForDocStructType> list = new ArrayList<>();
        mdgType.setTypes(list);
        MetadataGroupType mdgType2 = new MetadataGroupType();
        mdgType2.setTypes(list);
        assertEquals(0, mdgType.getMetadataTypeList().size());
        assertEquals(0, mdgType2.getMetadataTypeList().size());
        mdgType.addMetadataType(new MetadataType(), null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        assertEquals(1, mdgType2.getMetadataTypeList().size());
        list.add(new MetadataTypeForDocStructType(new MetadataType()));
        assertEquals(2, list.size());
        assertEquals(2, mdgType.getMetadataTypeList().size());
        assertEquals(2, mdgType2.getMetadataTypeList().size());
    }

    /* Tests for the method addMetadataType(MetadataType, String, boolean, boolean) */
    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddMetadataTypeGivenNullAsFirstArgument() {
        assertEquals(0, mdgType.getMetadataTypeList().size());
        assertThrows(IllegalArgumentException.class, () -> mdgType.addMetadataType(null, null, false, false));
    }

    @Test
    public void testAddMetadataTypeGivenSameNamedMetadataTypeObjectTwice() {
        MetadataType mdType = new MetadataType();
        mdType.setName("name");
        assertEquals(0, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(mdType, null, false, true);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        assertFalse(new MetadataTypeForDocStructType(mdgType.getMetadataTypeList().get(0)).isInvisible());
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed to avoid the NullPointerException.")
    @Test
    public void testAddMetadataTypeGivenUnnamedMetadataTypeObjectTwice() {
        MetadataType mdType = new MetadataType();
        assertEquals(0, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals(1, mdgType.getMetadataTypeList().size());
    }

    @Test
    public void testAddMetadataTypeAgainstModificationsOfTheAddedObjectFromOutside1() throws MetadataTypeNotAllowedException {
        // add MetadataType without setting its language list first
        MetadataType mdType = new MetadataType();
        mdType.setName("name");
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals("name", mdgType.getMetadataTypeList().get(0).getName());
        assertNull(mdgType.getMetadataTypeList().get(0).getAllLanguages());
        Metadata md = new Metadata(mdType);
        assertThrows(NullPointerException.class, () -> mdType.getAllLanguages().size());
        assertThrows(NullPointerException.class, () -> md.getType().getAllLanguages().size());
        assertThrows(NullPointerException.class, () -> mdgType.getMetadataTypeList().get(0).getAllLanguages().size());
        md.getType().setAllLanguages(new HashMap<String, String>());
        assertEquals(0, mdType.getAllLanguages().size());
        assertEquals(0, md.getType().getAllLanguages().size());
        assertThrows(NullPointerException.class, () -> mdgType.getMetadataTypeList().get(0).getAllLanguages().size());
    }

    @Ignore("The logic in the method cannot pass this test. Deep copy needed for MetadataType objects.")
    @Test
    public void testAddMetadataTypeAgainstModificationsOfTheAddedObjectFromOutside2() throws MetadataTypeNotAllowedException {
        // add MetadataType after setting its language list first
        MetadataType mdType = new MetadataType();
        mdType.setName("name");
        mdType.setAllLanguages(new HashMap<String, String>());
        Metadata md = new Metadata(mdType);
        mdgType.addMetadataType(mdType, null, false, false);
        assertEquals(0, mdType.getAllLanguages().size());
        assertEquals(0, md.getType().getAllLanguages().size());
        assertEquals(0, mdgType.getMetadataTypeList().get(0).getAllLanguages().size());
        // modifications on the language list via the Metadata object should not affect the language list inside the MetadataGroupType object
        md.getType().addLanguage("de", "Deutsch");
        assertEquals(1, mdType.getAllLanguages().size());
        assertEquals(1, md.getType().getAllLanguages().size());
        assertEquals(0, mdgType.getMetadataTypeList().get(0).getAllLanguages().size());
    }

    /* Tests for the method removeMetadataType(MetadataType) */

}


