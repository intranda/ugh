package ugh.dl;

/***************************************************************
 * Copyright notice
 *
 * ugh.dl / MetadataGroupType.java
 *
 * (c) 2013 Robert Sehr <robert.sehr@intranda.com>
 *
 * All rights reserved
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
 ***************************************************************/

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Robert Sehr
 */
public class MetadataGroupType implements Serializable, PrefsType {

    private static final long serialVersionUID = -2935555025180170310L;

    private List<MetadataTypeForDocStructType> metadataTypeList = new LinkedList<>();

    //    private List<MetadataGroupForDocStructType> allMetadataGroups = new LinkedList<>();

    // Unique name of MetadataType.
    private String name;

    // Maximum number of occurences of this MetadataType for one DocStrct (can
    // be 1 (1), one or more (+) or as many as you want (*).
    private String max_number;

    // Hash containing all languages.
    private HashMap<String, String> allLanguages;

    protected Map<String, String> allGroups = new HashMap<>();

    public List<MetadataType> getMetadataTypeList() {
        List<MetadataType> out = new LinkedList<>();

        Iterator<MetadataTypeForDocStructType> it = metadataTypeList.iterator();
        while (it.hasNext()) {
            MetadataTypeForDocStructType mdtfdst = it.next();
            out.add(mdtfdst.getMetadataType());
        }
        return out;
    }

    public void setMetadataTypeList(List<MetadataType> metadataTypeList) {
        for (MetadataType mdt : metadataTypeList) {
            MetadataTypeForDocStructType mdtfdst = new MetadataTypeForDocStructType(mdt);
            this.metadataTypeList.add(mdtfdst);
        }
    }

    public void setTypes(List<MetadataTypeForDocStructType> metadataTypeList) {
        this.metadataTypeList = metadataTypeList;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMetadataType(MetadataType metadataToAdd, String inNumber, boolean isDefault, boolean isInvisible) {
        // New MetadataType obejct which is added to this DocStructType.
        MetadataType myType;

        // Metadata is already available.
        if (isMetadataTypeAlreadyAvailable(metadataToAdd)) {
            return;
        }

        // Make a copy of this object and add the copy - necessary, cause we
        // have own instances for each document structure type.
        myType = metadataToAdd.copy();

        MetadataTypeForDocStructType mdtfdst = new MetadataTypeForDocStructType(myType);
        mdtfdst.setNumber(inNumber);
        mdtfdst.setDefaultdisplay(isDefault);
        mdtfdst.setInvisible(isInvisible);
        this.metadataTypeList.add(mdtfdst);
    }

    public void removeMetadataType(MetadataType metadataToRemove) {

        List<MetadataTypeForDocStructType> ll = new LinkedList<>(metadataTypeList);

        Iterator<MetadataTypeForDocStructType> it = ll.iterator();
        while (it.hasNext()) {
            MetadataTypeForDocStructType mdtfdst = it.next();
            if (mdtfdst.getMetadataType().equals(metadataToRemove)) {
                metadataTypeList.remove(mdtfdst);
                return;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this.name.equals(((MetadataGroupType) obj).getName());
    }

    @Override
    public HashMap<String, String> getAllLanguages() {
        return allLanguages;
    }

    public void setAllLanguages(HashMap<String, String> allLanguages) {
        this.allLanguages = allLanguages;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setNum(String in) {

        if (!in.equals("1m") && !in.equals("1o") && !in.equals("+") && !in.equals("*")) {
            // Unknown syntax.
            return;
        }
        this.max_number = in;

    }

    public String getLanguage(String theLanguage) {
        for (Map.Entry<String, String> lang : getAllLanguages().entrySet()) {
            if (lang.getKey().equals(theLanguage)) {
                return lang.getValue();
            }
        }

        return null;
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
        return this.max_number;
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

        Iterator<MetadataTypeForDocStructType> it = metadataTypeList.iterator();
        while (it.hasNext()) {
            MetadataTypeForDocStructType mdtfdst = it.next();
            if (mdtfdst.getMetadataType().getName().equals(inType.getName())) {
                return mdtfdst.getNumber();
            }
        }

        return "0";
    }

    public MetadataGroupType copy() {

        MetadataGroupType newMDType = new MetadataGroupType();

        newMDType.setAllLanguages(this.allLanguages);
        newMDType.setName(this.name);
        if (this.max_number != null) {
            newMDType.setNum(this.max_number);
        }
        List<MetadataTypeForDocStructType> newList = new LinkedList<>();
        for (MetadataTypeForDocStructType mdt : metadataTypeList) {
            MetadataTypeForDocStructType newType = new MetadataTypeForDocStructType(mdt.getMetadataType());
            newType.setNumber(mdt.getNumber());
            newType.setInvisible(mdt.isInvisible());
            newType.setDefaultdisplay(mdt.isDefaultdisplay());

            newList.add(newType);
        }
        newMDType.setTypes(newList);

        return newMDType;
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

        // Check, if MetadataType is already available.
        Iterator<MetadataTypeForDocStructType> it = metadataTypeList.iterator();
        while (it.hasNext()) {
            test = it.next();
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

    public List<MetadataType> getAllDefaultDisplayMetadataTypes() {
        List<MetadataType> out = new LinkedList<>();

        Iterator<MetadataTypeForDocStructType> it = metadataTypeList.iterator();
        while (it.hasNext()) {
            MetadataTypeForDocStructType mdtfdst = it.next();
            if (mdtfdst.isDefaultdisplay()) {
                out.add(mdtfdst.getMetadataType());
            }
        }

        return out;
    }

    //    /***************************************************************************
    //     * <p>
    //     * Add and remove MetadataGroups.
    //     * </p>
    //     *
    //     * @param in
    //     **************************************************************************/
    //    public void setAllMetadataGroups(List<MetadataGroupType> in) {
    //
    //        for (MetadataGroupType mdt : in) {
    //            MetadataGroupForDocStructType mdtfdst = new MetadataGroupForDocStructType(mdt);
    //            this.allMetadataGroups.add(mdtfdst);
    //        }
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Retrieves all MetadataGroup objects for this DocStructType instance.
    //     * </p>
    //     *
    //     * @return List containing MetadataGroup objects; These MetadataGroup-objects are just local objects
    //     **************************************************************************/
    //    public List<MetadataGroupType> getAllMetadataGroupTypes() {
    //
    //        List<MetadataGroupType> out = new LinkedList<>();
    //
    //        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
    //        while (it.hasNext()) {
    //            MetadataGroupForDocStructType mdtfdst = it.next();
    //            out.add(mdtfdst.getMetadataGroup());
    //        }
    //
    //        return out;
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Retrieves all MetadataGroup objects for this DocStructType instance, that have the "DefaultDisplay" attribute in the configuration set to
    //     * "true".
    //     * </p>
    //     *
    //     * @return List containing MetadataGroup objects; These MetadataGroup objects are just local objects.
    //     **************************************************************************/
    //    public List<MetadataGroupType> getAllDefaultDisplayMetadataGroups() {
    //
    //        List<MetadataGroupType> out = new LinkedList<>();
    //
    //        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
    //        while (it.hasNext()) {
    //            MetadataGroupForDocStructType mdtfdst = it.next();
    //            if (mdtfdst.isDefaultdisplay()) {
    //                out.add(mdtfdst.getMetadataGroup());
    //            }
    //        }
    //
    //        return out;
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Gets the number of metadata objects, which are possible for a special MetadataGroup for this special document structure type. MetadataGroup are
    //     * compared using the internal name.
    //     * </p>
    //     *
    //     * @param inType MetadataGroup - can be a global type
    //     * @return String containing the number (number can be: "1o", "1m", "*", "+")
    //     **************************************************************************/
    //    public String getNumberOfMetadataGroups(MetadataGroupType inType) {
    //
    //        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
    //        while (it.hasNext()) {
    //            MetadataGroupForDocStructType mdtfdst = it.next();
    //            if (mdtfdst.getMetadataGroup().getName().equals(inType.getName())) {
    //                return mdtfdst.getNumber();
    //            }
    //        }
    //
    //        return "0";
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Gives very general information if a given MDType is allowed in a documentstructure of the type represented by this instance, or not.
    //     * </p>
    //     *
    //     * @param inMDType MetadataType - can be a global type (with same internal name)
    //     * @return true, if it is allowed; otherwise false
    //     **************************************************************************/
    //    public boolean isMDTGroupAllowed(MetadataGroupType inMDType) {
    //        Iterator<MetadataGroupForDocStructType> it = allMetadataGroups.iterator();
    //        while (it.hasNext()) {
    //            MetadataGroupType mdt = it.next().getMetadataGroup();
    //            if (mdt.getName().equals(inMDType.getName())) {
    //                return true; // it is already available
    //            }
    //        }
    //
    //        return false; // sorry, not available
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Removes a MetadataGroup object.
    //     * </p>
    //     *
    //     * @param type MetadataGroup
    //     * @return true if successful, otherwise false
    //     **************************************************************************/
    //    public boolean removeMetadataGroup(MetadataGroupType type) {
    //
    //        List<MetadataGroupForDocStructType> ll = new LinkedList<>(this.allMetadataGroups);
    //
    //        Iterator<MetadataGroupForDocStructType> it = ll.iterator();
    //        while (it.hasNext()) {
    //            MetadataGroupForDocStructType mdtfdst = it.next();
    //            if (mdtfdst.getMetadataGroup().equals(type)) {
    //                this.allMetadataGroups.remove(mdtfdst);
    //                return true;
    //            }
    //        }
    //
    //        return false;
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Retrieves the local MetadataGroup object (created when adding a global MetadataGroup object). This is necessary, if you just like to have the
    //     * global MetadataGroup (from Preferences).
    //     * </p>
    //     *
    //     * @param inMDType global MetadataGroup object (from Preferences)
    //     * @return MetadataGroup or null, if not available for this DocStructType
    //     **************************************************************************/
    //    public MetadataGroupType getMetadataGroupByGroup(MetadataGroupType inMDType) {
    //
    //        // Check, if MetadataType is already available.
    //        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
    //        while (it.hasNext()) {
    //            MetadataGroupForDocStructType mdtfdst = it.next();
    //            MetadataGroupType mdt = mdtfdst.getMetadataGroup();
    //
    //            if (mdt.getName().equals(inMDType.getName())) {
    //                return mdt;
    //            }
    //        }
    //
    //        return null;
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Adds a MetadataGroup object to this DocStructType instance; this means, that all document structures of this type can have at least one
    //     * metadata object of this type because for each DocStructType object we have separate MetadataGroup objects, the MetadataGroup instance given as
    //     * the only parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
    //     * </p>
    //     *
    //     * @param type MetadataType object which should be added
    //     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
    //     * @return newly created copy of the MetadataGroup object; if not successful null is returned
    //     **************************************************************************/
    //    public MetadataGroupType addMetadataGroup(MetadataGroupType type, String inNumber) {
    //
    //        // New MetadataType obejct which is added to this DocStructType.
    //        MetadataGroupType myType;
    //
    //        // Metadata is already available.
    //        if (isMetadataGroupAlreadyAvailable(type)) {
    //            return null;
    //        }
    //
    //        // Make a copy of this object and add the copy - necessary, cause we
    //        // have own instances for each document structure type.
    //        myType = type.copy();
    //
    //        MetadataGroupForDocStructType mdtfdst = new MetadataGroupForDocStructType(myType);
    //        mdtfdst.setNumber(inNumber);
    //        this.allMetadataGroups.add(mdtfdst);
    //
    //        return myType;
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Adds a MetadataGroup object to this DocStructType instance; this means, that all document structures of this type can have at least one
    //     * metadata object of this type because for each DocStructType object we have separate MetadataGroup objects, the MetadataGroup instance given as
    //     * the only parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
    //     * </p>
    //     *
    //     * @param type MetadataGroup object which should be added
    //     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
    //     * @param isDefault if set to true, this metadatatype will be displayed (even if it's empty)
    //     * @return newly created copy of the MetadataType object; if not successful null is returned
    //     **************************************************************************/
    //    public MetadataGroupType addMetadataGroup(MetadataGroupType type, String inNumber, boolean isDefault, boolean isInvisible) {
    //
    //        // New MetadataType obejct which is added to this DocStructType.
    //        MetadataGroupType myType;
    //
    //        // Metadata is already available.
    //        if (isMetadataGroupAlreadyAvailable(type)) {
    //            return null;
    //        }
    //
    //        // Make a copy of this object and add the copy - necessary, cause we
    //        // have own instances for each document structure type.
    //        myType = type.copy();
    //
    //        MetadataGroupForDocStructType mdtfdst = new MetadataGroupForDocStructType(myType);
    //        mdtfdst.setNumber(inNumber);
    //        mdtfdst.setDefaultdisplay(isDefault);
    //        mdtfdst.setInvisible(isInvisible);
    //        this.allMetadataGroups.add(mdtfdst);
    //
    //        return myType;
    //    }
    //
    //    /***************************************************************************
    //     * <p>
    //     * Checks, if the MetadataGroup has already been added and is already available in the list of all MetadataGroup.
    //     * </p>
    //     *
    //     * @param type
    //     * @return true, if is is already available
    //     **************************************************************************/
    //    private boolean isMetadataGroupAlreadyAvailable(MetadataGroupType type) {
    //
    //        MetadataGroupForDocStructType test;
    //        String testname;
    //        String typename;
    //
    //        // Check, if MetadataType is already available.
    //        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
    //        while (it.hasNext()) {
    //            test = it.next();
    //            MetadataGroupType mdt = test.getMetadataGroup();
    //            testname = mdt.getName();
    //            typename = type.getName();
    //
    //            if (testname.equals(typename)) {
    //                // It is already available.
    //                return true;
    //            }
    //        }
    //
    //        return false;
    //    }

    @Override
    public void addLanguage(String lang, String value) {

        Map.Entry<String, String> test;
        String key;

        // Check, if language already available.
        Iterator<Map.Entry<String, String>> it = this.allLanguages.entrySet().iterator();
        while (it.hasNext()) {
            test = it.next();
            key = test.getKey();
            if (key.equals(lang)) {
                // Language is already available.
                return;
            }
        }

        this.allLanguages.put(lang, value);
    }

    @Override
    public String getNameByLanguage(String lang) {

        String languageName = this.allLanguages.get(lang);
        if (languageName == null) {
            return null;
        }

        return languageName;
    }

    public void addGroupTypeAsChild(String groupName, String occurrence) {
        // Check if the DocStruct is not existing yet, and add it then.
        allGroups.put(groupName, occurrence);

    }


    public void removeGroupTypeAsChild(String inString) {

        allGroups.remove(inString);

    }

    public Map<String, String> getAllAllowedGroupTypeTypes() {
        return allGroups;
    }
}
