package ugh.dl;

/*******************************************************************************
 * ugh.dl / DigitalDocument.java
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import lombok.extern.log4j.Log4j2;
import ugh.dl.Md.MdType;
import ugh.exceptions.ContentFileNotLinkedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

/*******************************************************************************
 * <p>
 * A DigitalDocument represents a digital version of a work. This representation contains the following information:
 * </p>
 * 
 * <ul>
 * <li>metadata</li>
 * <li>structure of a work</li>
 * <li>content</li>
 * </ul>
 * 
 * <p>
 * Those three different objects can be linked to each other in ways forming a very complex object. The underlying document model tries to reduce the
 * complexity by defining some rules:
 * </p>
 * 
 * <ul>
 * <li>every <code>DigitalDocument</code> has two kind of structures:
 * 
 * <ul>
 * <li>logical structure: this structure represents the logical view. The logical view is normally represented by chapters, paragraphs etc.</li>
 * 
 * <li>physical structure: The physical structure represents the physical representation of a work. For a book the physical binding and the pages can
 * be regarded a part of the physical structure.</li>
 * 
 * Each structure has a single top structure element. These structure elements are represented by <code>DocStruct</code> objects and may have
 * children.
 * 
 * </ul>
 * <li>metadata to this digital document is stored in structure entities</li>
 * <li>the content is represented by content files</li>
 * <li>ContentFiles can be linked to structure entities</li>
 * </ul>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2010-02-23
 * @see DocStruct, Metadata, Prefs
 * 
 * 
 *      CHANGELOG
 * 
 *      29.03.2010 --- Funk --- Added this.setFileSet(null); to avoid adding multiple files with an XStream read (DPD-406).
 * 
 *      23.02.2010 --- Sehr --- Changed XStream DOM driver against XStream Stax driver.
 * 
 *      14.02.2010 --- Funk --- Remove all the Metadata, person, and ContentFile output lines and implement the toString() methods in the classes
 *      itself!!
 * 
 *      28.01.2010 -- Funk --- Added some NPE checks to addAllContentFiles().
 * 
 *      27.01.2010 --- Funk --- Re-added addAllCOntentFiles() for compatibility reasons.
 * 
 *      22.01.2010 --- Funk --- Added method toString(). --- Improved things due to findbugs.
 * 
 *      19.01.2010 --- Funk --- Fixed bug updating physical DocStruct with logical DocStruct.
 * 
 *      21.12.2009 --- Funk --- Minor changes. --- Added VERSION to this class.
 * 
 *      16.12.2009 --- Mahnke --- Marked print methods as deprecated.
 * 
 *      14.12.2009 --- Funk --- Added to the equals() method, must be fixed!
 * 
 *      09.12.2009 --- Funk --- Maekrd addAllContentFiles() deprecated. --- Added addContentFileFromPhysicalPage() to add content files to a DocStruct
 *      "page".
 * 
 *      08.12.2009 --- Funk --- Added FileSet printout method. --- VirtualFileGroup are preserved now before adding all content files. --- Slightly
 *      refactored some loops and conditionals.
 * 
 *      03.12.2009 --- Funk --- Slightly improved printChildDocStruct().
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      30.10.2009 --- Funk --- Added generated serialVersionUID.
 * 
 *      28.10.2009 --- Funk --- Slightly refactored PreferencesException throwing in readXStreamXml().
 * 
 *      06.10.2009 --- Funk --- Changed printDocStruct underline character.
 * 
 *      30.09.2009 --- Funk --- New public method sortMetadataRecursively, and alphabetically recursively. -- printAllLogDocStruct() and
 *      printAllPhysDocStruct() now really prints, and do not log again!
 * 
 *      10.09.2009 --- Funk --- Updating DocStructTYPES and MetadataTYPES now! Correctly! Not only NAMES in a LIST! HMPF! --- Corrected the
 *      MetadataType sort algorithm, now the MetadataTypes are sorted as stated in the Prefs' DocStructTypes.
 * 
 *      07.09.2009 --- Funk --- Added physical DocStruct to update mechanism.
 * 
 *      08.06.2009 --- Funk --- Declared dome deprecated methods, deleted debug output.
 * 
 *      11.12.2008 --- Funk --- Writing the XStream the content files are written to the Digital Document first.
 * 
 *      03.12.2008 --- Funk --- Added updating the DigitalDocument from the Prefs after reading XStream.
 * 
 *      24.10.2008 --- Funk --- Added XStream read and write methods.
 * 
 *      14.10.2008 --- Funk --- read() and write() implemented to serialize and de-serialize.
 * 
 *      29.09.2008 --- Funk --- Logging added.
 * 
 ******************************************************************************/

@Log4j2
public class DigitalDocument implements Serializable {

    private static final long serialVersionUID = 3806816628185949759L;

    private static final String VERSION = "2.0-20100223";

    private static final String LINE = "--------------------" + "--------------------" + "--------------------" + "--------------------";

    private DocStruct topPhysicalStruct;
    private DocStruct topLogicalStruct;
    // Contains all files, which are referenced from this digital document (e.g.
    // imagefiles, textfiles etc...).
    private FileSet allImages;

    // This contains the list of techMds. Currently only one amdSec is allowed, to comply with DFG-Viewer
    private AmdSec amdSec;

    public enum PhysicalElement {
        PAGE("page"),
        AUDIO("audio"),
        VIDEO("video"),
        OBJECT("object");

        private String name;

        private PhysicalElement(String elementName) {
            name = elementName;
        }

        public String getName() {
            return name;
        }

        public static PhysicalElement getTypeFromValue(String type) {
            for (PhysicalElement ss : values()) {
                if (ss.getName().equals(type)) {
                    return ss;
                }
            }
            return PAGE;
        }

        public static boolean checkPhysicalType(String type) {
            for (PhysicalElement ss : values()) {
                if (ss.getName().equals(type)) {
                    return true;
                }
            }
            return false;
        }
    }

    /***************************************************************************
     * <p>
     * Constructor.
     * </p>
     **************************************************************************/
    public DigitalDocument() {
        super();
    }

    //
    // Factory classes.
    //

    /***************************************************************************
     * <p>
     * Create a DocStruct instance for the Digital Document.
     * </p>
     * 
     * @param dsType Is a DocStructType object.
     * @throws TypeNotAllowedForParentException Is thrown, if this docstruct is not allowed for a parent.
     **************************************************************************/
    public DocStruct createDocStruct(DocStructType dsType) throws TypeNotAllowedForParentException {

        DocStruct ds = new DocStruct(dsType);
        ds.setDigitalDocument(this);

        return ds;
    }

    //
    // Setter and Getter.
    //

    /***************************************************************************
     * @param inStruct
     **************************************************************************/
    public void setLogicalDocStruct(DocStruct inStruct) {

        if (this.topLogicalStruct != null) {
            this.topLogicalStruct.setLogical(false);
        }

        this.topLogicalStruct = inStruct;
        // Set DocStruct and all children to logical.
        inStruct.setLogical(true);
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public DocStruct getLogicalDocStruct() {
        return this.topLogicalStruct;
    }

    /***************************************************************************
     * @param inStruct
     **************************************************************************/
    public void setPhysicalDocStruct(DocStruct inStruct) {

        if (this.topPhysicalStruct != null) {
            this.topPhysicalStruct.setPhysical(false);
        }

        this.topPhysicalStruct = inStruct;
        // Set DocStruct and all children to physical.
        inStruct.setPhysical(true);
    }

    public DocStruct getPhysicalDocStruct() {
        return this.topPhysicalStruct;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String result = "";

        // First get the fileset's information.
        if (this.getFileSet() != null) {
            result = this.getFileSet().toString();
        } else {
            result += LINE + "\n" + "FileSet" + "\n";
            result += LINE + "\n" + "NONE" + "\n";
        }

        // Then assemble the logical DocStruct.
        result += printCompleteDocStruct(this.topLogicalStruct);

        // Finally assemble the physical DocStruct.
        result += printCompleteDocStruct(this.topPhysicalStruct);

        return result.trim();
    }

    /***************************************************************************
     * <p>
     * Prints all the given DocStruct's data.
     * </p>
     * 
     * @return
     **************************************************************************/
    private String printCompleteDocStruct(DocStruct theDocStruct) {

        String result = "";

        if (theDocStruct != null) {
            result += printChildDocStruct(theDocStruct, 0);
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Prints a DocStruct including persons and metadata.
     * </p>
     * 
     * @param inDocStruct
     * @param hierarchy
     * @return
     **************************************************************************/
    private String printChildDocStruct(DocStruct inDocStruct, int hierarchy) {

        StringBuilder result = new StringBuilder();

        StringBuffer hierarchyBuffer = new StringBuffer();
        for (int i = 0; i < hierarchy; i++) {
            hierarchyBuffer.append("\t");
        }

        // Type of this document structure (inDocStruct).
        DocStructType myType;

        if (inDocStruct == null) {
            return "";
        }

        // Get and print DocStruct type.
        myType = inDocStruct.getType();
        if (myType != null) {
            result.append(hierarchyBuffer).append(LINE).append("\n");
            result.append(hierarchyBuffer).append("DocStruct '").append(myType.getName()).append("'").append("\n");
            result.append(hierarchyBuffer).append(LINE).append("\n");
        }

        // Get and print metadata.
        List<Metadata> allMD = inDocStruct.getAllMetadata();
        if (allMD != null) {
            for (Metadata currentMD : allMD) {
                result.append(hierarchyBuffer).append(currentMD.toString());
            }
        }

        // Get and print Groups.
        List<MetadataGroup> allGroups = inDocStruct.getAllMetadataGroups();
        if (allGroups != null) {
            for (MetadataGroup currentGroup : allGroups) {
                result.append(hierarchyBuffer).append(currentGroup.toString());
            }
        }

        // Get and print persons.
        List<Person> allPS = inDocStruct.getAllPersons();
        if (allPS != null) {
            for (Person currentPS : allPS) {
                result.append(hierarchyBuffer).append(currentPS.toString());
            }
        }

        // Get and print contentFiles.
        List<ContentFile> allCF = inDocStruct.getAllContentFiles();
        if (allCF != null) {
            for (ContentFile currentCF : allCF) {
                result.append(hierarchyBuffer).append(currentCF.toString());
            }
        }

        // Get children.
        List<DocStruct> allChildren = inDocStruct.getAllChildren();
        if (allChildren != null) {
            for (DocStruct testChild : allChildren) {
                result.append(printChildDocStruct(testChild, hierarchy + 1));
            }
        }

        return result.toString();
    }

    /***************************************************************************
     * <p>
     * Gets all document structures of a certain type, independent of their location in the structure tree and indepedent, if they belong to the
     * logical or physical tree.
     * </p>
     * 
     * @param inTypeName
     * @return List Containing DocStruct objects or null, if none are available.
     **************************************************************************/
    public List<DocStruct> getAllDocStructsByType(String inTypeName) {

        List<DocStruct> physicallist = null;
        List<DocStruct> logicallist = null;
        List<DocStruct> commonlist = new LinkedList<>();

        if (this.topPhysicalStruct != null) {
            physicallist = getAllDocStructsByTypePrivate(this.topPhysicalStruct, inTypeName);
            if (!physicallist.isEmpty()) {
                commonlist.addAll(physicallist);
            }
        }

        if (this.topLogicalStruct != null) {
            logicallist = getAllDocStructsByTypePrivate(this.topLogicalStruct, inTypeName);
            if (!logicallist.isEmpty()) {
                commonlist.addAll(logicallist);
            }
        }

        return commonlist;
    }

    /***************************************************************************
     * @param inStruct
     * @param inTypeName
     * @return
     **************************************************************************/
    private List<DocStruct> getAllDocStructsByTypePrivate(DocStruct inStruct, String inTypeName) {

        List<DocStruct> selectedChildren = new LinkedList<>();
        List<DocStruct> children = inStruct.getAllChildren();

        if (children == null) {
            return selectedChildren;
        }

        for (DocStruct child : children) {
            if (child.getType().getName().equals(inTypeName)) {
                selectedChildren.add(child);
            }

            List<DocStruct> anotherselectedlist = getAllDocStructsByTypePrivate(child, inTypeName);

            if (anotherselectedlist != null && !anotherselectedlist.isEmpty()) {
                selectedChildren.addAll(anotherselectedlist);
            }
        }

        return selectedChildren;
    }

    /***************************************************************************
     * @param inSet
     **************************************************************************/
    public void setFileSet(FileSet inSet) {
        this.allImages = inSet;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public FileSet getFileSet() {
        return this.allImages;
    }

    /***************************************************************************
     * <p>
     * Reads an XStream XML DigitalDocument from disk.
     * </p>
     * 
     * <p>
     * Reads all given DocStructTypes and MetadataTypes from the given Preferences and gives all needed information to the DigitalDocument we just
     * read. Checks inconsistencies and updates the DigitalDocument.
     * </p>
     * 
     * @param filename
     * @return
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws PreferencesException
     **************************************************************************/
    public DigitalDocument readXStreamXml(String theFilename, Prefs thePrefs) throws FileNotFoundException, UnsupportedEncodingException {

        BufferedReader infile = new BufferedReader(new InputStreamReader(new FileInputStream(theFilename), StandardCharsets.UTF_8));

        // Read the DigitalDocument from an XStream file.
        XStream xStream = new XStream() {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(@SuppressWarnings("rawtypes") Class definedIn, String fieldName) {
                        if (definedIn == Object.class) {
                            return false;
                        }
                        return super.shouldSerializeMember(definedIn, fieldName);
                    }
                };
            }
        };
        xStream.allowTypes(new Class[] { DigitalDocument.class, Metadata.class, Person.class, MetadataGroup.class, Corporate.class, DocStruct.class,
                MetadataTypeForDocStructType.class, MetadataGroupForDocStructType.class, Reference.class, ContentFileReference.class,
                AllowedMetadataGroupType.class, ContentFile.class, NamePart.class });

        DigitalDocument digDoc = (DigitalDocument) xStream.fromXML(infile);

        // Set the loaded DigitalDocument to this.
        this.setLogicalDocStruct(digDoc.getLogicalDocStruct());
        this.setPhysicalDocStruct(digDoc.getPhysicalDocStruct());
        this.setFileSet(digDoc.getFileSet());

        // Update the logical and physical DocStruct recursively, if digdoc is
        // not null.
        log.info("Updating DigitalDocument with data from Preferences");

        try {
            if (this.getLogicalDocStruct() != null) {
                updateLogicalDocStruct(thePrefs);
            }
            if (this.getPhysicalDocStruct() != null) {
                updatePhysicalDocStruct(thePrefs);
            }
        } catch (PreferencesException e) {
            log.warn("Updating DocStruct failed due to a PreferencesException!", e);
        }

        // Process files from the physical metadata, if no fileset is existing
        // yet.
        log.info("Updating FileSet from physical metadata");

        restoreFileSetFromPhysicalMetadata();

        return this;
    }

    /***************************************************************************
     * <p>
     * Sorts all metadata and persons alphabetically (and recursively).
     * </p>
     ***************************************************************************/
    public synchronized void sortMetadataRecursivelyAbcdefg() {

        // Sort metadata of top logical struct.
        sortMetadataRecursivelyAbcdefg(this.topLogicalStruct);

        // Sort metadata of top physical struct.
        sortMetadataRecursivelyAbcdefg(this.topPhysicalStruct);
    }

    /***************************************************************************
     * <p>
     * Sorts all metadata and persons recursively in this DocStruct according to their occurance in the preferences file.
     * </p>
     **************************************************************************/
    public synchronized void sortMetadataRecursively(Prefs thePrefs) {

        // Sort metadata of top logical struct.
        sortMetadataRecursively(this.topLogicalStruct, thePrefs);

        // Sort metadata of top physical struct.
        sortMetadataRecursively(this.topPhysicalStruct, thePrefs);
    }

    /***************************************************************************
     * <p>
     * Updates the top logical DocStruct.
     * </p>
     * 
     * @param thePrefs
     * @throws PreferencesException
     **************************************************************************/
    private void updateLogicalDocStruct(Prefs thePrefs) throws PreferencesException {

        this.setLogicalDocStruct(updateDocStruct(this.getLogicalDocStruct(), thePrefs));
    }

    /***************************************************************************
     * <p>
     * Updates the top physical DocStruct.
     * </p>
     * 
     * @param thePrefs
     * @throws PreferencesException
     **************************************************************************/
    private void updatePhysicalDocStruct(Prefs thePrefs) throws PreferencesException {

        this.setPhysicalDocStruct(updateDocStruct(this.getPhysicalDocStruct(), thePrefs));
    }

    /***************************************************************************
     * <p>
     * Updates a DocStruct tree.
     * </p>
     * 
     * <p>
     * NOTE This method only is needed for XStream de-serialisation!
     * </p>
     * 
     * @param theStruct
     * @param thePrefs
     * @throws PreferencesException
     **************************************************************************/
    private DocStruct updateDocStruct(DocStruct theStruct, Prefs thePrefs) throws PreferencesException {

        // If prefs are empty, throw exception!
        if (thePrefs == null) {
            throw new PreferencesException("No preferences loaded!");
        }

        // If struct is empty, just return.
        if (theStruct == null) {
            log.warn("DocStruct is empty! Update of DocStruct from Prefs failed!");
            return null;
        }

        DocStructType structTypeFromDigdoc = theStruct.getType();
        DocStructType structTypeFromPrefs = thePrefs.getDocStrctTypeByName(structTypeFromDigdoc.getName());

        // Check, if the current DocStruct name (from DigDoc) is contained in
        // the Prefs.
        if (structTypeFromPrefs != null) {
            log.debug("DocStruct '" + structTypeFromDigdoc.getName() + "' from DigitalDocument contained in prefs");

            // Update DocStructType from the prefs.
            theStruct.setType(structTypeFromPrefs);
            log.trace("Updated DocStructType '" + structTypeFromDigdoc.getName() + "' from prefs");

            // Update MetadataTypes from Prefs.
            structTypeFromPrefs.getAllMetadataTypes();
            List<Metadata> mList = theStruct.getAllMetadata();
            if (mList != null) {
                for (Metadata m : mList) {
                    // Get MetadataType from prefs.
                    MetadataType mtypeFromPrefs = thePrefs.getMetadataTypeByName(m.getType().getName());
                    if (mtypeFromPrefs != null) {
                        m.setType(mtypeFromPrefs);
                        log.trace("Updated MetadataType '" + m.getType().getName() + "' from prefs");
                    }
                }
            }
        } else {
            PreferencesException pe =
                    new PreferencesException("DocStruct '" + structTypeFromDigdoc.getName() + "' from DigitalDocument NOT contained in prefs!");
            log.error(pe.getMessage());
            throw new PreferencesException();
        }

        log.debug("DocStructType '" + structTypeFromDigdoc.getName() + "' and all MetadataTypes updated from prefs");

        // Recursively call all DocStructs.
        if (theStruct.getAllChildren() != null) {
            for (DocStruct ds : theStruct.getAllChildren()) {
                updateDocStruct(ds, thePrefs);
            }
        }

        return theStruct;
    }

    /***************************************************************************
     * <p>
     * Sorts all metadata and persons recursively for the given DocStruct alphabetically (and recursively).
     * </p>
     **************************************************************************/
    private synchronized void sortMetadataRecursivelyAbcdefg(DocStruct theStruct) {

        if (theStruct == null) {
            return;
        }

        if (theStruct.getAllChildren() != null) {
            for (DocStruct d : theStruct.getAllChildren()) {
                sortMetadataRecursivelyAbcdefg(d);
            }
        }

        theStruct.sortMetadataAbcdefg();
    }

    /***************************************************************************
     * <p>
     * Sorts all metadata and persons recursively for the given DocStruct according to their occurance in the preferences file.
     * </p>
     **************************************************************************/
    private synchronized void sortMetadataRecursively(DocStruct theSruct, Prefs thePrefs) {

        if (thePrefs == null) {
            log.warn("Cannot sort metadata according to prefs! No prefs available!");
            return;
        }

        if (theSruct == null) {
            return;
        }

        if (theSruct.getAllChildren() != null) {
            for (DocStruct d : theSruct.getAllChildren()) {
                sortMetadataRecursively(d, thePrefs);
            }
        }

        theSruct.sortMetadata(thePrefs);
    }

    /***************************************************************************
     * <p>
     * Writes a DigitalDocument to disk as an XStream XML file.
     * </p>
     * 
     * @param filename
     * @deprecated
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     **************************************************************************/
    @Deprecated
    public void writeXStreamXml(String filename) throws FileNotFoundException, UnsupportedEncodingException {

        // Write the DigitalDocument as an XStream file.
        XStream xStream = new XStream(new DomDriver());

        BufferedWriter outfile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8));
        xStream.toXML(this, outfile);
    }

    /***************************************************************************
     * <p>
     * Restores all content files to the digital document according to the pathimagefiles metadata. All FileSet data (ContentFiles, VrtualFileGroups,
     * etc.) will be lost!
     * </p>
     **************************************************************************/
    private void restoreFileSetFromPhysicalMetadata() {

        FileSet newFileSet = new FileSet();

        // Get the physical DocStruct.
        DocStruct physicalDocStruct = this.getPhysicalDocStruct();

        // Iterate throught all the physical docstruct's metadata.
        if (physicalDocStruct != null && physicalDocStruct.getAllChildren() != null) {

            // Iterate over all DocStructs "page" with metadata "physPageNumber"
            // and add a content file each, if none is existing.
            for (DocStruct ds : physicalDocStruct.getAllChildren()) {
                for (Metadata m : ds.getAllMetadata()) {
                    if ("physPageNumber".equals(m.getType().getName())) {
                        createContentFile(ds, m.getValue());
                        newFileSet.addFile(ds.getAllContentFiles().get(0));
                    }
                }
            }
        }

        // If files were found in the pages, set the new FileSet and override
        // the old one (fixes DPD-406).
        if (!newFileSet.getAllFiles().isEmpty()) {
            this.setFileSet(newFileSet);
        }
    }

    /**************************************************************************
     * <p>
     * Adds a content file to a DocStruct "page"! All FileSet data (ContentFiles, VrtualFileGroups, etc.) will be lost!
     * </p>
     * 
     * @param theStruct
     **************************************************************************/
    public void addContentFileFromPhysicalPage(DocStruct theStruct) {
        // Return, if called with a DocStruct other than "page" or a content
        // file is already existing.
        if (!PhysicalElement.checkPhysicalType(theStruct.getType().getName()) || theStruct.getAllContentFiles() != null) {
            return;
        }

        // Iterate over all metadata with type name "physpagenumber".
        List<Metadata> metadataList = theStruct.getAllMetadata();
        if (metadataList != null) {
            for (Metadata md : metadataList) {
                if ("physPageNumber".equals(md.getType().getName())) {
                    // Create new content file.
                    createContentFile(theStruct, md.getValue());
                }
            }
        }
    }

    /***************************************************************************
     * <p>
     * Add all content files to the digital document according to the pathimagefiles metadata. The pages in the physical DocStruct must already exist!
     * </p>
     * 
     * 
     **************************************************************************/

    public void addAllContentFiles() {

        // Get the physical DocStruct.
        DocStruct tp = this.getPhysicalDocStruct();

        // Delete the existing fileset before adding the files, and save the
        // virtualFileGroups!
        if (this.getFileSet() != null && this.getFileSet().getVirtualFileGroups() != null) {
            List<VirtualFileGroup> vfgList = this.getFileSet().getVirtualFileGroups();
            this.setFileSet(new FileSet());
            this.getFileSet().setVirtualFileGroups(vfgList);
        }

        String representative = "";

        // Iterate throught all the physical docstruct's metadata.
        if (tp != null) {

            // Set the path to the images.
            String pif = "";
            if (tp.getAllMetadata() != null) {
                for (Metadata md : tp.getAllMetadata()) {
                    if ("pathimagefiles".equals(md.getType().getName())) {
                        pif = md.getValue();
                    } else if ("_representative".equals(md.getType().getName())) {
                        representative = md.getValue();
                    }
                }
            }

            // Iterate over all pages and add all the content files.
            if (tp.getAllChildren() != null) {
                for (DocStruct ds : tp.getAllChildren()) {
                    ContentFile cf = new ContentFile();

                    if (PhysicalElement.checkPhysicalType(ds.getType().getName())) {
                        // Iterate over all metadata.
                        for (Metadata md : ds.getAllMetadata()) {
                            if ("physPageNumber".equals(md.getType().getName())) {
                                cf.setLocation(pif + "/" + new DecimalFormat("00000000").format(Integer.parseInt(md.getValue())) + ".tif");
                                cf.setMimetype("image/tiff");
                                if (!representative.isEmpty() && representative.equals(md.getValue())) {
                                    cf.setRepresentative(true);
                                }
                                // Remove all content files from the page, if
                                // existing.
                                if (ds.getAllContentFiles() != null) {
                                    for (ContentFile oldCf : ds.getAllContentFiles()) {
                                        try {
                                            ds.removeContentFile(oldCf);
                                            cf.setLocation(oldCf.getLocation());
                                        } catch (ContentFileNotLinkedException e) {
                                            // Do nothing, because we want to
                                            // remove them anyway. If they do
                                            // not exist, we have no problem!
                                        }
                                    }
                                }
                                // Add the current content file to page.
                                ds.addContentFile(cf);

                                log.trace("Added file '" + cf.getLocation() + "' to DocStruct '" + ds.getType().getName() + "'");
                            }
                        }
                    }
                }
            }
        }
    }

    /***************************************************************************
     * <p>
     * Overrides ContentFiles of DigitalDocument with new names for images. Code mostly taken from old addAllContentFiles method.
     * </p>
     * 
     * @param a List of sorted image names
     * 
     * @author Robert Sehr
     * 
     **************************************************************************/

    public void overrideContentFiles(List<String> images) {

        // Get the physical DocStruct.
        DocStruct tp = this.getPhysicalDocStruct();

        // Delete the existing fileset before adding the files, and save the
        // virtualFileGroups!
        List<VirtualFileGroup> vfgList = this.getFileSet().getVirtualFileGroups();
        this.setFileSet(new FileSet());
        this.getFileSet().setVirtualFileGroups(vfgList);
        String representative = "";
        // Iterate throught all the physical docstruct's metadata.
        if (tp != null && tp.getAllMetadata() != null) {

            // Set the path to the images.
            String pif = "";
            for (Metadata md : tp.getAllMetadata()) {
                if ("pathimagefiles".equals(md.getType().getName())) {
                    pif = md.getValue();
                } else if ("_representative".equals(md.getType().getName())) {
                    representative = md.getValue();
                }
            }

            // Iterate over all pages and add all the content files.
            if (tp.getAllChildren() != null) {
                for (DocStruct ds : tp.getAllChildren()) {
                    ContentFile cf = new ContentFile();
                    if (PhysicalElement.checkPhysicalType(ds.getType().getName())) {
                        // Iterate over all metadata.
                        for (Metadata md : ds.getAllMetadata()) {
                            if ("physPageNumber".equals(md.getType().getName())) {

                                if (!representative.isEmpty() && representative.equals(md.getValue())) {
                                    cf.setRepresentative(true);
                                }
                                int value = Integer.parseInt(md.getValue());
                                cf.setLocation(pif + File.separator + images.get(value - 1));
                                // Remove all content files from the page, if
                                // existing.
                                if (ds.getAllContentFiles() != null) {
                                    for (ContentFile oldCf : ds.getAllContentFiles()) {
                                        cf.setMimetype(oldCf.getMimetype());
                                        cf.setLocation(oldCf.getLocation());
                                        try {
                                            ds.removeContentFile(oldCf);
                                        } catch (ContentFileNotLinkedException e) {
                                            // Do nothing, because we want to
                                            // remove them anyway. If they do
                                            // not exist, we have no problem!
                                        }
                                    }
                                } else {
                                    cf.setLocation(pif + File.separator + images.get(value - 1));
                                    cf.setMimetype("image/tiff");
                                }
                                ds.addContentFile(cf);
                            }
                        }
                    }
                }
            }
        }
    }

    /**************************************************************************
     * <p>
     * Just returns the path to the image files.
     * </p>
     * 
     * @return
     **************************************************************************/
    private String getPathToImages() {

        String pathToImageFiles = "";
        if (this.getPhysicalDocStruct() != null && this.getPhysicalDocStruct().getAllMetadata() != null) {
            for (Metadata md : this.getPhysicalDocStruct().getAllMetadata()) {
                if ("pathimagefiles".equals(md.getType().getName())) {
                    pathToImageFiles = md.getValue();
                    break;
                }
            }
        }
        return pathToImageFiles;
    }

    /**************************************************************************
     * <p>
     * Adds a single content file to a DocStruct.
     * </p>
     * 
     * TODO Get the mimetype from anywhere, and not assume it was tiff!
     * 
     * @param theStruct
     * @param theName
     **************************************************************************/
    private void createContentFile(DocStruct theStruct, String theName) {

        // Create new content file, set location and mimetype.
        ContentFile newCf = new ContentFile();
        newCf.setLocation(getPathToImages() + "/" + new DecimalFormat("00000000").format(Integer.parseInt(theName)) + ".tif");
        newCf.setMimetype("image/tiff");

        // Remove all content files from the page, if existing.
        if (theStruct.getAllContentFiles() != null) {
            for (ContentFile oldCf : theStruct.getAllContentFiles()) {
                try {
                    theStruct.removeContentFile(oldCf);
                } catch (ContentFileNotLinkedException e) {
                    // Do nothing, because we want to remove them anyway. If
                    // they do not exist, we have no problem!
                }
            }
        }

        // Set the fileset of the current DigitalDocument, set DigitalDocument
        // first.
        theStruct.setDigitalDocument(this);
        theStruct.addContentFile(newCf);

        log.trace("Added file '" + newCf.getLocation() + "' to DocStruct '" + theStruct.getType().getName() + "'");
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public static String getVersion() {
        return VERSION;
    }

    /**
     * @param techMd the techMd to set
     */
    public void addTechMd(Node techMdNode, MdType type) {
        if (this.amdSec == null) {
            amdSec = new AmdSec(new ArrayList<>());
        }
        Md techMd = new Md(techMdNode, type);
        this.amdSec.addTechMd(techMd);
    }

    /**
     * @param techMd the techMd to set
     */
    public void addTechMd(Md techMd) {
        if (this.amdSec == null) {
            amdSec = new AmdSec(new ArrayList<>());
        }
        this.amdSec.addTechMd(techMd);
    }

    /**
     * @return the techMd
     */
    public List<Node> getTechMdsAsNodes() {
        if (this.amdSec == null) {
            amdSec = new AmdSec(new ArrayList<>());
        }
        return amdSec.getTechMdsAsNodes();
    }

    /**
     * @return the techMd
     */
    public List<Md> getTechMds() {
        if (amdSec == null) {
            return new ArrayList<>();
        }
        return amdSec.getTechMdList();
    }

    public Md getTechMd(String id) {
        if (amdSec == null || amdSec.getTechMdList() == null) {
            return null;
        }

        for (Md techMd : this.amdSec.getTechMdList()) {
            if (techMd.getId() != null && techMd.getId().trim().contentEquals(id.trim())) {
                return techMd;
            }
        }
        return null;
    }

    public void setAmdSec(AmdSec sec) {
        this.amdSec = sec;
    }

    public void setAmdSec(String id) {
        this.amdSec = new AmdSec(new ArrayList<>());
        this.amdSec.setId(id);
    }

    public AmdSec getAmdSec(String id) {
        if (amdSec == null) {
            return null;
        } else if (amdSec.getId().trim().contentEquals(id.trim())) {
            return amdSec;
        }
        return null;
    }

    public AmdSec getAmdSec() {
        return amdSec;
    }

    /***************************************************************************
     * <p>
     * Creates a deep copy of the DigitalDocument.
     * </p>
     * 
     * @return the new DigitalDocument instance
     **************************************************************************/

    public DigitalDocument copyDigitalDocument() throws WriteException {

        DigitalDocument newDigDoc = null;

        try {

            // remove techMd list for serialization
            ArrayList<Md> tempList = new ArrayList<>(getTechMds());
            getTechMds().clear();

            // Write the object out to a byte array.
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            newDigDoc = (DigitalDocument) in.readObject();

            // reattach techMd list
            for (Md md : tempList) {
                newDigDoc.addTechMd(md);
            }

        } catch (IOException e) {
            String message = "Couldn't obtain OutputStream!";
            log.error(message, e);
            throw new WriteException(message, e);
        } catch (ClassNotFoundException e) {
            String message = "Could not find some class!";
            log.error(message, e);
            throw new WriteException(message, e);
        }

        return newDigDoc;
    }

    public static String detectMimeType(Path path) {

        String mimeType = "";
        if (path == null || Files.isDirectory(path)) {
            return mimeType;
        }
        try {
            // first try to detect mimetype from OS map
            mimeType = Files.probeContentType(path);
        } catch (IOException e) {
            // do nothing
        }
        // if this didn't work, try to get it from the internal FileNameMap to resolve the type from the extension
        if (StringUtils.isBlank(mimeType)) {
            mimeType = URLConnection.guessContentTypeFromName(path.getFileName().toString());
        }
        // we are on a mac, compare against list of known file formats
        if (StringUtils.isBlank(mimeType) || "application/octet-stream".equals(mimeType)) {
            String fileExtension = path.getFileName().toString();
            if (!fileExtension.contains(".")) {
                return "application/octet-stream";
            }
            fileExtension = fileExtension.substring(fileExtension.lastIndexOf(".") + 1).toLowerCase(); // .tar.gz will not work
            switch (fileExtension) {
                case "jpg":
                case "jpeg":
                case "jpe":
                    mimeType = "image/jpeg";
                    break;
                case "jp2":
                    mimeType = "image/jp2";
                    break;
                case "tif":
                case "tiff":
                    mimeType = "image/tiff";
                    break;
                case "png":
                    mimeType = "image/png";
                    break;
                case "gif":
                    mimeType = "image/gif";
                    break;
                case "pdf":
                    mimeType = "application/pdf";
                    break;
                case "mp3":
                    mimeType = "audio/mpeg";
                    break;
                case "wav":
                    mimeType = "audio/wav";
                    break;
                case "mpeg":
                case "mpg":
                case "mpe":
                    mimeType = " video/mpeg ";
                    break;
                case "mp4":
                    mimeType = "video/mp4";
                    break;
                case "mxf":
                    mimeType = "video/mxf";
                    break;
                case "ogg":
                    mimeType = "video/ogg";
                    break;
                case "webm":
                    mimeType = "video/webm";
                    break;
                case "mov":
                    mimeType = "video/quicktime";
                    break;
                case "avi":
                    mimeType = "video/x-msvideo";
                    break;
                case "xml":
                    mimeType = "application/xml";
                    break;
                case "txt":
                    mimeType = "text/plain";
                    break;
                case "x3d":
                case "x3dv":
                case "x3db":
                    mimeType = "model/x3d+XXX";
                    break;
                case "obj":
                case "ply":
                case "stl":
                case "fbx":
                case "gltf":
                case "glb":
                    mimeType = "object/" + fileExtension;
                    break;
                default:
                    // use a default value, if file extension is not mapped
                    mimeType = "application/octet-stream";
            }

        }

        return mimeType;
    }
}
