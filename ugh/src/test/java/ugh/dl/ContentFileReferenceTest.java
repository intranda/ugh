package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

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
        assertNotEquals(reference, null);
    }

    @Test
    public void testEqualsToItself() {
        ContentFileReference ref = new ContentFileReference();
        ContentFile cf = new ContentFile();
        ref.setCf(cf);
        assertEquals(ref, ref);
    }

    @Test
    public void testEqualsToItselfWithoutInitialization() {
        assertEquals(reference, reference);
    }

    @Test
    public void testEqualsGivenReferencesWithDifferentCFs() {
        ContentFileReference reference1 = new ContentFileReference();
        ContentFileReference reference2 = new ContentFileReference();
        ContentFile cf1 = new ContentFile();
        ContentFile cf2 = new ContentFile();
        cf1.setLocation("location1");
        cf2.setLocation("location2");
        reference1.setCf(cf1);
        reference2.setCf(cf2);
        assertNotEquals(reference1, reference2);
    }

}
