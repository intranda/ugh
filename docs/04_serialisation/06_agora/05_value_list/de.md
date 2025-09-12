---
title: Wertelisten
published: true
keywords:
    - UGH
---

Eine Werteliste kann beim Schreiben in die Datenbank bestimmte Werte ersetzen. Dies kann dann sinnvoll sein, um Sprachcodes umzuschreiben. In einer entsprechenden Mappingdatei werden die alten Werten (aus der API) den neuen Werten (in der Datenbank) gegenüber gestellt. Die Mappingdatei ist nichts weiter als eine Textdatei, die pro Zeile ein Wertepaar enthält. Der erste Wert ist der zu ersetzende Wert (also der in der UGH Bibliothek verwendete Wert), der zweite Wert ist der entsprechende Wert aus der Datenbank. Beide Werte sind durch ein Leerzeichen getrennt.

_Beispiel: Aufbau der Mappingdatei_

```text
de 57
en 58
fr 626
```

Eine Werteliste hat immer einen Namen und einen Pfad, der in das lokale Dateisystem zeigt. Der Name der Werteliste wird im `<ListName>` Element gespeichert, der Pfad im `<FileName>` Element.

_Beispiel: Definition einer Werteliste_

```xml
<ValueList>
    <ListName>languagelist</ListName>
    <FileName>C:/olms/language.txt</FileName>
</ValueList>
```

Der Name der Werteliste dient dazu, sie aus dem Metadatentyp-Mapping heraus ansprechen zu können. Daher muss deren Name eindeutig sein.

