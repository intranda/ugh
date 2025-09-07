---
title: Serialisation
published: true
keywords:
    - UGH
---

Data created using the API should of course be able to be read and written. For this purpose the API has an internal JAVA interface, which can be implemented by different classes and provides methods for reading and writing. Whether these read and write operations used a database or the file system, or which file format is used, is not relevant. These specific things are encapsulated by the interface.

By implementing the interface, new formats can subsequently be supported by the UGH library, whereby a serialization class implements exactly one data format. When using the serialization classes, it must be ensured that they support the complete document model - that is, that they can save and load the document without loss of data. Any restrictions must be stated in the documentation of the serialization class.

A serialization can only ever be carried out for a complete document. Changes to individual document parts always lead to the entire document being rewritten. The application itself must therefore make sure that write accesses do not take place in competition.

Since serialization is not strictly speaking part of the API - there is a Java interface that can be implemented by any other serialization classes - it is defined in a special area of the ruleset. This area is enclosed by the `<Formats>` element. Within this element there is exactly one element for each serialization class, which contains the complete configuration for the respective document format. Mostly this is mapping information, such as how internal structure and metadata types can be mapped into the respective format. The configuration options are adapted to the format.

The recommended serialization format for internal storage is METS/MODS, which stores the structural data in METS format and the metadata in MODS containers (per structural element) in a Goobi XML namespace. This makes the data human-readable and editable even outside the UGH library - for example in a text or XML editor. For the export/import of [DFG Viewer METS with MODS metadata](http://dfg-viewer.de/profil-der-metadaten/) see the corresponding chapter.

