package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class PersonTest {

    private MetadataType type;
    private Person person;

    @Before
    public void setUp() {
        type = new MetadataType();
        type.setIsPerson(true);
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

    @Test
    public void testToStringGivenNullAsLastname() throws MetadataTypeNotAllowedException {
        type.setName("TypeName");
        // I never used this constructor, I only use new Person(type). - Robert
        person = new Person(type, "First", null);
        assertEquals("Person (TypeName): NULL, \"First\"\n", person.toString());
    }

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

    @Test
    public void testToStringGivenNoName() throws MetadataTypeNotAllowedException {
        person = new Person(type, "", null);
        assertEquals("", person.toString());
    }

    @Test
    public void testToStringGivenNullTypeButFullNames() throws MetadataTypeNotAllowedException {
        person = new Person(type, "First", "Last");
        person.setType(null);
        assertNull(person.getType());
        assertEquals("Person (WITHOUT TYPE!!): \"Last\", \"First\"\n", person.toString());
    }

    @Test
    public void testToStringGivenNullTypeAndNullAsFirstname() throws MetadataTypeNotAllowedException {
        person = new Person(type, null, "Last");
        person.setType(null);
        assertNull(person.getType());
        assertEquals("Person (WITHOUT TYPE!!): \"Last\", NULL\n", person.toString());
    }

    @Test
    public void testToStringGivenNullTypeAndEmptyFirstname() throws MetadataTypeNotAllowedException {
        person = new Person(type, "", "Last");
        person.setType(null);
        assertNull(person.getType());
        assertEquals("Person (WITHOUT TYPE!!): \"Last\", \"\"\n", person.toString());
    }

    @Test
    public void testToStringGivenNullTypeAndNullAsLastname() throws MetadataTypeNotAllowedException {
        person = new Person(type, "First", null);
        person.setType(null);
        assertNull(person.getType());
        assertEquals("Person (WITHOUT TYPE!!): NULL, \"First\"\n", person.toString());
    }

    @Test
    public void testToStringGivenNullTypeAndEmptyLastname() throws MetadataTypeNotAllowedException {
        person = new Person(type, "First", "");
        person.setType(null);
        assertNull(person.getType());
        assertEquals("Person (WITHOUT TYPE!!): \"\", \"First\"\n", person.toString());
    }

    @Test
    public void testToStringGivenNullTypeAndNoValidName() throws MetadataTypeNotAllowedException {
        person = new Person(type, "", null);
        person.setType(null);
        assertNull(person.getType());
        assertEquals("Person (WITHOUT TYPE!!): NULL, \"\"\n", person.toString());
    }

    /* Tests for the method equals(Person) */
    @Test
    public void testEqualsToItself() throws MetadataTypeNotAllowedException {
        person = new Person(type);
        assertTrue(person.equals(person));
    }

    @Test
    public void testEqualsGivenNull() throws MetadataTypeNotAllowedException {
        person = new Person(type);
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
        assertFalse(person.equals(extendedPerson));
        person = new Person(type, "First", "Last");
        extendedPerson = new ExtendedPerson(type, "First", "Last");
        assertFalse(extendedPerson.equals(person));
        person.setDisplayname("display name");
        assertFalse(extendedPerson.equals(person));
        extendedPerson.setDisplayname("display name");
        assertFalse(person.equals(extendedPerson));
        extendedPerson.setExtendedProperty("property");
        assertFalse(person.equals(extendedPerson));
        assertFalse(extendedPerson.equals(person));
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
    @Test(expected = IllegalArgumentException.class)
    public void testAddNamePartGivenNull() throws MetadataTypeNotAllowedException {
        person = new Person(type);
        person.addNamePart(null);
    }

    @Test
    public void testSetAdditionalNamePartsOnTwoPersonObjectsUsingSameListThenAddNamePartToJustOne() throws MetadataTypeNotAllowedException {
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

