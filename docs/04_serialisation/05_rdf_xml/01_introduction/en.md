---
title: Introduction
published: true
---

The RDF/XML format is the data format originally developed for the AGORA document management system (AGORA DMS), which is supported by the AGORA document management system for import. Furthermore, the associated meta and structural data acquisition program AGORA Editor generates this format. It is therefore not a generic RDF-based XML format, but rather a very special form of describing digitized documents, assuming that each page is stored as a single image. In order to be able to take over data in this format and import it into the DMS, the corresponding data format is supported by UGH.

The RDF/XML format does not represent the complete document model. While the logical structure with its metadata is freely configurable, the physical structure is only preserved to a very limited extent. Pages are combined into so-called page areas with identical pagination (pagination sequences), which means that a logical structural element can have only one start page and one end page. Interruptions in the course of the page are not possible.

The structure types for the physical structure are predefined so that the mapping functions accordingly. The highest-level physical force element must be of type `BoundBook`. The underlying pages are of type `page`. If the types differ, there is a risk that the pagination sequences cannot be generated.

Furthermore, the RDF/XML format does not recognize content files. In order to generate corresponding objects internally, the image files must follow a specific naming convention: 8 digits in ascending order starting with 1 (with preceding zeros) and 3 digits for the ending `tif`, for example `00000001.tif`.

