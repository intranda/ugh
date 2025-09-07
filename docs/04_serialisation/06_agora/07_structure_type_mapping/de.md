---
title: Strukturtyp-Mapping
published: true
keywords:
    - UGH
---

Der Dokumentstrukturtyp wird relativ einfach gemappt. Da der Name des Strukturtyps ebenfalls in der Datenbank gespeichert wird, jedoch mittels einer separaten Tabelle über eine ID mit der eigentlichen Struktureinheit verknüpft wird, ist der entsprechende Typname lediglich ein einziges Mal in der Datenbank gespeichert.

Neue Strukturtypen werden durch die Serialisierungsklasse automatisch in der Tabelle hinzugefügt, sobald die Klasse ihre Präferenzen geladen hat, das heißt, ein Update der Strukturtypen erfolgt automatisch vor den eigentlichen Schreiboperationen für das jeweilige Dokument.

Mittels des `<DocStruct>` Elements kann der entsprechende interne Name mit dem in der Datenbank gespeicherten Namen gemappt werden. Der interne Name wird dabei im `<InternalName>` Element definiert. Das `<DBName>` Element enthält den Namen aus der `BibCategory`-Tabelle der Datenbank.

_Beispiel: Mapping der Strukturtypen für die AGORA-Datenbank_

```xml
<DocStruct>
    <InternalName>PeriodicalIssue</InternalName>
    <DBName>ZSCHR_HEFT</DBName>
</DocStruct>
```

