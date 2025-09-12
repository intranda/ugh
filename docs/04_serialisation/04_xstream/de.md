---
title: XStream
published: true
keywords:
    - UGH
    - XSTREAM
---

Serialisierungsklasse:

```text
 ugh.fileformats.mets.XStream (v1.2)
```

Die Serialisierung via XStream hat zunächst das RDF/XML-Schema abgelöst, um unabhängig zu sein von Mappings und zusätzlichen Einträgen im Regelsatz. XStream kann das Dokumentmodell komplett serialisieren, hat jedoch einige Nachteile (siehe unten), so dass dieses Format in Zukunft aus der UGH Bibliothek entfernt wird, lesender Zugriff wird jedoch weiterhin aus Kompatibilitätsgründen möglich sein!

[XStream](http://xstream.codehaus.org/) serialisiert das komplette Java-Objekt `DigitalDocument` im XStream XML-Format, so dass alle Strukturtypen, Metadaten, Referenzen, etc. des Dokumentmodells gesichert werden. Einträge im Regelsatz oder ein Mapping sind hier nicht nötig. Da es jedoch enge Beziehungen des `DigitalDocument` zum Regelsatz gibt (Namen der Metadaten, Quantifier, etc.), werden zunächst einige Teile des Regelsatzes mit serialisiert. Nach dem Laden des serialisierten Objekts wird es deswegen mit dem aktuellen Regelsatz abgeglichen, so dass Änderungen im Regelsatz auf das Objekt abgebildet werden.

Obwohl XStream als Serialisierungsformat den Vorteil hat, dass keine weitere Konfiguration vorgenommen werden muss, um es zu nutzen, hat es den doch den Nachteil, dass sehr große und komplexe XML-Dateien in einem komplexen Format entstehen, die schlecht bis gar nicht außerhalb der UGH Bibliothek - evtl. per Hand - bearbeitet werden können.

