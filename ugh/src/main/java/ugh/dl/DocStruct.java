package ugh.dl;

/*******************************************************************************
 * ugh.dl / DocStruct.java
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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.exceptions.ContentFileNotLinkedException;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.IncompletePersonObjectException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;

/*******************************************************************************
 * <p>
 * A DocStruct object represents a structure entity in work. Every document consists of a structure, which can be separated into several structure
 * entities, which build hierarchical structure. Usually a <code>DigitalDocument</code> contains two structures; a logical and a physical one. Each
 * structure consists of a top DocStruct element that is embedded in some kind of structure. This structure is represented by parent and children of
 * <code>DocStruct</code> objects.
 * </p>
 * 
 * <p>
 * This class contains methods to:
 * <ul>
 * <li>Retrieve information about the structure (add, move and remove children),
 * <li>set the parent (the top element has no parent),
 * <li>set and retrieve metadata, which describe a structure entity,
 * <li>handle content files, which are linked to a structure entity.
 * </ul>
 * </p>
 * 
 * <p>
 * Every structure entity is of a special kind. The kind of entity is stored in a <code>DocStructType</code> element. Depending on the type of
 * structure entities certain metadata and children a permitted or forbidden.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2013-05-08
 * @see DigitalDocument
 * 
 *      TODOLOG
 * 
 * 
 * 
 *      CHANGELOG
 * 
 *      05.05.2010 --- Funk --- Minor changes.
 * 
 *      22.01.2010 --- Funk --- Minor changes due to findbugs.
 * 
 *      18.01.2010 --- Funk --- Adapted class to changed DocStruct.getAllMetadataByType(). Re-refactored method name.
 * 
 *      21.12.2009 --- Funk --- Re-added some missing (?) line. --- Added some logging.
 * 
 *      14.12.2009 --- Funk --- Removed an NPE.
 * 
 *      09.12.2009 --- Funk --- Some changes.
 * 
 *      08.12.2009 --- Funk --- Minor changes.
 * 
 *      30.11.2009 --- Funk --- Fixed NPE in getAllContentFiles().
 * 
 *      21.11.2009 --- Funk --- Fixed some NPEs.
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      13.10.2009 --- Funk --- Slightly improved addMetadata()'s error handling --- Corrected the DocStruct update from the prefs.
 * 
 *      10.11.2009 --- Funk --- Changed getAllVisibleMetadata(), ignoring now empty metadata fields.
 * 
 *      05.11.2009 --- Funk --- Added getAllVisibleMetadata().
 * 
 *      28.10.2009 --- Funk --- Added HIDDEN_METADATA_CHAR.
 * 
 *      21.10.2009 --- Funk --- Removed some unmappable character for encoding ASCII.
 * 
 *      20.10.2009 --- Funk --- Refactored some list constructs and conditionals --- Changed getDisplayMetadataTypes() that no "internal" metadata
 *      types (starting with "_") are returned.
 * 
 *      05.10.2009 --- Funk --- Adapted metadata and person constructors.
 * 
 *      30.09.2009 --- Funk --- Moved the recursively sorting methods to DigitalDocument. --- Added "private" to all method attributes.
 * 
 *      29.09.2009 --- Funk --- Added the sortMetadataAbcdefg() methods.
 * 
 *      17.09.2009 --- Funk --- Fixed some NullPointerException occurances in sortMetadata().
 * 
 *      11.09.2009 --- Wulf Riebensahm --- Equals method overloaded.
 * 
 *      08.06.2009 --- Funk --- Added the method sortMetadataRecursively, to be able to sort the metadata according to the prefs occurance.
 * 
 *      03.06.2009 --- Funk --- Added null check in countMDofthisType
 * 
 *      22.05.2009 --- Funk --- Fixed Bug DPD-216: NullPointerExfeption.
 * 
 *      30.04.2009 --- Funk --- Removed a bug. Look for the fix, if you want :-)
 * 
 *      29.09.2008 --- Funk --- Logging added.
 * 
 *      28.07.2008 --- Funk --- Check if persons are existing (somewhere).
 * 
 *      07.07.2008 --- Funk --- Persons are checked with the getAllMetadataByType() method.
 * 
 *      25.06.2008 - Funk - Already used persons are considered now in the method getAddableMetadataTypes.
 * 
 ******************************************************************************/

@Log4j2
public class DocStruct implements Serializable, HoldingElement {

    private static final long serialVersionUID = -4531356062293054921L;

    private static final String HIDDEN_METADATA_CHAR = "_";

    // List containing all Metadata instances.
    private List<Metadata> allMetadata;
    // List containing Metadata instances which has been removed; this instances
    // must be deleted from database etc.
    private List<Metadata> removedMetadata;

    private List<MetadataGroup> allMetadataGroups;

    private List<MetadataGroup> removedMetadataGroups;

    // List containing all DocStrct-instances being children of this instance.
    private List<DocStruct> children;
    // List containing all references to Contentfile objects.
    private List<ContentFileReference> contentFileReferences = new ArrayList<>();
    // List of all persons; list containing all Person objects.
    private List<Person> persons;

    private List<Corporate> corporates;

    private DocStruct parent;
    // All references to other DocStrct instances (containing References
    // objects).
    private List<Reference> docStructRefsTo = new ArrayList<>();
    // All references from another DocStruct to this one.
    private List<Reference> docStructRefsFrom = new ArrayList<>();
    // Type of this instance.
    private DocStructType type;
    // Local identifier of this docstruct.
    private String identifier = null;
    // Digital document, to which this DocStruct belongs.
    private DigitalDocument digdoc;
    // ID in database table (4 byte long).
    private long databaseid = 0;
    private transient Object origObject = null;
    // Information, if database instance is the same than this one.
    private boolean logical = false;
    private boolean physical = false;
    // String containing an identifier or a URL to the anchor.
    private String referenceToAnchor;
    //the amdSec referenced by this docStruct, if any
    private AmdSec amdSec;
    //the list of techMd sections referenced by this docStruct, if any
    private List<Md> techMdList;
    // the type of the docstruct, can be div or area
    private String docstructType = "div";

    private String admId;

    @Getter
    @Setter
    private boolean validationErrorPresent;
    @Getter
    @Setter
    private String validationMessage;

    // can be used to add additional data to the DocStruct. i.e. the written docstruct name in lido or the double page information for mets/mods
    @Getter
    @Setter
    private String additionalValue;

    // can be used to add additional data to the DocStruct, it can be exported to the div orderlabel attribute for logical docstructs
    @Getter
    @Setter
    private String orderLabel;

    // can be used to generate a link to another element
    @Getter
    @Setter
    private String link;

    /***************************************************************************
     * <p>
     * Constructor just used to be compatible with JavaBeans.
     * </p>
     * 
     * @deprecated
     **************************************************************************/
    @Deprecated(since = "1.0")
    public DocStruct() {
        super();
    }

    /***************************************************************************
     * <p>
     * Constructs a new DocStruct object of a given type. The type can be changed later using the <code>setType</code> method.
     * </p>
     * 
     * @param inType type of this DocStruct instance
     * @throws TypeNotAllowedForParentException is thrown, if this docstruct is not allowed for a parent
     **************************************************************************/
    protected DocStruct(DocStructType inType) throws TypeNotAllowedForParentException {

        // We have to check, if this type is allowed here, this depends on the
        // parent DocStruct.
        setType(inType);
    }

    /***************************************************************************
     * @param dd
     **************************************************************************/
    protected void setDigitalDocument(DigitalDocument dd) {
        this.digdoc = dd;
    }

    /***************************************************************************
     * <p>
     * Sets the type of this DocStruct instance. When changing the type, the allowed metadata elements and children are NOT checked. Therefore it is
     * possible to create documents, that are not valid against the current preferences file.
     * </p>
     * 
     * @param inType DocStructType to be set
     **************************************************************************/
    public void setType(DocStructType inType) {

        // Usually we had to check, if the new type is allowed. Search for
        // parent and see if the parent allows this type.
        this.type = inType;
    }

    /***************************************************************************
     * <p>
     * Get the type of an instance.
     * </p>
     * 
     * @return DocStructType of this DocStruct
     **************************************************************************/
    @Override
    public DocStructType getType() {
        return this.type;
    }

    /***************************************************************************
     * <p>
     * Returns all Children of an instance.
     * </p>
     * 
     * @return List containing DocStruct instances; if this instance has no children, null is returned.
     **************************************************************************/
    public List<DocStruct> getAllChildren() {

        if (this.children == null || this.children.isEmpty()) {
            return null; // NOSONAR
        }

        return this.children;
    }

    /**************************************************************************
     * <p>
     * Retrieves the identifier/URN/URL of the anchor. The anchor is another DocStruct which is stored in another DigitalDocument; e.g. a "Journal"
     * can be the anchor for PeriodicalVolumes. Both DocStructs are stored in different DigitalDocuments (different mets files) and are linked
     * together by an identifier. The identifier of the anchor should be stored here. On the side of the anchor, this identifier is stored as a normal
     * metadata field...
     * </p>
     * 
     * @return
     **************************************************************************/
    public String getReferenceToAnchor() {
        return this.referenceToAnchor;
    }

    /**************************************************************************
     * <p>
     * Sets the identifier of the anchor.
     * </p>
     * 
     * @param in
     **************************************************************************/
    public void setReferenceToAnchor(String in) {
        this.referenceToAnchor = in;
    }

    /***************************************************************************
     * <p>
     * Gets all Children for a DocStruct instance, which are of a special type, and which have a special type of metadata. E.g. you can get all
     * Articles wihch have an author. It is possible to use "*" as a parameter value for MetadataType and DocStructType. In this case, the "*" is a
     * wildcard.
     * </p>
     * 
     * 
     * @param theDocTypeName internal name of the structure type (as String)
     * @param theMDTypeName internal name of metadata type (as String)
     * @return List containing DocStruct instances; ; if this instance has no children, null is returned
     **************************************************************************/
    public List<DocStruct> getAllChildrenByTypeAndMetadataType(String theDocTypeName, String theMDTypeName) {

        List<DocStruct> resultList = new LinkedList<>();
        boolean docTypeTestPassed = false;
        boolean mdTypeTestPassed = false;
        List<Metadata> allMD;

        if (this.children == null || this.children.isEmpty()) {
            return null; // NOSONAR
        }

        for (DocStruct child : this.children) {

            // Check doctype.
            if ("*".equals(theDocTypeName)) {
                // Wildcard; we do not have to check the doctype.
                docTypeTestPassed = true;
            } else {
                DocStructType singleType = child.getType();
                String singlename = singleType.getName();
                if (singlename != null && singlename.equals(theDocTypeName)) {
                    docTypeTestPassed = true;
                } else {
                    // Wrong type.
                    continue;
                }
            }

            // Get all Metadatatypes.
            allMD = child.getAllMetadata();
            // Child has no metadata.
            if (allMD == null) {
                // MetadataType doesn't matter anyhow, so we can add this one,
                // too.
                if ("*".equals(theMDTypeName)) {
                    mdTypeTestPassed = true;
                } else {
                    mdTypeTestPassed = false;
                }
            } else {
                for (Metadata md : allMD) {
                    if ("*".equals(theMDTypeName)) {
                        mdTypeTestPassed = true;
                    } else {
                        PrefsType mdtype = md.getType();
                        String mdtypename = mdtype.getName();

                        if (mdtypename != null && mdtypename.equals(theMDTypeName)) {
                            mdTypeTestPassed = true;
                        }
                    }
                }
            }
            if (mdTypeTestPassed && docTypeTestPassed) {
                // Doctype and metadatatype test passed, add it.
                resultList.add(child);
            }
        }

        if (resultList.isEmpty()) {
            return null; //NOSONAR
        }

        return resultList;
    }

    /***************************************************************************
     * <p>
     * Sets the local identifier; currently there is no automatic check, if the identifier is used for another docstruct or metadata element.
     * </p>
     * 
     * @param in
     **************************************************************************/
    @Override
    public void setIdentifier(String in) {
        this.identifier = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    /***************************************************************************
     * <p>
     * Extracts a list with all Metadata objects which are identifiers (their MetadataType has the identifier flag set).
     * </p>
     * 
     * @return a List containing Metadata instances; if none were found null is returned.
     **************************************************************************/
    public List<Metadata> getAllIdentifierMetadata() {

        List<Metadata> result = new LinkedList<>();

        if (this.allMetadata == null) {
            return null; //NOSONAR
        }

        for (Metadata md : this.allMetadata) {
            if (md.getType().isIdentifier) {
                result.add(md);
            }
        }

        if (result.isEmpty()) {
            return null; //NOSONAR
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Copies a DocStruct element with all the associated Metadata and Person objects.
     * </p>
     * 
     * @param cpmetadata copies Metadata if set to true
     * @param recursive copies all children as well, if set to true
     * @return a new DocStruct instance
     * @throws TypeNotAllowedForParentException
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
    public DocStruct copy(boolean cpmetadata, boolean recursive) {

        DocStruct newStruct = null;
        try {
            newStruct = new DocStruct(this.getType());
        } catch (TypeNotAllowedForParentException e) {
            // This should never happen as we are creating the same
            // DocStructType.
            log.error(e);
        }

        // Copy the link to the parent.
        newStruct.setParent(this.getParent());
        if (this.logical) {
            newStruct.logical = this.logical;
        }

        // Copy metadata and persons.
        if (cpmetadata) {
            if (this.getAllMetadata() != null) {
                for (Metadata md : this.getAllMetadata()) {
                    try {
                        Metadata mdnew = new Metadata(md.getType());
                        mdnew.setValue(md.getValue());
                        if (md.getValueQualifier() != null && md.getValueQualifierType() != null) {
                            mdnew.setValueQualifier(md.getValueQualifier(), md.getValueQualifierType());
                        }
                        if (md.getAuthorityID() != null && md.getAuthorityValue() != null && md.getAuthorityURI() != null) {
                            mdnew.setAuthorityFile(md.getAuthorityID(), md.getAuthorityURI(), md.getAuthorityValue());
                        } else if (StringUtils.isNotBlank(md.getAuthorityValue())) {
                            mdnew.setAuthorityFile("", "", md.getAuthorityValue());
                        }
                        newStruct.addMetadata(mdnew);
                    } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                        // This should never happen, as we are adding the same
                        // MetadataType.
                        log.error(e);
                    }
                }
            }

            if (this.getAllMetadataGroups() != null) {
                for (MetadataGroup md : this.getAllMetadataGroups()) {
                    try {
                        MetadataGroup mdnew = new MetadataGroup(md.getType());
                        mdnew.setParent(newStruct);
                        List<Metadata> newmdlist = new LinkedList<>();
                        List<Person> newPersonList = new LinkedList<>();
                        for (Metadata meta : md.getMetadataList()) {
                            Metadata newMeta = new Metadata(meta.getType());
                            newMeta.setValue(meta.getValue());
                            if (meta.getValueQualifier() != null && meta.getValueQualifierType() != null) {
                                newMeta.setValueQualifier(meta.getValueQualifier(), meta.getValueQualifierType());
                            }
                            if (meta.getAuthorityID() != null && meta.getAuthorityValue() != null && meta.getAuthorityURI() != null) {
                                newMeta.setAuthorityFile(meta.getAuthorityID(), meta.getAuthorityURI(), meta.getAuthorityValue());
                            } else if (StringUtils.isNotBlank(meta.getAuthorityValue())) {
                                newMeta.setAuthorityFile("", "", meta.getAuthorityValue());
                            }
                            newmdlist.add(newMeta);
                        }

                        for (Person ps : md.getPersonList()) {
                            Person newps = new Person(ps.getType());
                            if (ps.getLastname() != null) {
                                newps.setLastname(ps.getLastname());
                            }
                            if (ps.getFirstname() != null) {
                                newps.setFirstname(ps.getFirstname());
                            }
                            if (ps.getAuthorityID() != null && ps.getAuthorityURI() != null && ps.getAuthorityValue() != null) {
                                newps.setAuthorityFile(ps.getAuthorityID(), ps.getAuthorityURI(), ps.getAuthorityValue());
                            } else if (StringUtils.isNotBlank(ps.getAuthorityValue())) {
                                newps.setAuthorityFile("", "", ps.getAuthorityValue());
                            }
                            if (ps.getInstitution() != null) {
                                newps.setInstitution(ps.getInstitution());
                            }
                            if (ps.getAffiliation() != null) {
                                newps.setAffiliation(ps.getAffiliation());
                            }
                            if (ps.getRole() != null) {
                                newps.setRole(ps.getRole());
                            }
                            newPersonList.add(newps);
                        }
                        mdnew.setMetadataList(newmdlist);
                        mdnew.setPersonList(newPersonList);
                        newStruct.addMetadataGroup(mdnew);

                    } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                        // This should never happen, as we are adding the same
                        // MetadataType.
                        log.error(e);
                    }
                }
            }

            // Copy the persons.
            if (this.getAllPersons() != null) {
                for (Person ps : this.getAllPersons()) {
                    try {
                        Person newps = new Person(ps.getType());
                        if (ps.getLastname() != null) {
                            newps.setLastname(ps.getLastname());
                        }
                        if (ps.getFirstname() != null) {
                            newps.setFirstname(ps.getFirstname());
                        }

                        if (ps.getAuthorityID() != null && ps.getAuthorityURI() != null && ps.getAuthorityValue() != null) {
                            newps.setAuthorityFile(ps.getAuthorityID(), ps.getAuthorityURI(), ps.getAuthorityValue());
                        }

                        if (ps.getInstitution() != null) {
                            newps.setInstitution(ps.getInstitution());
                        }
                        if (ps.getAffiliation() != null) {
                            newps.setAffiliation(ps.getAffiliation());
                        }
                        if (ps.getRole() != null) {
                            newps.setRole(ps.getRole());
                        }
                        newStruct.addPerson(newps);
                    } catch (IncompletePersonObjectException | MetadataTypeNotAllowedException e) {
                        // This should never happen as we are adding the same
                        // person type.
                        log.error(e);
                    }
                }
            }
            // copy corporations
            if (corporates != null) {
                for (Corporate c : corporates) {
                    try {
                        Corporate newC = new Corporate(c.getType());
                        newC.setMainName(c.getMainName());
                        newC.setSubNames(c.getSubNames());
                        newC.setPartName(c.getPartName());
                        newC.setAuthorityFile(c.getAuthorityID(), c.getAuthorityURI(), c.getAuthorityValue());
                        newStruct.addCorporate(newC);
                    } catch (MetadataTypeNotAllowedException e) {
                        log.error(e);
                    }
                }
            }
        }

        // Iterate over all children, if recursive set to true.
        if (recursive && this.getAllChildren() != null) {
            for (DocStruct child : this.getAllChildren()) {
                DocStruct copiedChild = child.copy(cpmetadata, recursive);
                try {
                    newStruct.addChild(copiedChild);
                } catch (TypeNotAllowedAsChildException e) {
                    String message = "This " + e.getClass().getName() + " should not have been occured!";
                    log.error(message, e);
                }
            }
        }

        return newStruct;
    }

    /***************************************************************************
     * <p>
     * Returns all References; parameter must be "to" or "from"; otherwise all references are returned a List is returned, containing "References"
     * instances.
     * </p>
     * 
     * @param in can be "to" or "from"
     * @return List containing Reference objects
     **************************************************************************/
    public List<Reference> getAllReferences(String in) {

        if (in == null) {
            return null; //NOSONAR
        }
        if ("to".equals(in)) {
            return this.docStructRefsTo;
        }
        if ("from".equals(in)) {
            return this.docStructRefsFrom;
        }

        return null; //NOSONAR
    }

    /***************************************************************************
     * <p>
     * Retrieves all References from this DocStruct to another - in other words: All References, in which this DocStruct is the Source.
     * </p>
     * 
     * @return List containing <code>References</code> objects
     **************************************************************************/
    public List<Reference> getAllToReferences() {
        return this.docStructRefsTo;
    }

    /***************************************************************************
     * <p>
     * Returns all References (just to-References from this DocStruct to another) of a specific type.
     * </p>
     * 
     * @param theType Type of the reference; e.g. "logical_physical" for references from logical structures to physical ones
     * @return List containing <code>References</code> objects
     **************************************************************************/
    public List<Reference> getAllToReferences(String theType) {

        List<Reference> refs = new ArrayList<>();

        if (this.docStructRefsTo != null) {
            for (Reference ref : this.docStructRefsTo) {
                if (ref.getType().equals(theType)) {
                    refs.add(ref);
                }
            }
        }

        if (refs.isEmpty()) {
            return null; //NOSONAR
        }

        return refs;
    }

    /***************************************************************************
     * <p>
     * Retrieves all References from this DocStruct from another - in other words: All References, in which this DocStruct is the target.
     * </p>
     * 
     * @return List containing <code>References</code> objects
     **************************************************************************/
    @JsonIgnore
    public List<Reference> getAllFromReferences() {
        return this.docStructRefsFrom;
    }

    /***************************************************************************
     * <p>
     * Returns all References (just from-References from this DocStruct to another) of a specific type.
     * </p>
     * 
     * @param theType Type of the reference; e.g. "logical_physical" for references from logical structures to physical ones
     * @return List containing <code>References</code> objects
     **************************************************************************/
    public List<Reference> getAllFromReferences(String theType) {

        List<Reference> refs = new ArrayList<>();

        if (this.docStructRefsFrom != null) {
            for (Reference ref : this.docStructRefsFrom) {
                if (ref.getType().equals(theType)) {
                    refs.add(ref);
                }
            }
        }

        if (refs.isEmpty()) {
            return null; //NOSONAR
        }

        return refs;
    }

    /***************************************************************************
     * <p>
     * Sets the parent; usually not necessary as the parent is set automatically, if a DocStruct instance is added as a child.
     * </p>
     * 
     * @param inParent
     **************************************************************************/
    public void setParent(DocStruct inParent) {

        if (inParent != null) {
            // Remove this DocStruct instance fromt he child's list.
            inParent.removeChild(this);
        }

        // Usually we had to check if this parent allows this instance being a
        // child because of its DocStructType.

        // Add child to this parent.
        this.parent = inParent;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    @JsonIgnore
    public DocStruct getParent() {
        return this.parent;
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
        if (this.allMetadataGroups == null || this.allMetadataGroups.isEmpty()) {
            return null; //NOSONAR
        }

        for (MetadataGroup mg : allMetadataGroups) {
            mg.checkDefaultDisplayMetadata();
        }

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

    /***************************************************************************
     * <p>
     * Gets all Metadata for this DocStruct instance.
     * </p>
     * 
     * @return List containing Metadata instances; if no metadata is available, null is returned.
     **************************************************************************/
    public List<Metadata> getAllMetadata() {

        if (this.allMetadata == null || this.allMetadata.isEmpty()) {
            return null; //NOSONAR
        }

        return this.allMetadata;
    }

    /***************************************************************************
     * <p>
     * Allows to set all Metadata. The Metadata objects are contained in a List. This method sets all Metadata; they are NOT added. Metadata which is
     * already available will be overwritten.
     * </p>
     * 
     * @param inList List containing Metadata objects.
     **************************************************************************/
    public void setAllMetadata(List<Metadata> inList) {
        this.allMetadata = inList;
    }

    public void setAllPersons(List<Person> personList) {
        this.persons = personList;
    }

    public void setAllCorporates(List<Corporate> corporateList) {
        corporates = corporateList;
    }

    /***************************************************************************
     * <p>
     * Retrieves all ContentFile objects, which belong to this instance.
     * </p>
     * 
     * @return List containing ContentFile objects; if no content files are available null is returned.
     **************************************************************************/
    @JsonIgnore
    public List<ContentFile> getAllContentFiles() {

        List<ContentFile> contentFiles = new ArrayList<>();

        if (this.contentFileReferences == null || this.contentFileReferences.isEmpty()) {
            return null; //NOSONAR
        }

        for (ContentFileReference contentFileReference : this.contentFileReferences) {
            // Add it, if it is not null AND it doesn't already belong to the
            // list.
            if (contentFileReference != null && !contentFiles.contains(contentFileReference.getCf())) {
                contentFiles.add(contentFileReference.getCf());
            }
        }

        return contentFiles;
    }

    /***************************************************************************
     * <p>
     * This method checks, if an instance of the DocStruct has a Metadata- or Person object of the given type.
     * </p>
     * 
     * @param inMDT
     * @return true, if available; otherwise false
     **************************************************************************/
    public boolean hasMetadataGroupType(MetadataGroupType inMDT) {

        // Check metadata.
        List<MetadataGroup> allMDs = this.getAllMetadataGroups();
        if (allMDs != null) {
            for (MetadataGroup md : allMDs) {
                MetadataGroupType mdt = md.getType();
                if (inMDT != null && inMDT.getName().equals(mdt.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * This method checks, if an instance of the DocStruct has a Metadata- or Person object of the given type.
     * </p>
     * 
     * @param inMDT
     * @return true, if available; otherwise false
     **************************************************************************/
    public boolean hasMetadataType(PrefsType inMDT) {
        if (inMDT == null) {
            return false;
        }
        // Check metadata.
        List<Metadata> allMDs = this.getAllMetadata();
        if (allMDs != null) {
            for (Metadata md : allMDs) {
                PrefsType mdt = md.getType();
                if (inMDT.getName().equals(mdt.getName())) {
                    return true;
                }
            }
        }

        // Check persons.
        List<Person> allPersons = this.getAllPersons();
        if (allPersons != null) {
            for (Person per : allPersons) {
                PrefsType mdt = per.getType();
                if (inMDT.getName().equals(mdt.getName())) {
                    return true;
                }
            }
        }
        // Check corporates
        if (corporates != null) {
            for (Corporate corp : corporates) {
                if (inMDT.getName().equals(corp.getType().getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Retrieves all References to ContentFiles.
     * </p>
     * 
     * @return List containing ContentFileReference objects
     * @see ContentFileReference
     **************************************************************************/
    public List<ContentFileReference> getAllContentFileReferences() {
        return this.contentFileReferences;
    }

    /***************************************************************************
     * <p>
     * Adds a new ContentFileReference to this DocStruct and adds the file to the FileSet.
     * </p>
     * 
     * 
     * @param theFile ContentFile object to be added
     * @return always true
     * @see FileSet
     **************************************************************************/
    public void addContentFile(ContentFile theFile) {

        // Create a new FileSet if there is none available.
        FileSet fs;
        if (this.digdoc.getFileSet() == null) {
            fs = new FileSet();
            this.digdoc.setFileSet(fs);
        } else {
            fs = this.digdoc.getFileSet();
        }

        // Add the file, existance check is done in FileSet.addFile() now.
        fs.addFile(theFile);

        if (this.contentFileReferences == null) {
            // Re-added this line, maybe was it's deletion an error?
            this.contentFileReferences = new LinkedList<>();
        }
        // Now we can add the reference to the ContentFile, if the reference is
        // not existing yet.
        ContentFileReference cfr = new ContentFileReference();
        cfr.setCf(theFile);
        if (!this.contentFileReferences.contains(cfr)) {
            this.contentFileReferences.add(cfr);
            theFile.addDocStructAsReference(this);
        }

    }

    /***************************************************************************
     * <p>
     * Adds a new ContentFile object to this DocStruct object; there is no check, if a ContentFile is already linked to this DocStruct.
     * </p>
     * <p>
     * Before adding ContentFile objects to a DocStruct, make sure they are already added to the FileSet.
     * </p>
     * 
     * 
     * @param inCF ContentFile object to be added
     * @return always true
     * @see FileSet
     **************************************************************************/
    public void addContentFile(ContentFile inCF, ContentFileArea inArea) {

        if (this.contentFileReferences == null) {
            // Re-added this line, maybe was it's deletion an error?
            this.contentFileReferences = new ArrayList<>();
        }

        // Check if ContentFile belongs already to the FileSet.
        FileSet fs = this.digdoc.getFileSet();
        // Get all content files of this digital document.
        List<ContentFile> allCFs = fs.getAllFiles();
        if (!allCFs.contains(inCF)) {
            // Doesn't contain this content file.
            fs.addFile(inCF);
        }

        // Now add reference to ContentFile.
        ContentFileReference cfr = new ContentFileReference();
        cfr.setCfa(inArea);
        cfr.setCf(inCF);
        this.contentFileReferences.add(cfr);
        inCF.addDocStructAsReference(this);

    }

    /***************************************************************************
     * <p>
     * Removes links between a ContentFile object and this DocStruct object. If a single ContentFile is referenced more than once from this DocStruct
     * all links are removed.<br>
     * For that reason all attached ContentFileReference objects are searched.
     * </p>
     * 
     * @throws ContentFileNotLinkedException if ContentFile is not linked to this DocStruct
     * @param inCF to be removed
     * @return true, if succeeded; otherwise false
     **************************************************************************/
    public boolean removeContentFile(ContentFile theContentFile) throws ContentFileNotLinkedException {

        boolean removed = false;

        if (this.contentFileReferences == null) {
            return false;
        }

        List<ContentFileReference> copiedContentFileReferences = new ArrayList<>(this.contentFileReferences);

        for (ContentFileReference cfr : copiedContentFileReferences) {
            if (cfr.getCf() != null && cfr.getCf().equals(theContentFile)) {
                // The ContentFile is in the Reference; so remove file and
                // reference.
                this.contentFileReferences.remove(cfr);
                ContentFile cf = cfr.getCf();
                cf.removeDocStructAsReference(this);
                removed = true;
            }
        }

        // Given ContentFile is NOT member.
        if (!removed) {
            String message = "Content file '" + theContentFile.getLocation() + "' is not a member of DocStruct '" + this.getType().getName() + "'";
            throw new ContentFileNotLinkedException(message);
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Adds a new reference to this DocStruct instance. References are always linked both ways. Both docstruct instances are storing a reference to
     * the other DocStruct instance. This methods stores the To-Reference. The DocStruct instance given as a parameter is the target of the Reference
     * (to which is linked to). The appropriate From-Reference (from the target to the source - this DocStruct instance) is set automatically. Each
     * Reference can contain a type (string).
     * </p>
     * 
     * @param inDocStruct Target of the Reference
     * @param theType String containing any information about the type of reference
     * @return a newly created References object containing information about linking both DocStructs
     **************************************************************************/
    public Reference addReferenceTo(DocStruct inDocStruct, String theType) {

        Reference ref = new Reference();
        if (this.databaseid == 0) {
            ref.setSource(this);
        } else {
            ref.setSourceID(this.databaseid);
        }
        if (inDocStruct.databaseid == 0) {
            ref.setTarget(inDocStruct);
        } else {
            ref.setTargetID(inDocStruct.databaseid);
        }
        ref.setType(theType);
        this.docStructRefsTo.add(ref);
        inDocStruct.docStructRefsFrom.add(ref);

        return ref;
    }

    /***************************************************************************
     * <p>
     * Adds a From-Reference. The current DocStruct instance is the target of the Reference. The appropriate To-Reference is added automatically to
     * the Source-DocStruct. For more detailed information, see addReferenceTo method.
     * </p>
     * 
     * @param inDocStruct DocStruct object, which is the source of the reference.
     * @param theType any kind of linking information
     * @return a newly created References object containing information about linking both DocStructs
     **************************************************************************/
    public Reference addReferenceFrom(DocStruct inDocStruct, String theType) {

        Reference ref = new Reference();
        if (this.databaseid == 0) {
            ref.setTarget(this);
        } else {
            ref.setTargetID(this.databaseid);
        }
        if (inDocStruct.databaseid == 0) {
            ref.setSource(inDocStruct);
        } else {
            ref.setSourceID(inDocStruct.databaseid);
        }
        ref.setType(theType);
        this.docStructRefsFrom.add(ref);
        inDocStruct.docStructRefsTo.add(ref);

        return ref;
    }

    /***************************************************************************
     * <p>
     * Removes a To-Reference (a reference to another docstruct instance). The corresponding From-Reference in the Target-Docstruct object is also
     * deleted. The References object is not used anymore and will be deleted at the next garbage collection.
     * </p>
     * 
     * @param inStruct target-DocStruct
     * @return true, if successful
     **************************************************************************/
    public boolean removeReferenceTo(DocStruct inStruct) {

        List<Reference> ll = new ArrayList<>(this.docStructRefsTo);

        for (Reference ref : ll) {
            if (ref.getTarget().equals(inStruct)) {
                // Remove reference from this instance.
                this.docStructRefsTo.remove(ref);
                DocStruct targetStruct = ref.getTarget();
                List<Reference> ll2 = targetStruct.docStructRefsFrom;
                // Remove the reference from target.
                if (ll2 != null) {
                    ll2.remove(ref);
                }
                break;
            }
        }

        return true;
    }

    /**************************************************************************
     * <p>
     * Removes a From-Reference (a reference from another docstruct instance to this one). The corresponding To-Reference in this DocStruct
     * (source-Docstruct object) is also deleted. The References object is not used anymore and will be deleted at the next garbage collection.
     * </p>
     * 
     * @param inStruct Source-DocStruct
     * @return true, if successful
     **************************************************************************/
    public boolean removeReferenceFrom(DocStruct inStruct) {

        List<Reference> ll = new ArrayList<>(this.docStructRefsFrom);

        for (Reference ref : ll) {
            if (ref.getSource().equals(inStruct)) {
                // Remove reference from this instance.
                this.docStructRefsFrom.remove(ref);
                DocStruct targetStruct = ref.getTarget();
                List<Reference> ll2 = targetStruct.docStructRefsTo;
                // Remove the reference from source.
                if (ll2 != null) {
                    ll2.remove(ref);
                }
                break;
            }
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Adds a metadata object to this instance; The method checks, if it is allowed to add one (based on the configuration). If so, the object is
     * added and returns true; otherwise it returns false.
     * </p>
     * <p>
     * The Metadata object must already include all necessary information as MetadataType and value.
     * </p>
     * <p>
     * For internal reasons this method changes the MetadataType object against a local copy, which is retrieved from the appropriate DocStructType of
     * this DocStruct instance. The internal name of both MetadataType objects must be identical. If a local copy cannot be found (which means, the
     * metadata type is NOT valid for this kind of DocStruct object), false is returned.
     * </p>
     * 
     * @param theMetadataGroup Metadata object to be added.
     * @return TRUE if metadata was added succesfully, FALSE otherwise.
     * @throws MetadataTypeNotAllowedException If the DocStructType of this DocStruct instance does not allow the MetadataType or if the maximum
     *             number of Metadata (of this type) is already available.
     * @throws DocStructHasNoTypeException If no DocStruct Type is set for the DocStruct object; for this reason the metadata can't be added, because
     *             we cannot check, wether if the metadata type is allowed or not.
     * @see Metadata
     **************************************************************************/
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
        if (this.type == null) {
            String message = "Error occured while adding metadata of type '" + inMdName + "' to DocStruct '" + this.getType().getName() + "'";
            log.error(message);
            throw new DocStructHasNoTypeException(message);
        }

        prefsMdType = this.type.getMetadataGroupByGroup(inMdType);

        // Ask DocStructType instance to get MetadataType by Type. At this point
        // we are creating a local copy of the MetadataType object.
        if (prefsMdType == null && !(inMdName.startsWith(HIDDEN_METADATA_CHAR))) {
            MetadataTypeNotAllowedException e = new MetadataTypeNotAllowedException(null, this.getType());
            log.error(e.getMessage());
            throw e;
        }

        // Check, if it's an internal MetadataType - all internal types begin
        // with the HIDDEN_METADATA_CHAR, we can have as many as we want.
        if (inMdName.startsWith(HIDDEN_METADATA_CHAR)) {
            maxnumberallowed = "*";
            prefsMdType = inMdType;
        } else {
            maxnumberallowed = this.type.getNumberOfMetadataGroups(prefsMdType);
        }

        // Check, if another Metadata instance is allowed.
        //
        // How many metadata are already available.
        number = countMDofthisType(inMdName);

        // As many as we want (zero or more).
        if ("*".equals(maxnumberallowed)) {
            insert = true;
        }

        // Once or more.
        if ("+".equals(maxnumberallowed)) {
            insert = true;
        }

        // Only one, if we have already one, we cannot add it.
        if ("1m".equalsIgnoreCase(maxnumberallowed) || "1o".equalsIgnoreCase(maxnumberallowed)) {
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
        maxnumbersallowed = this.type.getNumberOfMetadataGroups(inMdType);

        if (!force && typesavailable == 1 && "+".equals(maxnumbersallowed)) {
            // There must be at least one.
            return false;
        }
        if (!force && typesavailable == 1 && "1m".equals(maxnumbersallowed)) {
            // There must be at least one.
            return false;
        }

        theMd.parent = null;

        if (this.removedMetadataGroups == null) {
            this.removedMetadataGroups = new LinkedList<>();
        }

        this.removedMetadataGroups.add(theMd);
        this.allMetadataGroups.remove(theMd);

        return true;
    }

    /***************************************************************************
     * <p>
     * Removes Metadata from this DocStruct object. If there must be at least one Metadata object of this kind, attached to this DocStruct instance
     * (according to configuration), the metadata is NOT removed.
     * </p>
     * <p>
     * If you want to remove Metadata of a specific type temporarily (e.g. to replace it), use the changeMetadata method instead.
     * </p>
     * 
     * @param inMD Metadata object which should be removed
     * @return true, if data can be removed; otherwise false
     * @see #canMetadataBeRemoved
     **************************************************************************/
    public boolean removeMetadataGroup(MetadataGroup inMD) {
        // Just calls removeMetadata with force set to false.
        return removeMetadataGroup(inMD, false);
    }

    /***************************************************************************
     * <p>
     * Exchanges a Metadata object against an old one. Only metadata objects of the same type (of the same MetadataType object) can be exchanged. The
     * Metadata-Type object of the new Metadata object is copied locally (as it is done, when adding metadata).
     * </p>
     * 
     * <p>
     * OLD COMMENT? : exchanges two metadata objects; can be used instead of doing a remove and an add later on. Must be used, if a Metadata object
     * cannot be removed because of DTD (there must always be at least one object). Therefore we can only change Metadata objects of the same
     * MetadataType.
     * </p>
     * 
     * @param theOldMd Metadata object which should be replaced.
     * @param theNewMd New Metadata object.
     * @return True, if Metadata object could be exchanged; otherwise false.
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
    @Override
    public void changeMetadataGroup(MetadataGroup theOldMd, MetadataGroup theNewMd) throws MetadataTypeNotAllowedException {

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
        MetadataGroupType mdType = this.type.getMetadataGroupByGroup(theOldMd.getType());
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

    /***************************************************************************
     * <p>
     * Adds a metadata object to this instance; The method checks, if it is allowed to add one (based on the configuration). If so, the object is
     * added and returns true; otherwise it returns false.
     * </p>
     * <p>
     * The Metadata object must already include all necessary information as MetadataType and value.
     * </p>
     * <p>
     * For internal reasons this method changes the MetadataType object against a local copy, which is retrieved from the appropriate DocStructType of
     * this DocStruct instance. The internal name of both MetadataType objects must be identical. If a local copy cannot be found (which means, the
     * metadata type is NOT valid for this kind of DocStruct object), false is returned.
     * </p>
     * 
     * @param theMetadata Metadata object to be added.
     * @throws MetadataTypeNotAllowedException If the DocStructType of this DocStruct instance does not allow the MetadataType or if the maximum
     *             number of Metadata (of this type) is already available.
     * @throws DocStructHasNoTypeException If no DocStruct Type is set for the DocStruct object; for this reason the metadata can't be added, because
     *             we cannot check, wether if the metadata type is allowed or not.
     * @see Metadata
     **************************************************************************/
    @Override
    public void addMetadata(Metadata theMetadata) throws MetadataTypeNotAllowedException, DocStructHasNoTypeException {

        PrefsType inMdType = theMetadata.getType();
        String inMdName = inMdType.getName();
        // Integer, number of metadata allowed for this metadatatype.
        String maxnumberallowed;
        // Integer, number of metadata already available.
        int number;
        // Metadata can only be inserted if set to true.
        boolean insert = false;
        // Prefs MetadataType.
        MetadataType prefsMdType;

        // First get MetadataType object for the DocStructType to which this
        // document structure belongs to get global MDType.
        if (this.type == null) {
            String message = "Error occured while adding metadata of type '" + inMdName + "' to DocStruct";
            log.error(message);
            throw new DocStructHasNoTypeException(message);
        }

        prefsMdType = this.type.getMetadataTypeByType(inMdType);

        // Ask DocStructType instance to get MetadataType by Type. At this point
        // we are creating a local copy of the MetadataType object.
        if (prefsMdType == null) {
            MetadataTypeNotAllowedException e = new MetadataTypeNotAllowedException(inMdType, this.getType());
            log.error(e.getMessage());
            throw e;
        }

        //        // Check, if it's an internal MetadataType - all internal types begin
        //        // with the HIDDEN_METADATA_CHAR, we can have as many as we want.

        maxnumberallowed = this.type.getNumberOfMetadataType(prefsMdType);

        // Check, if another Metadata instance is allowed.
        //
        // How many metadata are already available.
        number = countMDofthisType(inMdName);

        // As many as we want (zero or more).
        if (maxnumberallowed == null) {
            maxnumberallowed = "*";
        }
        if ("*".equals(maxnumberallowed)) {
            insert = true;
        }

        // Once or more.
        if ("+".equals(maxnumberallowed)) {
            insert = true;
        }

        // Only one, if we have already one, we cannot add it.
        if ("1m".equalsIgnoreCase(maxnumberallowed) || "1o".equalsIgnoreCase(maxnumberallowed)) {
            if (number < 1) {
                insert = true;
            } else {
                insert = false;
            }
        }

        // Add metadata.
        if (insert) {
            // Set type to MetadataType of the DocStructType.
            theMetadata.setType(prefsMdType);
            // Set this document structure as myDocStruct.
            theMetadata.setParent(this);
            if (this.allMetadata == null) {
                // Create list, if not already available.
                this.allMetadata = new LinkedList<>();
            }
            this.allMetadata.add(theMetadata);
        } else {
            log.debug("Not allowed to add metadata '" + inMdName + "'");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(inMdType, this.getType());
            log.error(mtnae.getMessage());
            throw mtnae;
        }
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
    public void removeMetadata(Metadata theMd, boolean force) {

        PrefsType inMdType;
        String maxnumbersallowed;
        int typesavailable;

        // Get Type of inMD.
        inMdType = theMd.getType();

        // How many metadata of this type do we have already.
        typesavailable = countMDofthisType(inMdType.getName());

        // How many types must be at least available.
        maxnumbersallowed = this.type.getNumberOfMetadataType(inMdType);

        if (!force && typesavailable == 1 && "+".equals(maxnumbersallowed)) {
            // There must be at least one.
            return;
        }
        if (!force && typesavailable == 1 && "1m".equals(maxnumbersallowed)) {
            // There must be at least one.
            return;
        }

        theMd.parent = null;

        if (this.removedMetadata == null) {
            this.removedMetadata = new LinkedList<>();
        }

        this.removedMetadata.add(theMd);
        this.allMetadata.remove(theMd);
    }

    /***************************************************************************
     * <p>
     * Removes Metadata from this DocStruct object. If there must be at least one Metadata object of this kind, attached to this DocStruct instance
     * (according to configuration), the metadata is NOT removed.
     * </p>
     * <p>
     * If you want to remove Metadata of a specific type temporarily (e.g. to replace it), use the changeMetadata method instead.
     * </p>
     * 
     * @param inMD Metadata object which should be removed
     * @return true, if data can be removed; otherwise false
     * @see #canMetadataBeRemoved
     **************************************************************************/
    public void removeMetadata(Metadata inMD) {
        // Just calls removeMetadata with force set to false.
        removeMetadata(inMD, false);
    }

    /***************************************************************************
     * <p>
     * Exchanges a Metadata object against an old one. Only metadata objects of the same type (of the same MetadataType object) can be exchanged. The
     * Metadata-Type object of the new Metadata object is copied locally (as it is done, when adding metadata).
     * </p>
     * 
     * <p>
     * OLD COMMENT? : exchanges two metadata objects; can be used instead of doing a remove and an add later on. Must be used, if a Metadata object
     * cannot be removed because of DTD (there must always be at least one object). Therefore we can only change Metadata objects of the same
     * MetadataType.
     * </p>
     * 
     * @param theOldMd Metadata object which should be replaced.
     * @param theNewMd New Metadata object.
     * @return True, if Metadata object could be exchanged; otherwise false.
     **************************************************************************/
    public boolean changeMetadata(Metadata theOldMd, Metadata theNewMd) {

        PrefsType oldMdt;
        PrefsType newMdt;
        String oldName;
        String newName;
        int counter = 0;

        // Get MetadataTypes.
        oldMdt = theOldMd.getType();
        newMdt = theNewMd.getType();

        // Get names.
        oldName = oldMdt.getName();
        newName = newMdt.getName();

        if (!oldName.equals(newName)) {
            // Different metadata types.
            return false;
        }

        // Remove old object; get place of old object in list.
        for (Metadata m : this.allMetadata) {
            // Found old metadata object.
            if (m.equals(theOldMd)) {
                // Get out of loop.
                break;
            }
            counter++;
        }

        // Ask DocStructType instance to get a new MetadataType object of the
        // same kind.
        MetadataType mdType = this.type.getMetadataTypeByType(theOldMd.getType());
        theNewMd.setType(mdType);

        this.allMetadata.remove(theOldMd);
        this.allMetadata.add(counter, theNewMd);

        return true;
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
    public List<? extends Metadata> getAllMetadataByType(PrefsType inType) { //NOSONAR

        List<Metadata> resultList = new LinkedList<>();

        // Check all metadata.
        if (inType != null && this.allMetadata != null) {
            for (Metadata md : this.allMetadata) {
                if (md.getType() != null && md.getType().getName().equals(inType.getName())) {
                    resultList.add(md);
                }
            }
        }

        // Check all persons.
        if (inType != null && this.persons != null) {
            for (Metadata md : this.persons) {
                if (md.getType() != null && md.getType().getName().equals(inType.getName())) {
                    resultList.add(md);
                }
            }
        }
        if (inType != null && corporates != null) {
            for (Metadata md : corporates) {
                if (md.getType() != null && md.getType().getName().equals(inType.getName())) {
                    resultList.add(md);
                }
            }
        }
        return resultList;
    }

    /***************************************************************************
     * <p>
     * Retrieves all Person object, which belong to this DocStruct and have a special type. Persons only!
     * </p>
     * 
     * @param inType MetadataType we are looking for.
     * @return List containing Metadata objects; if no metadata ojects are available, null is returned.
     **************************************************************************/
    public List<Person> getAllPersonsByType(PrefsType inType) {

        List<Person> resultList = new LinkedList<>();

        if (inType == null) {
            return null; //NOSONAR
        }

        // Check all persons.
        if (this.persons != null) {
            for (Person per : this.persons) {
                if (per.getType() != null && per.getType().getName().equals(inType.getName())) {
                    resultList.add(per);
                }
            }
        }

        // List is empty.
        if (resultList.isEmpty()) {
            return null; //NOSONAR
        }

        return resultList;
    }

    public List<Corporate> getAllCorporatesByType(PrefsType inType) {

        List<Corporate> resultList = new LinkedList<>();

        if (inType == null) {
            return null; //NOSONAR
        }

        // Check all persons.
        if (corporates != null) {
            for (Corporate corp : corporates) {
                if (corp.getType() != null && corp.getType().getName().equals(inType.getName())) {
                    resultList.add(corp);
                }
            }
        }

        // List is empty.
        if (resultList.isEmpty()) {
            return null; //NOSONAR
        }

        return resultList;
    }

    /***************************************************************************
     * <p>
     * Gets all Metadata for the current DocStruct, which shall be displayed, what includes all metadata that are not starting with the
     * HIDDEN_METADATA_CHAR.
     * </p>
     * 
     * @return List containing MetadataType objects
     **************************************************************************/
    public List<Metadata> getAllVisibleMetadata() {

        // Start with the list of all metadata.
        List<Metadata> result = new LinkedList<>();

        // Iterate over all metadata.
        if (getAllMetadata() != null) {
            for (Metadata md : getAllMetadata()) {
                // If the metadata does not start with the HIDDEN_METADATA_CHAR,
                // add it to the result list.
                if (md.getType().getName() != null && !md.getType().getName().startsWith(HIDDEN_METADATA_CHAR)) {
                    result.add(md);
                }
            }
        }

        if (result.isEmpty()) {
            result = null;
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Gets all MetadataTypes, which shall ALWAYS be displayed, even though they have no value.<br/>
     * 
     * Includes all metadata with attributes defaultDisplay="true" in the prefs. Hidden metadata, which start with the HIDDEN_METADATA_CHAR, will not
     * be included.
     * </p>
     * 
     * @return List containing MetadataType objects
     **************************************************************************/
    public List<MetadataGroupType> getDefaultDisplayMetadataGroupTypes() {

        List<MetadataGroupType> result = new LinkedList<>();

        if (this.type == null) {
            return null; //NOSONAR
        }

        // Start with the list of MetadataTypes, which are having the
        // "defaultDisplay" attribute set.
        List<MetadataGroupType> allDefaultMdTypes = this.type.getAllDefaultDisplayMetadataGroups();

        if (allDefaultMdTypes != null) {
            // Iterate over all defaultDisplay metadata types and check, if
            // metadata of this type is already available.
            for (MetadataGroupType mdt : allDefaultMdTypes) {
                if (!hasMetadataGroup(mdt.getName()) && !mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
                    // If none of these metadata is available, AND it is not a
                    // hidden metadata type, add it to the result list.
                    result.add(mdt);
                }
            }
        }

        if (result.isEmpty()) {
            result = null;
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * See getDefaultDisplayMetadataTypes().
     * </p>
     * 
     * @deprecated
     * @return List containing MetadataType objects
     **************************************************************************/
    @Deprecated(since = "1.0")
    public List<MetadataGroupType> getDisplayMetadataGroupTypes() {
        return getDefaultDisplayMetadataGroupTypes();
    }

    /***************************************************************************
     * @param metadataTypeName
     * @return
     **************************************************************************/
    private boolean hasMetadataGroup(String metadataGroupTypeName) {

        if (this.allMetadataGroups != null) {
            for (MetadataGroup md : this.allMetadataGroups) {
                MetadataGroupType mdt = md.getType();
                if (mdt == null) {
                    continue;
                }
                String name = mdt.getName();
                if (name.equals(metadataGroupTypeName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Gets all MetadataTypes, which shall ALWAYS be displayed, even though they have no value.<br/>
     * 
     * Includes all metadata with attributes defaultDisplay="true" in the prefs. Hidden metadata, which start with the HIDDEN_METADATA_CHAR, will not
     * be included.
     * </p>
     * 
     * @return List containing MetadataType objects
     **************************************************************************/
    public List<MetadataType> getDefaultDisplayMetadataTypes() {

        List<MetadataType> result = new LinkedList<>();

        if (this.type == null) {
            return null; //NOSONAR
        }

        // Start with the list of MetadataTypes, which are having the
        // "defaultDisplay" attribute set.
        List<MetadataType> allDefaultMdTypes = this.type.getAllDefaultDisplayMetadataTypes();

        if (allDefaultMdTypes != null) {
            // Iterate over all defaultDisplay metadata types and check, if
            // metadata of this type is already available.
            for (MetadataType mdt : allDefaultMdTypes) {
                if (!hasMetadata(mdt.getName()) && !mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
                    // If none of these metadata is available, AND it is not a
                    // hidden metadata type, add it to the result list.
                    result.add(mdt);
                }
            }
        }

        if (result.isEmpty()) {
            result = null;
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * See getDefaultDisplayMetadataTypes().
     * </p>
     * 
     * @deprecated
     * @return List containing MetadataType objects
     **************************************************************************/
    @Deprecated(since = "1.0")
    public List<MetadataType> getDisplayMetadataTypes() {
        return getDefaultDisplayMetadataTypes();
    }

    /***************************************************************************
     * @param metadataTypeName
     * @return
     **************************************************************************/
    private boolean hasMetadata(String metadataTypeName) {

        if (this.allMetadata != null) {
            for (Metadata md : this.allMetadata) {
                PrefsType mdt = md.getType();
                if (mdt == null) {
                    continue;
                }
                String name = mdt.getName();
                if (name.equals(metadataTypeName)) {
                    return true;
                }
            }
        }

        if (this.persons != null) {
            for (Person per : this.persons) {
                PrefsType mdt = per.getType();
                if (mdt == null) {
                    continue;
                }
                String name = mdt.getName();
                if (name.equals(metadataTypeName)) {
                    return true;
                }
            }
        }

        if (corporates != null) {
            for (Corporate corp : corporates) {
                PrefsType mdt = corp.getType();
                if (mdt == null) {
                    continue;
                }
                String name = mdt.getName();
                if (name.equals(metadataTypeName)) {
                    return true;
                }
            }
        }
        return false;
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

        if (this.allMetadata != null) {
            for (Metadata md : this.allMetadata) {
                testtype = md.getType();
                if (testtype != null && testtype.getName().equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }
        }

        if (allMetadataGroups != null) {
            for (MetadataGroup mdg : allMetadataGroups) {
                MetadataGroupType mgt = mdg.getType();
                if (mgt != null && mgt.getName().equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }

        }

        if (this.persons != null) {
            for (Person per : this.persons) {
                testtype = per.getType();
                if (testtype != null && testtype.getName().equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }
        }

        if (corporates != null) {
            for (Corporate corp : corporates) {
                testtype = corp.getType();
                if (testtype != null && testtype.getName().equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }
        }
        return counter;
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
    public List<MetadataGroupType> getAddableMetadataGroupTypes() {

        // If e.g. the topstruct has no Metadata, or something...
        if (this.type == null) {
            return null; //NOSONAR
        }

        // Get all Metadatatypes for my DocStructType.
        List<MetadataGroupType> addableMetadata = new LinkedList<>();
        List<MetadataGroupType> allTypes = this.type.getAllMetadataGroupTypes();

        // Get all metadata types which are known, iterate over them and check,
        // if they are still addable.
        for (MetadataGroupType mdt : allTypes) {

            // Metadata beginning with the HIDDEN_METADATA_CHAR are internal
            // metadata are not user addable.
            if (!mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
                String maxnumber = this.type.getNumberOfMetadataGroups(mdt);

                // Metadata can only be available once; so we have to check if
                // it is already available.
                if ("1m".equals(maxnumber) || "1o".equals(maxnumber)) {
                    // Check metadata here only.
                    List<? extends MetadataGroup> availableMD = this.getAllMetadataGroupsByType(mdt);

                    if (availableMD.isEmpty()) {
                        // Metadata is NOT available; we are allowed to add it.
                        addableMetadata.add(mdt);
                    }
                } else {
                    // We can add as many metadata as we want (+ or *).
                    addableMetadata.add(mdt);
                }
            }
        }

        if (addableMetadata.isEmpty()) {
            return null; //NOSONAR
        }

        return addableMetadata;
    }

    public List<MetadataGroupType> getPossibleMetadataGroupTypes() {
        return getAddableMetadataGroupTypes();
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
        if (this.type == null) {
            return null; //NOSONAR
        }

        // Get all Metadatatypes for my DocStructType.
        List<MetadataType> addableMetadata = new LinkedList<>();
        List<MetadataType> allTypes = this.type.getAllMetadataTypes();

        // Get all metadata types which are known, iterate over them and check,
        // if they are still addable.
        for (MetadataType mdt : allTypes) {

            // Metadata beginning with the HIDDEN_METADATA_CHAR are internal
            // metadata are not user addable.
            if (includeHiddenMetadata || !mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
                String maxnumber = this.type.getNumberOfMetadataType(mdt);

                // Metadata can only be available once; so we have to check if
                // it is already available.
                if ("1m".equals(maxnumber) || "1o".equals(maxnumber)) {
                    // Check metadata here only.
                    List<? extends Metadata> availableMD = this.getAllMetadataByType(mdt);

                    if (!mdt.isPerson && (availableMD.isEmpty())) {
                        // Metadata is NOT available; we are allowed to add it.
                        addableMetadata.add(mdt);
                    }
                    // Then check persons here.
                    boolean used = false;
                    if (mdt.getIsPerson() && this.getAllPersons() != null) {
                        for (Person per : this.getAllPersons()) {
                            // If the person of the current metadata type is
                            // already used, set the flag.
                            if (per.getRole().equals(mdt.getName())) {
                                used = true;
                            }
                        }

                        // Only add the metadata type, if the person was not
                        // already used.
                        if (!used) {
                            addableMetadata.add(mdt);
                        }
                    }
                } else {
                    // We can add as many metadata as we want (+ or *).
                    addableMetadata.add(mdt);
                }
            }
        }

        if (addableMetadata == null || addableMetadata.isEmpty()) {
            return null; //NOSONAR
        }

        return addableMetadata;
    }

    //
    // Handle children.
    //
    // All methods to add, remove, modify or change the position of children in
    // the tree are in here.
    //

    /***************************************************************************
     * <p>
     * Adds a DocStruct object as a child to this instance. The new child will automatically become the last child in the list. When adding a
     * DocStruct, configuration is checked, wether a DocStruct of this type can be added. If not, it is not added and false is returned. The parent of
     * this child (this instance) is set automatically.
     * </p>
     * 
     * @param inchild DocStruct to be added
     * @return true, if child was added; otherwise false
     * @throws TypeNotAllowedAsChildException if a child should be added, but it's DocStruct type isn't member of this instance's DocStruct type
     **************************************************************************/
    public boolean addChild(DocStruct inchild) throws TypeNotAllowedAsChildException {

        if (inchild == null || inchild.getType() == null) {
            log.warn("DocStruct or DocStructType is null");
            return false;
        }

        DocStructType childtype;
        boolean allowed = false;

        // Check, if type of child is allowed.
        childtype = inchild.getType();
        // Get all allowed types.
        for (String tempType : this.type.getAllAllowedDocStructTypes()) {
            if ((childtype.getName()).equals(tempType)) {
                allowed = true;
            }
        }

        if (!allowed) {
            TypeNotAllowedAsChildException tnaace = new TypeNotAllowedAsChildException(childtype);
            log.error("DocStruct type '" + childtype + "' not allowed as child of type '" + this.getType().getName() + "'");
            throw tnaace;
        }

        // Create List for children, if not already available.
        if (this.children == null) {
            this.children = new LinkedList<>();
        }

        // Set status to logical or physical.
        if (this.isLogical()) {
            inchild.setLogical(true);
        }
        if (this.isPhysical()) {
            inchild.setPhysical(true);
        }

        // Add child to end of List.
        inchild.setParent(this);
        return this.children.add(inchild);
    }

    /***************************************************************************
     * <p>
     * Removes a child from this DocStruct object.
     * </p>
     * 
     * @return true, if child was returned; false, if it was (e.g. didn't belong to the children of this DocStruct instance).
     **************************************************************************/
    public boolean removeChild(DocStruct inchild) {

        if (this.children.remove(inchild)) {
            // Delete reference to parent.
            inchild.setParent(null);
            // It's not in the logical tree anymore.
            if (this.isLogical()) {
                inchild.setLogical(false);
            }
            // It's not in the physical tree anymore.
            if (this.isPhysical()) {
                inchild.setPhysical(false);
            }

            // Delete the parent reference.
            inchild.setParent(null);
            return true;
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Moves a child to another position in the list of all children. The DocStruct to be moved must already be child of this DocStruct.
     * </p>
     * 
     * @param inchild DocStruct to be moved
     * @param position first child has position 1
     * @return true, if successful; otherwise false
     **************************************************************************/
    public boolean moveChild(DocStruct inchild, int position) {

        if (position < 0) {
            return false;
        }
        if (position > this.children.size()) {
            position = this.children.size();
        }
        // Remove child first.
        if (!this.children.remove(inchild)) {
            return false;
        }

        // Add to the new position.
        try {
            this.children.add(position, inchild);
        } catch (UnsupportedOperationException | ClassCastException | IllegalArgumentException uoe) {
            return false;
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Moves a child to another position in the list of all children. Moves the DocStruct after another child. Both DocStruct objects must already be
     * child of this docstruct object.
     * </p>
     * 
     * @param inchild DocStruct to be moved
     * @param afterchild child after which the DocStruct should be moved to.
     * @return true, if it worked; otherwise false
     **************************************************************************/
    public boolean moveChildafter(DocStruct inchild, DocStruct afterchild) {

        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // Child found.
            if (test.equals(afterchild)) {
                return (moveChild(inchild, i + 1));
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Moves a child to another position in the list of all children. Moves the DocStruct before another child. Both DocStruct objects must already be
     * child of this docstruct object.
     * </p>
     * 
     * @param inchild DocStruct to be moved
     * @param beforechild child before the DocStruct should be moved to.
     * @return true, if it worked; otherwise false
     **************************************************************************/
    public boolean moveChildbefore(DocStruct inchild, DocStruct beforechild) {

        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // Child found.
            if (test.equals(beforechild)) {
                return (moveChild(inchild, i));
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Retrieves the position of a child in the list of all children.
     * </p>
     * 
     * @param inchild DocStruct object, whose position should be retrieved
     * @return position as an integer or -1 if child is not in the list
     **************************************************************************/
    public int getPositionofChild(DocStruct inchild) {

        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // Child found.
            if (test.equals(inchild)) {
                return i;
            }
        }

        return -1;
    }

    /***************************************************************************
     * <p>
     * Get the next Child in the list of all children. If the given DocStruct object is NOT a child of the current DocStruct instance, null is
     * returned.
     * </p>
     * 
     * @param inChild DocStruct object
     * @return the next DocStruct object after inChild; if none is available null is returned
     **************************************************************************/
    public DocStruct getNextChild(DocStruct inChild) {

        DocStruct nextchild;
        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // Child found.
            if (test.equals(inChild)) {
                if (i != this.children.size() - 1) {
                    nextchild = this.children.get(i + 1);
                    return nextchild;
                }

                // This is already the last child.
                return null;
            }
        }

        // inChild is not member of children.
        return null;
    }

    /***************************************************************************
     * <p>
     * getPreviousChild returns the previous child.
     * </p>
     * 
     * If there is no previous child or given DocStruct object isn't a child at all null is returned
     * 
     * @param inChild
     * @return
     **************************************************************************/
    public DocStruct getPreviousChild(DocStruct inChild) {

        DocStruct prevchild;
        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // CHild found.
            if (test.equals(inChild)) {
                if (i != 0) {
                    prevchild = this.children.get(i - 1);
                    return prevchild;
                }

                // This is already the last child.
                return null;
            }
        }

        // inChild is not member of children.
        return null;
    }

    /***************************************************************************
     * <p>
     * Checks if this structure entity can have another entity of a special kind as a child. The child is NOT added by this method.
     * </p>
     * 
     * @param inType the <code>DocStructType</code> of the child
     * @return true, if it can be added; otherwise false
     **************************************************************************/
    public boolean isDocStructTypeAllowedAsChild(DocStructType inType) {

        List<String> allTypes = this.type.getAllAllowedDocStructTypes();
        String typename = inType.getName();
        String testname;

        for (String type2 : allTypes) {
            testname = type2;
            // Jep, it's in here.
            if (testname.equals(typename)) {
                return true;
            }
        }

        return false;
    }

    public boolean isMetadataGroupBeRemoved(MetadataGroupType inMDType) {

        // How many metadata of this type do we have already.
        int typesavailable = countMDofthisType(inMDType.getName());
        // How many types must be at least available.
        String maxnumbersallowed = this.type.getNumberOfMetadataGroups(inMDType);

        if (typesavailable == 1 && "+".equals(maxnumbersallowed)) {
            // There must be at least one.
            return false;
        }

        if (typesavailable == 1 && "1m".equals(maxnumbersallowed)) {
            // There must be at least one.
            return false;
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Checks, if Metadata of a special kind can be removed. There is no special function, to check wether persons or corporations can be removed. As
     * the <code>Person</code> and <code>Corporate</code> object are just inherited from the <code>Metadata</code> it has a <code>MetadataType</code>.
     * Therefore this method can be used, to check if a person is removable or not.
     * </p>
     * 
     * @see #removeMetadata
     * @see #removePerson
     * @param inMDType MetadataType object
     * @return true, if it can be removed; otherwise false
     **************************************************************************/
    @Override
    public boolean isMetadataTypeBeRemoved(PrefsType inMDType) {

        // How many metadata of this type do we have already.
        int typesavailable = countMDofthisType(inMDType.getName());
        // How many types must be at least available.
        String maxnumbersallowed = this.type.getNumberOfMetadataType(inMDType);

        if (typesavailable == 1 && "+".equals(maxnumbersallowed)) {
            // There must be at least one.
            return false;
        }

        if (typesavailable == 1 && "1m".equals(maxnumbersallowed)) {
            // There must be at least one.
            return false;
        }

        return true;
    }

    @Override
    public void addPerson(Person in) throws MetadataTypeNotAllowedException, IncompletePersonObjectException {

        // Max number of persons (from configuration).
        String maxnumberallowed = null;
        // Number of persons currently available.
        int number = 0;
        // Store, wether we can or cannot add information.
        boolean insert = false;

        // Check, if person is complete.
        if (in.getType() == null) {
            IncompletePersonObjectException ipoe = new IncompletePersonObjectException();
            log.error("Incomplete data for person metadata");
            throw ipoe;
        }

        // Get MetadataType of this person get MetadataType from docstructType
        // object with the same name.
        PrefsType mdtype = this.type.getMetadataTypeByType(in.getType());
        if (mdtype == null) {
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException();
            log.error("MetadataType " + in.getType().getName() + " is not available for DocStruct '" + this.getType().getName() + "'");
            throw mtnae;
        }

        // Check, if docstruct may have this person ??? depends on the role
        // value of person.
        maxnumberallowed = this.type.getNumberOfMetadataType(mdtype);

        // Check, if another Person of this type is allowed. How many persons
        // are already available.
        number = countMDofthisType(mdtype.getName());

        // As many as we want (zero or more).
        if ("*".equals(maxnumberallowed)) {
            insert = true;
        }
        // One or more.
        if ("+".equals(maxnumberallowed)) {
            insert = true;
        }
        // Only one, if we have already one, we cannot add it.
        if ("1m".equals(maxnumberallowed) || "1o".equals(maxnumberallowed)) {
            if (number < 1) {
                insert = true;
            } else {
                insert = false;
            }
        }

        // We can add this person.
        if (insert) {
            in.setParent(this);
            if (this.persons == null) {
                this.persons = new LinkedList<>();
            }
            this.persons.add(in);

            return;
        }

        MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException();
        log.error("Person MetadataType '" + in.getType().getName() + "' not allowed for DocStruct '" + this.getType().getName() + "'");
        throw mtnae;
    }

    @Override
    public void addCorporate(Corporate corp) throws MetadataTypeNotAllowedException {

        // Max number of persons (from configuration).
        String maxnumberallowed = null;
        // Number of persons currently available.
        int number = 0;
        // Store, wether we can or cannot add information.
        boolean insert = false;

        // Check, if person is complete.
        if (corp.getType() == null) {
            IncompletePersonObjectException ipoe = new IncompletePersonObjectException();
            log.error("Incomplete data for person metadata");
            throw ipoe;
        }

        // Get MetadataType of this person get MetadataType from docstructType
        // object with the same name.
        PrefsType mdtype = this.type.getMetadataTypeByType(corp.getType());
        if (mdtype == null) {
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException();
            log.error("MetadataType " + corp.getType().getName() + " is not available for DocStruct '" + this.getType().getName() + "'");
            throw mtnae;
        }

        // Check, if docstruct may have this person ??? depends on the role
        // value of person.
        maxnumberallowed = this.type.getNumberOfMetadataType(mdtype);

        // Check, if another Person of this type is allowed. How many persons
        // are already available.
        number = countMDofthisType(mdtype.getName());

        // As many as we want (zero or more).
        if ("*".equals(maxnumberallowed)) {
            insert = true;
        }
        // One or more.
        if ("+".equals(maxnumberallowed)) {
            insert = true;
        }
        // Only one, if we have already one, we cannot add it.
        if ("1m".equals(maxnumberallowed) || "1o".equals(maxnumberallowed)) {
            if (number < 1) {
                insert = true;
            } else {
                insert = false;
            }
        }

        // We can add this person.
        if (insert) {
            corp.setParent(this);
            if (this.corporates == null) {
                this.corporates = new LinkedList<>();
            }
            this.corporates.add(corp);

        } else {

            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException();
            log.error("Corporate MetadataType '" + corp.getType().getName() + "' not allowed for DocStruct '" + this.getType().getName() + "'");
            throw mtnae;
        }
    }

    /***************************************************************************
     * <p>
     * Removes a Person object.
     * </p>
     * 
     * @param in Person object to be removed
     * @param force if set to true, person is removed, even if invalid document is the result
     * @return true, if removed; otherwise false
     * @throws IncompletePersonObjectException if the first parameter is not a complete person object
     **************************************************************************/
    @Override
    public void removePerson(Person in, boolean force) throws IncompletePersonObjectException {

        if (this.persons == null) {
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
        String maxnumbersallowed = this.type.getNumberOfMetadataType(inMDType);

        if (force && typesavailable == 1 && "+".equals(maxnumbersallowed)) {
            // There must be at least one.
            return;
        }
        if (force && typesavailable == 1 && "1m".equals(maxnumbersallowed)) {
            // There must be at least one.
            return;
        }

        this.persons.remove(in);
    }

    @Override
    public void removeCorporate(Corporate in, boolean force) throws IncompletePersonObjectException {

        if (this.corporates == null) {
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
        String maxnumbersallowed = this.type.getNumberOfMetadataType(inMDType);

        if (force && typesavailable == 1 && "+".equals(maxnumbersallowed)) {
            // There must be at least one.
            return;
        }
        if (force && typesavailable == 1 && "1m".equals(maxnumbersallowed)) {
            // There must be at least one.
            return;
        }

        this.corporates.remove(in);
    }

    /***************************************************************************
     * @param in
     * @return true, if removed; otherwise false
     * @throws IncompletePersonObjectException
     **************************************************************************/
    public void removePerson(Person in) throws IncompletePersonObjectException {
        removePerson(in, false);
    }

    public void removeCorporate(Corporate in) throws IncompletePersonObjectException {
        removeCorporate(in, false);
    }

    /***************************************************************************
     * <p>
     * Get a list of all Person objects.
     * </p>
     * 
     * @return List containing Person objects; if no such objects are available null is returned.
     **************************************************************************/
    public List<Person> getAllPersons() {

        if (this.persons == null || this.persons.isEmpty()) {
            return null; //NOSONAR
        }

        return this.persons;
    }

    public List<Corporate> getAllCorporates() {
        if (this.corporates == null || this.corporates.isEmpty()) {
            return null; //NOSONAR
        }
        return this.corporates;
    }

    /***************************************************************************
     * @return the logical
     **************************************************************************/
    public boolean isLogical() {
        return this.logical;
    }

    /***************************************************************************
     * @param logical the logical to set
     **************************************************************************/
    public void setLogical(boolean logical) {

        this.logical = logical;

        List<DocStruct> childList = this.getAllChildren();
        if (childList != null) {
            for (DocStruct ds : childList) {
                ds.setLogical(logical);
            }
        }
    }

    /***************************************************************************
     * @return theOrigObject
     **************************************************************************/
    @JsonIgnore
    public Object getOrigObject() {
        return this.origObject;
    }

    /***************************************************************************
     * @param theOrigObject theOrigObject to set
     **************************************************************************/
    public void setOrigObject(Object theOrigObject) {
        this.origObject = theOrigObject;
    }

    /***************************************************************************
     * @return the physical
     **************************************************************************/
    public boolean isPhysical() {
        return this.physical;
    }

    /***************************************************************************
     * @param physical the physical to set
     **************************************************************************/
    public void setPhysical(boolean physical) {

        this.physical = physical;

        List<DocStruct> childList = this.getAllChildren();
        if (childList != null) {
            for (DocStruct ds : childList) {
                ds.setPhysical(physical);
            }
        }
    }

    /***************************************************************************
     * <p>
     * Creates a list of metadata and persons to be displayed in a MetadataForm. The list is based on the <code>DefaultDisplay</code> attribute in the
     * preference file (in each metadata element). This list includes metadata and person objects which already exist (and have content) and empty
     * objects (objects without any content), which are created by this method. These emtpy objects are not only added to the list, but also to the
     * internal Metadata list and person list of the this DocStruct instance. After the form has been displayed an processed, you may want to call the
     * method <code>deleteUnusedPersonsAndMetadata()</code> to delete unused objects created by this method.
     * </p>
     * 
     * @param lang language name to be used for sorting the list
     * @param personsTop if true, person objects are at the beginning of the list, otherwise at the end
     * @return a List containing Metadata and Person objects
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
    public List<Metadata> showMetadataForm(String lang, boolean personsTop) throws MetadataTypeNotAllowedException {

        // Get all MetadataType elements which have the DefaultDisplay attribute
        // set.
        List<MetadataType> dmt = this.getDefaultDisplayMetadataTypes();

        List<Metadata> allMDs = this.getAllMetadata();
        // No default metadata.
        if (dmt == null) {
            return null; //NOSONAR
        }

        for (MetadataType mdt : dmt) {
            // Check, if mdt is already in the allMDs Metadata list.
            boolean notIncluded = true;
            for (Metadata md : allMDs) {
                PrefsType mdt2 = md.getType();

                // Compare the display MetadataType and the type of current
                // Metadata.
                if (mdt.getName().equals(mdt2.getName())) {
                    // Is included; need not to be displayed seperatly.
                    notIncluded = false;
                    break;
                }
            }

            // Create new Metadata or Person element.
            if (notIncluded) {
                if (mdt.isPerson) {
                    // It's a person, create person element.
                    Person psFoo = new Person(mdt);
                    // The role is the name of the metadata type.
                    psFoo.setRole(mdt.getName());
                    try {
                        // Add this new metadata element.
                        this.addPerson(psFoo);
                    } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                        // person type is not allowed
                    }
                } else if (mdt.isCorporate) {
                    Corporate corp = new Corporate(mdt);
                    corp.setRole(mdt.getName());
                    try {
                        addCorporate(corp);
                    } catch (UGHException e) {
                        // corporate type is not allowed
                    }
                }

                else {
                    // It's metadata, so create a new Metadata element.
                    Metadata metaFoo = new Metadata(mdt);
                    try {
                        // Add this new metadata element.
                        this.addMetadata(metaFoo);
                    } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                        // metadata type is not allowed
                    }
                }
            }
        }

        // Sort all Metadata by typename.
        LinkedList<Metadata> resultList = new LinkedList<>();

        for (Metadata md : this.getAllMetadata()) {
            // If nothing is in the result list, just add it.
            if (resultList.isEmpty()) {
                resultList.add(md);
                // Continue with next iteration.
                continue;
            }

            String compare = md.getType().getNameByLanguage(lang);

            // Iterate over result list and find position for the metadata.
            boolean elementinserted = false;
            for (int i = 0; i < resultList.size(); i++) {
                Metadata mdcomp = resultList.get(i);
                String mdcompLang = mdcomp.getType().getNameByLanguage(lang);

                // Compare both strings.
                if (compare.compareTo(mdcompLang) < 0 || compare.compareTo(mdcompLang) == 0) {
                    // Add md before mdcomp.
                    resultList.add(i, md);
                    elementinserted = true;
                    // Get out of loop.
                    break;
                }
            }

            // If metadata element has not been inserted, we insert it to the
            // end.
            if (!elementinserted) {
                resultList.addLast(md);
            }

        }

        // Currently we don't sort Persons; we simple add Persons on the top or
        // the end of the resultList.
        if (this.getAllPersons() != null && !this.getAllPersons().isEmpty()) {
            // Just add persons, if any person is available.
            if (personsTop) {
                // On top of list.
                resultList.addAll(0, this.getAllPersons());
            } else {
                // At end of list..
                resultList.addAll(this.getAllPersons());
            }
        }

        // The Result list contains Persons and Metadata in one list.
        return resultList;
    }

    /***************************************************************************
     * <p>
     * This method cleans the metadata list and person list of instances which do not have a value (empty objects). This method is usually used in
     * connection with the <code>showMetadataForm</code> method. After the <code>showMetadataForm</code> has been called and the form has been
     * displayed, this method should be called to delete the created empty metadata instances by the <code>showMetadataForm</code> method.
     * </p>
     * 
     * <p>
     * An empty metadata instance is:
     * 
     * <ul>
     * <li>A metadata object with a value of null.</li>
     * <li>A person object with neither a lastname nor a firstname nor an identifier nor an institution.</li>
     * </ul>
     * 
     * </p>
     **************************************************************************/
    public void deleteUnusedPersonsAndMetadata() {

        // Handle Persons first: Person objects are available.
        if (this.getAllPersons() != null) {
            List<Person> personlist = this.getAllPersons();
            // Copy person list, so we can iterate over this list and delete
            // from the persons list.
            List<Person> iteratorList = new LinkedList<>(personlist);
            for (Person per : iteratorList) {
                if (StringUtils.isBlank(per.getLastname()) && StringUtils.isBlank(per.getFirstname()) && getAllPersons() != null) {
                    // Delete this person from list of all Persons.
                    this.getAllPersons().remove(per);
                }
            }
        }
        if (getAllCorporates() != null) {
            List<Corporate> corporateList = new LinkedList<>(getAllCorporates());
            for (Corporate corp : corporateList) {
                if (StringUtils.isBlank(corp.getMainName()) && StringUtils.isBlank(corp.getPartName()) && corp.getSubNames().isEmpty()) {
                    getAllCorporates().remove(corp);
                }
            }
        }

        // Handle Metadata: Metadata objects are available.
        if (this.getAllMetadata() != null) {
            List<Metadata> metadatalist = this.getAllMetadata();
            // Copy Metadata list, so we can iterate over this list and delete
            // from the metadata list.
            List<Metadata> iteratorList = new LinkedList<>(metadatalist);
            for (Metadata md : iteratorList) {
                if (StringUtils.isBlank(md.getValue()) && this.getAllMetadata() != null) {
                    // Delete the metadata element.
                    this.getAllMetadata().remove(md);
                }
            }
        }

        if (this.getAllMetadataGroups() != null) {
            List<MetadataGroup> metadatalist = this.getAllMetadataGroups();

            List<MetadataGroup> iteratorList = new LinkedList<>(metadatalist);
            for (MetadataGroup md : iteratorList) {
                boolean isEmpty = true;
                if (md.getMetadataList() != null) {
                    for (Metadata meta : md.getMetadataList()) {
                        if (StringUtils.isNotBlank(meta.getValue())) {
                            isEmpty = false;
                            break;
                        }
                    }
                }

                if (md.getCorporateList() != null) {
                    for (Corporate corp : md.getCorporateList()) {
                        if (StringUtils.isNotBlank(corp.getMainName()) || StringUtils.isNotBlank(corp.getPartName())
                                || !corp.getSubNames().isEmpty()) {
                            isEmpty = false;
                            break;
                        }
                    }
                    if (md.getPersonList() != null) {
                        for (Person per : md.getPersonList()) {
                            if (StringUtils.isNotBlank(per.getLastname()) || StringUtils.isNotBlank(per.getFirstname())) {
                                isEmpty = false;
                                break;
                            }
                        }
                    }
                }
                if (isEmpty) {
                    this.getAllMetadataGroups().remove(md);
                }
            }

        }
    }

    /***************************************************************************
     * <p>
     * Sorts the metadata and persons in the current DocStruct according to their occurance in the preferences file.
     * </p>
     * 
     * @param thePrefs
     **************************************************************************/
    public synchronized void sortMetadata(Prefs thePrefs) {

        List<Metadata> newMetadata = new LinkedList<>();
        List<Person> newPersons = new LinkedList<>();
        List<Metadata> oldMetadata = new LinkedList<>();
        List<Person> oldPersons = new LinkedList<>();

        List<Corporate> oldCorporates = new LinkedList<>();
        List<Corporate> newCorporates = new LinkedList<>();

        if (this.allMetadata != null) {
            oldMetadata = new LinkedList<>(this.allMetadata);
        }
        if (this.persons != null) {
            oldPersons = new LinkedList<>(this.persons);
        }

        if (corporates != null) {
            oldCorporates = new LinkedList<>(corporates);
        }
        // Get all MetadataTypes defined in the prefs for this DocStruct.
        DocStructType docStructType = thePrefs.getDocStrctTypeByName(this.getType().getName());

        // If the DocStructType is NOT existing, we have no metadata to sort,
        // just do return.
        if (docStructType == null) {
            return;
        }

        List<MetadataType> prefsMetadataTypeList = docStructType.getAllMetadataTypes();

        // Iterate over all that metadata types.
        for (PrefsType mType : prefsMetadataTypeList) {

            // Go through all persons of the current DocStruct.
            List<Person> op = this.getAllPersons();
            if (op != null && !op.isEmpty()) {
                for (Person p : op) {
                    if (p.getType() != null && mType.getName().equals(p.getType().getName())) {
                        // Add to the new list and remove from the old, if names
                        // do match.
                        newPersons.add(p);
                        oldPersons.remove(p);
                    }
                }
            }
            List<Corporate> oc = getAllCorporates();
            if (oc != null) {
                for (Corporate p : oc) {
                    if (p.getType() != null && mType.getName().equals(p.getType().getName())) {
                        newCorporates.add(p);
                        oldCorporates.remove(p);
                    }
                }
            }

            // Go throught all metadata of the curretn DocStruct.
            List<Metadata> om = this.getAllMetadata();
            if (om != null && !om.isEmpty()) {
                for (Metadata m : om) {
                    if (mType.getName().equals(m.getType().getName())) {
                        // Add to the new list and remove from the old, if names
                        // do match.
                        newMetadata.add(m);
                        oldMetadata.remove(m);
                    }
                }
            }
        }

        // Add left-over types.
        if (oldPersons != null && !oldPersons.isEmpty()) {
            newPersons.addAll(oldPersons);
        }
        if (oldMetadata != null && !oldMetadata.isEmpty()) {
            newMetadata.addAll(oldMetadata);
        }
        if (oldCorporates != null && !oldCorporates.isEmpty()) {
            newCorporates.addAll(oldCorporates);
        }

        // Re-set the lists.
        this.allMetadata = newMetadata;
        this.persons = newPersons;
        corporates = newCorporates;
    }

    /***************************************************************************
     * <p>
     * Sorts the metadata and persons in the current DocStruct alphabetically.
     * </p>
     **************************************************************************/
    public synchronized void sortMetadataAbcdefg() {

        // Create empty (sorted) TreeSets and lists.
        TreeSet<Metadata> newMetadata = new TreeSet<>(new MetadataComparator());
        TreeSet<Person> newPersons = new TreeSet<>(new MetadataComparator());
        List<Metadata> metadataList = new LinkedList<>();
        List<Person> personList = new LinkedList<>();

        // Add all metadata to the new TreeSets (sorted).
        if (this.allMetadata != null) {
            newMetadata.addAll(this.allMetadata);
        }
        if (this.persons != null) {
            newPersons.addAll(this.persons);
        }

        // Re-transfer the sorted sets to the linked lists.
        metadataList.addAll(newMetadata);
        personList.addAll(newPersons);

        // Re-set the lists.
        this.allMetadata = metadataList;
        this.persons = personList;

    }

    /***************************************************************************
     * <p>
     * The metadata comparator. Simply compares metadata (and persons) according to their type names alphabetically.
     * </p>
     * 
     * @author funk
     **************************************************************************/
    class MetadataComparator implements Comparator<Object> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Object o1, Object o2) {

            Metadata m1 = (Metadata) o1;
            Metadata m2 = (Metadata) o2;

            if (m1.getType().getName().equals(m2.getType().getName())) {
                return 0;
            }

            return m1.getType().getName().compareTo(m2.getType().getName());
        }

    }

    /***************************************************************************
     * <p>
     * The metadata comparator. Simply compares metadata (and persons) according to their type names alphabetically.
     * </p>
     * 
     * @author funk
     **************************************************************************/
    class MetadataGroupComparator implements Comparator<Object> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Object o1, Object o2) {

            MetadataGroup m1 = (MetadataGroup) o1;
            MetadataGroup m2 = (MetadataGroup) o2;

            return m1.getType().getName().compareTo(m2.getType().getName());
        }

    }

    public AmdSec getAmdSec() {
        return amdSec;
    }

    public void setAmdSec(AmdSec amdSec) {
        this.amdSec = amdSec;
    }

    public List<Md> getTechMds() {
        return techMdList;
    }

    public void addTechMd(Md techMd) {
        if (techMdList == null) {
            techMdList = new ArrayList<>();
        }
        if (techMd != null) {
            techMdList.add(techMd);
        }
    }

    public void setTechMds(List<Md> mds) {
        if (mds != null) {
            this.techMdList = mds;
        }
    }

    public String getImageName() {
        if (contentFileReferences != null && !contentFileReferences.isEmpty()) {
            for (ContentFileReference cfr : contentFileReferences) {
                if (cfr.getCf() != null) {
                    String location = cfr.getCf().getLocation();
                    if (StringUtils.isNotBlank(location)) {
                        File imagefile = new File(location);
                        return imagefile.getName();
                    }
                }
            }
        }
        return null;
    }

    public void setImageName(String newfilename) {
        if (contentFileReferences != null && !contentFileReferences.isEmpty()) {
            for (ContentFileReference cfr : contentFileReferences) {
                if (cfr.getCf() != null) {
                    cfr.getCf().setLocation(newfilename);
                    return;
                } else {
                    ContentFile cf = new ContentFile();
                    cf.setLocation(newfilename);
                    cfr.setCf(cf);
                    return;
                }
            }
        } else {
            ContentFile cf = new ContentFile();
            cf.setLocation(newfilename);
            this.addContentFile(cf);
        }
    }

    /**
     * Get the element type, can be div or area
     * 
     * @return
     */

    public String getDocstructType() {
        return docstructType;
    }

    /**
     * Set the element type, can be div or area
     * 
     */

    public void setDocstructType(String docstructType) {
        this.docstructType = docstructType;
    }

    /**
     * Get all children of the current docstruct as a flat list
     * 
     * @return
     */

    public List<DocStruct> getAllChildrenAsFlatList() {
        List<DocStruct> list = new LinkedList<>();
        if (children != null) {
            for (DocStruct ds : children) {
                list.add(ds);
                if (ds.getAllChildren() != null) {
                    list.addAll(ds.getAllChildrenAsFlatList());
                }
            }
        }

        return list;
    }

    public String getAdmId() {
        return admId;
    }

    public void setAdmId(String admId) {
        this.admId = admId;
    }
}
