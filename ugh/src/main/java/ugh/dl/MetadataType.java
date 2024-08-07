package ugh.dl;

/*******************************************************************************
 * ugh.dl / MetadataType.java
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

/*******************************************************************************
 * <p>
 * When using, storing, writing or reading metadata, groups or classes of special metadata objects can be formed, which have something in common. They
 * are all of the same kind. Metadata of the same kind can be stored using the same MetadataType object. Each MetadataType object can be identified
 * easily by using its internal name.
 * </p>
 * 
 * <p>
 * Besides the internal name, a MetadataType object contains information, about it occurences; some metadata may occur just once, other may occur many
 * times.
 * </p>
 * <p>
 * E.g. for all titles of a document there can be a seperate MetadataType element, which contains information about this class of metadata elements.
 * Information which they share are information about their occurences; each structure entity can only have a single title.<BR>
 * MetadataType objects can occur in two different ways:
 * </p>
 * 
 * <ul>
 * <li>globally
 * <li>locally
 * </ul>
 * 
 * <p>
 * <b>Global</b> <code>MetadataType</code> objects can be retrieved from the <code>Prefs</code> object by giving the internal name. Some of the
 * information of a MetadataType object depends on the context in which it is used. Context means it depends on the <code>DocStructType</code> object,
 * in which a MetadataType object is used. When adding a <code>MetadataType</code> object to a <DocStructType> object, an internal copy is created and
 * stored with the <code>DocStructType</code> object. This copy is called locally and may store information about its occurences in this special
 * <code>DocStructType</code> object. The <code>DocStructType</code> class contains methods to retrieve local <code>MetadataType</code> objects from
 * global ones.
 * </p>
 * <p>
 * <code>MetadataType</code> objects are used, to create new <code>Metadata</code> objects. They are the only parameter in the constructor of the
 * <code>Metadata</code> object.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2010-02-13
 * @see Metadata#setType
 * @see DocStructType#getMetadataTypeByType
 * 
 *      CHANGELOG
 * 
 *      13.02.2010 -- Funk --- Refactored some whiles and iterators.
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      06.05.2009 --- Wulf Riebensahm --- equals() method overloaded.
 * 
 ******************************************************************************/

public class MetadataType implements Serializable, PrefsType {

    private static final long serialVersionUID = 1285824825128157626L;

    // Unique name of MetadataType.
    private String name;

    // Maximum number of occurences of this MetadataType for one DocStrct (can
    // be 1 (1), one or more (+) or as many as you want (*).
    private String maxNumber = "1o";

    // Hash containing all languages.
    private Map<String, String> allLanguages;

    // Is set to true, if metadata is a person.
    protected boolean isPerson = false;

    // is set to true, if metadata is a corporate
    @Getter
    @Setter
    protected boolean isCorporate = false;

    // Is set to true, if this MetadataType acts as an element; which means,
    // that a metadata with the same value cannot be available twice.
    protected boolean isIdentifier = false;

    private boolean allowNameParts = false;
    private boolean allowNormdata = false;

    @Getter
    @Setter
    protected boolean allowAccessRestriction = false;

    private String validationExpression = "";

    @Getter
    @Setter
    private Map<String, String> validationErrorMessages;

    /***************************************************************************
     * Constructor.
     **************************************************************************/
    public MetadataType() {
        super();
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setName(String in) {
        this.name = in;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setNum(String in) {
        if (StringUtils.isBlank(in) || (!"1m".equals(in) && !"1o".equals(in) && !"+".equals(in) && !"*".equals(in))) {
            // Unknown syntax.
        } else {
            this.maxNumber = in;
        }
    }

    /***************************************************************************
     * <p>
     * Creates an exact copy of this MetadataType instance. This method is used, when adding a MetadataType to a <code>DocStructType</code>.
     * </p>
     * <p>
     * The copy contains the same languages, the same internal name and the number of possible occurences as the original.
     * </p>
     * 
     * @return the newly created MetadataType
     * @see DocStructType
     **************************************************************************/
    public MetadataType copy() {

        MetadataType newMDType = new MetadataType();

        newMDType.setAllLanguages(this.allLanguages);
        newMDType.setName(this.name);
        if (this.maxNumber != null) {
            newMDType.setNum(this.maxNumber);
        }
        newMDType.setIdentifier(this.isIdentifier());
        newMDType.setIsPerson(this.isPerson);
        newMDType.setCorporate(isCorporate);
        newMDType.setAllowNameParts(this.allowNameParts);
        newMDType.setAllowNormdata(this.allowNormdata);
        newMDType.setAllowAccessRestriction(allowAccessRestriction);
        newMDType.setValidationExpression(validationExpression);
        newMDType.setValidationErrorMessages(validationErrorMessages);
        return newMDType;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    @Override
    public String getName() {
        return this.name;
    }

    /***************************************************************************
     * <p>
     * If the MetadataType is an identifier, a Metadata instance of this type and a specific value can only be available once. Usually the identifier
     * should be unique at least within the document. The identifier can be used to reference from other documents / parts of documents to a DocStruct
     * instance.
     * </p>
     * 
     * @return the isIdentifier
     **************************************************************************/
    public boolean isIdentifier() {
        return this.isIdentifier;
    }

    /***************************************************************************
     * @param isIdentifier the isIdentifier to set
     **************************************************************************/
    public void setIdentifier(boolean isIdentifier) {
        this.isIdentifier = isIdentifier;
    }

    /***************************************************************************
     * <p>
     * Retrieves the number of possible Metadata objects for a DocStruct. This is now based on the type of DocStruct and is therefor stored in the
     * DocStructType.
     * </p>
     * 
     * @return number of MetadataType
     **************************************************************************/
    public String getNum() {
        return this.maxNumber;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setAllLanguages(Map<String, String> in) {
        this.allLanguages = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    @Override
    public Map<String, String> getAllLanguages() {
        return this.allLanguages;
    }

    /***************************************************************************
     * <p>
     * Adds a name (in the given language) for this instance of MetadataType.
     * </p>
     * 
     * @param lang language code
     * @param value name of the metadata type in the given language
     * @return true, if successful
     **************************************************************************/
    @Override
    public void addLanguage(String theLanguage, String theValue) {
        // null should not be used as key
        if (theLanguage == null) {
            return;
        }
        if (this.allLanguages == null) {
            this.allLanguages = new HashMap<>();
        }
        this.allLanguages.put(theLanguage, theValue);
    }

    /***************************************************************************
     * <p>
     * Retrieves the name for a certain language.
     * </p>
     * 
     * @param lang language code
     * @return the translation of this MetadataType; or null, if it has no translation for this language.
     **************************************************************************/
    @Override
    public String getNameByLanguage(String lang) {
        if (this.allLanguages == null) {
            this.allLanguages = new HashMap<>();
        }
        return this.allLanguages.get(lang); // might be null
    }

    /***************************************************************************
     * <p>
     * Changes the name of this instance for a certain language.
     * </p>
     * 
     * @param lang language code
     * @param content new name
     **************************************************************************/
    public void changeLanguageByName(String lang, String content) {
        if (this.allLanguages == null) {
            this.allLanguages = new HashMap<>();
        }
        removeLanguage(lang);
        addLanguage(lang, content);
    }

    /***************************************************************************
     * <p>
     * Removes a language for this MetadataType instance.
     * </p>
     * 
     * @param lang language code
     * @return true, if successful
     **************************************************************************/
    public boolean removeLanguage(String theLanguage) {
        if (theLanguage == null) {
            return false;
        }
        if (this.allLanguages == null) {
            this.allLanguages = new HashMap<>();
        }

        // Check, if language already is available, if so, remove it.
        for (Map.Entry<String, String> lang : this.allLanguages.entrySet()) {
            if (lang.getKey().equals(theLanguage)) {
                this.allLanguages.remove(lang.getKey());
                return true;
            }
        }

        // Language unavailable, could not be removed.
        return false;
    }

    /***************************************************************************
     * @param language
     * @return
     **************************************************************************/
    public String getLanguage(String theLanguage) {
        if (this.allLanguages == null) {
            this.allLanguages = new HashMap<>();
        }

        // Find language "inLanguage".
        for (Map.Entry<String, String> lang : getAllLanguages().entrySet()) {
            if (lang.getKey().equals(theLanguage)) {
                return lang.getValue();
            }
        }

        return null;
    }

    /***************************************************************************
     * @param value
     **************************************************************************/
    public void setIsPerson(boolean value) {
        this.isPerson = value;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public boolean getIsPerson() {
        return this.isPerson;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isCorporate, isIdentifier, isPerson, name);
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
        MetadataType other = (MetadataType) obj;
        return isCorporate == other.isCorporate && isIdentifier == other.isIdentifier && isPerson == other.isPerson
                && Objects.equals(name, other.name);
    }

    public boolean isAllowNameParts() {
        return allowNameParts;
    }

    public void setAllowNameParts(boolean allowNameParts) {
        this.allowNameParts = allowNameParts;
    }

    public boolean isAllowNormdata() {
        return allowNormdata;
    }

    public void setAllowNormdata(boolean allowNormdata) {
        this.allowNormdata = allowNormdata;
    }

    public String getValidationExpression() {
        return validationExpression;
    }

    public void setValidationExpression(String validationExpression) {
        this.validationExpression = validationExpression;
    }
}
