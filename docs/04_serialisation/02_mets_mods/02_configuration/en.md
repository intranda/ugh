---
title: General configuration
published: true
---

Serialization in METS/MODS with the MetsMods class does not require any further configuration, almost all tags within the `<METS>` tag in the `Format` section of the ruleset refer exclusively to the `MetsModsImportExport` class. Only the tag `<AnchorIdentifierMetadataType>` must be present here.

_Example: The necessary configuration for storage via MetsMods_

```xml
<METS>
      <AnchorIdentifierMetadataType>
            CatalogIDDigital
      </AnchorIdentifierMetadataType>
</METS>
```

Since the metadata format within METS (`MetsModsImportExport`) is quite flexible, some basic things need to be configured first. These are located directly below the enclosing `<METS>` element.

## NamespaceDefinition

The element `<NamespaceDefinition>` contains a namespace definition which is required in addition to the namespaces used by UGH anyway in the MODS mapping. The namespaces and also the schema locations (if available) of `METS`, `MODS`, `XLINK`, `GOOBI`, `XSI` and `DV` are already defined in the class MetsMods and - if available in the rule set - will be overwritten with the values defined here. Care should be taken here if another version of a schema already defined in UGH is changed in the rule set, because generally UGH is fixed to a specific version here. As elements, `<prefix>` contains the definition of the prefix, the element `<URI>` specifies the URI of the namespace, and finally the element `<schemaLocation>` specifies the URI of the XML Schema file against which validation is to be performed.

_Example: Definition of an additional namespace used in MODS mapping_

```xml
<NamespaceDefinition>
      <URI>http://zvdd.gdz-cms.de/</URI>
      <prefix>zvdd</prefix>
      <schemaLocation>
            http://pfad/zu/schema/xsd/zvdd.xsd
      </schemaLocation>
</NamespaceDefinition>
```

## The link to the anchor file

A special feature is the configuration of the anchors: If anchors are stored in a separate file, a metadata must be available to reference this anchor. This metadata contains the value of the corresponding identifier which uniquely references the anchor. This metadata is stored in a corresponding XML field within the metadata record.

The element `<XPathAnchorQuery>` defines the element within the metadata (MODS) whose content is the identifier of the anchor file. The anchor file is stored in an additional METS file which is saved and reloaded separately. The specification is entered as XPath query.

_Example: An XPath to anchor file identifier_

```xml
<XPathAnchorQuery> 
./mods:mods/mods:relatedItem[@type='host']/mods:recordInfo/mods:reco
rdIdentifier
     [@source='gbv-ppn']
</XPathAnchorQuery>
```

The following example describes the XPath for the `<recordIdentifier>` element with the value `PPN123456789 Here is an excerpt from a METS file of a child structure (The content of the attribute`source`comes from the metadata mapping of the metadata`CatalogIDDigital\`, but more on this later):

```xml
<mods:mods>
    <mods:relatedItem type="host">
        <mods:recordInfo>
            <mods:recordIdentifier source="gbv-ppn">
                PPN123456789
            </mods:recordIdentifier>
        </mods:recordInfo>
    </mods:relatedItem>
</mods:mods>
```

Here is the corresponding location in the anchor METS file:

```xml
<mods:recordInfo>
    <mods:recordIdentifier source="gbv-ppn">
        PPN123456789
    </mods:recordIdentifier>
</mods:recordInfo>
```

## The anchor identifier Metadata type

The element `<AnchorIdentifierMetadataType>` describes the internal metadata type of the element that is to serve as the anchor identifier. In our example the element `CatalogIDDigital` with the value `PN123456789`.

_Example: An anchor identifier Metadata type_

```xml
<AnchorIdentifierMetadataType>
      CatalogIDDigital
</AnchorIdentifierMetadataType>
```

This referencing of anchor file (parent document structure) and subsequent document structure is defined in the above mentioned METS profile (see "dmdSec requirement 4: Hierarchical linking of documents using MODS").

If the content of the anchor metadata is to be changed by a regular expression, this is possible with the tag `<ValueRegExp>` at this point (for regular expressions see below).

