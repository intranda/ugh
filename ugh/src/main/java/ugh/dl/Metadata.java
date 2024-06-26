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
import lombok.extern.log4j.Log4j2;
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

@Log4j2
public class Metadata implements Serializable {

    private static final long serialVersionUID = -2535548431060378914L;

    protected MetadataType MDType;
    // Document structure or group to which this metadata type belongs to.
    @Getter
    @Setter
    protected HoldingElement parent;

    private String metadataValue;
    private String MetadataVQ;
    private String MetadataVQType;

    // Contains the native object; e.g. the element of a DOM tree
    @Deprecated
    private Object nativeObject;

    private String authorityURI;

    private String authorityID;

    private String authorityValue;

    private boolean updated = false;

    private Map<String, String> authorityUriMap = new HashMap<>();

    @Getter
    @Setter
    private boolean validationErrorPresent;
    @Getter
    @Setter
    private String validationMessage;

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

    //    /***************************************************************************
    //     * <p>
    //     * Sets the Document structure entity to which this object belongs to.
    //     * </p>
    //     *
    //     * @param inDoc
    //     **************************************************************************/
    //    public void setDocStruct(DocStruct inDoc) {
    //        this.myDocStruct = inDoc;
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Returns the DocStruct instance, to which this metadata object belongs. This is extremly helpful, if only the metadata instance is stored in a
    //     * list; the reference to the associated DocStrct instance is always kept.
    //     * </p>
    //     *
    //     * @return DocStruct instance.
    //     **************************************************************************/
    //    @JsonIgnore
    //    public DocStruct getDocStruct() {
    //        return this.myDocStruct;
    //    }

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
     * TODO For future versions: Check, if the value is of the correct type?
     * 
     * @param inValue The value as String.
     **************************************************************************/
    public void setValue(String inValue) {
        // When the same value is set again, should we change the `updated` flag? - Zehong
        this.metadataValue = inValue;
        this.updated = true;
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

        this.MetadataVQ = inVQ;
        this.MetadataVQType = inVQType;

    }

    /***************************************************************************
     * <p>
     * Retrieves the value of the ValueQualifier.
     * </p>
     * 
     * @return Value of ValueQualifier as String.
     **************************************************************************/
    public String getValueQualifier() {
        return this.MetadataVQ;
    }

    /***************************************************************************
     * <p>
     * Retrieves the type of the ValueQualifier.
     * </p>
     * 
     * @return Type of ValueQualifier as string.
     **************************************************************************/
    public String getValueQualifierType() {
        return this.MetadataVQType;
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
        return Objects.hash(MDType, MetadataVQ, MetadataVQType, authorityValue, metadataValue);
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

    /***************************************************************************
     * <p>
     * Overwritten method compares this MetaData with parameter metadata.
     * </p>
     * 
     * @author Wulf Riebensahm
     * @return TRUE if type and value are the same.
     * @param MetaData metadata
     **************************************************************************/
    //    @Override
    //    public boolean equals(Object other) {
    //
    //        Metadata metadata = (Metadata) other;
    //
    //        if (metadata == null) {
    //            return false;
    //        }
    //        if (this.getClass() != metadata.getClass()) {
    //            return false;
    //        }
    //
    //        log.debug("\r\n" + "metaData getClass()=" + this.getClass() + " ->id:" + this.getType().getName());
    //
    //        if (!(this.getType().equals(metadata.getType()))) {
    //            return false;
    //        }
    //
    //        // Processing Strings in a try block.
    //        try {
    //            log.debug("Values: md1/md2 " + this.getValue() + "/" + metadata.getValue());
    //            if (!((this.getValue() == null && metadata.getValue() == null) || this.getValue().equals(metadata.getValue()))) {
    //                log.debug("false returned");
    //                return false;
    //            }
    //
    //            if (!((this.getValueQualifier() == null && metadata.getValueQualifier() == null) || this.getValueQualifier()
    //                    .equals(
    //                            metadata.getValueQualifier()))) {
    //                log.debug("false returned");
    //                return false;
    //            }
    //
    //            if (!((this.getValueQualifierType() == null && metadata.getValueQualifierType() == null) || this.getValueQualifierType()
    //                    .equals(
    //                            metadata.getValueQualifierType()))) {
    //                log.debug("false returned");
    //                return false;
    //            }
    //        }
    //        // TODO Teldemokles says: "Do never catch a NullPointerException"!
    //        catch (NullPointerException npe) {
    //            log.debug("NPE thrown and caught");
    //            return false;
    //        }
    //
    //        log.debug("true returned");
    //        return true;
    //    }

}
