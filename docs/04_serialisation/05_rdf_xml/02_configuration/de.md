---
title: Konfiguration
published: true
---

Die Konfiguration des Formats findet ausschließlich im umschließenden `<RDF>` Element statt.

Strukturtypen werden einfach 1:1 gemappt. Daher umfasst das `<DocStrct>` Element auch lediglich die beiden Unterelemente `<Name>` und `<RDFName>`. `<Name>` enthält den internen Namen des Strukturtyps wie im Regelsatz oben definiert. `<RDFName>` ist der Name des Strukturtyps, so wie er im Attribut TYPE des Elements `<DocStruct>` in der RDF/XML-Datei gespeichert wird.

Das Metadaten-Mapping ist prinzipiell sehr ähnlich, jedoch aufgrund der Struktur von RDF/XML etwas erweitert: Das `<Metadata>` Element umfasst die Konfiguration für einen Metadatentyp. Der interne Name wird im Element `<Name>` festgelegt, der entsprechende Name des XML- Elements in der XML-Datei in dem Element `<RDFName>`.

_Beispiel: Konfiguration des Metadatenmappings für RDF/XML_

```xml
<Metadata>
      <Name>TitleDocMain</Name>
      <RDFName>AGORA:TitleDocMain</RDFName>
</Metadata>
```

_Beispiel: Umsetzung in der RDF/XML Datei_

```xml
<AGORA:DocStrct AGORA:Type="AGORA:Monograph">
    <AGORA:TitleDocMain>Juristenzeitung</AGORA:TitleDocMain>
</AGORA:DocStrct>
```

Als Ergänzung zu dem 1:1 Mapping können Metadaten auch in `<RDF:Bag>` oder `<RDF:Seq>` Elementen gespeichert werden. Dies kann mittels der Attribute `rdfList` und `rdfListType` angepasst werden. `rdfList` enthält dabei den Namen des umschliessenden XML-Elementes, in denen ein `<RDF:Bag>` oder `<RDF:Seq>` Element enthalten ist. Das eigentliche Metadatum wir dann als separates Element innerhalb des `<RDF:Li>` Elements gespeichert.

_Beispiel: RDF-List Konfiguration_

```xml
<Metadata rdfList="AGORA:ListOfCreators" rdfListType="seq">
    <Name>IllustratorArtist</Name> <RDFName>AGORA:Illustrator</RDFName>
</Metadata>
```

_Beispiel: Umsetzung in der RDF/XML-Datei_

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

Wie an dem Beispiel zu sehen ist, sind die RDF-Listen vor allem für Personen sinnvoll, da nur in Ihnen zwischen Vor- und Nachnamen der Person unterschieden wird.

Für das Einlesen der Daten ist theoretisch auch ein n:1 Mapping möglich, das heißt, unterschiedliche RDF/XML-Typen werden auf denselben internen Metadatentyp gemappt. Für das Schreiben wird jedoch immer die erste Mappingdefinition verwendet, hier ist also nur ein 1:1 Mapping möglich.

