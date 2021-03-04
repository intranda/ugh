package ugh.dl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
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

public class MetadataGroup implements Serializable {

    private static final long serialVersionUID = -6283388063178498292L;

    private static final Logger LOGGER = Logger.getLogger(ugh.dl.DigitalDocument.class);

    protected MetadataGroupType MDType;
    // Document structure to which this metadata type belongs to.
    protected DocStruct myDocStruct;

    @Getter
    @Setter
    private List<Metadata> metadataList;
    @Getter
    @Setter
    private List<Person> personList;
    @Getter
    @Setter
    private List<Corporate> corporateList;

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

        this.MDType = theType;

        metadataList = new LinkedList<>();
        personList = new LinkedList<>();
        corporateList = new LinkedList<>();
        for (MetadataType mdt : MDType.getMetadataTypeList()) {
            if (mdt.getIsPerson()) {
                Person p = new Person(mdt);
                p.setRole(mdt.getName());
                personList.add(p);
            } else if (mdt.isCorporate()) {
                Corporate c = new Corporate(mdt);
                c.setRole(mdt.getName());
                corporateList.add(c);
            } else {
                Metadata md = new Metadata(mdt);
                metadataList.add(md);
            }
        }

    }

    /***************************************************************************
     * <p>
     * Sets the Document structure entity to which this object belongs to.
     * </p>
     * 
     * @param inDoc
     **************************************************************************/
    public void setDocStruct(DocStruct inDoc) {
        this.myDocStruct = inDoc;
    }

    /***************************************************************************
     * <p>
     * Returns the DocStruct instance, to which this metadataGroup object belongs. This is extremly helpful, if only the metadata instance is stored
     * in a list; the reference to the associated DocStrct instance is always kept.
     * </p>
     * 
     * @return DocStruct instance.
     **************************************************************************/
    public DocStruct getDocStruct() {
        return this.myDocStruct;
    }

    /***************************************************************************
     * <p>
     * Returns the type of the metadataGroup instance; The MetadataGroupType object which is returned, may have the same name, but be a different
     * object than the MetadataGroupType object from another MetadataGroupType.
     * </p>
     * 
     * @return MetadataGroupType instance
     **************************************************************************/
    public MetadataGroupType getType() {
        return this.MDType;
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
        this.MDType = inType;
    }

    public void addMetadata(Metadata metadata) throws MetadataTypeNotAllowedException {
        MetadataType type = metadata.getType();
        String inMdName = type.getName();
        boolean insert = false;

        String maxnumberallowed = type.getNum();
        if (StringUtils.isBlank(maxnumberallowed) || maxnumberallowed.equals("*") || maxnumberallowed.equals("+")) {
            insert = true;
        } else if (maxnumberallowed.equalsIgnoreCase("1m") || maxnumberallowed.equalsIgnoreCase("1o")) {
            // check if the metadatatype was used
            for (Metadata other : metadataList) {
                if (other.getType().getName().equals(inMdName)) {
                    // metadata was used before, another insertion is not allowed
                    insert = false;
                    break;

                }
                // metadata type was not used before
                insert = true;
            }
        }

        if (insert) {
            this.metadataList.add(metadata);
        } else {
            LOGGER.debug("Not allowed to add metadata '" + inMdName + "'");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(type, MDType);
            LOGGER.error(mtnae.getMessage());
            throw mtnae;
        }
    }

    public void addPerson(Person person) throws MetadataTypeNotAllowedException {

        MetadataType type = person.getType();
        String inMdName = type.getName();
        boolean insert = false;

        String maxnumberallowed = type.getNum();
        if (StringUtils.isBlank(maxnumberallowed) || maxnumberallowed.equals("*") || maxnumberallowed.equals("+")) {
            insert = true;
        } else if (maxnumberallowed.equalsIgnoreCase("1m") || maxnumberallowed.equalsIgnoreCase("1o")) {
            // check if the metadatatype was used
            for (Person other : personList) {
                if (other.getType().getName().equals(inMdName)) {
                    // metadata was used before, another insertion is not allowed
                    insert = false;
                    break;

                }
                // metadata type was not used before
                insert = true;
            }
        }

        if (insert) {
            personList.add(person);
        } else {
            LOGGER.debug("Not allowed to add metadata '" + inMdName + "'");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(type, MDType);
            LOGGER.error(mtnae.getMessage());
            throw mtnae;
        }
    }

    public void addCorporate(Corporate corporate)throws MetadataTypeNotAllowedException {


        MetadataType type = corporate.getType();
        String inMdName = type.getName();
        boolean insert = false;

        String maxnumberallowed = type.getNum();
        if (StringUtils.isBlank(maxnumberallowed) || maxnumberallowed.equals("*") || maxnumberallowed.equals("+")) {
            insert = true;
        } else if (maxnumberallowed.equalsIgnoreCase("1m") || maxnumberallowed.equalsIgnoreCase("1o")) {
            // check if the metadatatype was used
            for (Corporate other : corporateList) {
                if (other.getType().getName().equals(inMdName)) {
                    // metadata was used before, another insertion is not allowed
                    insert = false;
                    break;

                }
                // metadata type was not used before
                insert = true;
            }
        }

        if (insert) {
            corporateList.add(corporate);
        } else {
            LOGGER.debug("Not allowed to add metadata '" + inMdName + "'");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(type, MDType);
            LOGGER.error(mtnae.getMessage());
            throw mtnae;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MetadataGroup [MDType:");
        sb.append(MDType.getName());
        sb.append(", myDocStruct:");
        sb.append(myDocStruct.getType().getName());
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
        result = prime * result + ((MDType == null) ? 0 : MDType.hashCode());
        result = prime * result + ((metadataList == null) ? 0 : metadataList.hashCode());
        result = prime * result + ((personList == null) ? 0 : personList.hashCode());
        result = prime * result + ((corporateList == null) ? 0 : corporateList.hashCode());
        result = prime * result + ((myDocStruct == null) ? 0 : myDocStruct.hashCode());
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
        if (MDType == null) {
            if (other.MDType != null) {
                return false;
            }
        } else if (!MDType.equals(other.MDType)) {
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
        if (myDocStruct == null) {
            if (other.myDocStruct != null) {
                return false;
            }
        } else if (!myDocStruct.equals(other.myDocStruct)) {
            return false;
        }
        return true;
    }

}
