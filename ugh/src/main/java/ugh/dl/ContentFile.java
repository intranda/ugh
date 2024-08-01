package ugh.dl;

/*******************************************************************************
 * ugh.dl / ContentFile.java
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*******************************************************************************
 * <p>
 * A ContentFile represents a file which must be accessable via the file system and contains the content of the <code>DigitalDocument</code>. A
 * ContentFile belongs always to <code>FileSet</code>, which provides methods to add and remove content files (<code>addFile</code> and
 * <code>removeFile</code>. ContentFile objects are not only be part of a FileSet but must also be linked to structure elements. Therefore references
 * to <code>DocStruct</code> objects exists.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2010-02-14
 * @since 2009-09-23
 * @see FileSet#addFile
 * @see FileSet#removeFile
 * 
 *      CHANGELOG
 * 
 *      14.02.2010 --- Funk --- Added method toString().
 * 
 *      13.02.2010 --- Funk --- Minor changes.
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      16.11.2009 --- Funk --- Added some "private"'s to class attributes. --- Removed some unused private variables. --- Refactored the get and
 *      setMimetype method.
 * 
 ******************************************************************************/

public class ContentFile implements Serializable {

    private static final long serialVersionUID = 367830986928498143L;

    // Contains metadata for this image.
    private List<Metadata> allMetadata;
    private List<Metadata> removedMetadata;
    // All physical document structures this ContentFile is referenced from.
    private List<DocStruct> referencedDocStructs;

    // Location of the pyshical image; URL or filename.
    private String location;
    // Is the MimeType of an Image stored as string.
    private String mimeType;

    private String identifier;

    private Map<String, String> uuidMap = new HashMap<>();

    //the list of techMd sections referenced by this File
    private List<Md> techMdList;

    private boolean isRepresentative = false;

    /***************************************************************************
     * <p>
     * Constructor.
     * </p>
     **************************************************************************/
    public ContentFile() {
        super();
    }

    /***************************************************************************
     * @param inMD
     * @return
     **************************************************************************/
    public boolean addMetadata(Metadata inMD) {
        this.allMetadata.add(inMD);
        return true;
    }

    /***************************************************************************
     * @param inMD
     * @return
     **************************************************************************/
    public boolean removeMetadata(Metadata inMD) {
        this.allMetadata.remove(inMD);
        this.removedMetadata.add(inMD);
        return true;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public List<Metadata> getAllMetadata() {
        return this.allMetadata;
    }

    /***************************************************************************
     * <p>
     * Sets the filename of the ContentFile; file must at least be readable from this position.
     * </p>
     * 
     * 
     * @param in
     **************************************************************************/
    public void setLocation(String in) {
        this.location = in;
    }

    /***************************************************************************
     * <p>
     * Retrieves the filename of the ContentFile. The filename is always an absolute file.
     * </p>
     * 
     * @return filename
     **************************************************************************/
    public String getLocation() {
        return this.location;
    }

    /**************************************************************************
     * @param in
     **************************************************************************/
    public void setMimetype(String in) {
        this.mimeType = in;
    }

    /***************************************************************************
     * @param in
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public void setMimeType(String in) {
        setMimetype(in);
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMimetype() {
        return this.mimeType;
    }

    /***************************************************************************
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public String getMimeType() {
        return getMimetype();
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setIdentifier(String in) {
        this.identifier = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getIdentifier() {
        return this.identifier;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    @JsonIgnore
    public List<DocStruct> getReferencedDocStructs() {
        return this.referencedDocStructs;
    }

    /***************************************************************************
     * <p>
     * Adds a reference to the current DocStruct element.
     * </p>
     * 
     * @param inStruct
     * @return true, if adding was successful; false otherwise.
     **************************************************************************/
    public boolean addDocStructAsReference(DocStruct inStruct) {

        if (this.referencedDocStructs == null) {
            this.referencedDocStructs = new LinkedList<>();
        }
        if (inStruct == null || this.referencedDocStructs.contains(inStruct)) {
            return false;
        }
        this.referencedDocStructs.add(inStruct);

        return true;
    }

    /***************************************************************************
     * @param inStruct
     * @return
     **************************************************************************/
    protected boolean removeDocStructAsReference(DocStruct inStruct) {

        if (this.referencedDocStructs == null) {
            // No references available.
            return false;
        }
        this.referencedDocStructs.remove(inStruct);

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ContentFile (ID: " + this.getIdentifier() + "): '" + this.getLocation() + "' (" + this.getMimetype() + ")" + "\n";
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
        // remove all nulls from the list
        while (this.techMdList.remove(null)) {
            // nothing
        }
    }

    public boolean isRepresentative() {
        return isRepresentative;
    }

    public void setRepresentative(boolean isRepresentative) {
        this.isRepresentative = isRepresentative;
    }

    public Map<String, String> getUuidMap() {
        return uuidMap;
    }

    public void addUUID(String type, String uuid) {
        uuidMap.put(type, uuid);
    }

    public String getUUID(String type) {
        return uuidMap.get(type);
    }

}
