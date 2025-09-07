---
title: RDF/XML
published: true
keywords:
    - UGH
    - RDF/XML
---

Serialization class:

```text
 ugh.fileformats.exel.RDFFile (v1.2)
```

For compatibility reasons, the serialization format RDF/XML is part of the UGH library  (for example, to read old RDF/XML files that are to be converted to a new format), because some newer parts of the document model cannot be serialized with it and would be lost when saved. This currently affects, for example, the extended pagination types "column count" and "sheet count". Therefore, the format should no longer be used for internal storage!

