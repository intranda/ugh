---
title: Introduction
published: true
---

The PICA+ format is mainly used in library catalogues and describes only the bibliographic unit; only metadata for a structural unit can be found in PICA+ and no structural data at all. If necessary, some metadata fields may contain references to other structural units, but the application must retrieve these from the library catalog again and link them via the API accordingly.

The basis of the PICA+ serialization of the UGH library is the format PICA+ XML, as it is [described by the GBV in its compound WIKI](http://www.gbv.de/wikis/cls/PICAplus_in_XML) and implemented by the `getOpac` classes of Jens Ludwig. The serialization class cannot download the data from a catalog on its own, but must be "fed" with a corresponding XML file.

The PICA+ serialization class can only create a structural unit with its metadata from a PICA record. This structure unit is always the highest logical structure unit in the document. The class only has a read method. Writing in the PICA+ format does not seem to make sense, since there are hardly any interfaces for uploading the data into the library system.

