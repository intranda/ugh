---
title: Einführung
published: true
keywords:
    - UGH
    - PICA
---

Das PICA+ Format wird überwiegend in Bibliothekskatalogen genutzt und beschreibt lediglich die bibliographische Einheit; es sind nur Metadaten für eine Struktureinheit in PICA+ zu finden und keinerlei Strukturdaten. Gegebenenfalls können einige Metadatenfelder Verweise auf andere Struktureinheiten enthalten, die jedoch durch die Applikation nochmals aus dem Bibliothekskatalog geholt werden und über die API entsprechend verknüpft werden muss.

Die Grundlage der PICA+ Serialisierung der UGH Bibliothek bildet das Format PICA+ XML, wie es [durch den GBV in seinem Verbund-WIKI beschrieben](http://www.gbv.de/wikis/cls/PICAplus_in_XML) wird und durch die `getOpac`-Klassen von

Jens Ludwig implementiert wurde. Die Serialisierungsklasse kann nicht die Daten selbständig aus einem Katalog herunterladen, sondern muss mit einer entsprechenden XML-Datei „gefüttert“ werden.

Die PICA+ Serialisierungsklasse kann aus einem PICA-Datensatz lediglich eine Struktureinheit mit ihren Metadaten erstellen. Diese Struktureinheit ist immer die oberste logische Struktureinheit des Dokumentes. Die Klasse verfügt lediglich über eine Lese-Methode. Ein Schreiben in das PICA+ Format erscheint nicht sinnvoll, da nicht zuletzt auch entsprechende Schnittstellen zum Upload der Daten in Bibliothekssystem kaum vorhanden sind.

