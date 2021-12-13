package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class CorporateTest {

    private MetadataType type;

    @Before
    public void setUp() {
        type = new MetadataType();
        type.setName("fixture");
        type.setNum("*");
    }

    @Test
    public void testConstructor() throws MetadataTypeNotAllowedException {
        Corporate corporate = new Corporate(type);
        assertNotNull(corporate);
    }

    @Test
    public void testRole() throws MetadataTypeNotAllowedException {
        Corporate corporate = new Corporate(type);
        assertNotNull(corporate);
        assertEquals("fixture", corporate.getRole());
        corporate.setRole("other role");
        assertEquals("other role", corporate.getRole());
    }

    @Test
    public void testMainName() throws MetadataTypeNotAllowedException {
        Corporate corporate = new Corporate(type);
        assertNotNull(corporate);
        assertEquals(null, corporate.getMainName());
        corporate.setMainName("name");
        assertEquals("name", corporate.getMainName());
    }

    @Test
    public void testPartName() throws MetadataTypeNotAllowedException {
        Corporate corporate = new Corporate(type);
        assertNotNull(corporate);
        assertEquals(null, corporate.getPartName());
        corporate.setPartName("name");
        assertEquals("name", corporate.getPartName());
    }

    @Test
    public void testSubNames() throws MetadataTypeNotAllowedException {
        Corporate corporate = new Corporate(type);
        assertNotNull(corporate);
        assertEquals(0, corporate.getSubNames().size());

        String firstSubName = "first";
        String secondSubName = "second";
        // add first sub name
        corporate.addSubName(new NamePart("subname", firstSubName));
        assertEquals(1, corporate.getSubNames().size());
        // add second sub name
        corporate.addSubName(new NamePart("subname",secondSubName));
        assertEquals(2, corporate.getSubNames().size());
        // add first name again, should not be added to list
        corporate.addSubName(new NamePart("subname",firstSubName));
        assertEquals(2, corporate.getSubNames().size());

        // remove first name from list, only second should remain
        corporate.removeSubName(new NamePart("subname",firstSubName));
        assertEquals(1, corporate.getSubNames().size());
        assertEquals(secondSubName, corporate.getSubNames().get(0).getValue());

    }

}
