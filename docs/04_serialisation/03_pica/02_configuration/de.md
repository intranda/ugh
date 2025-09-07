---
title: Konfiguration
published: true
keywords:
    - UGH
    - PICA
---

Die PICA+ Konfiguration erfolgt innerhalb des umschließenden `<PicaPlus>` Elements, welches direkt dem `<Formats>` Element untergeordnet ist. Innerhalb des `<PicaPlus>` Elements kommen die typ-spezifischen Elemente `<Person>`, `<Metadata>` und `<DocStruct>` zu Einsatz, die jeweils einen internen Datentyp mappen. Ebenso wie beim RDF/XML-Format findet hier immer ein 1:1 Mapping statt.

Für das Mapping wird auf die Merkmale der PICA+ Struktur zurückgegriffen, innerhalb dieser typ- spezifischen Elemente sind folgende Unterelemente erlaubt:

| Tag | Beschreibung |
| :--- | :--- |
| `<picaMainTag>` | Die PICA+ Feldnummer. Dieses Element muss genau einmal vorhanden sein. |
| `<picaSubTag>` | Der PICA+ Feldtrenner innerhalb des mit `<picaMainTag>` angegebenen Feldes. Der Inhalt dieses Unterfeldes entspricht dem Wert des im `<Name>` Element angegebenen Metadatums. Innerhalb des `<Metadata>` und `<DocStruct>` Elements muss dieses Feld genau einmal vorhanden sein. Für das `<Person>` Element gelten andere Regeln (siehe unten). |
| `<valueCondition>` | Bedingtes Mapping: Mit der Angabe eines regulären Ausdrucks (Perl5-Syntax) kann hier eine Bedingung angegeben werden. Nur, wenn diese Bedingung auf den Inhalt des oben definierten PICA+ Feldes zutrifft, wird der Wert dem internen Metadatum zugewiesen. Dieses Feld ist optional, Beispiel siehe unten. |
| `<name>` | Der interne Metadatenname. Dieses Element muss ebenfalls genau einmal vorhanden sein. |
| `<valueRegExp>` | Nachträgliche Bearbeitung von PICA+ Feldwerten: Mit der Angabe eines regulären Ausdrucks (Perl5-Syntax) kann der Wert des Metadatums manipuliert werden. Dieses Feld ist optional, Beispiel siehe unten. |

_Beispiel: Mapping von PICA+ Felder_

```xml
<Metadata>
      <picaMainTag>021A</picaMainTag>
      <picaSubTag>a</picaSubTag>
      <Name>TitleDocMain</Name>
</Metadata>
```

_Beispiel: Nutzung von bedingten Zuweisungen_

```xml
<Metadata>
    <picaMainTag>007S</picaMainTag>
    <picaSubTag>0</picaSubTag>
    <ValueCondition>/^VD17/</ValueCondition>
    <Name>CatalogFieldVDseventeen</Name>
    <ValueRegExp>s/^VD17\s(.*)/$1/</ValueRegExp>
</Metadata>
<Metadata>
    <picaMainTag>007S</picaMainTag>
    <picaSubTag>0</picaSubTag>
    <ValueCondition>/^VD18/</ValueCondition>
    <Name>CatalogFieldVDeighteen</Name>
    <ValueRegExp>s/^VD18\s(.*)/$1/</ValueRegExp>
</Metadata>
```

Der Wert von `picaMainTag 007s` und `picaSubTag 0` wird nur dann dem internen Metadatum `CatalogFieldVDseventeen` zugewiesen, wenn dessen Inhalt mit `VD17` beginnt. Sollte der Inhalt des PICA+ Feldes mit `VD18` beginnen, wird dieser dem internen Metadatum `CatalogFieldVDeighteen` zugewiesen.

Wenn eine der Bedingungen erfüllt ist, wird zusätzlich noch das `VD17` oder das `VD18` vor der eigentlichen VD17- bzw. VD18-Nummer (incl. dem Leerzeichen) entfernt.

_Beispiel: Manipulation von Metadaten beim Import_

```xml
<Metadata>
    <picaMainTag>003@</picaMainTag>
    <picaSubTag>0</picaSubTag>
    <Name>CatalogIDDigital</Name>
    <ValueRegExp>s/(.*)/PPN$1/</ValueRegExp>
</Metadata>
```

Mit diesem regulären Ausdruck wird der Wert des PICA+ (PPN) um die Zeichenkette `PPN` ergänzt.

Da Personen weitere Merkmale aufweisen und nicht nur ein einfaches Typ-Wert-Paar sind, kann für jedes dieser Merkmale ein `<picaSubTag>` innerhalb des `<Person>` Elements existieren. Das entsprechende Attribut `type` gibt das entsprechende Merkmal an. Folgende Werte für dieses Attribut sind gültig:

| Attribut | Beschreibung |
| :--- | :--- |
| `firstname` | Vorname der Person |
| `lastname` | Nachname der Person |
| `identifier` | Identifier der Person (bspw. aus der Personennormdatenbank) |
| `expansion` | Es können weiterhin Vor- und Nachname aus dem Pica-Feld „Expansion der Ansetzungsform“ extrahiert werden. |

_Beispiel: Mapping von Personen_

```xml
<Person>
<picaMainTag>028A</picaMainTag> <Name>Author</Name>
<picaSubTag type="firstname">d</picaSubTag> <picaSubTag type="lastname">a</picaSubTag> <picaSubTag type="identifier">9</picaSubTag> <picaSubTag type="expansion">8</picaSubTag>
</Person>
```

Körperschaften bestehen ebenfalls aus mehreren Feldern. Sie werden innerhalb eines `<Corporate>` Elements definiert. Hier steht ähnlich wie bei den Personen das Attribut `type` für `<picaSubTag>` zur Verfügung, um zu definieren, in welches Feld ein Wert importiert werden soll. Folgende Werte sind möglich:

| Attribut | Beschreibung |
|-- |-- |
| `mainName` | Dieses Feld enthält den `code` des `<subfield>` Elements, aus dem der Hauptname der Körperschaft importiert werden soll. Das Feld ist wiederholbar, um verschiedene `code` angeben zu können. Wenn mehr als ein Feld gefunden wurde, wird der erste Wert importiert. |
| `subName` | Dieses Feld enthält den `code`, aus dem weitere Namensangaben der Körperschaft importiert werden sollen. Das Feld ist wiederholbar, um verschiedene `code` angeben zu können. Wenn mehr als ein Feld gefunden wurde, wird jeder Wert separat importiert. |
| `partName` | Dieses Feld enthält den `code`, aus dem Zählungen, Orte oder Datumsangaben zur Körperschaft importiert werden sollen. Das Feld ist wiederholbar, um verschiedene `code` angeben zu können. Wenn mehr als ein Feld gefunden wurde, werden alle Werte in ein gemeinsames Feld importiert, die einzelnen Einträge werden durch den in `<separator>` konfigurierten Wert getrennt. |
| `identifier` |  Dieses Feld enthält den `code` des `<subfield>` Elements, in dem ein Normdatenidentifier der Körperschaft definiert wurde.|

_Beispiel: Mapping von Körperschaften_

```xml
<Corporate>
    <Name>Corporation</Name>
    <picaMainTag>029F</picaMainTag>
    <picaSubTag type="mainName">a</picaSubTag>
    <picaSubTag type="subName">b</picaSubTag>
    <picaSubTag type="partName">c</picaSubTag>
    <picaSubTag type="partName">d</picaSubTag>
    <picaSubTag type="partName">n</picaSubTag>
    <picaSubTag type="identifier">7</picaSubTag>
</Corporate>
```

Da der Typ der jeweiligen Dokumentstruktur abhängig vom Wert eines Feldes ist, existiert innerhalb des Elements `<DocStrct>` noch ein weiteres Pflichtelement `<picaContent>`. Nur wenn das mit `<picaMainTag>` und `<picaSubTag>` spezifizierte Element den in `<picaContent>` definierten Content aufweist, wird eine entsprechende Dokumentstruktur anlegt und es werden ihr die Metadaten zugeordnet. Der Typ der Dokumentstruktur wird im Element `<Name>` spezifiziert.

Hierbei kann es auch zu einem n:1 Mapping kommen, das heißt, es können mehrere Typen aus dem Pica-System einem internen Typ zugewiesen werden. Dies ist notwendig, da die Buchstabenkombination des PICA+ Formats nicht nur den bibliographischen Typ, sondern auch die Erscheinungsform (gedruckt, digital, Microform) enthält. Entsprechend muss für jede Buchstabenkombination ein Mapping vorgenommen werden.

_Beispiel: Mapping eines MultiVolume-Werkes_

```xml
<DocStruct>
      <picaMainTag>002@</picaMainTag>
      <picaSubTag>0</picaSubTag>
      <picaContent>Oc</picaContent>
      <Name>MultivolumeWork</Name>
</DocStruct>
<DocStruct>
      <picaMainTag>002@</picaMainTag>
      <picaSubTag>0</picaSubTag>
      <picaContent>Ac</picaContent>
      <Name>MultivolumeWork</Name>
</DocStruct>
```

Für das Mapping von Dokumentstrukturen ist zu beachten, dass lediglich die ersten im Regelsatz definierten Buchstaben verglichen werden. Dies ist quasi mit einer trunkierten Suche gleichzusetzen. Da in dem Beispiel oben lediglich zwei Buchstaben im Feld `<picaContent>` angegeben sind, werden auch nur die ersten beiden Buchstaben in der PICA+ Datei berücksichtigt. Ferner erfolgt der Vergleich unter Beachtung der Groß/Kleinschreibung (case-sensitive).
