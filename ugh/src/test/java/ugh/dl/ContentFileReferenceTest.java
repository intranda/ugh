package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.exceptions.ContentFileAreaTypeUnknownException;

public class ContentFileReferenceTest {
    private ContentFileReference reference;

    @Before
    public void setUp() {
        reference = new ContentFileReference();
    }

    @Test
    public void testConstructor() {
        assertNull(reference.getCf());
        assertNull(reference.getCfa());
    }

    @Test
    public void testGetterSetter() {
        ContentFileArea area = new ContentFileArea();
        ContentFile file = new ContentFile();
        reference.setCf(file);
        reference.setCfa(area);
        assertEquals(file, reference.getCf());
        assertEquals(area, reference.getCfa());
    }

    @Test
    public void testEqualsGivenOneNull() {
        assertFalse(reference.equals(null));
    }

    @Test
    public void testEqualsToItself() {
        ContentFileReference reference = new ContentFileReference();
        ContentFile cf = new ContentFile();
        reference.setCf(cf);
        assertTrue(reference.equals(reference));
    }

    @Test
    public void testEqualsToItselfWithoutInitialization() {
        assertTrue(reference.equals(reference));
    }

    @Test
    public void testEqualsGivenReferencesWithNullCF() {
        ContentFileReference reference1 = new ContentFileReference();
        ContentFileReference reference2 = new ContentFileReference();
        assertThrows(NullPointerException.class, () -> reference1.equals(reference2));
    }

    @Test
    public void testEqualsGivenReferencesWithSameEmptyCF() {
        ContentFileReference reference1 = new ContentFileReference();
        ContentFileReference reference2 = new ContentFileReference();
        ContentFile cf = new ContentFile();
        reference1.setCf(cf);
        reference2.setCf(cf);
        assertTrue(reference1.equals(reference2));
    }

    @Test
    public void testEqualsGivenReferencesWithDifferentCFs() throws ContentFileAreaTypeUnknownException {
        ContentFileReference reference1 = new ContentFileReference();
        ContentFileReference reference2 = new ContentFileReference();
        ContentFile cf1 = new ContentFile();
        ContentFile cf2 = new ContentFile();
        cf1.setLocation("location1");
        cf2.setLocation("location2");
        reference1.setCf(cf1);
        reference2.setCf(cf2);
        assertFalse(reference1.equals(reference2));
    }

    @Ignore("The logic to be tested still lies in the TODO list.")
    @Test
    public void testEqualsGivenReferencesWithSameCFButDifferentEmptyCFAs() {
        ContentFileReference reference1 = new ContentFileReference();
        ContentFileReference reference2 = new ContentFileReference();
        ContentFile cf = new ContentFile();
        reference1.setCf(cf);
        reference2.setCf(cf);
        ContentFileArea cfa1 = new ContentFileArea();
        ContentFileArea cfa2 = new ContentFileArea();
        reference1.setCfa(cfa1);
        reference2.setCfa(cfa2);
        assertTrue(reference1.equals(reference2));
    }

    @Ignore("The logic to be tested still lies in the TODO list.")
    @Test
    public void testEqualsGivenReferencesWithSameCFButDifferentCFAs() {
        ContentFileReference reference1 = new ContentFileReference();
        ContentFileReference reference2 = new ContentFileReference();
        ContentFile cf = new ContentFile();
        reference1.setCf(cf);
        reference2.setCf(cf);
        ContentFileArea cfa1 = new ContentFileArea();
        ContentFileArea cfa2 = new ContentFileArea();
        cfa1.setFrom("from1");
        cfa2.setFrom("from2");
        reference1.setCfa(cfa1);
        reference2.setCfa(cfa2);
        assertFalse(reference1.equals(reference2));
    }

}
