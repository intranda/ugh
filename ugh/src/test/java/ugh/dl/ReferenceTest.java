package ugh.dl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
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
}

