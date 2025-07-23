---
title: Definition of the Persistent Identifier in the database
published: true
---

Furthermore, some functionally relevant information must be provided. For example, the element `<persistentIdentifier>` determines which metadata type is valid for the persistent identification of a structural unit. This element must exist exactly once, since it is used to check whether a document has already been imported into the repository and, if so, to reject serialization.

_Example: Configuration of the PPN as persistent identifier:_

```xml
<persistentIdentifier>CatalogIDDigital</persistentIdentifier>
```

