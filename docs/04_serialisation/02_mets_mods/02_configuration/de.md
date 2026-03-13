---
title: Allgemeine Konfiguration
published: true
keywords:
    - UGH
    - METS
    - MODS
---

Die Serialisierung in METS/MODS mit der Klasse MetsMods bedarf keiner weiteren Konfiguration, nahezu alle Tags innerhalb des `<METS>` Tags in der `Format`-Sektion des Regelsatzes beziehen sich ausschließlich auf die Klasse `MetsModsImportExport`. Lediglich der Tag `<AnchorIdentifierMetadataType>` muss hier vorhanden sein.

_Beispiel: Die notwendige Konfiguration für die Speicherung per MetsMods_

```xml
<METS>
      <AnchorIdentifierMetadataType>
            CatalogIDDigital
      </AnchorIdentifierMetadataType>
</METS>
```

Da das Metadatenformat innerhalb von METS (`MetsModsImportExport`) recht flexibel ist, müssen zunächst einige grundlegende Dinge konfiguriert werden. Diese befinden sich direkt unterhalb des umschließenden `<METS>` Elements.

## NamespaceDefinition

Das Element `<NamespaceDefinition>` enthält eine Namespace-Definition, die zusätzlich zu den von UGH sowieso genutzten Namespaces im MODS-Mapping benötigt wird. Die Namespaces und auch die SchemaLocations (falls vorhanden) von `METS`, `MODS`, `XLINK`, `GOOBI`, `XSI` und `DV` sind bereits in der Klasse MetsMods definiert und werden – falls im Regelsatz vorhanden – mit den hier definierten Werten überschrieben. Vorsicht ist hier geboten, sollte eine andere Version eines bereits in UGH definierten Schemas im Regelsatz geändert werden, denn im Allgemeinen ist UGH hier auf eine bestimmte Version festgelegt. Als Elemente enthält `<prefix>` die Definition des Prefixes, das Element `<URI>` die URI des Namespaces, und schließlich gibt das Element `<schemaLocation>` die URI der XML-Schema-Datei an, gegen die validiert werden soll.

_Beispiel: Definition eines zusätzlichen im MODS-Mapping genutzten Namespaces_

```xml
<NamespaceDefinition>
      <URI>http://zvdd.gdz-cms.de/</URI>
      <prefix>zvdd</prefix>
      <schemaLocation>
            http://pfad/zu/schema/xsd/zvdd.xsd
      </schemaLocation>
</NamespaceDefinition>
```

## Der Link zur Anker-Datei

Eine Besonderheit stellt die Konfiguration der Anker dar: Wenn Anker in einer separaten Datei gespeichert werden, muss ein Metadatum zur Referenzierung auf diesen Anker vorhanden sein. Dieses Metadatum enthält den Wert des entsprechenden Identifiers, welches den Anker eindeutig referenziert. Dieses Metadatum wird in einem entsprechenden XML-Feld innerhalb des Metadatensatzes gespeichert.

Das Element `<XPathAnchorQuery>` definiert das Element innerhalb der Metadaten (MODS), dessen Inhalt der Identifier der Anker-Datei ist. Die Anker-Datei ist in einer zusätzlichen METS-Datei gespeichert, die separat gespeichert und dann auch wieder geladen wird. Die Angabe ist als XPath-Query eingetragen.

_Beispiel: Ein XPath zum Anker-Datei-Identifier_

```xml
<XPathAnchorQuery> 
./mods:mods/mods:relatedItem[@type='host']/mods:recordInfo/mods:reco
rdIdentifier
     [@source='gbv-ppn']
</XPathAnchorQuery>
```

Das folgende Beispiel beschreibt den XPath zu dem Element `<recordIdentifier>` mit dem Wert `PPN123456789`. Hier ein Auszug aus einer METS-Datei einer untergeordneten Struktur (Der Inhalt des Attributs `source` kommt aus dem Metadatenmapping des Metadatums `CatalogIDDigital`, dazu jedoch später):

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

Hier die entsprechende Stelle in der Anker METS-Datei:

```xml
<mods:recordInfo>
    <mods:recordIdentifier source="gbv-ppn">
        PPN123456789
    </mods:recordIdentifier>
</mods:recordInfo>
```

## Der Anker Identifier Metadatentyp

Das Element `<AnchorIdentifierMetadataType>` beschreibt den internen Metadatentyp desjenigen Elements, das als Anker-Identifier dienen soll. In unserem Beispiel das Element `CatalogIDDigital` mit dem Wert `PPN123456789`.

_Beispiel: Ein Anker Identifier Metadatentyp_

```xml
<AnchorIdentifierMetadataType>
      CatalogIDDigital
</AnchorIdentifierMetadataType>
```

Diese Referenzierung von Ankerdatei (übergeordnete Dokumentstruktur) und nachfolgender Dokumentstruktur ist im oben genannten METS-Profil definiert (siehe „dmdSec Anforderung 4: Hierarchische Verknüpfung von Dokumenten mittels MODS“).

Soll der Inhalt des Anker-Metadatums per regulärem Ausdruck verändert werden, ist dies mit dem Tag `<ValueRegExp>` an dieser Stelle möglich (zu regulären Ausdrücken siehe weiter unten).

