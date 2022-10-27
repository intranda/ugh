package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ugh.exceptions.TypeNotAllowedForParentException;

public class ReferenceTest {
    private DocStructType dsType1;
    private DocStructType dsType2;
    private DocStruct ds1;
    private DocStruct ds2;
    private Reference rf;

    @Before
    public void setUp() {
        rf = new Reference();
    }

    @Test
    public void testConstructor() {
        assertNull(rf.getSource());
        assertNull(rf.getTarget());
        assertNull(rf.getType());
        assertEquals(0, rf.getSourceID());
        assertEquals(0, rf.getTargetID());
    }

    @Test
    public void testGettersAndSettersUnderNormalScenario() throws TypeNotAllowedForParentException {
        dsType1 = new DocStructType();
        dsType2 = new DocStructType();
        ds1 = new DocStruct(dsType1);
        ds2 = new DocStruct(dsType2);
        rf.setSource(ds1);
        assertEquals(ds1, rf.getSource());
        assertEquals(0, rf.getSourceID());
        rf.setTarget(ds2);
        assertEquals(ds2, rf.getTarget());
        assertEquals(0, rf.getTargetID());
        rf.setSourceID(10086);
        assertNull(rf.getSource());
        assertEquals(10086, rf.getSourceID());
        rf.setTargetID(1415926535);
        assertNull(rf.getTarget());
        assertEquals(1415926535, rf.getTargetID());
    }

    @Ignore("The test passes, but it is just to show to what surprise the design could lead to. Check the comments below. Same for the method setTarget.")
    @Test
    public void testSetSourceGivenNull() throws TypeNotAllowedForParentException {
        dsType1 = new DocStructType();
        ds1 = new DocStruct(dsType1);
        // Compare the following two tracks
        // 1. the usual one: reset sourceID to some nonzero long to set the source to null, then this long would be a mark for invalid source
        rf.setSource(ds1);
        assertNotNull(rf.getSource());
        assertEquals(0, rf.getSourceID());
        rf.setSourceID(10086);
        assertNull(rf.getSource());
        assertNotEquals(0, rf.getSourceID());
        // 2. the unusual one: set source to null directly, then we still get a zero which might somehow contradicts to the internal logic of the first track
        rf.setSource(null);
        assertNull(rf.getSource());
        assertEquals(0, rf.getSourceID());
    }


}

