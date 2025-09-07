---
title: Inheritable metadata types
published: true
keywords:
    - UGH
---

The element `<inheirableMetadataTypes>` contains all the metadata types that are to be inherited from a structural unit to underlying structural units. Corresponding entries are duplicated in the database and thus allow fast access or filtering. However, the metadata types are only duplicated if the structural unit does not have its own metadata of this type. Within the `<inheirableMetadataTypes>` element, there is an `<internalName>` element for each metadata type.

_Example: Inheritance of the Digital Collection during database import_

```text
<inheirableMetadataTypes>
    <internalName>singleDigCollection</internalName>
</inheirableMetadataTypes>
```

