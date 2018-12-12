package ugh.fileformats.slimjson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.ContentFileReference;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Person;
import ugh.dl.Reference;

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
    @JsonIgnore
    private transient SlimDigitalDocument digitalDocument;

    private String id;
    private String type;
    private String parentId;

    private List<SlimMetadata> allMetadata = new ArrayList<>();
    private List<SlimMetadataGroup> allMetadataGroups = new ArrayList<>();
    private List<Person> persons;
    private List<String> children = new ArrayList<>();
    private List<SlimContentFileReference> contentFileReferences = new ArrayList<>();
    private List<SlimReference> docStructRefsTo = new ArrayList<>();
    private List<SlimReference> docStructRefsFrom = new ArrayList<>();
    private boolean updated = false;
    private boolean logical = false;
    private boolean physical = false;

    /**
     * Creates a SlimDocStruct from a DocStruct.
     * 
     * @param ds
     * @return
     */
    public static SlimDocStruct fromDocStruct(DocStruct ds, SlimDigitalDocument sdd) {
        SlimDocStruct sds = new SlimDocStruct();
        sds.setDigitalDocument(sdd);
        //set all simple properties
        sds.setId(ds.getIdentifier());
        sds.setLogical(ds.isLogical());
        sds.setPhysical(ds.isPhysical());
        // add metadata
        for (Metadata meta : ds.getAllMetadata()) {
            sds.allMetadata.add(SlimMetadata.fromMetadata(meta, sdd));
        }
        //add metadata groups
        for (MetadataGroup mg : ds.getAllMetadataGroups()) {
            sds.allMetadataGroups.add(SlimMetadataGroup.fromMetadataGroup(mg, sdd));
        }
        //add persons
        sds.persons = ds.getAllPersons();
        // add children
        for (DocStruct cds : ds.getAllChildren()) {
            SlimDocStruct scds = sdd.getDsMap().get(cds.getIdentifier());
            if (scds == null) {
                scds = SlimDocStruct.fromDocStruct(cds, sdd);
            }
            sds.children.add(scds.getId());
        }
        //add contentFileReferences
        for (ContentFileReference cfr : ds.getAllContentFileReferences()) {
            sds.contentFileReferences.add(SlimContentFileReference.fromContentFileReference(cfr, sdd));
        }
        //add to-references
        for (Reference ref : ds.getAllToReferences()) {
            sds.docStructRefsTo.add(SlimReference.fromReference(ref, sdd));
        }
        //add from-references
        for (Reference ref : ds.getAllFromReferences()) {
            sds.docStructRefsFrom.add(SlimReference.fromReference(ref, sdd));
        }
        sdd.addSlimDocStruct(sds);
        return sds;
    }
}
