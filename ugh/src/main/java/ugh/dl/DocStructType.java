package ugh.dl;

/*******************************************************************************
 * ugh.dl / DocStructType.java
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*******************************************************************************
 * <p>
 * A <code>DocStructType</code> object defines a kind of class to which a structure entitiy (represented by a DocStruct object) belongs. All DocStruct
 * objects belonging to a similar class have something in common (e.g. possible children, special kind of metadata which can be available for a class,
 * a naming etc...). These things are stored in a DocStructType object.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2013-05-08
 * @see DocStruct#setType
 * 
 *      CHANGELOG
 * 
 *      13.02.2010 --- Funk --- Refcatored some overloaded methods, and set some methods deprecated.
 * 
 *      22.01.2010 --- Funk --- Improvements due to findbugs.
 * 
 *      21.12.2009 --- Funk --- Added method toString().
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      20.10.2009 --- Funk --- Added some modifiers for class attributes.
 * 
 *      11.09.2009 --- Wulf Riebensahm --- equals() method overloaded.
 * 
 *      24.10.2008 --- Funk --- Commented out the field myPrefs and its getter and setter methods. We do not need that!
 * 
 ******************************************************************************/

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocStructType implements Serializable, PrefsType {

    private static final long serialVersionUID = -3819246407494198735L;

    private String name;
    // Set to true if DocStructType can be anchor.
    private boolean isanchor = false;
    // Preferences, which created this instance.
    private boolean hasfileset = true;
    private boolean topmost = false;

    // Map containing name of this DocStrctType for the appropriate languages.
    protected transient Map<String, String> allLanguages;

    // List containing all Metadatatypes (MetadataTypeForDocStructType
    // instances). PLEASE NOTE: Some tricky inheriting od something else things
    // do not let parameterise this attribute! Don't worry! Nevertheless, it
    // works!
    protected transient List<MetadataTypeForDocStructType> allMetadataTypes;

    // LinkedList containing all possible document structure types which might
    // be children of this one here.
    protected transient List<String> allChildrenTypes;

    private transient List<MetadataGroupForDocStructType> allMetadataGroups;

    /***************************************************************************
     * <p>
     * List does not containg DocStructType objects but just the name (so just Strings).
     * </p>
     **************************************************************************/
    public DocStructType() {
        this.allChildrenTypes = new LinkedList<>();
        this.allMetadataTypes = new LinkedList<>();
        this.allMetadataGroups = new LinkedList<>();
        this.allLanguages = new HashMap<>();
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setName(String in) {
        this.name = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    @Override
    public String getName() {
        return this.name;
    }

    /***************************************************************************
     * @deprecated replaced by isTopmost
     * @return true, if this DocStructType is the topmost element
     **************************************************************************/
    @Deprecated
    public boolean isTopMost() {
        return this.topmost;
    }

    /***************************************************************************
     * @deprecated replaced by setTopmost
     **************************************************************************/
    @Deprecated
    public void setTopMost(boolean in) {
        this.topmost = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public boolean hasFileSet() {
        return this.hasfileset;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setHasFileSet(boolean in) {
        this.hasfileset = in;
    }

    /***************************************************************************
     * @return the hasfileset
     **************************************************************************/
    public boolean isHasfileset() {
        return this.hasfileset;
    }

    /***************************************************************************
     * @param hasfileset the hasfileset to set
     **************************************************************************/
    public void setHasfileset(boolean hasfileset) {
        this.hasfileset = hasfileset;
    }

    /***************************************************************************
     * @deprecated
     * @return the isanchor
     **************************************************************************/
    @Deprecated
    public boolean isIsanchor() {
        return this.isanchor;
    }

    /***************************************************************************
     * @param isanchor the isanchor to set
     * @deprecated
     **************************************************************************/
    @Deprecated
    public void setIsanchor(boolean isanchor) {
        this.isanchor = isanchor;
    }

    /***************************************************************************
     * @return the topmost
     **************************************************************************/
    public boolean isTopmost() {
        return this.topmost;
    }

    /***************************************************************************
     * @param topmost the topmost to set
     **************************************************************************/
    public void setTopmost(boolean topmost) {
        this.topmost = topmost;
    }

    /***************************************************************************
     * <p>
     * Sets information, wether this type is an anchor (virtual structure entity) or not.
     * </p>
     * 
     * @param inBool
     **************************************************************************/
    public void isAnchor(boolean inBool) {
        this.isanchor = inBool;
    }

    /***************************************************************************
     * <p>
     * Retrieves information, wether this type is an anchor or not. An anchor is a special type of document structure, which groups other structure
     * entities togehter, but has no own content. E.h. a periodical as such can be an anchor. The perodical itself is a virtual structure entity
     * without any own content, but groups all volumes together.
     * </p>
     * 
     * @return boolean, which is set to true, if it can be used as an anchor
     **************************************************************************/
    public boolean isAnchor() {
        return this.isanchor;
    }

    /***************************************************************************
     * <p>
     * Set a HashMap, which contain translations of the name of a DocStructType into several languages (e.g. for display in an user-interface). The
     * key in the HashMap is the language code (iso-two-letter code) The value in the HashMap is the translation This methods replaces all other
     * language information for this DocStructType object.
     * </p>
     * 
     * @param in HashMap containing language code and value
     **************************************************************************/
    public void setAllLanguages(Map<String, String> in) {

        this.allLanguages = in;
    }

    /***************************************************************************
     * <p>
     * Retrieves all languages as a HashMap.
     * </p>
     * 
     * @return HashMap with key/value pairs; key= language code; value= translation in this language
     **************************************************************************/
    @Override
    public Map<String, String> getAllLanguages() {
        return this.allLanguages;
    }

    /***************************************************************************
     * <p>
     * Adds a translation (into a given language).
     * </p>
     * 
     * @param lang two-letter code of the language
     * @param value translation of this StructType
     * @return true; if translation is already available false is returned
     **************************************************************************/
    @Override
    public void addLanguage(String lang, String value) {
        if (lang == null) {
            return;
        }
        this.allLanguages.put(lang, value);
    }

    /***************************************************************************
     * <p>
     * Retrieves the name for a certain language.
     * </p>
     * 
     * @param lang language code
     * @return name of this DocStructType in the specified language; or null if no translation is available
     **************************************************************************/
    @Override
    public String getNameByLanguage(String lang) {

        String languageName = this.allLanguages.get(lang);
        if (languageName == null) {
            return null;
        }

        return languageName;
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
        removeLanguage(lang);
        addLanguage(lang, content);
    }

    /***************************************************************************
     * <p>
     * Removes a translation.
     * </p>
     * 
     * @param lang two-letter code of language, which should be removed
     * @return true, if successful; otherwise false
     **************************************************************************/
    public boolean removeLanguage(String lang) {

        Map.Entry<String, String> test;
        String key;

        for (Entry<String, String> element : this.allLanguages.entrySet()) {
            test = element;
            key = test.getKey();
            if (key.equals(lang)) {
                this.allLanguages.remove(lang);
                // Language is available, so remove it.
                return true;
            }
        }

        // Language unavailable, could not be removed.
        return false;
    }

    /***************************************************************************
     * <p>
     * Add and remove metadatatypes.
     * </p>
     * 
     * @param in
     **************************************************************************/
    public void setAllMetadataTypes(List<MetadataType> in) {
        if (in != null) {
            allMetadataTypes.clear();
            for (MetadataType mdt : in) {
                MetadataTypeForDocStructType mdtfdst = new MetadataTypeForDocStructType(mdt);
                this.allMetadataTypes.add(mdtfdst);
            }
        }
    }

    /***************************************************************************
     * <p>
     * Retrieves all MetadataType objects for this DocStructType instance.
     * </p>
     * 
     * @return List containing MetadataType objects; These MetadataType-objects are just local objects
     **************************************************************************/
    public List<MetadataType> getAllMetadataTypes() {

        List<MetadataType> out = new LinkedList<>();

        for (MetadataTypeForDocStructType mdtfdst : this.allMetadataTypes) {
            out.add(mdtfdst.getMetadataType());
        }

        return out;
    }

    /***************************************************************************
     * <p>
     * Retrieves all MetadataType objects for this DocStructType instance, that have the "DefaultDisplay" attribute in the configuration set to
     * "true".
     * </p>
     * 
     * @return List containing MetadataType objects; These MetadataType objects are just local objects.
     **************************************************************************/
    public List<MetadataType> getAllDefaultDisplayMetadataTypes() {

        List<MetadataType> out = new LinkedList<>();

        for (MetadataTypeForDocStructType mdtfdst : this.allMetadataTypes) {
            if (mdtfdst.isDefaultdisplay()) {
                out.add(mdtfdst.getMetadataType());
            }
        }

        return out;
    }

    /**************************************************************************
     * <p>
     * Deprecated method, please use getAllDefaultDisplayMetadataTypes() in the future.
     * </p>
     * 
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public List<MetadataType> getAllDefaultMetadataTypes() {
        return getAllDefaultDisplayMetadataTypes();
    }

    /***************************************************************************
     * <p>
     * Gets the number of metadata objects, which are possible for a special MetadataType for this special document structure type. MetadataTypes are
     * compared using the internal name.
     * </p>
     * 
     * @param inType MetadataType - can be a global type
     * @return String containing the number (number can be: "1o", "1m", "*", "+")
     **************************************************************************/
    public String getNumberOfMetadataType(PrefsType inType) {
        if (inType != null) {
            for (MetadataTypeForDocStructType mdtfdst : this.allMetadataTypes) {
                if (mdtfdst.getMetadataType().getName().equals(inType.getName())) {
                    return mdtfdst.getNumber();
                }
            }
        }
        return "0";
    }

    /***************************************************************************
     * <p>
     * Adds a MetadataType object to this DocStructType instance; this means, that all document structures of this type can have at least one metadata
     * object of this type because for each DocStructType object we have separate MetadataType objects, the MetadataType instance given as the only
     * parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
     * </p>
     * 
     * @param type MetadataType object which should be added
     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
     * @return newly created copy of the MetadataType object; if not successful null is returned
     **************************************************************************/
    public PrefsType addMetadataType(MetadataType type, String inNumber) {

        // New MetadataType obejct which is added to this DocStructType.
        MetadataType myType;

        // Metadata is already available.
        if (type == null || isMetadataTypeAlreadyAvailable(type)) {
            return null;
        }

        // Make a copy of this object and add the copy - necessary, cause we
        // have own instances for each document structure type.
        myType = type.copy();

        MetadataTypeForDocStructType mdtfdst = new MetadataTypeForDocStructType(myType);
        mdtfdst.setNumber(inNumber);
        this.allMetadataTypes.add(mdtfdst);

        return myType;
    }

    /***************************************************************************
     * <p>
     * Adds a MetadataType object to this DocStructType instance; this means, that all document structures of this type can have at least one metadata
     * object of this type because for each DocStructType object we have separate MetadataType objects, the MetadataType instance given as the only
     * parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
     * </p>
     * 
     * @param type MetadataType object which should be added
     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
     * @param isDefault if set to true, this metadatatype will be displayed (even if it's empty)
     * @return newly created copy of the MetadataType object; if not successful null is returned
     **************************************************************************/
    public PrefsType addMetadataType(MetadataType type, String inNumber, boolean isDefault, boolean isInvisible) {

        // New MetadataType obejct which is added to this DocStructType.
        MetadataType myType;

        // Metadata is already available.
        if (isMetadataTypeAlreadyAvailable(type)) {
            return null;
        }

        // Make a copy of this object and add the copy - necessary, cause we
        // have own instances for each document structure type.
        myType = type.copy();

        MetadataTypeForDocStructType mdtfdst = new MetadataTypeForDocStructType(myType);
        mdtfdst.setNumber(inNumber);
        mdtfdst.setDefaultdisplay(isDefault);
        mdtfdst.setInvisible(isInvisible);
        this.allMetadataTypes.add(mdtfdst);

        return myType;
    }

    /***************************************************************************
     * <p>
     * Checks, if the MetadataType has already been added and is already available in the list of all MetadataTypes.
     * </p>
     * 
     * @param type
     * @return true, if is is already available
     **************************************************************************/
    private boolean isMetadataTypeAlreadyAvailable(PrefsType type) {

        MetadataTypeForDocStructType test;
        String testname;
        String typename;

        for (MetadataTypeForDocStructType element : this.allMetadataTypes) {
            test = element;
            PrefsType mdt = test.getMetadataType();
            testname = mdt.getName();
            typename = type.getName();

            if (testname.equals(typename)) {
                // It is already available.
                return true;
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Removes a MetadataType object.
     * </p>
     * 
     * @param type MetadataType
     * @return true if successful, otherwise false
     **************************************************************************/
    public boolean removeMetadataType(MetadataType type) {

        List<MetadataTypeForDocStructType> ll = new LinkedList<>(this.allMetadataTypes);

        for (MetadataTypeForDocStructType mdtfdst : ll) {
            if (mdtfdst.getMetadataType().equals(type)) {
                this.allMetadataTypes.remove(mdtfdst);
                return true;
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Retrieves the local MetadataType object (created when adding a global MetadataType object). This is necessary, if you just like to have the
     * global MetadataType (from Preferences).
     * </p>
     * 
     * @param inMDType global MetadataType object (from Preferences)
     * @return MetadataType or null, if not available for this DocStructType
     **************************************************************************/
    public MetadataType getMetadataTypeByType(PrefsType inMDType) {

        for (MetadataTypeForDocStructType mdtfdst : this.allMetadataTypes) {
            MetadataType mdt = mdtfdst.getMetadataType();

            if (mdt.getName().equals(inMDType.getName())) {
                return mdt;
            }
        }

        return null;
    }

    /***************************************************************************
     * @param inString
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public boolean addDocStructtypeasChild(String inString) {
        return addDocStructTypeAsChild(inString);
    }

    /***************************************************************************
     * <p>
     * Add another DocStructType, which might be a children only the name (as String) is stored in the list; not the DocStructType object itself.
     * </p>
     * 
     * @param inString
     * @return
     **************************************************************************/
    public boolean addDocStructTypeAsChild(String inString) {

        // Check if the DocStruct is not existing yet, and add it then.
        if (this.allChildrenTypes.isEmpty() || !this.allChildrenTypes.contains(inString)) {
            return this.allChildrenTypes.add(inString);
        }

        return false;
    }

    /***************************************************************************
     * @param inType
     * @return
     **************************************************************************/
    public boolean addDocStructTypeAsChild(DocStructType inType) {
        return addDocStructTypeAsChild(inType.getName());
    }

    /***************************************************************************
     * @param inType
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public boolean addDocStructtypeasChild(DocStructType inType) {
        return addDocStructTypeAsChild(inType);
    }

    /***************************************************************************
     * @param inString name of the DocStructType
     * @deprecated
     * @return true, if it was removed, otherwise false
     **************************************************************************/
    @Deprecated
    public boolean removeDocStructtypeasChild(String inString) {
        return removeDocStructTypeAsChild(inString);
    }

    /***************************************************************************
     * <p>
     * Removes the given type from the list of allowed children for the appropriate DocStruct.
     * </p>
     * 
     * @param inString name of the DocStructType
     * @return true, if it was removed, otherwise false
     **************************************************************************/
    public boolean removeDocStructTypeAsChild(String inString) {

        if (this.allChildrenTypes.remove(inString)) {
            return true;
        }

        return false;
    }

    /***************************************************************************
     * @param inType
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public boolean removeDocStructtypeasChild(DocStructType inType) {
        return removeDocStructTypeAsChild(inType);
    }

    /***************************************************************************
     * @param inType
     * @return
     **************************************************************************/
    public boolean removeDocStructTypeAsChild(DocStructType inType) {
        return removeDocStructTypeAsChild(inType.getName());
    }

    /***************************************************************************
     * <p>
     * Returns a List containing the names of all DocStructTypes which are allowed as children.
     * </p>
     * 
     * @return
     **************************************************************************/
    public List<String> getAllAllowedDocStructTypes() {
        return this.allChildrenTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getName();

    }

    /***************************************************************************
     * <p>
     * Add and remove MetadataGroups.
     * </p>
     * 
     * @param in
     **************************************************************************/
    public void setAllMetadataGroups(List<MetadataGroupType> in) {
        allMetadataGroups.clear();
        if (in != null) {
            for (MetadataGroupType mdt : in) {
                MetadataGroupForDocStructType mdtfdst = new MetadataGroupForDocStructType(mdt);
                this.allMetadataGroups.add(mdtfdst);
            }
        }
    }

    /***************************************************************************
     * <p>
     * Retrieves all MetadataGroup objects for this DocStructType instance.
     * </p>
     * 
     * @return List containing MetadataGroup objects; These MetadataGroup-objects are just local objects
     **************************************************************************/
    public List<MetadataGroupType> getAllMetadataGroupTypes() {

        List<MetadataGroupType> out = new LinkedList<>();

        for (MetadataGroupForDocStructType mdtfdst : this.allMetadataGroups) {
            out.add(mdtfdst.getMetadataGroup());
        }

        return out;
    }

    /***************************************************************************
     * <p>
     * Retrieves all MetadataGroup objects for this DocStructType instance, that have the "DefaultDisplay" attribute in the configuration set to
     * "true".
     * </p>
     * 
     * @return List containing MetadataGroup objects; These MetadataGroup objects are just local objects.
     **************************************************************************/
    public List<MetadataGroupType> getAllDefaultDisplayMetadataGroups() {

        List<MetadataGroupType> out = new LinkedList<>();

        for (MetadataGroupForDocStructType mdtfdst : this.allMetadataGroups) {
            if (mdtfdst.isDefaultdisplay()) {
                out.add(mdtfdst.getMetadataGroup());
            }
        }

        return out;
    }

    /***************************************************************************
     * <p>
     * Gets the number of metadata objects, which are possible for a special MetadataGroup for this special document structure type. MetadataGroup are
     * compared using the internal name.
     * </p>
     * 
     * @param inType MetadataGroup - can be a global type
     * @return String containing the number (number can be: "1o", "1m", "*", "+")
     **************************************************************************/
    public String getNumberOfMetadataGroups(MetadataGroupType inType) {
        if (inType == null) {
            return "0";
        }
        for (MetadataGroupForDocStructType mdtfdst : this.allMetadataGroups) {
            if (mdtfdst.getMetadataGroup().getName().equals(inType.getName())) {
                return mdtfdst.getNumber();
            }
        }

        return "0";
    }

    /***************************************************************************
     * <p>
     * Gives very general information if a given MDType is allowed in a documentstructure of the type represented by this instance, or not.
     * </p>
     * 
     * @param inMDType MetadataType - can be a global type (with same internal name)
     * @return true, if it is allowed; otherwise false
     **************************************************************************/
    public boolean isMDTGroupAllowed(MetadataGroupType inMDType) {
        if (inMDType != null) {
            Iterator<MetadataGroupForDocStructType> it = allMetadataGroups.iterator();
            while (it.hasNext()) {
                MetadataGroupType mdt = it.next().getMetadataGroup();
                if (mdt.getName().equals(inMDType.getName())) {
                    return true; // it is already available
                }
            }
        }
        return false; // sorry, not available
    }

    /***************************************************************************
     * <p>
     * Removes a MetadataGroup object.
     * </p>
     * 
     * @param type MetadataGroup
     * @return true if successful, otherwise false
     **************************************************************************/
    public boolean removeMetadataGroup(MetadataGroupType type) {

        List<MetadataGroupForDocStructType> ll = new LinkedList<>(this.allMetadataGroups);

        for (MetadataGroupForDocStructType mdtfdst : ll) {
            if (mdtfdst.getMetadataGroup().equals(type)) {
                this.allMetadataGroups.remove(mdtfdst);
                return true;
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Retrieves the local MetadataGroup object (created when adding a global MetadataGroup object). This is necessary, if you just like to have the
     * global MetadataGroup (from Preferences).
     * </p>
     * 
     * @param inMDType global MetadataGroup object (from Preferences)
     * @return MetadataGroup or null, if not available for this DocStructType
     **************************************************************************/
    public MetadataGroupType getMetadataGroupByGroup(MetadataGroupType inMDType) {
        if (inMDType != null) {
            for (MetadataGroupForDocStructType mdtfdst : this.allMetadataGroups) {
                MetadataGroupType mdt = mdtfdst.getMetadataGroup();

                if (mdt.getName().equals(inMDType.getName())) {
                    return mdt;
                }
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Adds a MetadataGroup object to this DocStructType instance; this means, that all document structures of this type can have at least one
     * metadata object of this type because for each DocStructType object we have separate MetadataGroup objects, the MetadataGroup instance given as
     * the only parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
     * </p>
     * 
     * @param type MetadataType object which should be added
     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
     * @return newly created copy of the MetadataGroup object; if not successful null is returned
     **************************************************************************/
    public MetadataGroupType addMetadataGroup(MetadataGroupType type, String inNumber) {
        if (type == null) {
            return null;
        }
        // New MetadataType obejct which is added to this DocStructType.
        MetadataGroupType myType;

        // Metadata is already available.
        if (isMetadataGroupAlreadyAvailable(type)) {
            return null;
        }

        // Make a copy of this object and add the copy - necessary, cause we
        // have own instances for each document structure type.
        myType = type.copy();

        MetadataGroupForDocStructType mdtfdst = new MetadataGroupForDocStructType(myType);
        mdtfdst.setNumber(inNumber);
        this.allMetadataGroups.add(mdtfdst);

        return myType;
    }

    /***************************************************************************
     * <p>
     * Adds a MetadataGroup object to this DocStructType instance; this means, that all document structures of this type can have at least one
     * metadata object of this type because for each DocStructType object we have separate MetadataGroup objects, the MetadataGroup instance given as
     * the only parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
     * </p>
     * 
     * @param type MetadataGroup object which should be added
     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
     * @param isDefault if set to true, this metadatatype will be displayed (even if it's empty)
     * @return newly created copy of the MetadataType object; if not successful null is returned
     **************************************************************************/
    public MetadataGroupType addMetadataGroup(MetadataGroupType type, String inNumber, boolean isDefault, boolean isInvisible) {

        // New MetadataType obejct which is added to this DocStructType.
        MetadataGroupType myType;

        // Metadata is already available.
        if (isMetadataGroupAlreadyAvailable(type)) {
            return null;
        }

        // Make a copy of this object and add the copy - necessary, cause we
        // have own instances for each document structure type.
        myType = type.copy();

        MetadataGroupForDocStructType mdtfdst = new MetadataGroupForDocStructType(myType);
        mdtfdst.setNumber(inNumber);
        mdtfdst.setDefaultdisplay(isDefault);
        mdtfdst.setInvisible(isInvisible);
        this.allMetadataGroups.add(mdtfdst);

        return myType;
    }

    /***************************************************************************
     * <p>
     * Checks, if the MetadataGroup has already been added and is already available in the list of all MetadataGroup.
     * </p>
     * 
     * @param type
     * @return true, if is is already available
     **************************************************************************/
    private boolean isMetadataGroupAlreadyAvailable(MetadataGroupType type) {

        MetadataGroupForDocStructType test;
        String testname;
        String typename;

        for (MetadataGroupForDocStructType element : this.allMetadataGroups) {
            test = element;
            MetadataGroupType mdt = test.getMetadataGroup();
            testname = mdt.getName();
            typename = type.getName();

            if (testname.equals(typename)) {
                // It is already available.
                return true;
            }
        }

        return false;
    }
}
