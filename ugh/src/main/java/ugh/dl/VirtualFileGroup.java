package ugh.dl;

/*******************************************************************************
 * ugh.dl / VirtualFileGroup.java
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/*******************************************************************************
 * <p>
 * A <code>VirtualFileGroup</code> contains all file groups needed for the class
 * MetsModsImportExport.
 * </p>
 * 
 * @author Stefan E. Funk
 * @version 2009-11-17
 * @since 2008-11-19
 * @see ContentFile
 * 
 *      CHANGELOG
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      30.10.2009 --- Funk --- Added generated serialVersionUID.
 * 
 *      16.01.2009 --- Funk --- Made the suffix input "." resistent!
 * 
 ******************************************************************************/

public class VirtualFileGroup implements Serializable {

    /**
     * A list constant representing the state that all files should be allowed for this FileGroup. Per default this is the case
     */
    public static final List<ContentFile>     ALL_FILES           = Collections.emptyList();
    private static final long	serialVersionUID	= 8594056041230503891L;


    private String				name				= "";
    private String				pathToFiles			= "";
    private String				mimetype			= "";
    private String				fileSuffix			= "";
    private String				idSuffix			= "";
    private List<ContentFile>   contentFiles        = ALL_FILES;
    private boolean mainGroup;

    @Getter @Setter
    private boolean ignoreConfiguredMimetypeAndSuffix;

    @Getter @Setter
    private String fileExtensionsToIgnore;

    /***************************************************************************
     * Default constructor.
     **************************************************************************/
    public VirtualFileGroup() {
        super();
    }

    /***************************************************************************
     * Constructor.
     **************************************************************************/
    public VirtualFileGroup(String theFilegroupName, String thePath,
            String theMimetype, String theFileSuffix) {

        super();
        this.name = theFilegroupName;
        this.pathToFiles = thePath;
        this.mimetype = theMimetype;
        this.fileSuffix = theFileSuffix;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getName() {
        return this.name;
    }

    /***************************************************************************
     * @param name
     **************************************************************************/
    public void setName(String name) {
        this.name = name;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getPathToFiles() {
        return this.pathToFiles;
    }

    /***************************************************************************
     * @param pathToFiles
     **************************************************************************/
    public void setPathToFiles(String pathToFiles) {
        this.pathToFiles = pathToFiles;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getMimetype() {
        return this.mimetype;
    }

    /***************************************************************************
     * @param mimetype
     **************************************************************************/
    public void setMimetype(String mimetype) {
    	if(mimetype != null) {	
    		this.mimetype = mimetype;
        }else {
        	this.mimetype = "";
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getFileSuffix() {
        return this.fileSuffix;
    }

    /***************************************************************************
     * @param fileSuffix
     **************************************************************************/
    public void setFileSuffix(String fileSuffix) {

    	if(fileSuffix != null) {
    		// If the given file suffix starts with a ".", remove the ".".
	        if (fileSuffix.startsWith(".")) {
	            this.fileSuffix = fileSuffix.replaceFirst("\\.", "");
	        } else {
	            this.fileSuffix = fileSuffix;
	        }
    	}else {
    		this.fileSuffix = "";
    	}
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getIdSuffix() {
        return this.idSuffix;
    }

    /***************************************************************************
     * @param idSuffix
     **************************************************************************/
    public void setIdSuffix(String idSuffix) {
        this.idSuffix = idSuffix;
    }

    /**
     * Returns the list of content files which should be written for this FileGroup
     * If the list is identical to {@link VirtualFileGroup#ALL_FILES ALL_FILES}
     * then all ContentFiles should be included in this FileGroup
     * 
     * @return the list of allowed ContentFiles for this FileGroup. Never null
     */
    public List<ContentFile> getContentFiles() {
        return contentFiles;
    }

    /**
     * Set the list of allowed ContentFiles to {@link VirtualFileGroup#ALL_FILES ALL_FILES}
     * so all ContentFiles may be written to this FileGroup
     */
    public void allowAllFiles() {
        this.contentFiles = ALL_FILES;
    }

    /**
     * Set the list to only allow files explicitly added via {@link #addContentFile} or {@link #addContentFiles}
     * 
     */
    public void restrictFiles() {
        if(this.contentFiles == ALL_FILES) {
            this.contentFiles = new ArrayList<>();
        }
    }

    /**
     * Adds a ContentFile to the list of ContentFiles allowed for this FileGroup
     * 
     * @param contentFile
     */
    public void addContentFile(ContentFile contentFile) {
        if(this.contentFiles == ALL_FILES) {
            this.contentFiles = new ArrayList<>();
        }
        this.contentFiles.add(contentFile);
    }

    /**
     * Adds a collection of ContentFiles to the list of ContentFiles allowed for this FileGroup
     * 
     * @param contentFiles
     */
    public void addContentFiles(Collection<ContentFile> contentFiles) {
        if(this.contentFiles == ALL_FILES) {
            this.contentFiles = new ArrayList<>();
        }
        this.contentFiles.addAll(contentFiles);
    }

    /**
     * Removes a ContentFile from the list of ContentFiles allowed for this FileGroup
     * 
     * @param contentFile
     */
    public void removeContentFile(ContentFile contentFile) {
        if(this.contentFiles.contains(contentFile)) {
            this.contentFiles.remove(contentFile);
        }
    }

    /**
     * Removes a collection of ContentFiles from the list of ContentFiles allowed for this FileGroup
     * 
     * @param contentFiles
     */
    public void removeContentFiles(Collection<ContentFile> contentFiles) {
        if(!this.contentFiles.isEmpty()) {
            this.contentFiles.removeAll(contentFiles);
        }
    }

    public boolean contains(ContentFile contentFile) {
        return this.contentFiles == ALL_FILES || this.contentFiles.contains(contentFile);
    }

    public boolean isMainGroup() {
        return mainGroup;
    }

    public void setMainGroup(boolean mainGroup) {
        this.mainGroup = mainGroup;
    }

}
