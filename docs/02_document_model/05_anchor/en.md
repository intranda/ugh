---
title: Anchor
published: true
keywords:
    - UGH
---

An anchor is a document structure that is more virtual in nature and can combine other structural units into a group. Such anchors are, for example, the structures Multi-volume Work or Journal, which holds all volumes of the work or all volumes of the journal together. The anchor itself has no content files - i.e. no image set with image files, it consists only of a structural unit that has descriptive metadata. The essential metadata is an identifier with which the anchor can be clearly identified.

In principle there are two ways to save the anchor:

* In the same file as the underlying structural unit. In this case, the structural unit serving as anchor is stored redundantly in each metadata file. For example, the RDF/XML format stores the document structure of the anchor as the top-level structural unit in the file.
* In a separate file, so that the anchor must be referenced from underlying structural units. For importing into databases and repositories, this means that the anchor must always be imported first. To ensure referencing, the anchor contains an identifier that is used in the reference in the underlying structural unit.

