package ugh.dl;

/*******************************************************************************
 * ugh.dl / Metadata.java
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;
import ugh.exceptions.MetadataTypeNotAllowedException;

/*******************************************************************************
 * <p>
 * A Metadata object represents a single Metadata element. Each Metadata element has at least a value and a type. The type of a metadata element is
 * stored as a <code>MetadataType</code> object.
 * </p>
 * 
 * <p>
 * Metadata can be any kind of data, which can be attached to a structure element. The most common metadata, which is available for almost any
 * structure element is a title.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2010-02-14
 * @see MetadataType
 * 
 *      CHANGELOG
 * 
 *      14.02.2010 --- Funk --- Added method toString().
 * 
 *      30.11.2009 --- Funk --- Again removed deprecated Metadata() constructor.
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      10.11.2009 --- Funk --- Removed deprecated Metadata() constructor.
 * 
 *      06.10.2009 --- Funk --- Adapted metadata and person constructors.
 * 
 *      06.05.2009 --- Wulf Riebensahm --- equals() method overloaded.
 * 
 ******************************************************************************/

public class Metadata implements Serializable {

    private static final long serialVersionUID = -2535548431060378914L;

    protected MetadataType MDType; // NOSONAR must be upper case, otherwise XStream documents cannot be read
    // Document structure or group to which this metadata type belongs to.
    @Getter
    @Setter
    protected transient HoldingElement parent;

    private String metadataValue;
    private String metadataVQ;
    private String metadataVQType;

    // Contains the native object; e.g. the element of a DOM tree
    @Deprecated
    private Object nativeObject;

    private String authorityURI;

    private String authorityID;

    private String authorityValue;

    private Map<String, String> authorityUriMap = new HashMap<>();

    @Getter
    @Setter
    private boolean validationErrorPresent;
    @Getter
    @Setter
    private String validationMessage;

    @Getter
    @Setter
    private boolean accessRestrict;

    @Deprecated
    public Metadata() {

    }

    /***************************************************************************
     * <p>
     * Constructor.
     * </p>
     * 
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
    public Metadata(MetadataType theType) throws MetadataTypeNotAllowedException {

        super();

        // Check for NULL MetadataTypes.
        if (theType == null) {
            String message = "MetadataType must not be null at Metadata creation!";
            throw new MetadataTypeNotAllowedException(message);
        }

        this.MDType = theType;
    }

    /***************************************************************************
     * <p>
     * Returns the type of the metadata instance; The MetadataType object which is returned, may have the same name, but be a different object than
     * the MetadataType object from another MetadataType.
     * </p>
     * 
     * @return MetadataType instance
     **************************************************************************/
    public MetadataType getType() {
        return this.MDType;
    }

    /***************************************************************************
     * <p>
     * Sets the MetadataType for this instance; only a MetadataType instance is used as the only parameter. The method returns true if MDType was set;
     * false if not.
     * </p>
     * 
     * @param inType
     * @return
     **************************************************************************/
    public void setType(MetadataType inType) {
        this.MDType = inType;
    }

    /***************************************************************************
     * <p>
     * Gets the Value of the Metadata object; is always a string value all types are converted to unicode strings and must be converted by the user.
     * </p>
     * 
     * @return String containing the value.
     **************************************************************************/
    public String getValue() {
        return this.metadataValue;
    }

    /***************************************************************************
     * <p>
     * Sets the Metadata value, and the flag `updated` to true or false according to the correctness of the value setting. The only parameter is the
     * value of the type String, all other types (integer, long etc.) must be converted to a string before.
     * </p>
     * 
     * 
     * @param inValue The value as String.
     **************************************************************************/
    public void setValue(String inValue) {
        this.metadataValue = inValue;
    }

    /***************************************************************************
     * <p>
     * If a metadata element should be linked to any record in an authority file (e.g. authority file for locations or persons), it can be done,
     * setting this ID. If no authority file exists, the ID can be used to link different metadata elements together; each element reprents a version
     * of this system.
     * </p>
     * 
     * @param fileID ID for this metadata in some authority file.
     * 
     * @param value value of the record in the authority file
     * 
     **************************************************************************/
    @Deprecated
    public void setAutorityFile(String authorityID, String authorityURI, String authorityValue) {
        setAuthorityFile(authorityID, authorityURI, authorityValue);
    }

    public void setAuthorityFile(String authorityID, String authorityURI, String authorityValue) {
        this.authorityID = authorityID;
        this.authorityURI = authorityURI;
        this.authorityValue = authorityValue;
    }

    public void setAuthorityID(String authorityID) {
        this.authorityID = authorityID;
    }

    public void setAuthorityURI(String authorityURI) {
        this.authorityURI = authorityURI;
    }

    public void setAuthorityValue(String authorityValue) {
        this.authorityValue = authorityValue;
    }

    /**
     * Add a new authority uri to the list.
     * 
     * @param authorityFile
     * @param uri
     */

    public void addAuthorityUriToMap(String authorityFile, String uri) {
        if (StringUtils.isBlank(authorityFile)) {
            throw new IllegalArgumentException("Empty identifier is not allowed for an authority file!");
        }
        authorityUriMap.put(authorityFile, uri);
    }

    /**
     * remove authority information from the list
     * 
     * @param authorityFile
     */

    public void removeAuthorityUriFromMap(String authorityFile) {
        if (authorityUriMap.containsKey(authorityFile)) {
            authorityUriMap.remove(authorityFile);
        }
    }

    public Map<String, String> getAuthorityUriMap() {
        return authorityUriMap;
    }

    /***************************************************************************
     * <p>
     * Returns the ID from the authority file.
     * </p>
     * 
     * @return Identifier from authority file.
     **************************************************************************/
    public String getAuthorityID() {
        return this.authorityID;
    }

    /***************************************************************************
     * <p>
     * Returns the ID from the value in the authority file.
     * </p>
     * 
     * @return Identifier from value.
     **************************************************************************/
    public String getAuthorityURI() {
        return this.authorityURI;
    }

    /***************************************************************************
     * <p>
     * Returns the ID from the value in the authority file.
     * </p>
     * 
     * @return Identifier from value.
     **************************************************************************/
    public String getAuthorityValue() {
        return this.authorityValue;
    }

    /***************************************************************************
     * <p>
     * Deprecated method for setValueQualifier().
     * </p>
     * 
     * @param inVQ ValueQualifier as a string.
     * @param inVQType Type of ValueQualifier as a string.
     * @deprecated
     **************************************************************************/
    @Deprecated
    public void setValueQualitifer(String inVQ, String inVQType) {
        setValueQualifier(inVQ, inVQType);
    }

    /***************************************************************************
     * <p>
     * Sets a value qualifier; a value qualifier qualifies the value; e.g. to a Metadata instance, the language of the value can be stored. For this
     * example the value-qualifier would be "en" for english, the type of the value qualifier would be "language". There can only be one ValueQualfier
     * for a Metadata-object. There is no controlled vocabulary for the type of ValueQualifier. If a ValueQualifier is set, both type and value must
     * be set.
     * </p>
     * 
     * @param inVQ ValueQualifier as a string.
     * @param inVQType Type of ValueQualifier as a string.
     * @return TRUE if successful, if ValueQualifier or type of ValueQualifier is not set, FALSE is returned.
     **************************************************************************/
    public void setValueQualifier(String inVQ, String inVQType) {

        if (inVQ == null || inVQType == null) {
            return;
        }

        this.metadataVQ = inVQ;
        this.metadataVQType = inVQType;

    }

    /***************************************************************************
     * <p>
     * Retrieves the value of the ValueQualifier.
     * </p>
     * 
     * @return Value of ValueQualifier as String.
     **************************************************************************/
    public String getValueQualifier() {
        return this.metadataVQ;
    }

    /***************************************************************************
     * <p>
     * Retrieves the type of the ValueQualifier.
     * </p>
     * 
     * @return Type of ValueQualifier as string.
     **************************************************************************/
    public String getValueQualifierType() {
        return this.metadataVQType;
    }

    /***************************************************************************
     * <p>
     * Sets the native object. The native object is a java Object which may represent anything; e.g. a representation of this Metadata instance in a
     * database.
     * </p>
     * 
     **************************************************************************/
    @Deprecated
    public void setNativeObject(Object inObj) {
        this.nativeObject = inObj;
    }

    /***************************************************************************
     * <p>
     * Retrieves the native object. This is used especially for updating files.
     * </p>
     * 
     * @return Can be any kind of java object.
     **************************************************************************/
    @Deprecated
    public Object getNativeObject() {
        return this.nativeObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        if (this.getType() != null) {
            // Get metadata type and value.
            result.append("Metadata (")
                    .append(this.getType().getName())
                    .append("): ")
                    .append(this.getValue() != null ? "\"" + this.getValue() + "\"" : "NULL")
                    .append("\n");
        } else {
            // Get metadata values without type.
            result.append("Metadata (WITHOUT TYPE!!): ").append(this.getValue() != null ? "\"" + this.getValue() + "\"" : "NULL").append("\n");
        }

        return result.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(MDType, metadataVQ, metadataVQType, authorityValue, metadataValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Metadata other = (Metadata) obj;
        return Objects.equals(MDType, other.MDType) && Objects.equals(getValueQualifier(), other.getValueQualifier())
                && Objects.equals(getValueQualifierType(), other.getValueQualifierType())
                && Objects.equals(getValue(), other.getValue());
    }
}
