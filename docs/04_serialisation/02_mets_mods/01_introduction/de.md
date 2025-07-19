---
title: Einführung
published: true
---

Das METS/MODS XML-Format kann das interne Dokumentmodell vollständig serialisieren, es sind sowohl Lese- und Schreibmethoden implementiert. Da es sich lediglich um eine Untermenge von METS handelt - es werden nicht alle METS Elemente genutzt - existiert eine Beschreibung des dieser Implementierung zugrunde liegenden Formats auf den Seiten des [DFG-Viewer](http://dfg-viewer.de/): Das DFG-Viewer METS-Profil in Version 2.0.

:::info
**Als PDF:**   
[http://dfg-viewer.de/fileadmin/groups/dfgviewer/METS\_Anwendungsprofil\_2.0.pdf ](http://dfg-viewer.de/fileadmin/groups/dfgviewer/METS_Anwendungsprofil_2.0.pdf%20)  
  
**Als XML-Profil:**   
[http://dfg-viewer.de/fileadmin/groups/dfgviewer/METS\_Anwendungsprofil\_2.0.xml](http://dfg-viewer.de/fileadmin/groups/dfgviewer/METS_Anwendungsprofil_2.0.xml)
:::

Die Strukturtypen werden grundsätzlich in METS serialisiert, die Metadaten je nach genutzter Klasse im MODS-Format des [DFG-Viewer-Profils](http://dfg-viewer.de/fileadmin/groups/dfgviewer/MODS_Anwendungsprofil_1.0.pdf) (`ugh.fileformat.mets. MetsModsImportExport`) oder in einem Goobi-Namespace innerhalb des MODS Extension-Tags (`ugh.fileformat.mets.MetsMods`). Diese Trennung ist nötig, weil für eine Speicherung im MODS-Anwendungsprofil ein Mapping der Metadatentypen des Dokumentmodells zu den MODS-Typen UND UMGEKEHRT zwingend nötig ist. Da die Metadatentypen jedoch frei im Regelsatz definiert werden können, ist ein Mapping nach MODS immer möglich, bei verschiedenen Autoren beispielsweise - für die es nicht notwendigerweise eine voneinander unterschiedliche Entsprechung in MODS gibt - aber dann nicht in der anderen Richtung.

Für die vollständige Serialisierung des Dokumentmodells kann die Klasse `MetsMods` genutzt werden, während die Klasse `MetsModsImportExport` zunächst den Export unterstützt, so dass METS-Dateien im DFG-Viewer METS-Profil exportiert werden können. Lesender Zugriff auf Dateien mit Metadaten im MODS-Anwendungsprofil ist bereits implementiert, unterliegt jedoch gewissen Einschränkungen, die im Kapitel „Mapping für den Import“ erläutert werden.

