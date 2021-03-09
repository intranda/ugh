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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Robert Sehr
 */
public class MetadataGroupType implements Serializable {

    private static final long serialVersionUID = -2935555025180170310L;

    private List<MetadataTypeForDocStructType> metadataTypeList = new ArrayList<>();

    // Unique name of MetadataType.
    private String name;

    // Maximum number of occurences of this MetadataType for one DocStrct (can
    // be 1 (1), one or more (+) or as many as you want (*).
    private String max_number;

    // Hash containing all languages.
    private HashMap<String, String> allLanguages;

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
     * TODO Was set to deprecated, who knows why?
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
    public String getNumberOfMetadataType(MetadataType inType) {


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
    private boolean isMetadataTypeAlreadyAvailable(MetadataType type) {

        MetadataTypeForDocStructType test;
        String testname;
        String typename;

        // Check, if MetadataType is already available.
        Iterator<MetadataTypeForDocStructType> it = metadataTypeList.iterator();
        while (it.hasNext()) {
            test = it.next();
            MetadataType mdt = test.getMetadataType();
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
}
