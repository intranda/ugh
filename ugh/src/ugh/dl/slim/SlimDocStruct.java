package ugh.dl.slim;

import java.util.List;
import java.util.Map;

import lombok.Data;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.Person;

/*******************************************************************************
 * ugh.dl / SlimDocStruct.java
 * 
 * Copyright 2018 Center for Retrospective Digitization, Göttingen (GDZ)
 * 
 * http://gdz.sub.uni-goettingen.de
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This Library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 ******************************************************************************/

/*******************************************************************************
 * <p>
 * This is a slim representation of a DocStruct without cyclic references. @see ugh.dl.DocStruct for a general description of a DocStruct. The aim of
 * this class is to offer a format of a DocStruct that can easily be serialized to JSON, It therefore keeps a HashMap of all child-DocStructs at the
 * root Element and the structure is described only by IDs. The same happens with DocStruct types and their possible children types.
 * </p>
 * 
 * @author Oliver Paetzel
 * 
 *******************************************************************************/
@Data
public class SlimDocStruct {

    private Map<String, SlimDocStruct> dsMap;
    private Map<String, DocStructType> dsTypeMap;
    private Map<String, MetadataType> metadataTypeMap;
    private Map<String, MetadataGroupType> metadataGroupTypeMap;

    private String id;
    private String type;
    private String parentId;

    private List<SlimMetadata> allMetadata;
    private List<SlimMetadataGroup> allMetadataGroups;
    private List<Person> persons;
    private List<String> children;
    private List<SlimContentFileReference> contentFileReferences;
    private List<SlimReference> docStructRefsTo;
    private List<SlimReference> docStructRefsFrom;
    private boolean updated = false;
    private boolean logical = false;
    private boolean physical = false;

    private transient SlimDocStruct rootDs;

    /**
     * Creates a SlimDocStruct from a DocStruct.
     * 
     * @param ds
     * @return
     */
    public static SlimDocStruct fromDocStruct(DocStruct ds) {
        return new SlimDocStruct();
    }
}
