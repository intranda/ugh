package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class CorporateTest {

    private static MetadataType type;
    private Corporate corporate;

    @BeforeClass
    public static void setUpBeforeAll() {
        type = new MetadataType();
        type.setName("fixture");
        type.setNum("*");
    }

    @Before
    public void setUpBeforeEach() throws MetadataTypeNotAllowedException {
        corporate = new Corporate(type);
    }

    @Test
    public void testConstructor() {
        assertNotNull(corporate);
        assertNotNull(corporate.getSubNames());
        assertNotNull(corporate.getRole());
        assertNull(corporate.getMainName());
    }

    @Test
    public void testRole() {
        assertEquals("fixture", corporate.getRole());
        corporate.setRole("other role");
        assertEquals("other role", corporate.getRole());
    }

    @Test
    public void testMainName() {
        corporate.setMainName("name");
        assertEquals("name", corporate.getMainName());
    }

    @Test
    public void testPartName() {
        assertNull(corporate.getPartName());
        corporate.setPartName("name");
        assertEquals("name", corporate.getPartName());
    }

    @Test
    public void testSubNamesInNormalCases() {
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

    @Test
    public void testAddSubNameGivenNull() {
        corporate.addSubName(null);
        assertFalse(corporate.getSubNames().contains(null));
    }

    @Test
    public void testAddSubNameGivenEmptyNamePart() {
        assertEquals(0, corporate.getSubNames().size());
        corporate.addSubName(new NamePart());
        assertEquals(1, corporate.getSubNames().size());
    }

    @Test
    public void testAddSubNameGivenEmptyNamePartMultipleTimes() {
        corporate.addSubName(new NamePart());
        corporate.addSubName(new NamePart());
        assertEquals(1, corporate.getSubNames().size());
        NamePart part1 = new NamePart();
        NamePart part2 = new NamePart();
        corporate.addSubName(part1);
        corporate.addSubName(part2);
        assertEquals(1, corporate.getSubNames().size());
    }

    @Test
    public void testAddSubNameGivenTwoEquivalentNamePartObjects() {
        NamePart part1 = new NamePart("type", "value");
        NamePart part2 = new NamePart("type", "value");
        assertNotSame(part1, part2); // part1 and part2 are actually two different objects located at different addresses
        assertEquals(part1, part2); // but they are equal regarding the rewritten equals(Object) method defined in the NamePart class
        corporate.addSubName(part1);
        assertEquals(1, corporate.getSubNames().size());
        corporate.addSubName(part2);
        assertEquals(1, corporate.getSubNames().size()); // redundant adding of equivalent NamePart objects should be avoided
    }

    @Test
    public void testRemoveSubNameGivenNull() {
        corporate.removeSubName(null);
        // no more assertions needed here
        // we are good as long as no exception is thrown
    }

    @Test
    public void testRemoveSubNameGivenEmptyNamePart() {
        corporate.addSubName(new NamePart());
        assertEquals(1, corporate.getSubNames().size());
        corporate.removeSubName(new NamePart());
        assertEquals(0, corporate.getSubNames().size());
    }

    @Test
    public void testRemoveSubNameGivenTwoEquivalentNamePartObjects() {
        NamePart part1 = new NamePart("type", "value");
        NamePart part2 = new NamePart("type", "value");
        assertEquals(0, corporate.getSubNames().size());
        corporate.addSubName(part1);
        assertEquals(1, corporate.getSubNames().size());
        corporate.removeSubName(part2); // it should be permitted to use an equivalent NamePart object for the remove action
        assertEquals(0, corporate.getSubNames().size());
    }


}
