---
title: Serialisierung
published: true
keywords:
    - UGH
---

Daten, die mittels der API angelegt wurden, sollen natürlich auch geschrieben und gelesen werden können. Dazu verfügt die API über ein internes JAVA-Interface, welches durch unterschiedliche Klassen implementiert werden kann und Methoden zum Lesen und Schreiben bereitstellt. Ob diese Lese- und Schreibvorgänge dazu eine Datenbank oder das Dateisystem nutzten, oder welches Dateiformat genutzt wird, ist nicht relevant. Diese spezifischen Dinge werden durch das Interface gekapselt.

Durch die Implementierung des Interfaces können nachträglich neue Formate durch die UGH Bibliothek unterstützt werden, wobei eine Serialisierungsklasse genau ein Datenformat implementiert. Bei der Nutzung der Serialisierungsklassen ist darauf zu achten, dass diese das komplette Dokumentmodell unterstützten - also das Dokument ohne Verlust von Daten speichern und laden können. Eventuelle Einschränkungen müssen aus der Dokumentation der Serialisierungsklasse hervorgehen.

Eine Serialisierung kann immer nur für ein komplettes Dokument erfolgen. Änderungen an einzelnen Dokumentteilen führen also immer dazu, dass das komplette Dokument neu geschrieben wird. Die Applikation hat daher selbst darauf zu achten, dass Schreibzugriffe nicht konkurrierend stattfinden.

Da die Serialisierung genau genommen nicht Bestandteil der API ist - es existiert ein Java- Interface, das von beliebigen weiteren Serialisierungsklassen implementiert werden kann, wird diese in einem speziellen Bereich des Regelsatzes definiert. Dieser Bereich ist mit dem `<Formats>` Element umschlossen. Innerhalb dieses Elements gibt es für jede Serialisierungsklasse genau ein Element, welches die komplette Konfiguration für das jeweilige Dokumentformat enthält. Zumeist handelt es sich hierbei um Mapping-Informationen, wie zum Beispiel interne Struktur- und Metadatentypen in das jeweilige Format gemappt werden können. Die Konfigurationsmöglichkeiten sind dabei dem Format angepasst.

Als Serialisierungsformat für die interne Speicherung wird METS/MODS empfohlen, das die Strukturdaten im METS-Format ablegt und die Metadaten in MODS-Containern (pro Strukturelement) in einem Goobi-XML-Namespace. Damit sind die Daten menschenlesbar und auch außerhalb der UGH Bibliothek editierbar - zum Beispiel in einem Text- oder XML-Editor. Für den Export/Import von [DFG-Viewer-METS mit MODS-Metadaten](http://dfg-viewer.de/profil-der-metadaten/) siehe das entsprechende Kapitel.

