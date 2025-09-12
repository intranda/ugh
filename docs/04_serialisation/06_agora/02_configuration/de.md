---
title: Datenbankkonfiguration
published: true
keywords:
    - UGH
---

Die Konfiguration befindet sich in dem umschließenden `<AGORADATABASE>` Element, welches sich dem `<Formats>` Element unterordnet. Zunächst müssen einige Basisparameter der Datenbank angegeben werden. Dies sind im Einzelnen:

| Parameter | Beschreibung |
| :--- | :--- |
| `<databaseURL>` | URL der Datenbank, auf die zugegriffen werden soll. |
| `<databaseUser>` | Benutzer, der entsprechenden Lese-/Schreibzugriff auf die Datenbank hat. |
| `<databasePassword>` | Passwort des Benutzers. |
| `<databaseDriver>` | Name der Klasse des JDBC-Treibers, die für die Kommunikation mit der Datenbank zuständig ist. |



