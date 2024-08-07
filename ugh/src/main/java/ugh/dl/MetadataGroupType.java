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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Robert Sehr
 */
public class MetadataGroupType implements Serializable, PrefsType {

    private static final long serialVersionUID = -2935555025180170310L;

    private List<MetadataTypeForDocStructType> metadataTypeList = new LinkedList<>();

    // Unique name of MetadataType.
    private String name;

    // Maximum number of occurences of this MetadataType for one DocStrct (can
    // be 1 (1), one or more (+) or as many as you want (*).
    private String maxNumber;

    // Hash containing all languages.
    private Map<String, String> allLanguages;

    protected transient List<AllowedMetadataGroupType> allGroups = new LinkedList<>();

    public List<MetadataType> getMetadataTypeList() {
        // if null return null
        if (metadataTypeList == null) {
            return null;
        }

        // otherwise make a copy of metadataTypeList
        List<MetadataType> out = new LinkedList<>();

        for (MetadataTypeForDocStructType mdtfdst : metadataTypeList) {
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
        // null should not be used as metadataToAdd
        if (metadataToAdd == null) {
            throw new IllegalArgumentException("Cannot add null as MetadataType!");
        }

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

        for (MetadataTypeForDocStructType mdtfdst : ll) {
            if (mdtfdst.getMetadataType().equals(metadataToRemove)) {
                metadataTypeList.remove(mdtfdst);
                return;
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
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
        MetadataGroupType other = (MetadataGroupType) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public Map<String, String> getAllLanguages() {
        return allLanguages;
    }

    public void setAllLanguages(Map<String, String> allLanguages) {
        this.allLanguages = allLanguages;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setNum(String in) {
        if (StringUtils.isBlank(in)) {
            maxNumber = "1o";
        } else if (!"1m".equals(in) && !"1o".equals(in) && !"+".equals(in) && !"*".equals(in)) {
            // Unknown syntax.
            maxNumber = "1o";
        } else {
            this.maxNumber = in;
        }
    }

    public String getLanguage(String theLanguage) {
        // initialize the field allLanguages if not done yet
        if (allLanguages == null) {
            allLanguages = new HashMap<>();
        }

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
        return this.maxNumber;
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
        if (inType == null) {
            return "0";
        }

        for (MetadataTypeForDocStructType mdtfdst : metadataTypeList) {
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
        if (this.maxNumber != null) {
            newMDType.setNum(this.maxNumber);
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

        for (AllowedMetadataGroupType amgt : allGroups) {
            newMDType.addGroupTypeAsChild(amgt.getGroupName(), amgt.getNumAllowed(), amgt.isDefaultDisplay(), amgt.isHidden());
        }
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

        for (MetadataTypeForDocStructType element : metadataTypeList) {
            test = element;
            PrefsType mdt = test.getMetadataType();
            testname = mdt.getName();
            typename = type.getName();

            if (testname == null || typename == null) {
                return testname == null && typename == null;
            }

            if (testname.equals(typename)) {
                // It is already available.
                return true;
            }
        }

        return false;
    }

    public List<MetadataType> getAllDefaultDisplayMetadataTypes() {
        List<MetadataType> out = new LinkedList<>();

        for (MetadataTypeForDocStructType mdtfdst : metadataTypeList) {
            if (mdtfdst.isDefaultdisplay()) {
                out.add(mdtfdst.getMetadataType());
            }
        }

        return out;
    }

    @Override
    public void addLanguage(String lang, String value) {

        Map.Entry<String, String> test;
        String key;

        // initialize the field allLanguages if not done
        if (allLanguages == null) {
            allLanguages = new HashMap<>();
        }

        // null should not be used as key
        if (lang == null) {
            throw new IllegalArgumentException("null should not be used as key");
        }

        for (Entry<String, String> element : this.allLanguages.entrySet()) {
            test = element;
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
        if (this.allLanguages == null) {
            return null;
        }

        String languageName = this.allLanguages.get(lang);
        if (languageName == null) {
            return null;
        }

        return languageName;
    }

    public void addGroupTypeAsChild(String groupName, String occurrence, boolean defaultDisplay, boolean hidden) {
        // null should not be used as groupName
        if (StringUtils.isBlank(groupName)) {
            throw new IllegalArgumentException("groupName should not be blank");
        }

        for (AllowedMetadataGroupType other : allGroups) {
            if (other.getGroupName().equals(groupName)) {
                // new element is already in list
                return;
            }
        }
        AllowedMetadataGroupType amgt = new AllowedMetadataGroupType(groupName, occurrence, defaultDisplay, hidden);

        allGroups.add(amgt);

    }

    public void removeGroupTypeAsChild(String groupName) {
        AllowedMetadataGroupType toDelete = null;
        for (AllowedMetadataGroupType other : allGroups) {
            if (other.getGroupName().equals(groupName)) {
                toDelete = other;
                break;
            }
        }
        if (toDelete != null) {
            allGroups.remove(toDelete);
        }
    }

    public List<AllowedMetadataGroupType> getAllAllowedGroupTypeTypes() {
        return allGroups;
    }

    public AllowedMetadataGroupType getAllowedMetadataGroupTypeByName(String groupName) {
        for (AllowedMetadataGroupType grp : allGroups) {
            if (grp.getGroupName().equals(groupName)) {
                return grp;
            }
        }
        return null;
    }

}
