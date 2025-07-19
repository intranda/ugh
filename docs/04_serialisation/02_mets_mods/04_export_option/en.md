---
title: Extended METS export options
published: true
---

For various possibilities of the zvdd/DFG-Viewer METS format, no equivalent data are provided in our document model. However, these data are essential for a proper METS export. Setter and getter methods are implemented in the class ```ugh.fileformats.mets.MetsModsImportExport`` for specifying values of the following fields - for further explanations see the zvdd/DFG Viewer METS profile mentioned above:

* The `rightsOwner`, `rightsOwnerLogo`, `rightsOwnerSiteURL`, `digiprovReference`, `digiprovPresentation`, `digiprovReferenceAnchor`, and `digiprovPresentationAnchor` attributes set values for the administrative metadata section of the METS file,these are the metadata on the rights holder, author, origin and online presentation - compare chapter "Administrative Metadata Section" in the zvdd/DFG Viewer METS profile.
* The attribute `purlUrl` sets the value of a persistent identifier, which is set in the METS file as PURL for the entire work at the corresponding position in the logical StructMap (see "structMap requirement 3: Complex document model").
* The attribute `contentIds`, on the other hand, is used to reference the individual pages of the physical structure. Currently it is used as a path to the single file, to which the filename is appended (This feature is still experimental).
* If a regular expression (in Perl5 syntax) is passed at the end of such a metadata - in the form `$REGEXP(s//)` - then this regular expression is applied to the entire value before it is transferred to the METS.

Example:\_

For example, if the value `http://opac.sub.uni-goettingen.de/DB=1/PPN?PPN=PPN123456789` is passed for the metadata `digiprovReference`, and if the `PPN123456789` comes from a specific metadata field and cannot be influenced in a specific context, `http://opac.sub.uni-goettingen.de/DB=1/PPN?PPN=PPN123456789$REGEXP(s/PPN=PPN/PPN/)` can be used to remove the `PPN` before the actual number.

The METS FileGroups passed via VirtualFileGroup require further explanation. Each VirtualFileGroup in the FileSet object of the DigitalDocument is exported as a METS FileGroup as explained in the METS profile's FileSec Requirements 2: File Groups chapter. With the following method

```text
DigitalDocument.getFileSet().addVirtualFileGroup()
```

these can be added to the document model. All required values can be set within the `VirtualFileGroup` object, see the implementation in the source code of the class `UghConvert`.

## Authority data

Standards data can be entered in UGH for each metadata and each person. Standard data always consists of the three information: database abbreviation, database URL and value within the database. They can be set with the method `ugh.dl.Metadata.setAutorityFile(String authorityID, String authorityURI, String authorityValue)`.

The METS export then generates the attributes `authority`, `authorityURI` and `valueURI` from the values.

```xml
<mods:name type="personal" authority="gnd" authorityURI="http://d-nb.info/gnd/" 
     valueURI="http://d-nb.info/gnd/116733721">
     <mods:namePart>Mann, Monika</mods:namePart>
     <mods:role>
          <mods:roleTerm type="code" authority="marcrelator">aut</mods:roleTerm>
     </mods:role>
</mods:name>
```

```xml
<mods:subject>
    <mods:topic authority="gnd" authorityURI="http://d-nb.info/gnd/" 
        valueURI="http://d-nb.info/gnd/4077445-4">Silicium</mods:topic>
</mods:subject>
```

## Persistent Identifier

The attribute `contentIDs` of the zvdd/DFG Viewer METS format is automatically generated during METS export if the metadata `_urn` exists for the structural unit.

