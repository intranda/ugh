---
title: Configuration of the mapping of structure and metadata
published: true
---

## Document structures and metadata

The mapping of document structure types of the document model - that is, the structure types defined in the rule set - to METS structure types is defined in the lower part of the METS section. The elements `<DocStruct>` define this mapping. If elements are not mentioned here, the name of the internal structure type is also used in the METS document. A mapping is necessary especially for the representation in the [DFG-Viewer](https://dfg-viewer.de/) (physical structure types), as well as for a mapping for example to [ZVDD](http://zvdd.de/) - here, internal structure types must be mapped to those existing in ZVDD. Within the element `<DocStruct>` there must be the following two subelements:

| Subelements |  |
| :--- | :--- |
| `<InternalName>` | Internal name of the document structure category |
| `<MetsType>` | The name to appear in the type attribute of the &lt;mets:div&gt; element |

_Example: Mapping of document structure types (physical structure)_

```xml
<DocStruct>
      <InternalName>BoundBook</InternalName>
      <MetsType>physSequence</MetsType>
</DocStruct>
```

_Extract from the METS file (extract):_

```xml
<mets:structMap TYPE="PHYSICAL">
      <mets:div type="physSequence"/>
</mets:structMap>
```

Metadata mapping is somewhat more complicated because MODS is a highly structured metadata format to be supported. To be able to map this structure, common XML standards are used. The mapping of an XML element to an internal metadata type and vice versa is defined using an XPath expression. This XPath expression contains the relative path starting from the element `<mets:xmlData>`. This XPath expression should select the element that contains the metadata value.

For the import, the mapping of a MODS element is mapped via XPath to an internal metadata type using the element `<XPath>`. For export, the content of an internal metadata is written to the MODS element defined by `<WriteXPath>`.

## Mapping for MODS export - Metadata

Only a subset of XPath can be used for writing XML files, which is explained below. This can cause the XPath expression used to write metadata (Export) to differ from the one used to read (Import) metadata. Therefore, the two elements `<XPath>` and `<WriteXPath>` are distinguished, each of which is used as a subelement of a `<Metadata>` element. Mappings can also be performed several times per internal metadata, see also the examples of conditional mappings and the manipulation of metadata values by regular expressions listed below.

The following conditions apply to the `<WriteXPath>` element:

* The write expression must necessarily start with `./` and then contain the complete path from the `<xmlData>` element. For MODS this means that even the enclosing `<mods>` element is defined. This has to do with the fact that during the internal setup of the XML structure, corresponding elements are created if they are not yet available.
* The hierarchy of elements in XPath can only be separated by `/`.
* Elements can be filtered at any level of the hierarchy. The filter expression must be placed between square brackets `[]`. When creating, the corresponding elements and attributes defined in this filter expression are also created.
* Attributes are identified as such by a preceding spider monkey `@`.
* Within filter expressions a value can be assigned to an element or an attribute. These assignments must be written directly after the attribute or element name, enclosed in single quotes `''` and separated from the name with an equal sign `=`. A valid assignment would be, for example, `@attribute='value'` or `element='value'`.
* Outside of single quotation marks, for example, the character `/` is used as a separator between the individual tags; within single quotation marks, special characters are also allowed.
* Assignments outside of filter expressions are not allowed, since the value is determined by the value of the assigned metadata.
* If there are several filter expressions per element, they must be placed in two separate brackets. An AND operation is assumed. For example, a valid expression would be `./element[@attribut1='1'][@attribut2='2']`.
* Simultaneous assignment of values to elements and attributes is possible by specifying `='value'` directly after the attribute assignments. Example: `./element[@attribut1='1'][@attribut2='2']='value'`.
* Other functions such as `not` are ignored for writing

Note in general that the element names of the data format used in the XPath expression may have a prefix according to the settings in the `<NamespaceDefinition>` elements of the ruleset if the targeted elements are not in the default namespace.

_Example: XPath expressions for the mapping_

```xml
<Metadata>
    <InternalName>TitleDocSub</InternalName>
    <WriteXPath>
        ./mods:mods/mods:titleInfo/mods:subTitle
    </WriteXPath>
</Metadata>
```

Basically, the system takes the existing XML structure into account when creating elements according to the XPath expression. Of course, it also contains all elements of other and the same metadata that have already been created. The system proceeds in such a way that it shuffles down the path element by element to create missing elements at the end of the path. For the above path in the above example, UGH checks whether an element `<mods>` already exists; if not, it creates one. If it does, the next element is checked as the child element of the `<mods>` element. In this case this would be `<titleInfo>`. If such an element is not present, it is always created as a child of the existing element, and so on and so forth. This works fine as long as there is only one metadata of the same type per document structure. If there are several of them, this procedure would lead to the fact that corresponding elements are no longer created, since the corresponding path in the DOM tree has already been created for the first metadata.

So if there are several identical metadata types per document structure, the hash character `#` is used to indicate that each of them is created and thus gets its own subtree in the XML structure. This character may only be located in the `<WriteXPath>` element, so it is only valid for writing. It marks the position in the XPath expression where the path check stops and a new subpath is to be created. For example, if there were now two subtitles for a document structure that should be written MODS-compliant according to the XPath expression above, the XPath expression for writing must look like this:

```xml
./mods:mods/mods:titleInfo/#mods:subTitle
```

This results in the element `<subTitle>` within `<titleInfo`&gt; being created repeatedly for each corresponding metadata. It is important that the hash character may only be at the beginning of an element name, i.e. it must also be before the prefix of the corresponding namespace. If the `#` were placed before the element `mods:titleInfo`, such a subtree would be created in the XML document for each metadata `TitleDocSub`.

_Example: Using attributes in filters_

```xml
<Metadata>
    <InternalName>singleDigCollection</InternalName>
    <WriteXPath>
        ./mods:mods/#mods:classification[@authority='ZVDD']
    </WriteXPath>
</Metadata>
```

_... generates the following XML structure ..._

```xml
<mods:mods>
    <mods:classification authority="ZVDD">
        VD17-nova
    </mods:classification>
</mods:mods>
```

If an attribute with the content of the metadata field is to be created, a `/@` with the following metadata name is appended to the end of the Xpath expression. For all non-MODS namespaces, the namespace prefixes must be specified here - for example, `/@slub:displayLabel`.

_Example: Using attributes to save the value_

```xml
<Metadata>
    <InternalName>CurrentNoSorting</InternalName>
    <WriteXPath>
        ./mods:mods/#mods:part[@type='host']/@order
    </WriteXPath>
</Metadata>
```

_...and..._

```xml
<Metadata>
    <InternalName>CurrentNo</InternalName>
    <WriteXPath>
        ./mods:mods/mods:part/mods:detail/mods:number
    </WriteXPath>
</Metadata>
```

_...generate the XML structure..._

```xml
<mods:mods>
    <mods:part order="100" type="host">
        <mods:detail>
            <mods:number>1</mods:number>
        </mods:detail>
    </mods:part>
</mods:mods>
```

MODS elements can be grouped by appending `[]` to the element name and a grouping number contained therein. All elements with the same grouping number are written together in one element. For example, it is possible to define two different `<originInfo>` elements in the rule set - one for the digitised material and one for the original work.

_Example: Grouping MODS elements_

```xml
<Metadata>
    <InternalName>PublisherName</InternalName>
    <WriteXPath>
        ./mods:mods/mods:originInfo[1]/#mods:publisher
    </WriteXPath>
</Metadata>
<Metadata>
    <InternalName>PlaceOfPublication</InternalName>
    <WriteXPath>
        ./mods:mods/mods:originInfo[1]/#mods:place/mods:placeTerm[@type='text']
    </WriteXPath>
</Metadata>
```

_...and..._

```xml
<Metadata>
     <InternalName>_placeOfElectronicOrigin</InternalName>
     <WriteXPath>
          ./mods:mods/mods:originInfo[2]/#mods:place/mods:placeTerm[@type='text']
     </WriteXPath>
</Metadata>
<Metadata>
     <InternalName>_dateDigitization</InternalName>
     <WriteXPath>
          ./mods:mods/mods:originInfo[2]/#mods:dateCaptured[@encoding='w3cdtf']
     </WriteXPath>
</Metadata>
```

_...generate the XML structure..._

```xml
<mods:originInfo>
    <mods:publisher>Tanzer</mods:publisher>
    <mods:place>
        <mods:placeTerm type="text">Grätz</mods:placeTerm>
    </mods:place>
</mods:originInfo>
<mods:originInfo>
    <mods:place>
        <mods:placeTerm type="text">Göttingen</mods:placeTerm>
    </mods:place>
    <mods:dateCaptured encoding="w3cdtf">2009</mods:dateCaptured>
</mods:originInfo>
```

_Example: Grouping by a group of metadata_

```xml
<Group>
    <InternalName>Title</InternalName>
    <WriteXPath>./mods:mods/#mods:titleInfo</WriteXPath>
    <Metadata>
        <InternalName>NonSort</InternalName>
        <WriteXPath>./mods:nonSort</WriteXPath>
    </Metadata>
    <Metadata>
        <InternalName>TitleDocMain</InternalName>
        <WriteXPath>./mods:title</WriteXPath>
    </Metadata>
    <Metadata>
        <InternalName>TitleDocSub</InternalName>
        <WriteXPath>./mods:subTitle</WriteXPath>
    </Metadata>
</Group>
```

_...generates the following XML structure..._

```xml
<mods:titleInfo>
    <mods:nonSort>Die</mods:nonSort>
    <mods:title>
        Bau- und Kunstdenkmäler im Regierungsbezirk Cassel
    </mods:title>
    <mods:subTitle>Kreis Gelnhausen</mods:subTitle>
</mods:titleInfo>
```

The group `<WriteXPath>` definition within the `<Group>` element creates the basic path. From then on, the relative path is formed by each `<WriteXPath>` element for the individual metadata. Mapping of personal data is also possible here and is explained in more detail in the section [Mapping for MODS Export - Persons](/en/other/ugh/serialisation/mets_mods/mapping#mapping-for-the-mods-export---persons).

The elements `<ValueCondition>` and `<ValueRegExp>` can be used - just like the PICA+ import - for conditional assignment and manipulation of metadata values, for example to remove the string `PN` preceding the PPN or generally to form more complex expressions. Again, this is done by using regular expressions in Perl5 syntax.

_Example: Creating a PURL from the CatalogIDDigital_

```xml
<Metadata>
    <InternalName>CatalogIDDigital</InternalName>
    <ValueRegExp>
        s/(.*)/http:\/\/resolver\.sub\.uni\-goettingen\.de\/purl\?$1/
    </ValueRegExp>
    <WriteXPath>
        ./mods:mods/mods:identifier[@type='purl']
    </WriteXPath>
</Metadata>
```

_Example: Removing the prefixed character string "PPN_

```xml
<Metadata>
    <InternalName>CatalogIDDigital</InternalName>
    <ValueRegExp>s/^PPN(.*)/$1/</ValueRegExp>
    <WriteXPath>
        ./mods:mods/mods:recordInfo/#mods:recordIdentifier[@source='gbv-ppn']
    </WriteXPath>
</Metadata>
```

_Example: Prefix-related mapping of an identifier metadata_

```xml
<Metadata>
    <ValueCondition>/^VD17/</ValueCondition>
    <InternalName>CatalogFieldVDid</InternalName>
    <WriteXPath>
        ./mods:mods/#mods:identifier[@type='vd17']
    </WriteXPath>
</Metadata>
<Metadata>
    <ValueCondition>/^VD18/</ValueCondition>
    <InternalName>CatalogFieldVDid</InternalName>
    <WriteXPath>
        ./mods:mods/#mods:identifier[@type='vd18']
    </WriteXPath>
</Metadata>
```

The last example shows a conditional mapping that creates either type `vd17` or `vd18` depending on the prefix of the value of the internal metadata `CatalogFieldVDid`.

## Mapping for the MODS export - persons

Since persons have additional characteristics, these characteristics must also be able to be written using XPath. For this purpose, the `<WriteXpath>` element of the rule set file can be followed by other elements. These select corresponding elements starting from the element selected by the expression specified in the `<WriteXpath>` element. Currently the following elements can be used for export:

| Element | Description |
| :--- | :--- |
| `<FirstnameXPath>` | Selects the field in which the first name of the person should be written. |
| `<LastnameXPath>` | Selects the field in which the person's last name should be written. |
| `<DisplayNameXPath>` | Selects the field in which the name of the person to be displayed should be written (here, if no metadata exists for it, the name is aggregated from `surname` and `firstname`, in the form `surname, firstname`) For export purposes, the name of the person is entered in this field in the form `surname, firstname` if no other value exists. |
| `IdentifierXPath` | Selects the field in which the identifier of the person (in the example an ID from the person norm file PND) should be written. This function is still directly integrated in the UGH library. A use is currently only possible as in the following example: <br />`<IdentifierXPath>` <br /> `../mods:name[@authority=&apos;pnd&apos;][@ID=&apos;&apos;`<br />`</IdentifierXPath>` |

A generally more precise mapping of the following still existing attributes of persons from the document model to MODS is planned: `affiliation`, `institution`, `identifierType`, `role`, `personType` and `isCorporation`.

_Example: Generation of persons_

```xml
<Metadata>
      <InternalName>
            Author
      </InternalName>
      <WriteXPath>
            ./mods:mods/#mods:name[@type='personal'] [mods:role/mods:roleTerm="aut" [@authority='marcrelator'][@type='code']]
      </WriteXPath>
      <FirstnameXPath>
            ./mods:namePart[@type='given']
      </FirstnameXPath>
      <LastnameXPath>
            ./mods:namePart[@type='family']
      </LastnameXPath>
      <DisplayNameXPath>
            ./mods:displayForm
      </DisplayNameXPath>
      <IdentifierXPath>
            ../mods:name[@authority='pnd'][@ID='']
      </IdentifierXPath>
</Metadata>
```

_...generates the following XML structure..._

```xml
<mods:name ID="pnd07658111X" authority="pnd" type="personal">
    <mods:role>
        <mods:roleTerm authority="marcrelator" type="code">
            aut
        </mods:roleTerm>
    </mods:role>
    <mods:namePart type="family">Castelli</mods:namePart>
    <mods:namePart type="given">Pietro</mods:namePart>
    <mods:displayForm>Castelli, Pietro</mods:displayForm>
</mods:name>
```

If a document structure has a metadata with the name `TitleDocMain`, this title is entered as `LABEL` of this structure in the `StructureMap`. This will be configurable in future versions of the UGH library.

_Example: Using the title as label for the StructMap (incomplete section)_

```xml
<mets:structMap TYPE="LOGICAL">
    <mets:div LABEL="Allgemeine deutsche Bibliothek" TYPE="Periodical">
        <mets:div LABEL="Allgemeine deutsche Bibliothek" TYPE="PeriodicalVolume">
            <mets:div LABEL="Des ersten Bandes erstes Stück." TYPE="PeriodicalIssue">
                <mets:div LABEL="Inhalt" TYPE="TableOfContents" />
            </mets:div>
        </mets:div>
    </mets:div>
</mets:structMap>
```

## Mapping for the MODS export - Corporations

Since corporate bodies, just like persons, have further characteristics, it must also be possible to write these characteristics via XPath. For this purpose, further elements can follow the `<WriteXpath>` element of the ruleset file. These select corresponding elements based on the element selected by the expression specified in the `<WriteXpath>` element. Currently, the following elements can be used for export:

| Element | Description |
|-- |-- |
| `<MainNameXPath>` | Defines the field in which the main name is to be written. |
| `<SubNameXPath>` | Defines the field in which the additional names are to be written. A separate field is created for each value. |
| `<PartNameXPath>` | Defines the field into which the counts, locations and dates are to be written. |

Example: Generation of corporate bodies

```xml
<Metadata>
    <InternalName>IssuingBody</InternalName>
    <WriteXPath>./mods:mods/#mods:name[@type='corporate'][mods:role/mods:roleTerm="isb"[@authority='marcrelator'][@type='code']]</WriteXPath>
    <MainNameXPath>./mods:namePart</MainNameXPath>
    <SubNameXPath>./mods:namePart</SubNameXPath>
    <PartNameXPath>./mods:namePart</PartNameXPath>
</Metadata>
```

...creates the following XML structure...

```xml
<mods:name type="corporate">
    <mods:role>
        <mods:roleTerm authority="marcrelator" type="code">
            isb
        </mods:roleTerm>
    </mods:role>
    <mods:namePart>Catholic Church.</mods:namePart>
    <mods:namePart>Province of Baltimore (Md.).</mods:namePart>
    <mods:namePart>Provincial Council</mods:namePart>
    <mods:namePart>10th: 1869</mods:namePart>
</mods:name>
```

## Metadata mapping for import

As explained in the introduction, you can easily import METS files whose metadata has been saved in the Goobi namespace. The class `ugh.fileformats.mets.MetsMods` implements read and write access of the interface `Fileformat`.

To import METS files with metadata in DFG Viewer MODS format, a unique mapping of MODS metadata to the internal metadata types of the document model must exist. Since a 1:1 mapping of arbitrary internal metadata types to MODS - especially for already existing data and rule sets - is often not possible, since MODS, in contrast to the internal metadata types, is limited in its descriptive possibilities, an import of such exported files with a mapping of the MODS metadata to the existing internal metadata types would no longer be unambiguously possible. Therefore, read access to METS files with MODS metadata in DFG Viewer MODS format is already implemented, but not yet activated in the class `ugh.fileformats.mets.MetsModsImportExport`.
