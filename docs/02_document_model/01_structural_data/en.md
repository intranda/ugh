---
title: Structural data
published: true
keywords:
    - UGH
---

Structure data describes the structure of a document. For this purpose, however, a rather extensive structural term is used, which does not only concern the internal, hierarchical structure of a document, but also understands the document itself as part of this structure - as the bibliographic unit "document".

In addition to this logical structure, the model can represent the physical structure as a second structure. This structure usually stores the "bound unit" and its subunits (i.e. the individual pages of the work). In both the logical structure and the physical structure, units can be nested hierarchically to any depth, and each unit can have only one parent unit. Furthermore, units from the logical and physical structure can be linked to each other, for example to reflect the relationship between a chapter and its corresponding pages. Example: chapter five (logical structure) comprises pages 11 to 27 (physical structure).

Each structural unit must always have a type (structure type/DocStruct). This classifies the structure unit and thus also defines which other structure units may exist as subordinate units. The same applies to metadata. In practice, this prevents incorrect assignment of metadata to a structure type, for example. It may not be possible to assign an ISBN to a chapter.

