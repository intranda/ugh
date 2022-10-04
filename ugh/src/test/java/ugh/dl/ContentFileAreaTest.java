package ugh.dl;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ugh.exceptions.ContentFileAreaTypeUnknownException;

class ContentFileAreaTest {
    private ContentFileArea testArea;

    @BeforeEach
    void setup() {
        testArea = new ContentFileArea();
    }

    @Test
    void testSetType() throws ContentFileAreaTypeUnknownException {
        String[] types = new String[] { "coordinates", "byteoffset", "smtpe", "xmlid" };
        for (String type : types) {
            testArea.setType(type);
            assertEquals(type, testArea.getType());
        }
        assertThrows(ContentFileAreaTypeUnknownException.class, () -> testArea.setType("unknown"));
    }

    @Test
    void testDefaultValues() {
        assertNull(testArea.getType());
        assertNull(testArea.getFrom());
        assertNull(testArea.getTo());
    }

    @DisplayName("Grouped tests for the method equals(ContentFileArea)")
    @Nested
    class TestEquals {
        private ContentFileArea anotherArea;

        @BeforeEach
        void setup() {
            anotherArea = new ContentFileArea();
        }

        @Test
        void testEqualsGivenBothNullValues() {
            assertTrue(testArea.equals(anotherArea));
        }

        @Test
        void testEqualsGivenOneNullType() throws ContentFileAreaTypeUnknownException {
            anotherArea.setType("xmlid");
            assertFalse(testArea.equals(anotherArea));
            assertFalse(anotherArea.equals(testArea));
        }

        @Test
        void testEqualsGivenOneNullFrom() {
            anotherArea.setFrom("from");
            assertFalse(testArea.equals(anotherArea));
        }

        @Test
        void testEqualsGivenOneEmptyFrom() {
            anotherArea.setFrom("");
            assertFalse(anotherArea.equals(testArea));
        }

        @Test
        void testEqualsGivenOneNullTo() {
            anotherArea.setTo("to");
            assertFalse(anotherArea.equals(testArea));
        }

        @Test
        void testEqualsGivenOneEmptyTo() {
            anotherArea.setTo(new String());
            assertFalse(testArea.equals(anotherArea));
        }

        @Test
        void testEqualsGivenTypeButEmptyFromTo() throws ContentFileAreaTypeUnknownException {
            testArea.setType("xmlid");
            testArea.setFrom("");
            testArea.setTo(new String());
            anotherArea.setType("smtpe");
            anotherArea.setFrom(new String());
            anotherArea.setTo("");
            assertFalse(testArea.equals(anotherArea));
            anotherArea.setType("xmlid");
            assertTrue(testArea.equals(anotherArea));
        }

        @Test
        void testEqualsGivenNormalValues() throws ContentFileAreaTypeUnknownException {
            testArea.setType("xmlid");
            anotherArea.setType("xmlid");
            testArea.setFrom("from");
            anotherArea.setFrom("another from");
            testArea.setTo("to");
            anotherArea.setTo("to");
            assertFalse(testArea.equals(anotherArea));
            anotherArea.setFrom("from");
            assertTrue(testArea.equals(anotherArea));
        }
    }


}
