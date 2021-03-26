package ugh.dl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.IncompletePersonObjectException;
import ugh.exceptions.MetadataTypeNotAllowedException;

/*******************************************************************************
 * <p>
 * A MetadataGroup object represents a single MetadataGroup element. Each MetadataGroup element has at least a metadata element. The type of a
 * MetadataGroup element is stored as a {@link MetadataGroupType} object.
 * </p>
 * 
 * <p>
 * MetadataGroups are a list of {@link Metadata}
 * </p>
 * 
 * @author Robert Sehr
 * @version 2013-05-08
 * @see MetadataGroupType
 * 
 * 
 ******************************************************************************/

@Log4j2
public class MetadataGroup implements Serializable, HoldingElement {

    private static final long serialVersionUID = -6283388063178498292L;


    protected MetadataGroupType metadataGroupType;
    // Document structure to which this metadata type belongs to.
    @Getter
    @Setter
    protected HoldingElement parent;
    @Getter
    @Setter
    private String identifier;

    @Getter
    @Setter
    private List<Metadata> metadataList;
    @Getter
    @Setter
    private List<Person> personList;
    @Getter
    @Setter
    private List<Corporate> corporateList;

    private List<MetadataGroup> allMetadataGroups;

    private List<MetadataGroup> removedMetadataGroups;

    /***************************************************************************
     * <p>
     * Constructor.
     * </p>
     * 
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
    public MetadataGroup(MetadataGroupType theType) throws MetadataTypeNotAllowedException {

        // Check for NULL MetadataTypes.
        if (theType == null) {
            String message = "MetadataType must not be null at Metadata creation!";
            throw new MetadataTypeNotAllowedException(message);
        }

        this.metadataGroupType = theType;

        metadataList = new LinkedList<>();
        personList = new LinkedList<>();
        corporateList = new LinkedList<>();
        allMetadataGroups = new LinkedList<>();
        //        for (MetadataType mdt : metadataGroupType.getAllDefaultDisplayMetadataTypes()) {
        //            if (mdt.getIsPerson() ) {
        //                Person p = new Person(mdt);
        //                p.setRole(mdt.getName());
        //                addPerson(p);
        //            } else if (mdt.isCorporate()) {
        //                Corporate c = new Corporate(mdt);
        //                c.setRole(mdt.getName());
        //                addCorporate(c);
        //            } else {
        //                Metadata md = new Metadata(mdt);
        //                addMetadata(md);
        //            }
        //        }

    }
    //
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
    //     * Returns the DocStruct instance, to which this metadataGroup object belongs. This is extremly helpful, if only the metadata instance is stored
    //     * in a list; the reference to the associated DocStrct instance is always kept.
    //     * </p>
    //     *
    //     * @return DocStruct instance.
    //     **************************************************************************/
    //    public DocStruct getDocStruct() {
    //        return this.myDocStruct;
    //    }

    /***************************************************************************
     * <p>
     * Returns the type of the metadataGroup instance; The MetadataGroupType object which is returned, may have the same name, but be a different
     * object than the MetadataGroupType object from another MetadataGroupType.
     * </p>
     * 
     * @return MetadataGroupType instance
     **************************************************************************/
    @Override
    public MetadataGroupType getType() {
        return this.metadataGroupType;
    }

    /***************************************************************************
     * <p>
     * Sets the MetadataGroupType for this instance; only a MetadataGroupType instance is used as the only parameter. The method returns true if
     * MDType was set; false if not.
     * </p>
     * 
     * @param inType
     **************************************************************************/
    public void setType(MetadataGroupType inType) {
        this.metadataGroupType = inType;
    }

    @Override
    public void addMetadata(Metadata metadata) throws MetadataTypeNotAllowedException {
        MetadataType type = metadata.getType();
        String inMdName = type.getName();
        boolean insert = false;
        List<MetadataType> allowedTypes = getAddableMetadataTypes(true);
        if (allowedTypes == null) {
            return;
        }
        for (MetadataType allowed : allowedTypes) {
            if (allowed.getName().equals(type.getName())) {
                insert = true;
                break;
            }
        }

        if (insert) {
            metadata.setParent(this);
            this.metadataList.add(metadata);
        } else {
            log.debug("Not allowed to add metadata '" + inMdName + "' ");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(type, metadataGroupType);
            log.error(mtnae.getMessage());
            throw mtnae;
        }
    }

    @Override
    public void addPerson(Person person) throws MetadataTypeNotAllowedException {

        MetadataType type = person.getType();
        String inMdName = type.getName();
        boolean insert = false;
        List<MetadataType> allowedTypes = getAddableMetadataTypes(true);
        if (allowedTypes == null) {
            return;
        }
        for (MetadataType allowed : allowedTypes) {
            if (allowed.getName().equals(type.getName())) {
                insert = true;
                break;
            }
        }

        if (insert) {
            person.setParent(this);
            personList.add(person);
        } else {
            log.debug("Not allowed to add metadata '" + inMdName + "'");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(type, metadataGroupType);
            log.error(mtnae.getMessage());
            throw mtnae;
        }
    }

    @Override
    public void addCorporate(Corporate corporate) throws MetadataTypeNotAllowedException {

        MetadataType type = corporate.getType();
        String inMdName = type.getName();
        boolean insert = false;
        List<MetadataType> allowedTypes = getAddableMetadataTypes(true);
        if (allowedTypes == null) {
            return;
        }
        for (MetadataType allowed : allowedTypes) {
            if (allowed.getName().equals(type.getName())) {
                insert = true;
                break;
            }
        }

        if (insert) {
            corporate.setParent(this);
            corporateList.add(corporate);
        } else {
            log.debug("Not allowed to add metadata '" + inMdName + "'");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(type, metadataGroupType);
            log.error(mtnae.getMessage());
            throw mtnae;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MetadataGroup [MDType:");
        sb.append(metadataGroupType.getName());
        sb.append(", myDocStruct:");
        sb.append(parent.getType().getName());
        if (metadataList != null && !metadataList.isEmpty()) {
            sb.append(", metadataList:");
            for (Metadata md : metadataList) {
                sb.append(" ");
                sb.append(md);
            }
        }
        if (personList != null && !personList.isEmpty()) {
            sb.append(", personList:");
            for (Person p : personList) {
                sb.append(" ");
                sb.append(p);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public List<Metadata> getMetadataByType(String theType) {
        List<Metadata> returnList = new ArrayList<>();
        for (Metadata md : metadataList) {
            if (md.getType().getName().equals(theType)) {
                returnList.add(md);
            }
        }
        return returnList;
    }

    public List<Person> getPersonByType(String theType) {
        List<Person> returnList = new ArrayList<>();
        for (Person md : personList) {
            if (md.getType().getName().equals(theType)) {
                returnList.add(md);
            }
        }
        return returnList;
    }

    public List<Corporate> getCorporateByType(String theType) {
        List<Corporate> returnList = new ArrayList<>();
        for (Corporate md : corporateList) {
            if (md.getType().getName().equals(theType)) {
                returnList.add(md);
            }
        }
        return returnList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metadataGroupType == null) ? 0 : metadataGroupType.hashCode());
        result = prime * result + ((metadataList == null) ? 0 : metadataList.hashCode());
        result = prime * result + ((personList == null) ? 0 : personList.hashCode());
        result = prime * result + ((corporateList == null) ? 0 : corporateList.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        return result;
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
        MetadataGroup other = (MetadataGroup) obj;
        if (metadataGroupType == null) {
            if (other.metadataGroupType != null) {
                return false;
            }
        } else if (!metadataGroupType.equals(other.metadataGroupType)) {
            return false;
        }
        if (metadataList == null) {
            if (other.metadataList != null) {
                return false;
            }
        } else if (!metadataList.equals(other.metadataList)) {
            return false;
        }
        if (personList == null) {
            if (other.personList != null) {
                return false;
            }
        } else if (!personList.equals(other.personList)) {
            return false;
        }
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isMetadataTypeBeRemoved(PrefsType inMDType) {
        // How many metadata of this type do we have already.
        int typesavailable = countMDofthisType(inMDType.getName());
        // How many types must be at least available.
        String maxnumbersallowed = metadataGroupType.getNumberOfMetadataType(inMDType);

        if (typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return false;
        }

        if (typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return false;
        }

        return true;
    }

    @Override
    public void removeMetadata(Metadata theMd, boolean force) {
        PrefsType inMdType;
        String maxnumbersallowed;
        int typesavailable;

        // Get Type of inMD.
        inMdType = theMd.getType();

        // How many metadata of this type do we have already.
        typesavailable = countMDofthisType(inMdType.getName());

        // How many types must be at least available.
        maxnumbersallowed = metadataGroupType.getNumberOfMetadataType(inMdType);

        if (!force && typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return;
        }
        if (!force && typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return;
        }

        theMd.parent = null;

        this.metadataList.remove(theMd);

    }

    @Override
    public void removeCorporate(Corporate in, boolean force) throws IncompletePersonObjectException {
        if (corporateList == null) {
            return;
        }

        PrefsType inMDType = in.getType();
        // Incomplete person.
        if (inMDType == null) {
            IncompletePersonObjectException ipoe = new IncompletePersonObjectException();
            log.error("Incomplete data for corporate metadata '" + in.getType().getName() + "'");
            throw ipoe;
        }

        // How many metadata of this type do we have already.
        int typesavailable = countMDofthisType(inMDType.getName());
        // How many types must be at least available.
        String maxnumbersallowed = metadataGroupType.getNumberOfMetadataType(inMDType);

        if (force && typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return;
        }
        if (force && typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return;
        }

        corporateList.remove(in);
    }

    @Override
    public void removePerson(Person in, boolean force) throws IncompletePersonObjectException {
        if (personList == null) {
            return;
        }

        PrefsType inMDType = in.getType();
        // Incomplete person.
        if (inMDType == null) {
            IncompletePersonObjectException ipoe = new IncompletePersonObjectException();
            log.error("Incomplete data for person metadata '" + in.getType().getName() + "'");
            throw ipoe;
        }

        // How many metadata of this type do we have already.
        int typesavailable = countMDofthisType(inMDType.getName());
        // How many types must be at least available.
        String maxnumbersallowed = metadataGroupType.getNumberOfMetadataType(inMDType);

        if (force && typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return;
        }
        if (force && typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return;
        }

        personList.remove(in);
    }

    /***************************************************************************
     * <p>
     * Gives number of Metadata elements belonging to this DocStruct of a specific type. The type must be given by the unique (internal) name as it is
     * retrievable from MetadataType's getName method.
     * </p>
     * <p>
     * This method does not only get the number of Metadata elements, but also the number of person objects belonging to one <code>DocStruct</code>
     * object.
     * </p>
     * 
     * @param inTypeName Internal name of object as String
     * @return Number of metadata as integer
     **************************************************************************/
    public int countMDofthisType(String inTypeName) {

        PrefsType testtype;
        int counter = 0;

        if (metadataList != null) {
            for (Metadata md : metadataList) {
                testtype = md.getType();
                if (testtype != null && testtype.getName().equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }
        }

        if (personList != null) {
            for (Person per : personList) {
                testtype = per.getType();
                if (testtype != null && testtype.getName().equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }
        }

        if (corporateList != null) {
            for (Corporate corp : corporateList) {
                testtype = corp.getType();
                if (testtype != null && testtype.getName().equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }
        }

        if (allMetadataGroups != null) {
            for (MetadataGroup mg : allMetadataGroups) {
                if (mg.getType().getName().equals(inTypeName)) {
                    counter++;
                }
            }

        }
        return counter;
    }

    public void checkDefaultDisplayMetadata() {
        List<MetadataType> allDefaultMdTypes = metadataGroupType.getAllDefaultDisplayMetadataTypes();
        if (allDefaultMdTypes != null) {
            // Iterate over all defaultDisplay metadata types and check, if
            // metadata of this type is already available.
            for (MetadataType mdt : allDefaultMdTypes) {
                if (!mdt.getName().startsWith("_") && countMDofthisType(mdt.getName()) == 0) {
                    // If none of these metadata is available, AND it is not a
                    // hidden metadata type, create it.
                    try {
                        if (mdt.getIsPerson()) {
                            Person p = new Person(mdt);
                            p.setRole(mdt.getName());
                            addPerson(p);
                        } else if (mdt.isCorporate()) {
                            Corporate c = new Corporate(mdt);
                            c.setRole(mdt.getName());
                            addCorporate(c);
                        } else {
                            Metadata md = new Metadata(mdt);
                            addMetadata(md);
                        }
                    } catch (MetadataTypeNotAllowedException e) {
                        log.error(e);
                    }
                }
            }
        }
    }

    /***************************************************************************
     * <p>
     * Get all metadatatypes, which can be added to a DocStruct. This method considers already added metadata (and persons!); e.g. metadata types
     * which can only be available once cannot be added a second time. Therefore this metadata type will not be included in this list.<br/>
     * 
     * "Internal" metadata, which start with the HIDDEN_METADATA_CHAR, will also not be included.
     * </p>
     * 
     * @return List containing MetadataType objects.
     **************************************************************************/
    @Override
    public List<MetadataType> getAddableMetadataTypes(boolean includeHiddenMetadata) {

        // If e.g. the topstruct has no Metadata, or something...
        if (metadataGroupType == null) {
            return null;
        }

        // Get all Metadatatypes for my DocStructType.
        List<MetadataType> addableMetadata = new LinkedList<>();
        List<MetadataType> allTypes = metadataGroupType.getMetadataTypeList();

        // Get all metadata types which are known, iterate over them and check,
        // if they are still addable.
        for (MetadataType mdt : allTypes) {

            // Metadata beginning with the HIDDEN_METADATA_CHAR are internal
            // metadata are not user addable.
            if (includeHiddenMetadata || !mdt.getName().startsWith("_")) {
                String maxnumber = metadataGroupType.getNumberOfMetadataType(mdt);

                // Metadata can only be available once; so we have to check if
                // it is already available.
                if (maxnumber.equals("1m") || maxnumber.equals("1o")) {
                    // Check metadata here only.
                    int availableMD = countMDofthisType(mdt.getName());
                    if (availableMD < 1) {
                        // Metadata is NOT available; we are allowed to add it.
                        addableMetadata.add(mdt);
                    }
                } else {
                    // We can add as many metadata as we want (+ or *).
                    addableMetadata.add(mdt);
                }
            }
        }

        if (addableMetadata == null || addableMetadata.isEmpty()) {
            return null;
        }

        return addableMetadata;
    }

    /***************************************************************************
     * <p>
     * Gets all MetadataGroups for this DocStruct instance.
     * </p>
     * 
     * @return List containing MetadataGroup instances; if no MetadataGroup is available, null is returned.
     **************************************************************************/
    @Override
    public List<MetadataGroup> getAllMetadataGroups() {
        return this.allMetadataGroups;
    }

    /***************************************************************************
     * <p>
     * Allows to set all MetadataGroup. The MetadataGroup objects are contained in a List. This method sets all MetadataGroup; they are NOT added.
     * MetadataGroup which is already available will be overwritten.
     * </p>
     * 
     * @param inList List containing MetadataGroup objects.
     **************************************************************************/
    @Override
    public void setAllMetadataGroups(List<MetadataGroup> inList) {
        this.allMetadataGroups = inList;
    }

    @Override
    public boolean addMetadataGroup(MetadataGroup theMetadataGroup) throws MetadataTypeNotAllowedException, DocStructHasNoTypeException {

        MetadataGroupType inMdType = theMetadataGroup.getType();
        String inMdName = inMdType.getName();
        // Integer, number of metadata allowed for this metadatatype.
        String maxnumberallowed;
        // Integer, number of metadata already available.
        int number;
        // Metadata can only be inserted if set to true.
        boolean insert = false;
        // Prefs MetadataType.
        MetadataGroupType prefsMdType;

        // First get MetadataType object for the DocStructType to which this
        // document structure belongs to get global MDType.
        if (metadataGroupType == null) {
            String message = "Error occured while adding metadata of type '" + inMdName + "' to DocStruct '" + this.getType().getName() + "'";
            log.error(message);
            throw new DocStructHasNoTypeException(message);
        }

        prefsMdType = theMetadataGroup.getType();

        // Ask DocStructType instance to get MetadataType by Type. At this point
        // we are creating a local copy of the MetadataType object.
        if (prefsMdType == null) {
            MetadataTypeNotAllowedException e = new MetadataTypeNotAllowedException(null, this.getType());
            log.error(e.getMessage());
            throw e;
        }

        // Check, if it's an internal MetadataType - all internal types begin
        // with the HIDDEN_METADATA_CHAR, we can have as many as we want.
        if (inMdName.startsWith("_")) {
            maxnumberallowed = "*";
            prefsMdType = inMdType;
        } else {
            AllowedMetadataGroupType otherType = metadataGroupType.getAllowedMetadataGroupTypeByName(inMdType.getName());
            if (otherType == null) {
                // group type is not allowed
                MetadataTypeNotAllowedException e = new MetadataTypeNotAllowedException(inMdType, getType());
                log.error(e.getMessage());
                throw e;
            } else {
                maxnumberallowed = otherType.getNumAllowed();
            }
        }

        // Check, if another Metadata instance is allowed.
        //
        // How many metadata are already available.
        number = countMDofthisType(inMdName);

        // As many as we want (zero or more).
        if (maxnumberallowed.equals("*")) {
            insert = true;
        }

        // Once or more.
        if (maxnumberallowed.equals("+")) {
            insert = true;
        }

        // Only one, if we have already one, we cannot add it.
        if (maxnumberallowed.equalsIgnoreCase("1m") || maxnumberallowed.equalsIgnoreCase("1o")) {
            if (number < 1) {
                insert = true;
            } else {
                insert = false;
            }
        }

        // Add metadata.
        if (insert) {
            // Set type to MetadataType of the DocStructType.
            theMetadataGroup.setType(prefsMdType);
            // Set this document structure as myDocStruct.
            theMetadataGroup.setParent(this);
            if (this.allMetadataGroups == null) {
                // Create list, if not already available.
                this.allMetadataGroups = new LinkedList<>();
            }
            this.allMetadataGroups.add(theMetadataGroup);
        } else {
            log.debug("Not allowed to add metadata '" + inMdName + "'");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(null, this.getType());
            log.error(mtnae.getMessage());
            throw mtnae;
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Removes Metadata from this DocStruct object. If there must be at least one Metadata object of this kind, attached to this DocStruct instance
     * (according to configuration), the metadata is NOT removed. By setting the second parameter to true, this behaviour can be influenced. This can
     * be necessary e.g. when programming user interfaces etc.
     * </p>
     * <p>
     * If you want to remove Metadata of a specific type temporarily (e.g. to replace it), use the changeMetadata method instead.
     * </p>
     * 
     * @param theMd Metadata object which should be removed
     * @param force set to true, the Metadata is removed even if it is not allowed to. You can create not validateable documents.
     * @return true, if data can be removed; otherwise false
     * @see #canMetadataBeRemoved
     **************************************************************************/
    @Override
    public boolean removeMetadataGroup(MetadataGroup theMd, boolean force) {

        MetadataGroupType inMdType;
        String maxnumbersallowed;
        int typesavailable;

        // Get Type of inMD.
        inMdType = theMd.getType();

        // How many metadata of this type do we have already.
        typesavailable = countMDofthisType(inMdType.getName());

        // How many types must be at least available.
        AllowedMetadataGroupType type = metadataGroupType.getAllowedMetadataGroupTypeByName(inMdType.getName());
        if (type == null) {
            // type is not allowed
            // TODO throw exception?
            return false;

        }
        maxnumbersallowed = type.getNumAllowed();

        if (!force && typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return false;
        }
        if (!force && typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return false;
        }

        theMd.parent = null;

        if (this.removedMetadataGroups == null) {
            this.removedMetadataGroups = new LinkedList<>();
        }

        if (allMetadataGroups == null) {
            // no groups to remove available, abort
            return false;
        }

        this.removedMetadataGroups.add(theMd);
        this.allMetadataGroups.remove(theMd);

        return true;
    }

    @Override
    public void changeMetadataGroup(MetadataGroup theOldMd, MetadataGroup theNewMd) {

        MetadataGroupType oldMdt;
        MetadataGroupType newMdt;
        String oldName;
        String newName;
        int counter = 0;

        // Get MetadataTypes.
        oldMdt = theOldMd.getType();
        newMdt = theNewMd.getType();

        // Get names.
        oldName = oldMdt.getName();
        newName = newMdt.getName();

        if (oldName.equals(newName)) {
            // Different metadata types.
            return;
        }

        // Remove old object; get place of old object in list.
        for (MetadataGroup m : this.allMetadataGroups) {
            // Found old metadata object.
            if (m.equals(theOldMd)) {
                // Get out of loop.
                break;
            }
            counter++;
        }

        // Ask DocStructType instance to get a new MetadataType object of the
        // same kind.
        MetadataGroupType mdType = theOldMd.getType();
        theNewMd.setType(mdType);

        this.allMetadataGroups.remove(theOldMd);
        this.allMetadataGroups.add(counter, theNewMd);

    }

    /***************************************************************************
     * <p>
     * Retrieves all Metadata object, which belong to this DocStruct and have a special type. Can be used to get all titles, authors etc... includes
     * Persons!
     * </p>
     * 
     * PLEASE NOTE This method no longer returns NULL, if no MetadataTypes are available! An empty list is returned now!
     * 
     * @param inType MetadataType we are looking for.
     * @return List containing Metadata objects; if no metadata ojects are available, an empty list is returned.
     **************************************************************************/
    @Override
    public List<MetadataGroup> getAllMetadataGroupsByType(MetadataGroupType inType) {

        List<MetadataGroup> resultList = new LinkedList<>();

        // Check all metadata.
        if (inType != null && this.allMetadataGroups != null) {
            for (MetadataGroup md : this.allMetadataGroups) {
                if (md.getType() != null && md.getType().getName().equals(inType.getName())) {
                    resultList.add(md);
                }
            }
        }

        return resultList;
    }

    public List<MetadataGroup> getAllMetadataGroupsByName(String inType) {

        List<MetadataGroup> resultList = new LinkedList<>();

        // Check all metadata.
        if (inType != null && this.allMetadataGroups != null) {
            for (MetadataGroup md : this.allMetadataGroups) {
                if (md.getType() != null && md.getType().getName().equals(inType)) {
                    resultList.add(md);
                }
            }
        }

        return resultList;
    }

    public List<String> getAddableMetadataGroupTypes() {

        // If e.g. the topstruct has no Metadata, or something...
        if (metadataGroupType == null) {
            return null;
        }

        // Get all Metadatatypes for my DocStructType.
        List<String> addableMetadata = new LinkedList<>();
        List<AllowedMetadataGroupType> allTypes = metadataGroupType.getAllAllowedGroupTypeTypes();

        // Get all metadata types which are known, iterate over them and check,
        // if they are still addable.
        for (AllowedMetadataGroupType mdt : allTypes) {

            // Metadata beginning with the HIDDEN_METADATA_CHAR are internal
            // metadata are not user addable.
            if (!mdt.isHidden()) {
                String maxnumber = mdt.getNumAllowed();

                // Metadata can only be available once; so we have to check if
                // it is already available.
                if (maxnumber.equals("1m") || maxnumber.equals("1o")) {
                    // Check metadata here only.
                    List<? extends MetadataGroup> availableMD = this.getAllMetadataGroupsByName(mdt.getGroupName());

                    if (availableMD.size() < 1) {
                        // Metadata is NOT available; we are allowed to add it.
                        addableMetadata.add(mdt.getGroupName());
                    }
                } else {
                    // We can add as many metadata as we want (+ or *).
                    addableMetadata.add(mdt.getGroupName());
                }
            }
        }

        if (addableMetadata == null || addableMetadata.isEmpty()) {
            return null;
        }

        return addableMetadata;
    }
}
