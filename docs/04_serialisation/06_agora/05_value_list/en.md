---
title: Value lists
published: true
keywords:
    - UGH
---

A value list can replace certain values when writing to the database. This can then be useful for rewriting language codes. In a corresponding mapping file, the old values (from the API) are compared with the new values (in the database). The mapping file is nothing more than a text file that contains one pair of values per line. The first value is the value to replace (i.e. the value used in the UGH library), the second value is the corresponding value from the database. Both values are separated by a space.

_Example: Structure of the mapping file_

```text
de 57
en 58
fr 626
```

A value list always has a name and a path that points to the local file system. The name of the value list is stored in the `<ListName>` item, the path in the `<FileName>` item.

_Example: Definition of a value list_

```xml
<ValueList>
    <ListName>languagelist</ListName>
    <FileName>C:/olms/language.txt</FileName>
</ValueList>
```

The name of the value list is used to address it from the metadata type mapping. Therefore their name must be unique.

