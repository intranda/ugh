package ugh.fileformats.slimjson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ugh.dl.ContentFileReference;
import ugh.dl.DocStruct;
import ugh.dl.Md;
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

    private String referenceToAnchor;
    //the amdSec referenced by this docStruct, if any
    private SlimAmdSec amdSec;
    //the list of techMd sections referenced by this docStruct, if any
    private List<SlimMd> techMdList = new ArrayList<>();

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
        if (ds.getIdentifier() != null) {
            sds.setId(ds.getIdentifier());
        } else {
            sds.id = UUID.randomUUID().toString();
            ds.setIdentifier(sds.id);
        }
        sds.setLogical(ds.isLogical());
        sds.setPhysical(ds.isPhysical());
        sds.type = ds.getType().getName();
        sdd.addDsType(ds.getType());
        // add metadata
        if (ds.getAllMetadata() != null) {
            for (Metadata meta : ds.getAllMetadata()) {
                sds.allMetadata.add(SlimMetadata.fromMetadata(meta, sdd));
            }
        }
        //add metadata groups
        if (ds.getAllMetadataGroups() != null) {
            for (MetadataGroup mg : ds.getAllMetadataGroups()) {
                sds.allMetadataGroups.add(SlimMetadataGroup.fromMetadataGroup(mg, sdd));
            }
        }
        //add persons
        sds.persons = ds.getAllPersons();
        // add children
        if (ds.getAllChildren() != null) {
            for (DocStruct cds : ds.getAllChildren()) {
                SlimDocStruct scds = sdd.getDsMap().get(cds.getIdentifier());
                if (scds == null) {
                    scds = SlimDocStruct.fromDocStruct(cds, sdd);
                }
                sds.children.add(scds.getId());
            }
        }
        //add contentFileReferences
        if (ds.getAllContentFileReferences() != null) {
            for (ContentFileReference cfr : ds.getAllContentFileReferences()) {
                sds.contentFileReferences.add(SlimContentFileReference.fromContentFileReference(cfr, sdd));
            }
        }
        //add to-references
        if (ds.getAllToReferences() != null) {
            for (Reference ref : ds.getAllToReferences()) {
                sds.docStructRefsFrom.add(SlimReference.fromReference(ref, sdd));
            }
        }
        //add from-references
        if (ds.getAllFromReferences() != null) {
            for (Reference ref : ds.getAllFromReferences()) {
                sds.docStructRefsTo.add(SlimReference.fromReference(ref, sdd));
            }
        }
        //add amdSec
        sds.amdSec = SlimAmdSec.fromAmdSec(ds.getAmdSec(), sdd);
        //add techMdList
        if (ds.getTechMds() != null) {
            for (Md md : ds.getTechMds()) {
                sds.techMdList.add(SlimMd.fromMd(md));
            }
        }
        sdd.addSlimDocStruct(sds);
        return sds;
    }
}
