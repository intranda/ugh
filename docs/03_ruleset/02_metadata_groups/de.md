---
title: Definition von Metadatengruppen
published: true
keywords:
    - UGH
---

Eine Metadatengruppe wird innerhalb des `<Group>` Elements definiert. Dieses Element muss als einziges Kind das `<Name>` Element besitzen, welches den internen Namen des Metadatentyps enthält. Der interne Name wird auch zur Referenzierung auf den Metadatentyp aus der Serialisierungskonfiguration heraus genutzt. Er darf keine Leerzeichen enthalten.

Weiterhin kann das `<Group>` Element beliebig viele `<language>` Elemente als Kind enthalten. Diese `<language>` Elemente übersetzen den internen Namen des Metadatentyps in die jeweilige Sprache, die im Attribut name angegeben ist. Daher ist es sinnvoll, mindestens ein `<language>` Element pro Metadatentyp zu definieren, damit dessen Wert in der Benutzeroberfläche angezeigt und genutzt werden kann. Pro Sprache und `<Group>` Element darf es nur ein einziges `<language>` Element geben.

Außerdem muss das `<Group>` Element mindestens ein `<metadata>` Element enthalten. Über diese Elemente können die Metadaten zur Gruppe hinzugefügt werden. Das Element muss dazu den internen Metadatennamen einer `<MetadataType>` Definition enthalten.

Darüberhinaus kann das `<metadata>` Element das Attribut `num` enthalten, mit dem die Häufigkeit der Verwendung im Kontext der Gruppe angeben werden kann.

```xml
<Group>
      <Name>Title</Name>
      <language name="de">Titel</language>
      <language name="en">Title</language>
      <metadata num="1o">NonSort</metadata>
      <metadata num="1m">SortingTitle</metadata>
      <metadata num="*">Subtitle</metadata>      
</Group>
```

Das Attribut `num` muss eines der folgende Werte enthalten:

|Wert des Attributs|Bedeutung des Attributs
|-- |-- 
| `*` | kein mal oder beliebig oft (0...n) 
| `+` | einmal oder beliebig oft (1...n) 
| `1o` | kein mal oder genau einmal (0...1) 
| `1m` | genau einmal (1) 

Ist kein Attribut `num` angegeben, wird standardmäßig der Wert `*` angenommen.


