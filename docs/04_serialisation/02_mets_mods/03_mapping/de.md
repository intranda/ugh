---
title: Konfiguration des Mappings von Struktur- und Metadaten
published: true
---

## Dokumentstrukturen und Metadaten

Das Mapping von Dokumentstrukturtypen des Dokumentmodells – also die im Regelsatz definierten Strukturtypen – zu METS-Strukturtypen ist im unteren Teil der METS-Sektion definiert. Die Elemente `<DocStruct>` definieren dieses Mapping. Werden hier Elemente nicht erwähnt, wird der Name des internen Strukturtyps auch im METS-Dokument verwendet. Ein Mapping ist vor allem für die Darstellung im [DFG-Viewer](https://dfg-viewer.de/) nötig (physische Strukturtypen), sowie für ein Mapping zum Beispiel nach [ZVDD](http://zvdd.de/) – hier müssen interne Strukturtypen auf die in ZVDD vorhandenen gemappt werden. Innerhalb des Elements `<DocStruct>` muss es folgende zwei Unterelemente geben:

| Unterelemente |  |
| :--- | :--- |
| `<InternalName>` | Interner Name des Dokumentstrukturtyps |
| `<MetsType>` | Der Name, der im Attribut type des &lt;mets:div&gt; Elements erscheinen soll |

_Beispiel: Mapping von Dokumentstrukturtypen (physische Struktur)_

```xml
<DocStruct>
      <InternalName>BoundBook</InternalName>
      <MetsType>physSequence</MetsType>
</DocStruct>
```

_Auszug aus der METS-Datei (Ausschnitt):_

```xml
<mets:structMap TYPE="PHYSICAL">
      <mets:div type="physSequence"/>
</mets:structMap>
```

Das Metadatenmapping gestaltet sich etwas komplizierter, da MODS als zu unterstützendes Metadatenformat in sich stark strukturiert ist. Um diese Struktur abbilden zu können, wird sich gängiger XML-Standards bedient. Das Mapping eines XML-Elements auf einen internen Metadatentyp und umgekehrt wird mittels eines XPath-Ausdrucks definiert. Dieser XPath-Ausdruck enthält den relativen Pfad ausgehend vom Element `<mets:xmlData>`. Hierbei sollte dieser XPath-Ausdruck das Element auswählen, welches den Wert des Metadatums zum Inhalt hat.

Für den Import wird das Mapping eines MODS-Elements per XPath auf einen internen Metadatentyp gemappt, wobei das Element `<XPath>` genutzt wird. Für den Export wird der Inhalt eines internen Metadatums in das MODS-Element geschrieben, das per `<WriteXPath>` definiert wird.

## Mapping für den MODS-Export – Metadaten

Für das Schreiben von XML-Dateien kann lediglich ein Subset von XPath verwendet werden, das im Folgenden erläutert wird. Hierdurch kann es vorkommen, dass der XPath-Ausdruck zum Schreiben von Metadaten (Export) von dem abweicht, der zum Lesen (Import) von Metadaten genutzt wird. Daher werden die beiden Elemente `<XPath>` und `<WriteXPath>` unterschieden, die jeweils als Unterelement eines `<Metadata>` Elements genutzt werden. Pro internem Metadatum können Mappings auch mehrmals erfolgen, siehe hierzu auch die weiter unten angeführten Beispiele zu bedingten Mappings sowie der Manipulation von Metadatenwerten per regulären Ausdrücken.

Für das `<WriteXPath>` Element gelten folgende Bedingungen:

* Der Schreibausdruck muss zwingend mit `./` beginnen und anschließend den kompletten Pfad vom `<xmlData>` Element ausgehen enthalten. Für MODS bedeutet dies, dass selbst das umschließende `<mods>` Element definiert wird. Das hat damit zu tun, dass beim internen Aufbau der XML-Struktur entsprechende Elemente angelegt werden, sollten sie noch nicht vorhanden sein.
* Die Hierarchie von Elementen im XPath können nur durch `/` getrennt werden.
* Elemente können auf jeder Hierarchiestufe gefiltert werden. Der Filterausdruck muss zwischen eckigen Klammern stehen `[]`. Beim Anlegen werden die entsprechenden Elemente und Attribute, die in diesem Filterausdruck definiert werden, ebenfalls angelegt.
* Attribute werden durch ein vorangestellten Klammeraffen `@` als solche gekennzeichnet.
* Innerhalb von Filterausdrücken kann einem Element oder einem Attribut ein Wert zugewiesen werden. Diese Zuweisungen sind direkt hinter den Attribut- oder Elementnamen zu schreiben, mit einfachen Anführungszeichen `’` zu umschließen und von dem Namen mit einem Gleichheitszeichen `=` zu trennen. Eine gültige Zuweisung wäre bspw. `@attribut=’wert’` oder aber auch `element=’wert’`.
* Außerhalb von einfachen Anführungszeichen wird z.B. das Zeichen `/` als Trennzeichen der einzelnen Tags verwendet, innerhalb einfacher Anführungszeichen sind auch Sonderzeichen erlaubt.
* Zuweisungen außerhalb von Filterausdrücken sind nicht zulässig, da der Wert ja durch den Wert des zugewiesenen Metadatums bestimmt wird.
* Existieren mehrere Filterausdrücke pro Element, so sind diese in zwei separaten Klammern unterzubringen. Es wird eine UND-Verknüpfung angenommen. Ein gültiger Ausdruck wäre also zum Beispiel `./element[@attribut1=’1’][@attribut2=’2’]`.
* Eine gleichzeitige Zuweisung von Werten zu Elementen und Attributen ist möglich durch die Angabe `=’wert’` direkt hinter den Attribut-Zuweisungen. Beispiel: `./element[@attribut1=’1’][@attribut2=’2’]=’wert’`.
* Weitere Funktionen wie beispielsweise `not` werden für das Schreiben ignoriert.

Zu beachten ist im allgemeinen, dass die im XPath-Ausdruck verwendeten Elementnamen des Datenformats entsprechend der Einstellungen in den `<NamespaceDefinition>` Elementen des Regelsatzes über einen Prefix verfügen können, falls sich die angestrebten Elemente nicht im Default-Namespace befinden.

_Beispiel: XPath-Ausdrücke für das Mapping_

```xml
<Metadata>
    <InternalName>TitleDocSub</InternalName>
    <WriteXPath>
        ./mods:mods/mods:titleInfo/mods:subTitle
    </WriteXPath>
</Metadata>
```

Grundsätzlich berücksichtigt das System beim Anlegen von Elementen entsprechend des XPath- Ausdruck die bereits vorhandene XML-Struktur. Diese enthält natürlich auch alle bereits angelegten Elemente anderer und derselben Metadaten. Dabei geht das System so vor, dass es sich Element für Element den Pfad hinunter hangelt, um fehlende Elemente am Ende des Pfades anzulegen. Für den obigen Pfad im obigen Beispiel prüft UGH, ob ein Element `<mods>` bereits vorhanden ist; falls nicht, wird ein solches angelegt. Falls doch, wird das nächste Element als Kind- Element des `<mods>` Elements überprüft. In diesem Fall also `<titleInfo>`. Ist ein solches Element nicht vorhanden, wird es immer als Kind des bereits vorhandenen Elements angelegt, und so weiter und so fort. Dieses Vorgehen funktioniert prächtig, solange es nur ein Metadatum vom selben Typ pro Dokumentstruktur gibt. Gibt es mehrere davon, würde dieses Vorgehen dazu führen, dass entsprechende Elemente gerade nicht mehr angelegt werden, da der entsprechende Pfad im DOM-Baum ja bereits für das erste Metadatum angelegt wurde.

Wenn es also mehrere gleiche Metadatentypen pro Dokumentstruktur gibt, wird das Hash-Zeichen `#` genutzt, um zu kennzeichnen, dass ein jedes davon angelegt wird und somit einen eigenen Teilbaum in der XML-Struktur bekommt. Dieses Zeichen darf sich ausschließlich im `<WriteXPath>` Element befinden, es gilt also nur zum Schreiben. Es kennzeichnet die Stelle im XPath-Ausdruck, an der mit der Überprüfung des Pfads gestoppt wird und ein neuer Teilpfad angelegt werden soll. Wären jetzt zum Beispiel zwei Untertitel für eine Dokumentstruktur vorhanden, die nach obigen XPath-Ausdruck MODS-konform geschrieben werden sollen, so muss der XPath-Ausdruck zum Schreiben wie folgt aussehen:

```xml
./mods:mods/mods:titleInfo/#mods:subTitle
```

Dies führt dazu, dass das Element `<subTitle>` innerhalb von `<titleInfo`&gt; für jedes entsprechende Metadatum wiederholt angelegt wird. Wichtig ist, dass das Hash-Zeichen nur zu Beginn eines Elementnamens stehen darf, das heißt, es muss sich auch vor dem Prefix des entsprechenden Namespaces befinden. Stünde der `#` vor dem Element `mods:titleInfo`, würde für jedes Metadatum `TitleDocSub` ein solcher Teilbaum im XML-Dokument angelegt.

_Beispiel: Verwendung von Attributen in Filtern_

```xml
<Metadata>
    <InternalName>singleDigCollection</InternalName>
    <WriteXPath>
        ./mods:mods/#mods:classification[@authority='ZVDD']
    </WriteXPath>
</Metadata>
```

_... erzeugt die folgende XML-Struktur..._

```xml
<mods:mods>
    <mods:classification authority="ZVDD">
        VD17-nova
    </mods:classification>
</mods:mods>
```

Soll ein Attribut mit dem Inhalt des Metadatenfeldes erzeugt werden, wird an das Ende des Xpath- Ausdrucks ein `/@` mit folgendem Metadatennamen angehängt. Für alle nicht-MODS Namespaces sind hier die Namespace-Präfixe mit anzugeben - zum Beispiel `/@slub:displayLabel`.

_Beispiel: Verwendung von Attributen zum Speichern des Wertes_

```xml
<Metadata>
    <InternalName>CurrentNoSorting</InternalName>
    <WriteXPath>
        ./mods:mods/#mods:part[@type='host']/@order
    </WriteXPath>
</Metadata>
```

_...und..._

```xml
<Metadata>
    <InternalName>CurrentNo</InternalName>
    <WriteXPath>
        ./mods:mods/mods:part/mods:detail/mods:number
    </WriteXPath>
</Metadata>
```

_...erzeugen die XML-Struktur..._

```xml
<mods:mods>
    <mods:part order="100" type="host">
        <mods:detail>
            <mods:number>1</mods:number>
        </mods:detail>
    </mods:part>
</mods:mods>
```

MODS Elemente können durch Anhängen von `[]` an den Elementnamen und einer darin enthaltenen Gruppierungsnummer gruppiert werden. Alle Elemente mit derselben Gruppierungsnummer werden gemeinsam in ein Element geschrieben. So ist es zum Beispiel möglich, im Regelsatz zwei verschiedene `<originInfo>` Elemente zu definieren - eines für das Digitalisat und eines für das originale Werk.

_Beispiel: Gruppierung von MODS-Elementen_

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

_...und..._

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

_...erzeugen die XML-Struktur..._

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

_Beispiel: Gruppierung durch eine Gruppe von Metadaten_

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

_...erzeugt die folgende XML-Struktur..._

```xml
<mods:titleInfo>
    <mods:nonSort>Die</mods:nonSort>
    <mods:title>
        Bau- und Kunstdenkmäler im Regierungsbezirk Cassel
    </mods:title>
    <mods:subTitle>Kreis Gelnhausen</mods:subTitle>
</mods:titleInfo>
```

Die Gruppe `<WriteXPath>` Definition innerhalb des `<Group>` Elementes erzeugt den Grundpfad. Von da an wird der relative Pfad durch jedes `<WriteXPath>` Element für die einzelnen Metadaten gebildet. Das mappen von Personendaten ist hier ebenfalls möglich und wird im [Abschnitt Mapping für den MODS-Export - Personen](https://docs.intranda.com/ugh-de/4/4.2/4.2.4) genauer erläutert.

Die Elemente `<ValueCondition>` und `<ValueRegExp>` können - genau wie beim PICA+ Import - zur bedingten Zuweisung und Manipulation von Metadatenwerten genutzt werden, beispielsweise zum Entfernen der der PPN vorangestellten Zeichenkette `PPN` oder generell zur Bildung von komplexeren Ausdrücken. Auch hier geschieht dies durch die Nutzung von regulären Ausdrücken in Perl5-Syntax.

_Beispiel: Bildung einer PURL aus der CatalogIDDigital_

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

_Beispiel: Entfernen der vorangestellten Zeichenfolge „PPN“_

```xml
<Metadata>
    <InternalName>CatalogIDDigital</InternalName>
    <ValueRegExp>s/^PPN(.*)/$1/</ValueRegExp>
    <WriteXPath>
        ./mods:mods/mods:recordInfo/#mods:recordIdentifier[@source='gbv-ppn']
    </WriteXPath>
</Metadata>
```

_Beispiel: Durch Präfix bedingtes Mapping eines Identifier-Metadatums_

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

Das letzte Beispiel zeigt ein bedingtes Mapping, das je nach Präfix des Wertes des internen Metadatums `CatalogFieldVDid` entweder den Typ `vd17` oder `vd18` erzeugt.

## Mapping für den MODS-Export – Personen

Da Personen weitere Merkmale haben, müssen diese Merkmale ebenfalls per XPath geschrieben werden können. Dazu können dem `<WriteXpath>` Element der Regelsatzdatei weitere Elemente folgen. Diese selektieren entsprechende Elemente ausgehend von dem Element, welches durch den in dem `<WriteXpath>` Element angegebenen Ausdruck ausgewählt wurde. Derzeit können die folgenden Elemente für den Export genutzt werden:


| Element | Beschreibung |
| :--- | :--- |
| `<FirstnameXPath>` | Selektiert das Feld, in das der Vorname der Person geschrieben werden soll. |
| `<LastnameXPath>` | Selektiert das Feld, in das der Nachname der Person geschrieben werden soll. |
| `<DisplayNameXPath>` | Selektiert das Feld, in das der Name der Person geschrieben werden soll, der zur Anzeigen dient (hier wird, wenn kein Metadatum dafür existiert, der Name aus `Nachname` und `Vorname` aggregiert, in der Form `Nachname, Vorname`). Für den Export wird in dieses Feld der Name der Person nach dem Muster `Nachname, Vorname` eingetragen, falls kein anderer Wert existiert. |
| `IdentifierXPath` | Selektiert das Feld, in das der Identifier der Person (im Beispiel eine ID aus der Personen-Norm-Datei PND) geschrieben werden soll. Diese Funktion ist noch direkt in die UGH Bibliothek integriert. Eine Nutzung ist momentan nur wie im folgenden Beispiel möglich: <br />`<IdentifierXPath>` <br /> `../mods:name[@authority=&apos;pnd&apos;][@ID=&apos;&apos;`<br />`</IdentifierXPath>` |


Eine allgemein exaktere Abbildung der folgenden noch vorhandenen Attribute von Personen aus dem Dokumentmodell nach MODS ist geplant: `affiliation`, `institution`, `identifierType`, `role`, `personType` und `isCorporation`.

_Beispiel: Generierung von Personen_

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

_...erzeugt folgende XML-Struktur..._

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

Besitzt eine Dokumentstruktur ein Metadatum mit Namen `TitleDocMain`, so wird dieser Titel als `LABEL` dieser Struktur in der `StructMap` eingetragen. Dies soll in zukünftigen Versionen der UGH Bibliothek konfigurierbar sein.

_Beispiel: Verwendung des Titels als Label für die StructMap (unvollständiger Ausschnitt)_

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

## Mapping für den MODS-Export – Körperschaften

Da Körperschaften genau wie Personen weitere Merkmale haben, müssen diese Merkmale ebenfalls per XPath geschrieben werden können. Dazu können dem `<WriteXpath>` Element der Regelsatzdatei weitere Elemente folgen. Diese selektieren entsprechende Elemente ausgehend von dem Element, welches durch den in dem `<WriteXpath>` Element angegebenen Ausdruck ausgewählt wurde. Derzeit können die folgenden Elemente für den Export genutzt werden:

| Element | Beschreibung |
|-- |-- |
| `<MainNameXPath>` | Definiert das Feld, in das der Hauptname geschrieben werden soll. |
| `<SubNameXPath>` | Definiert das Feld, in das die weiteren Namen geschrieben werden sollen. Für jeden Wert wird ein eigenes Feld erzeugt. |
| `<PartNameXPath>` | Definiert das Feld, in das die Zählungen, Orte und Daten geschrieben werden sollen. |

Beispiel: Generierung von Körperschaften

```xml
<Metadata>
    <InternalName>IssuingBody</InternalName>
    <WriteXPath>./mods:mods/#mods:name[@type='corporate'][mods:role/mods:roleTerm="isb"[@authority='marcrelator'][@type='code']]</WriteXPath>
    <MainNameXPath>./mods:namePart</MainNameXPath>
    <SubNameXPath>./mods:namePart</SubNameXPath>
    <PartNameXPath>./mods:namePart</PartNameXPath>
</Metadata>
```

...erzeugt folgende XML-Struktur...

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

## Metadatenmapping für den Import

Wie bereits in der Einleitung erläutert, ist ein Import von METS-Dateien, dessen Metadaten im Goobi-Namespace gespeichert wurden, ohne Probleme möglich. Die Klasse `ugh.fileformats.mets.MetsMods` implementiert lesenden und schreibenden Zugriff des Interfaces `Fileformat`.

Für den Import von METS-Dateien mit Metadaten im DFG-Viewer MODS-Format muss ein eindeutiges Mapping von MODS-Metadaten zu den internen Metadatentypen des Dokumentmodells existieren. Da ein 1:1-Mapping von beliebigen internen Metadatentypen nach MODS – vor allem bei bereits vorhandenen Daten und Regelsätzen – oft nicht möglich ist, da MODS im Gegensatz zu den internen Metadatentypen beschränkt ist in seinen beschreibenden Möglichkeiten, wäre ein Import von solchen exportierten Dateien mit einem Mapping der MODS- Metadaten zu den bestehenden internen Metadatentypen nicht mehr eindeutig möglich. Daher ist der lesender Zugriff auf METS-Dateien mit MODS-Metadaten im DFG-Viewer MODS-Format bereits implementiert, jedoch noch nicht aktiviert in der Klasse `ugh.fileformats.mets.MetsModsImportExport`.
