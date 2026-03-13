---
title: Definition von Strukturtypen
published: true
keywords:
    - UGH
---

Strukturtypen werden ähnlich definiert wie Metadatentypen. Das Element zur Definition eines Strukturtyps heißt `<DocStrctType>` und genau wie bei den Metadatentypen existieren auch hier die Elemente `<Name>` und `<language>` mit denselben Eigenschaften.

_Beispiel: Minimaldefinition eines Strukturtyps mit zusätzlichen `<language>` Elementen_

```xml
<DocStrctType>
    <Name>Acknowledgment</Name>
    <language name="de">Danksagung</language>
    <language name="en">Acknowledgment</language>
</DocStrctType>
```

Sollen einem Strukturtyp weitere Strukturtypen als Kinder untergeordnet werden, muss im Regelsatz diese potentielle Zugehörigkeit definiert werden. Das Element `<allowedchildtype>` enthält dazu den internen Namen des entsprechende Strukturtyps. Hierbei ist die Reihenfolge der Strukturtypen egal.

_Beispiel: Strukturtypdefinition mit potentiellen Kindern_

```xml
<DocStrctType>
    <Name>Acknowledgment</Name>
    <language name="de">Danksagung</language>
    <language name="en">Acknowledgment</language> <allowedchildtype>OtherDocStrct</allowedchildtype>
</DocStrctType>
```

Ähnlich wie die Definition potentieller Kinder eines Strukturtyps definiert das Element `<metadata>` potentielle Metadatentypen, die zu einem Strukturtyp hinzugefügt werden dürfen. Das Element muss dazu den entsprechenden internen Metadatennamen enthalten. Darüber hinaus kann das `<metadata>` Element weitere Attribute enthalten, die beispielsweise die Häufigkeit der Verwendung sowie die Art der Nutzung eines Metadatentyps im Kontext des Strukturtyps angeben. Ein Strukturtyp `Artikel` soll zum Beispiel zwingend einen Titel als Metadatum bekommen, jedoch keine oder beliebig viele Autoren.

Folgende Attribute stehen zur Verfügung:

## Attribut `num`

Das Attribut muss eines der folgende Werte enthalten:

| Wert des Attributs | Bedeutung des Attributs |
| :--- | :--- |
| `*` | kein mal oder beliebig oft (0...n) |
| `+` | ein mal oder beliebig oft (1...n) |
| `1o` | kein mal oder genau einmal (0...1) |
| `1m` | genau einmal (1) |

Ist kein Attribut angegeben, wird standardmäßig der Wert `*` angenommen.

## Attribut `DefaultDisplay`

Das Attribut gibt an, ob der Metadatentyp für das entsprechende Strukturelement standardmäßig angezeigt werden soll (auch dann, wenn es noch keinen Inhalt hat). Metadaten mit Inhalt werden in jedem Fall angezeigt. In einer Metadaten-Erfassungsmaske lassen sich durch Abfragen dieses Attributs dann Leermasken generieren. Dazu muss das Attribut den Wert `true` besitzen. Andere Werte oder ein Fehlen dieses Attributs ist mit dem Wert `false` gleichzusetzen.

_Beispiel: Komplexe Strukturtypinformation_

```xml
<DocStrctType>
    <Name>PeriodicalVolume</Name>
    <language name="de">Zeitschriften-Band</language>
    <language name="en">Periodical Volume</language>
    <metadata num="*">Author</metadata>
    <metadata DefaultDisplay="true" num="1m">CatalogID</metadata>
    <group num="*">CauseEntry</group>
    <metadata num="*">SICI</metadata>
    <allowedchildtype>Advertising</allowedchildtype>
    <allowedchildtype>Appendix</allowedchildtype>
    <allowedchildtype>Article</allowedchildtype>
    <allowedchildtype>OtherDocStrct</allowedchildtype>
    <allowedchildtype>PeriodicalIssue</allowedchildtype>
    <allowedchildtype>PeriodicalPart</allowedchildtype>
    <allowedchildtype>TitlePage</allowedchildtype>
</DocStrctType>
```

Ähnlich wie die Definition potentieller Metadaten eines Strukturtyps definiert das Element `<group>` potentielle Metadatengruppen, die zu einem Strukturtyp hinzugefügt werden dürfen. Das Element muss dazu den entsprechenden internen Gruppennamen enthalten.

Dabei stehen zur Konfiguration der Häufigkeit die selben Attribute wie bei den `<metadata>` Elementen zur Verfügung.

Strukturelemente, die als Anker dienen, werden mittels des Attributs anchor als solche gekennzeichnet. Diese Strukturelemente müssen immer die obersten Strukturelemente eines Dokuments sein. Das anchor Attribut des `<DocStrctType>` Elements enthält in diesen Fällen den Wert `true`.

