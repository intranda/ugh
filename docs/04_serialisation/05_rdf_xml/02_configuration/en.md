---
title: Configuration
published: true
---

The configuration of the format takes place exclusively in the enclosing \`' element.

Structure types are simply mapped 1:1. Therefore, the `<DocStrct>' element only includes the two subelements`' and `<RDFName>'.contains the internal name of the structure type as defined in the rule set above.is the name of the structure type as it is stored in the TYPE attribute of the`\` element in the RDF/XML file.

Metadata mapping is very similar in principle, but is somewhat extended due to the structure of RDF/XML: The `<Metadata>' element contains the configuration for a metadata type. The internal name is defined in the element '`', the corresponding name of the XML element in the XML file in the element '\`'.

_Example: Configuration of metadata mapping for RDF/XML_

```xml
<Metadata>
      <Name>TitleDocMain</Name>
      <RDFName>AGORA:TitleDocMain</RDFName>
</Metadata>
```

_Example: Conversion in the RDF/XML file_

```xml
<AGORA:DocStrct AGORA:Type="AGORA:Monograph">
    <AGORA:TitleDocMain>Juristenzeitung</AGORA:TitleDocMain>
</AGORA:DocStrct>
```

In addition to the 1:1 mapping, metadata can also be stored in `<RDF:Bag>` or `<RDF:Seq>` elements. This can be adjusted using the attributes 'rdfList' and 'rdfListType'. `rdfList` contains the name of the enclosing XML element, which contains a `<RDF:Bag>' or`' element. The actual metadata is then stored as a separate element within the \`' element.

_Example: RDF List Configuration_

```xml
<Metadata rdfList="AGORA:ListOfCreators" rdfListType="seq">
    <Name>IllustratorArtist</Name> <RDFName>AGORA:Illustrator</RDFName>
</Metadata>
```

_Example: Conversion in the RDF/XML file_

```xml
<AGORA:ListOfCreators>
      <RDF:Seq>
            <RDF:Li>
                  <AGORA:Author>
                  <AGORA:CreatorLastName>Meier</AGORA:CreatorLastName>
                  <AGORA:CreatorFirstName>T</AGORA:CreatorFirstName>
                  </AGORA:Author>
            </RDF:Li>
      </RDF:Seq>
</AGORA:ListOfCreators>
```

As can be seen from the example, the RDF lists are particularly useful for people, since only in them is a distinction made between the first and last name of the person.

In theory, n:1 mapping is also possible for reading the data, i.e. different RDF/XML types are mapped to the same internal metadata type. However, the first mapping definition is always used for writing, so only 1:1 mapping is possible here.

