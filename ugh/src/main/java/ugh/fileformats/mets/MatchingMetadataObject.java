package ugh.fileformats.mets;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/*******************************************************************************
 * ugh.fileformats.mets / MatchingMetadataObject.java
 * 
 * Copyright 2010 Center for Retrospective Digitization, Göttingen (GDZ)
 * 
 * http://gdz.sub.uni-goettingen.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 ******************************************************************************/

/*******************************************************************************
 * @author Stefan Funk
 * @author Robert Sehr
 * @version 2009-12-21
 * @since 2009-05-09
 * 
 *        TODOLOG
 * 
 *        CHANGELOG
 * 
 *        21.12.2009 --- Funk --- Added modsGrouping.
 * 
 *        20.11.2009 --- Funk --- Changed all method modifiers to protected.
 * 
 ******************************************************************************/

public class MatchingMetadataObject {

    // Internal name and some skipped boolean.
    private String internalName = null;
    private boolean skipped = false;

    // RegExp value replacing.
    private String valueCondition = null;
    private String valueRegExp = null;

    // Role of a person.
    private String role = null;

    // Variables used for writing and reading MODS.
    private String readxQuery = null;
    private String writexQuery = null;
    private String writemodsName = null;

    // Additional XQueries are only used for persons.
    private String firstnameXQuery = null;
    private String lastnameXQuery = null;
    private String affiliationXQuery = null;
    private String identifierXQuery = null;
    private String identifierTypeXQuery = null;
    private String authorityIDXquery = null;
    private String authorityURIXquery = null;
    private String authorityValueXquery = null;
    private String displayNameXQuery = null;
    private String persontypeXQuery = null;

    // All these are official MODS attributes used for reading.
    private String readmodsName = null;
    private String modstype = null;
    private String modsencoding = null;
    private String modsauthority = null;
    private String modsID = null;
    private String modstransliteration = null;

    private String modslang = null;
    private String modsxmllang = null;

    // Used for grouping of MODS subtags.
    private String modsGrouping = null;

    private Map<String, Map<String, String>> metadataGroupXQueries = new LinkedHashMap<>();

    private String database = null;

    @Getter
    @Setter
    private String mainNameXQuery = null;
    @Getter
    @Setter
    private String subNameXQuery = null;
    @Getter
    @Setter
    private String partNameXQuery = null;

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getReadModsName() {
        return this.readmodsName;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getWriteModsName() {
        return this.writemodsName;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getInternalName() {
        return this.internalName;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMODSType() {
        return this.modstype;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMODSEncoding() {
        return this.modsencoding;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMODSAuthority() {
        return this.modsauthority;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMODSID() {
        return this.modsID;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMODSTransliteration() {
        return this.modstransliteration;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMODSScript() {
        return this.modstransliteration;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMODSLang() {
        return this.modslang;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMODSXMLLang() {
        return this.modsxmllang;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public boolean isSkipped() {
        return this.skipped;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRole() {
        return this.role;
    }

    /***************************************************************************
     * @return Returns the isSortingtitle.
     **************************************************************************/
    public void setReadModsName(String in) {
        this.readmodsName = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setWriteModsName(String in) {
        this.writemodsName = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setInternalName(String in) {
        this.internalName = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setMODSType(String in) {
        this.modstype = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setMODSEncoding(String in) {
        this.modsencoding = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setMODSAuthority(String in) {
        this.modsauthority = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setMODSID(String in) {
        this.modsID = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setMODSTransliteration(String in) {
        this.modstransliteration = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setMODSScript(String in) {
        setMODSTransliteration(in);
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setMODSLang(String in) {
        this.modslang = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setMODSXMLLang(String in) {
        this.modsxmllang = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void isSkipped(boolean in) {
        this.skipped = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setRole(String in) {
        this.role = in;
    }

    /***************************************************************************
     * @return the firstnameXQuery
     **************************************************************************/
    public String getFirstnameXQuery() {
        return this.firstnameXQuery;
    }

    /***************************************************************************
     * @param firstnameXQuery the firstnameXQuery to set
     **************************************************************************/
    public void setFirstnameXQuery(String firstnameXQuery) {
        this.firstnameXQuery = firstnameXQuery;
    }

    /***************************************************************************
     * @return the lastnameXQuery
     **************************************************************************/
    public String getLastnameXQuery() {
        return this.lastnameXQuery;
    }

    /***************************************************************************
     * @param lastnameXQuery the lastnameXQuery to set
     **************************************************************************/
    public void setLastnameXQuery(String lastnameXQuery) {
        this.lastnameXQuery = lastnameXQuery;
    }

    /***************************************************************************
     * @return the affiliationXQuery
     **************************************************************************/
    public String getAffiliationXQuery() {
        return this.affiliationXQuery;
    }

    /***************************************************************************
     * @param affiliationXQuery the affiliationXQuery to set
     **************************************************************************/
    public void setAffiliationXQuery(String affiliationXQuery) {
        this.affiliationXQuery = affiliationXQuery;
    }

    /***************************************************************************
     * @return the persontypeXQuery
     **************************************************************************/
    public String getPersontypeXQuery() {
        return this.persontypeXQuery;
    }

    /***************************************************************************
     * @param persontypeXQuery the persontypeXQuery to set
     **************************************************************************/
    public void setPersontypeXQuery(String persontypeXQuery) {
        this.persontypeXQuery = persontypeXQuery;
    }

    /***************************************************************************
     * @return the xQuery
     **************************************************************************/
    public String getReadXQuery() {
        return this.readxQuery;
    }

    /***************************************************************************
     * @param query the xQuery to set
     **************************************************************************/
    public void setReadXQuery(String query) {
        this.readxQuery = query;
    }

    /***************************************************************************
     * Returns the write XQuery only, if available; otherwise the read XQuery is returned
     * 
     * @return the writexQuery
     **************************************************************************/
    public String getWriteXPath() {
        if (this.writexQuery == null) {
            return this.readxQuery;
        }
        return this.writexQuery;
    }

    /***************************************************************************
     * @param query the xQuery to set
     **************************************************************************/
    public void setWriteXQuery(String query) {
        this.writexQuery = query;
    }

    /***************************************************************************
     * @return the authorityFileIDXquery
     **************************************************************************/
    public String getAuthorityIDXquery() {
        return this.authorityIDXquery;
    }

    /***************************************************************************
     * @param authorityFileIDXquery the authorityFileIDXquery to set
     **************************************************************************/
    public void setAuthorityIDXquery(String authorityIDXquery) {
        this.authorityIDXquery = authorityIDXquery;
    }

    public String getAuthorityURIXquery() {
        return this.authorityURIXquery;
    }

    public void setAuthorityURIXquery(String authorityURIXquery) {
        this.authorityURIXquery = authorityURIXquery;
    }

    public String getAuthorityValueXquery() {
        return this.authorityValueXquery;
    }

    public void setAuthorityValueXquery(String authorityValueXquery) {
        this.authorityValueXquery = authorityValueXquery;
    }

    /***************************************************************************
     * @return the displayNameXQuery
     **************************************************************************/
    public String getDisplayNameXQuery() {
        return this.displayNameXQuery;
    }

    /***************************************************************************
     * @param displayNameXQuery the displayNameXQuery to set
     **************************************************************************/
    public void setDisplayNameXQuery(String displayNameXQuery) {
        this.displayNameXQuery = displayNameXQuery;
    }

    /***************************************************************************
     * @return the identifierTypeXQuery
     **************************************************************************/
    public String getIdentifierTypeXQuery() {
        return this.identifierTypeXQuery;
    }

    /***************************************************************************
     * @param identifierTypeXQuery the identifierTypeXQuery to set
     **************************************************************************/
    public void setIdentifierTypeXQuery(String identifierTypeXQuery) {
        this.identifierTypeXQuery = identifierTypeXQuery;
    }

    /***************************************************************************
     * @return the identifierXQuery
     **************************************************************************/
    public String getIdentifierXQuery() {
        return this.identifierXQuery;
    }

    public void setDatabaseXQuery(String databaseXQuery) {
        this.database = databaseXQuery;
    }

    public String getDatabaseXQuery() {
        return database;
    }

    /***************************************************************************
     * @param identifierXQuery the identifierXQuery to set
     **************************************************************************/
    public void setIdentifierXQuery(String identifierXQuery) {
        this.identifierXQuery = identifierXQuery;
    }

    /***************************************************************************
     * @return the valueCondition
     **************************************************************************/
    public String getValueCondition() {
        return this.valueCondition;
    }

    /***************************************************************************
     * @param valueCondition the valueCondition to set
     **************************************************************************/
    public void setValueCondition(String valueCondition) {
        this.valueCondition = valueCondition;
    }

    /***************************************************************************
     * @return the valueRegExp
     **************************************************************************/
    public String getValueRegExp() {
        return this.valueRegExp;
    }

    /***************************************************************************
     * @param valueCondition the valueCondition to set
     **************************************************************************/
    public void setValueRegExp(String valueRegExp) {
        this.valueRegExp = valueRegExp;
    }

    /**************************************************************************
     * @return
     **************************************************************************/
    public String getModsGrouping() {
        return this.modsGrouping;
    }

    /**************************************************************************
     * @param modsGrouping
     **************************************************************************/
    public void setModsGrouping(String modsGrouping) {
        this.modsGrouping = modsGrouping;
    }

    public Map<String, Map<String, String>> getMetadataGroupXQueries() {
        return metadataGroupXQueries;
    }

    public void setMetadataGroupXQueries(Map<String, Map<String, String>> metadataGroupXQueries) {
        this.metadataGroupXQueries = metadataGroupXQueries;
    }

    public void addToMap(String key, Map<String, String> value) {
        metadataGroupXQueries.put(key, value);
    }
}
