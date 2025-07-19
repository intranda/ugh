---
title: Structure Type Mapping
published: true
---

The document structure type is mapped relatively simply. As the name of the structure type is also stored in the database but is linked to the actual structural unit by means of a separate table via an ID, the corresponding type name is only stored once in the database.

New structure types are automatically added to the table by the serialization class as soon as the class has loaded its preferences, i.e. an update of the structure types takes place automatically before the actual write operations for the respective document.

With the `<DocStruct>` element the corresponding internal name can be mapped with the name stored in the database. The internal name is defined in the `<InternalName>` element. The `<DBName>` element contains the name from the `BibCategory` table of the database.

_Example: Mapping the structure types for the AGORA database_

```xml
<DocStruct>
    <InternalName>PeriodicalIssue</InternalName>
    <DBName>ZSCHR_HEFT</DBName>
</DocStruct>
```

