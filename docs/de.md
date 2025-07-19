---
title: UGH Handbuch
published: true
sidemenu: true
description: Dokumentation für die Open Source Programmbibliothek UGH für Regelsätze und Metadatenmappings
---

## Über dieses Handbuch

Die Verwaltung von Dokumenten ist seit jeher die Hauptaufgabe von Bibliotheken. Mit dem Beginn der Digitalisierung von Informationen hat sich die Aufgabe stark gewandelt. Vermehrt werden Metadaten zum Auffinden, Sortieren und Beurteilen von Informationen genutzt. Das gebundene Buch als Informationseinheit wird zunehmend in eine Vielzahl kleinerer Einheiten \(Strukturen/Dokumentstrukturen) aufgeteilt, um diese im Internet sinnvoll und gut nutzbar präsentieren zu können. Um diese Funktionalitäten umsetzen zu können, wurden in den vergangenen Jahren unterschiedliche Datenformate erarbeitet, die die erforderlichen Meta-Informationen zu einem Dokument abspeichern und auf den eigentlichen Inhalt verweisen \(z.B. TIFF-, XML- oder HTML- Dateien). Ebenso ist es übliche Praxis, Metadaten zu einem Dokument aus unterschiedlichen Quellen zu aggregieren und gegebenenfalls zu ergänzen.

Für die Softwareentwicklung ergeben sich daraus einige Besonderheiten. Da sich Datenformate schnell ändern bzw. erweitert werden sowie unterschiedliche Projekte unterschiedliche Anforderungen an Datenformate haben \(es müssen unterschiedliche Daten abgespeichert werden), ist es ratsam, ein möglichst flexibles und wohldefiniertes Datenformat zu nutzen. Um unterschiedliche Formate be- und verarbeiten zu können ist es ferner ratsam, dass alle Tools und Programme auf eine einheitliche Programmierschnittstelle \(API) aufsetzen.

Die UGH Bibliothek stellt eine solche Programmierschnittstelle dar, die durch unterschiedliche Tools und Programme genutzt werden kann und soll, um Daten zu laden und zu speichern. Diese Schnittstelle implementiert ein universelles Dokumentmodell, welches von unterliegenden Formaten zur Beschreibung von Metainformationen unabhängig ist. Vielmehr existieren unterschiedliche Klassen zur Serialisierung eines Dokumentes. Jede dieser Klassen implementiert genau ein Datenformat. Überliegende Programmschichten \(bspw. die Business-Logik) ist somit vollkommen unabhängig vom Datenformat. Zu beachten ist jedoch, dass nicht immer alle Datenformate auch das komplette universelle Dokumentmodell umsetzen können. Ebenso können einige Serialisierungsklassen lediglich Daten lesen, da ein Schreiben für dieses Datenformat nicht benötigt wird oder wenig sinnvoll erscheint. Dies gilt beispielsweise für Klassen, die Datenformate aus Bibliothekskatalogen übernehmen. Da dort üblicherweise nur die bibliographische Ebene abgebildet wird, könnten sämtliche Strukturinformationen sowie Referenzen nicht geschrieben werden. Auf eine Schreibmethode wurde daher für diese Klassen verzichtet.


Der Quelltext von UGH befindet sich auf GitHub.

[https://github.com/intranda/ugh](https://github.com/intranda/ugh)

## Ansprechpartner

Zu Fragen rund um diese Dokumentation, für Anregungen zum weiteren Ausbau dieses Handbuchs sowie zu allgemeinen Fragen zu Goobi, Digitalisierungsprojekten im Allgemeinen und natürlich auch bezüglich der Weiterentwicklung von Goobi, wenden Sie sich bitte gerne jederzeit an die intranda GmbH:

| **Kontakt** |  |
| :--- | :--- |
| Anschrift: | intranda GmbH, Bertha-von-Suttner Str. 9, D-37085 Göttingen |
| Telefon: | +49 551 291 76 100 |
| E-Mail: | [info@intranda.com](mailto:info@intranda.com) |
| URL: | [https://www.intranda.com](https://www.intranda.com) |

## Urheberrechte

Bitte beachten Sie, dass die vorliegende Dokumentation nicht verändert oder in veränderter Form weitergegeben werden darf. Eine kommerzielle Nutzung dieser Dokumentation ist nicht gestattet.

:::info
![copyright](icon_cc.png) 

Dieses Werk ist unter einer Creative Commons Lizenz vom Typ Namensnennung - Nicht kommerziell - Keine Bearbeitungen 4.0 International zugänglich. Um eine Kopie dieser Lizenz einzusehen, konsultieren Sie [http://creativecommons.org/licenses/by-nc-nd/4.0/](http://creativecommons.org/licenses/by-nc-nd/4.0/) oder wenden Sie sich brieflich an Creative Commons, Postfach 1866, Mountain View, California, 94042, USA.
:::



---
description: Regelsätze und Referenzdokumentation für Goobi workflow
---

# Dokumentation UGH Bibliothek



