---
title: Regelsatz
published: true
keywords:
    - UGH
---

Das oben beschriebene Dokumentmodell ist recht allgemein gehalten, es legt keinerlei Metadaten- oder Strukturtypen fest. Um überhaupt Instanzen von Struktureinheiten und Metadaten anlegen zu können, ist es notwendig, in einem Regelsatz entsprechende Typen zu definieren. Weiterhin enthält dieser auch entsprechende Abschnitte für die Serialisierungsinformationen der jeweiligen Serialisierungsklassen.

Technisch wird der Regelsatz beim ersten API-Aufruf durch die Applikation geladen und nur der Teil interpretiert, der die Meta- und Strukturtypen definiert. Erst wenn entsprechende Klassen zum Serialisieren der Daten aufgerufen werden, wird der entsprechende Abschnitt zur Konfiguration der Serialisierung interpretiert \(da dies erst in den entsprechenden Serialisierungsklassen implementiert ist). Das bedeutet, dass bei fehlerhafter Konfiguration auch während der Laufzeit Fehler auftreten können. Diese werden als Exceptions von der `UGH Bibliothek` geworfen und müssen entsprechend durch die Applikation abzufangen werden.

Der Regelsatz selber ist als XML-Datei implementiert. Der Wurzelknoten in jedem Regelsatz ist das `<Preferences>` Element. Innerhalb dieses Elementes befinden sich alle Definitionen der Metadaten- und Strukturtypen. Wichtig ist hierbei, dass Metadatentypen vor Gruppierungen und Strukturtypen definiert werden müssen.

