package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class PersonTest {

    private MetadataType type;
    private Person person;

    @Before
    public void setUp() {
        type = new MetadataType();
    }

    /* Tests for constructors */
    @Test
    public void testConstructorGivenNull() {
        assertThrows(MetadataTypeNotAllowedException.class, () -> new Person(null));
    }

    @Test
    public void testConstructorGivenEmptyMetadataType() throws MetadataTypeNotAllowedException {
        person = new Person(type);
        assertNull(person.getFirstname());
        assertNull(person.getLastname());
        assertNull(person.getDisplayname());
        assertNull(person.getAffiliation());
        assertNull(person.getInstitution());
        assertNull(person.getRole());
        assertNull(person.getPersontype());
        assertNull(person.getAdditionalNameParts());
    }

    @Test
    public void testConstructorGivenFirstnameButNullAsLastname() throws MetadataTypeNotAllowedException {
        person = new Person(type, "First", null);
        assertEquals("First", person.getFirstname());
        assertNull(person.getLastname());
    }

    @Test
    public void testConstructorGivenLastnameButNullAsFirstname() throws MetadataTypeNotAllowedException {
        person = new Person(type, null, "Last");
        assertEquals("Last", person.getLastname());
        assertNull(person.getFirstname());
    }

    @Test
    public void testConstructorGivenNullForBothNames() throws MetadataTypeNotAllowedException {
        person = new Person(type, null, null);
        assertNull(person.getFirstname());
        assertNull(person.getLastname());
    }

    /* Tests for the method toString() */
    @Test
    public void testToStringGivenNormalInput() throws MetadataTypeNotAllowedException {
        type.setName("TypeName");
        person = new Person(type, "First", "Last");
        assertEquals("Person (TypeName): \"Last\", \"First\"\n", person.toString());
    }

    @Test
    public void testToStringGivenNullAsFirstname() throws MetadataTypeNotAllowedException {
        type.setName("TypeName");
        person = new Person(type, null, "Last");
        assertEquals("Person (TypeName): \"Last\", NULL\n", person.toString());
    }

    @Test
    public void testToStringGivenEmptyFirstname() throws MetadataTypeNotAllowedException {
        type.setName("TypeName");
        person = new Person(type, "", "Last");
        assertEquals("Person (TypeName): \"Last\", \"\"\n", person.toString());
    }

    @Ignore("The logic in the method cannot pass this test. The checking logic following if seems contradictory to the formulation logic in the block.")
    @Test
    public void testToStringGivenNullAsLastname() throws MetadataTypeNotAllowedException {
        type.setName("TypeName");
        // TODO fix toString method, one of firstname / lastname is needed
        // also I never used this constructor, I only use new Person(type)
        person = new Person(type, "First", null);
        assertEquals("Person (TypeName): NULL, \"First\"\n", person.toString());
    }

    @Ignore("The logic in the method cannot pass this test. The checking logic following if seems contradictory to the formulation logic in the block.")
    @Test
    public void testToStringGivenEmptyLastname() throws MetadataTypeNotAllowedException {
        type.setName("TypeName");
        person = new Person(type, "First", "");
        assertEquals("Person (TypeName): \"\", \"First\"\n", person.toString());
    }

    @Test
    public void testToStringGivenMetadataTypeWithoutName() throws MetadataTypeNotAllowedException {
        person = new Person(type, "First", "Last");
        assertEquals("Person (null): \"Last\", \"First\"\n", person.toString());
    }

    /* Tests for the method equals(Person) */
    @Test
    public void testEqualsToItself() throws MetadataTypeNotAllowedException {
        person = new Person(type);
        assertTrue(person.equals(person));
    }

    @Ignore("The logic in the method cannot pass this test. Null check needed.")
    @Test
    public void testEqualsGivenNull() throws MetadataTypeNotAllowedException {
        person = new Person(type);
        // TODO add null check
        assertFalse(person.equals(null));
    }

    @Test
    public void testEqualsGivenPersonObjectsCreatedFromTheSameMetadataTypeObject() throws MetadataTypeNotAllowedException {
        person = new Person(type);
        type.setName("type name");
        Person anotherPerson = new Person(type);
        assertEquals("type name", person.getType().getName());
        assertTrue(person.equals(anotherPerson));
    }

    @Test
    public void testEqualsGivenObjectOfAnExtendedClassOfPerson() throws MetadataTypeNotAllowedException {
        person = new Person(type);
        ExtendedPerson extendedPerson = new ExtendedPerson(type);
        assertTrue(person.equals(extendedPerson));
        person = new Person(type, "First", "Last");
        extendedPerson = new ExtendedPerson(type, "First", "Last");
        assertTrue(extendedPerson.equals(person));
        person.setDisplayname("display name");
        assertFalse(extendedPerson.equals(person));
        extendedPerson.setDisplayname("display name");
        assertTrue(person.equals(extendedPerson));
        extendedPerson.setExtendedProperty("property");
        assertTrue(person.equals(extendedPerson));
        assertTrue(extendedPerson.equals(person));
    }

    // class needed for the test case above
    private class ExtendedPerson extends Person {
        private String extendedProperty = null;
        public ExtendedPerson(MetadataType t) throws MetadataTypeNotAllowedException {
            super(t);
        }

        public ExtendedPerson(MetadataType t, String in1, String in2) throws MetadataTypeNotAllowedException {
            super(t, in1, in2);
        }

        public void setExtendedProperty(String property) {
            this.extendedProperty = property;
        }

        public String getExtendedProperty() {
            return this.extendedProperty;
        }
    }

    /* Tests for the methods setAdditionalNameParts(List<NamePart> && addNamePart(NamePart)*/
    @Ignore("The logic in the method cannot pass this test. Null check needed.")
    @Test
    public void testAddNamePartGivenNull() throws MetadataTypeNotAllowedException {
        person = new Person(type);
        // TODO add null check
        assertThrows(IllegalArgumentException.class, () -> person.addNamePart(null));
    }

    @Ignore("The logic in the method cannot pass this test. Might be a better idea to make a deep copy of a list.")
    @Test
    public void testSetAdditionalNamePartsOnTwoPersonObjectsUsingSameListThenAddNamePartToJustOne() throws MetadataTypeNotAllowedException {
        // TODO I am not sure what the expected behavior should be and if a change would break existing code
        // but I think its better if each object gets its own list. Just change the setter to new ArrayList<>(list)
        person = new Person(type);
        ArrayList<NamePart> list = new ArrayList<>();
        person.setAdditionalNameParts(list);
        assertEquals(0, person.getAdditionalNameParts().size());
        Person anotherPerson = new Person(type, "First", "Last");
        assertFalse(anotherPerson.equals(person));
        anotherPerson.setAdditionalNameParts(list);
        assertEquals(0, anotherPerson.getAdditionalNameParts().size());
        person.addNamePart(new NamePart()); // this operation should not affect the same field of another person
        assertEquals(1, person.getAdditionalNameParts().size());
        assertEquals(0, anotherPerson.getAdditionalNameParts().size());
    }

}

