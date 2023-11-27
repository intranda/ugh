package ugh.fileformats.mets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class MetsModsImportExportTest {

    @Test
    public void testSplitRegularExpressions() {
        // null value returns empty list
        String regex = null;
        assertTrue(MetsModsImportExport.splitRegularExpression(regex).isEmpty());
        // empty value returns empty list
        regex = "";
        assertTrue(MetsModsImportExport.splitRegularExpression(regex).isEmpty());

        // complete value returns two parts
        regex = "s/search/replace/g";
        List<String> parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(2, parts.size());
        assertEquals("search", parts.get(0));
        assertEquals("replace", parts.get(1));

        // without s and g modifier
        regex = "/search/replace/";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(2, parts.size());
        assertEquals("search", parts.get(0));
        assertEquals("replace", parts.get(1));

        // escaped slashes in values remain
        regex = "/sea\\/rch/replace/";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(2, parts.size());
        assertEquals("sea\\/rch", parts.get(0));
        assertEquals("replace", parts.get(1));

        // search value without replacement returns only one value
        regex = "/search/";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(1, parts.size());
        assertEquals("search", parts.get(0));

        // value without any sed/perl syntax is returned completely
        regex = "something";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(1, parts.size());
        assertEquals("something", parts.get(0));

        // empty replacement value is returned too
        regex = "/search//";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(2, parts.size());
        assertEquals("search", parts.get(0));
        assertEquals("", parts.get(1));

        // search value special characters
        regex = "/^CC BY-SA$/";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(1, parts.size());
        assertEquals("^CC BY-SA$", parts.get(0));

        regex = "^CC BY-SA$";
        parts = MetsModsImportExport.splitRegularExpression(regex);
        assertEquals(1, parts.size());
        assertEquals("^CC BY-SA$", parts.get(0));
    }

}
