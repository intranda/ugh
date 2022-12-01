package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.exceptions.MetadataTypeNotAllowedException;

public class MetadataTest {
    private Metadata md;
    private MetadataType type;

    @Before
    public void setUp() throws MetadataTypeNotAllowedException {
        type = new MetadataType();
        md = new Metadata(type);
    }

    /* Tests for the Constructor */
    @Test
    public void testConstructorGivenNull() {
        assertThrows(MetadataTypeNotAllowedException.class, () -> new Metadata(null));
    }

    @Test
    public void testConstructorGivenDefaultInput() {
        assertNotNull(md.getAuthorityUriMap());
        assertFalse(md.wasUpdated());
        assertFalse(md.isValidationErrorPresent()); // the flag validationErrorPresent will be automatically initiated with false
        assertNull(md.getValidationMessage()); // but the string fields will remain null
    }

    @Test
    public void testConstructorAppliedTwiceUsingSameMetadataTypeObject() throws MetadataTypeNotAllowedException {
        assertSame(type, md.getType());
        Metadata md2 = new Metadata(type);
        assertSame(md.getType(), md2.getType()); // Metadata objects created from the same MetadataType object will share it
    }

    /* Tests for getters and setters */
    @Test
    public void testSetTypeGivenTwoMetadataObjectsCreatedFromTheSameMetadataTypeObject() throws MetadataTypeNotAllowedException {
        Metadata md2 = new Metadata(type);
        assertSame(md.getType(), md2.getType());
        MetadataType type2 = new MetadataType();
        md2.setType(type2);
        assertNotSame(type, type2);
        assertTrue(type.equals(type2));
        assertNotSame(md.getType(), md2.getType());
        assertTrue(md.getType().equals(md2.getType()));
    }

    @Test
    public void testSetValueGetValueTogetherWithWasUpdated() {
        assertFalse(md.wasUpdated());
        assertNull(md.getValue());
        md.setValue("value");
        assertEquals("value", md.getValue());
        assertTrue(md.wasUpdated());
    }

    @Ignore("The logic in the method cannot pass this test. Comparison logic needed in the setValue(String) method.")
    @Test
    public void testSetValueGetValueTogetherWithWasUpdatedGivenNullToSet() {
        // TODO remove wasUpdated from code, it is not needed or used anywhere
        // Actually it is used in ugh.fileformats.slimjson.SlimMetadata on Line 85. - Zehong
        // I will postpone the fix of these two issues, since I haven't really figured out the original purpose of the flag `updated`.
        // It might be possible that even when the same value is set again, we still want this flag to be set to true. - Zehong 
        assertFalse(md.wasUpdated());
        assertNull(md.getValue());
        md.setValue(null);
        assertNull(md.getValue());
        assertEquals(null, md.getValue());
        assertFalse(md.wasUpdated()); // should setting null again change the flag of updated?
    }

    @Ignore("The logic in the method cannot pass this test. Comparison logic needed in the setValue(String) method.")
    @Test
    public void testSetValueGetValueTogetherWithWasUpdatedGivenSameValueToSetTwice() {
        // TODO remove wasUpdated from code, it is not needed or used anywhere
        assertFalse(md.wasUpdated());
        assertNull(md.getValue());
        md.setValue("value");
        assertEquals("value", md.getValue());
        assertTrue(md.wasUpdated());
        md.wasUpdated(false); // set the updated flag back to false manually
        assertEquals("value", md.getValue()); // recheck that the value was not infected
        md.setValue("value"); // set the same value again
        assertFalse(md.wasUpdated()); // should setting the same value again change the flag of updated?
    }

    @Test
    public void testSetValueGetValueTogetherWithWasUpdatedGivenEmptyStringToSet() {
        assertFalse(md.wasUpdated());
        assertNull(md.getValue());
        md.setValue("");
        assertEquals(new String(), md.getValue());
        assertTrue(md.wasUpdated());
    }

    @Test
    public void testSetAuthorityMethods() throws MetadataTypeNotAllowedException {
        Metadata md2 = new Metadata(new MetadataType());
        String id = "id";
        String uri = "uri";
        String value = "value";
        // set authorityID, authorityURI, authorityValue of md separately using three setters
        md.setAuthorityID(id);
        md.setAuthorityURI(uri);
        md.setAuthorityValue(value);
        // set authorityID, authorityURI, authorityValue of md2 using the method setAuthorityFile(String, String, String)
        md2.setAutorityFile(id, uri, value);
        assertTrue(md.getAuthorityID() == md2.getAuthorityID()
                && md.getAuthorityURI() == md2.getAuthorityURI()
                && md.getAuthorityValue() == md2.getAuthorityValue());
    }

    @Test
    public void testSetAndGetValueQualifierGivenNull() {
        assertNull(md.getValueQualifier());
        assertNull(md.getValueQualifierType());
        md.setValueQualifier(null, null); // setter with two null inputs should not work
        assertNull(md.getValueQualifier());
        assertNull(md.getValueQualifierType());
        md.setValueQualifier("vq", null); // setter with one null input should not work either
        assertNull(md.getValueQualifier());
        assertNull(md.getValueQualifierType());
        md.setValueQualifier(null, "vqtype");
        assertNull(md.getValueQualifier());
        assertNull(md.getValueQualifierType());
    }

    @Test
    public void testSetAndGetValueQualifierGivenEmptyStrings() {
        assertNull(md.getValueQualifier());
        assertNull(md.getValueQualifierType());
        md.setValueQualifier("", new String());
        assertEquals(new String(), md.getValueQualifier());
        assertEquals("", md.getValueQualifierType());
    }

    /* Tests for the method addAuthorityUriToMap(String, String) */
    @Test
    public void testAddAuthorityUriToMapGivenNullAsFirstArgument() {
        // null should not be allowed as an identifier for an authorityFile
        assertThrows(IllegalArgumentException.class, () -> md.addAuthorityUriToMap(null, "uri"));
    }

    @Test
    public void testAddAuthorityUriToMapGivenEmptyStringAsFirstArgument() {
        // empty string should not be allowed as an identifier for an authorityFile
        assertThrows(IllegalArgumentException.class, () -> md.addAuthorityUriToMap("", "uri"));
    }

    @Test
    public void testAddAuthorityUriToMapGivenSameInputsTwice() {
        // same things should not be added twice
        assertEquals(0, md.getAuthorityUriMap().size());
        md.addAuthorityUriToMap("file", "uri");
        assertEquals(1, md.getAuthorityUriMap().size());
        md.addAuthorityUriToMap("file", "uri");
        assertEquals(1, md.getAuthorityUriMap().size());
    }

    @Test
    public void testAddAuthorityUriToMapGivenSameFirstArgumentButDifferentSecondArguments() {
        assertEquals(0, md.getAuthorityUriMap().size());
        md.addAuthorityUriToMap("file", "uri");
        assertTrue(md.getAuthorityUriMap().containsValue("uri"));
        assertEquals("uri", md.getAuthorityUriMap().get("file"));
        md.addAuthorityUriToMap("file", "URI");
        assertFalse(md.getAuthorityUriMap().containsValue("uri"));
        assertTrue(md.getAuthorityUriMap().containsValue("URI"));
        assertEquals("URI", md.getAuthorityUriMap().get("file"));
    }

    /* Tests for the method removeAuthorityUriFromMap(String) */
    @Test
    public void testRemoveAuthorityUriFromMapGivenNull() {
        md.removeAuthorityUriFromMap(null);
        // intentionally left blank, since we do not need any more assertions here
        // we are good as long as there is no exception thrown
    }

    @Test
    public void testRemoveAuthorityUriFromMapGivenKnownKey() {
        md.addAuthorityUriToMap("file", "uri");
        assertTrue(md.getAuthorityUriMap().containsKey("file"));
        assertTrue(md.getAuthorityUriMap().containsValue("uri"));
        md.removeAuthorityUriFromMap("file");
        assertFalse(md.getAuthorityUriMap().containsKey("file"));
        assertFalse(md.getAuthorityUriMap().containsValue("uri"));
    }

    @Test
    public void testRemoveAuthorityUriFromMapGivenUnknownKey() {
        md.addAuthorityUriToMap("file", "uri");
        assertTrue(md.getAuthorityUriMap().containsKey("file"));
        assertTrue(md.getAuthorityUriMap().containsValue("uri"));
        md.removeAuthorityUriFromMap("unknown");
        assertTrue(md.getAuthorityUriMap().containsKey("file"));
        assertTrue(md.getAuthorityUriMap().containsValue("uri"));
    }

    /* Tests for the method toString() */
    @Test
    public void testToStringGivenNoValueAndNoType() {
        md.setType(null);
        assertEquals("Metadata (WITHOUT TYPE!!): NULL\n", md.toString());
    }

    @Test
    public void testToStringGivenValueButNoType() {
        md.setType(null);
        md.setValue("value");
        assertEquals("Metadata (WITHOUT TYPE!!): \"value\"\n", md.toString());
    }

    @Test
    public void testToStringGivenNoValueAndEmptyType() {
        assertEquals("Metadata (null): NULL\n", md.toString());
    }

    @Test
    public void testToStringGivenValueAndEmptyType() {
        md.setValue("value");
        assertEquals("Metadata (null): \"value\"\n", md.toString());
    }

    @Test
    public void testToStringGivenValueAndNonEmptyType() {
        md.setValue("value");
        md.getType().setName("type name");
        assertEquals("Metadata (type name): \"value\"\n", md.toString());
    }

    /* Tests for the method equals(Metadata) */
    @Test
    public void testEqualsToItself() {
        assertTrue(md.equals(md));
    }

    @Test
    public void testEqualsGivenNull() {
        assertFalse(md.equals(null));
    }

    @Test
    public void testEqualsGivenTwoMetadataObjectsCreatedFromTheSameMetadataTypeObject() throws MetadataTypeNotAllowedException {
        assertTrue(md.equals(new Metadata(type)));
    }

    @Test
    public void testEqualsGivenTwoMetadataObjectsCreatedFromTwoDefaultMetadataTypeObjects() throws MetadataTypeNotAllowedException {
        assertTrue(md.equals(new Metadata(new MetadataType())));
    }

    @Test
    public void testEqualsGivenTwoMetadataObjectsCreatedFromTwoUnmodifiedCopiesOfTheSameMetadataTypeObject() throws MetadataTypeNotAllowedException {
        assertTrue(md.equals(new Metadata(type.copy())));
    }

    @Test
    public void testEqualsGivenTwoMetadataObjectsCreatedFromTwoModifiedCopiesOfTheSameMetadataTypeObject() throws MetadataTypeNotAllowedException {
        MetadataType typeCopy = type.copy();
        typeCopy.setName("");
        Metadata md2 = new Metadata(typeCopy);
        assertFalse(md.equals(md2));
    }

    @Test
    public void testEqualsGivenTwoMetadataObjectsCreatedFromTwoCopiesOfTheSameMetadataTypeObjectAndThenModifyOne()
            throws MetadataTypeNotAllowedException {
        Metadata md2 = new Metadata(type.copy());
        md2.getType().setName("");
        assertFalse(md.equals(md2));
    }

    @Test
    public void testEqualsGivenTwoMetadataObjectsCreatedFromTwoEqualButDifferentMetadataTypeObjects() throws MetadataTypeNotAllowedException {
        // The method equals(MetadataType) defined in the class MetadataType compares only the following items:
        // isPerson, isIdentifier, name
        assertFalse(type.isCorporate());
        MetadataType type2 = new MetadataType();
        type2.setCorporate(true);
        assertTrue(type2.isCorporate());
        Metadata md2 = new Metadata(type2);
        assertTrue(type.equals(type2));
        assertNotSame(type.isCorporate(), type2.isCorporate());
        assertTrue(md.equals(md2));
    }

    @Test
    public void testEqualsGivenTwoEqualButDifferentMetadataObjectsCreatedFromTheSameMetadataTypeObject() throws MetadataTypeNotAllowedException {
        // The method equals(Metadata) defined in the class Metadata compares only the following items:
        // MDType, metadataValue, MetadataVQ, MetadataVQType
        // In this test we suppose that both Metadata objects have the same MDType
        Metadata md2 = new Metadata(type);
        // assure that both have the same metadataValue, MetadataVQ and MetadataVQType
        String value = "value";
        String vq = "vq";
        String vqType = "vqType";
        md.setValue(value);
        md2.setValue(value);
        md.setValueQualifier(vq, vqType);
        md2.setValueQualifier(vq, vqType);
        assertEquals(md.getValue(), md2.getValue());
        assertEquals(md.getValueQualifier(), md2.getValueQualifier());
        assertEquals(md.getValueQualifierType(), md2.getValueQualifierType());
        // assure that they are however somehow different
        md.setAutorityFile("id1", "uri1", "value1");
        md2.setAutorityFile("id2", "uri2", "value2");
        md.setValidationErrorPresent(false);
        md2.setValidationErrorPresent(true);
        // then we do the comparison
        assertTrue(md.equals(md2));
    }

    @Test
    public void testEqualsGivenInheritedObjects() throws MetadataTypeNotAllowedException {
        type.setIsPerson(true);
        type.isCorporate = true;
        Person p = new Person(type, "first name", "second name");
        Metadata mdp = p;
        Corporate c = new Corporate(type);
        Metadata mdc = c;
        assertNotSame(mdp.getClass(), mdc.getClass());
        assertFalse(md.equals(mdp));
        assertFalse(mdp.equals(mdc));
    }

}
