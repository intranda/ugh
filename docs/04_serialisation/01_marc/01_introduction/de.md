---
title: Einführung
published: true
---

Das Format **MARC** (**MA**chine-**R**eadable **C**ataloging) ist ein Datenformat, das zum Austausch von bibliographischen Daten entwickelt wurde. Das Format enthält Metadaten zu einer einzelnen Struktureinheit. Strukturdaten können über das Format nicht beschrieben werden. Daher kann das Format lediglich zum Import von Datensätzen genutzt werden, das Schreiben von Metadaten im MARC Format ist in UGH nicht vorgesehen.

Die vorliegende UGH Erweiterung erwartet als Eingabe einen Datensatz im Format MARCXML, wie es von der [Library of Congress beschrieben](http://www.loc.gov/marc/bibliographic/) wird.

Die Erweiterung kann nicht selbst auf einen Katalog zugreifen, der Datensatz muss bereits lokal vorliegen. Er kann entweder in Form einer Datei oder als Zeichenkette an die UGH Erweiterung übergeben werden.

Das Format MARC XML besteht aus drei Bereichen. Jeder `<record>` enthält genau ein `<leader>` Element. Hier sind einige Angaben zum Dokumententyp codiert. Anschließend folgt eine Reihe von `<controlfield>` Elementen. In `001` ist der Identifier des Datensatzes enthalten, `007` und `008` enthalten weitere Angaben zum Dokumententyp. Anschließend folgen `<datafield>` Elemente, die wiederum eine Liste von `<subfield>` enthalten.

```xml
<record xmlns="http://www.loc.gov/MARC21/slim">
    <leader>xxxxxntm a22yyyyy c 4500</leader>
    <controlfield tag="001">722181507</controlfield>
    <controlfield tag="003">DE-601</controlfield>
    <controlfield tag="005">20150128152124.0</controlfield>
    <controlfield tag="008">120823m18611871gw 000 0 lat d</controlfield>
    <datafield tag="035" ind1=" " ind2=" ">
        <subfield code="a">(DE-599)GBV722181507</subfield>
    </datafield>
    <datafield tag="040" ind1=" " ind2=" ">
        <subfield code="a">GBVCP</subfield>
        <subfield code="b">ger</subfield>
        <subfield code="c">GBVCP</subfield>
        <subfield code="e">rakwb</subfield>
    </datafield>
    <datafield tag="041" ind1="0" ind2=" ">
        <subfield code="a">lat</subfield>
        <subfield code="a">ger</subfield>
    </datafield>
    <datafield tag="100" ind1="1" ind2=" ">
        <subfield code="a">Pichler, Pauline</subfield>
        <subfield code="e">Besitzer</subfield>
        <subfield code="0">(DE-601)722181795</subfield>
        <subfield code="0">(DE-588)1025374479</subfield>
    </datafield>
    <datafield tag="245" ind1="1" ind2="0">
        <subfield code="a">[Stammbuch Pauline Pichler]</subfield>
        <subfield code="h">Manuskript</subfield>
    </datafield> 
    ...
</record>
```

