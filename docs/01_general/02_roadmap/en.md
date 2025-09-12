---
title: Further development
published: true
keywords:
    - UGH
---

This document provides a brief overview of the document model and its customisability using a rule set. Some classes and their configuration options for serialising the data are also described here.

In the future, it may make sense to further expand the configuration options of the rule set. For example, a typification of metadata values is conceivable. Currently, a metadata value is only considered a string. In the future, it may make sense for some use cases to follow a certain scheme - such as a certain time or date format - or to allow only integer values for certain metadata types. This would save a lot of additional business logic for the superordinate application, which currently has to check corresponding inputs before further processing. A descriptive XML schema that maps such things would be very desirable and helpful. Furthermore, a mechanism would be very useful that can also map things that cannot initially be covered by an XML schema, such as a check whether a metadata defined at the beginning of the ruleset reappears in all required areas in the ruleset. For example, in a particular `<formats>` section or in the structural data; extended in this way, a method of the UGH library could easily check the correctness of a rule set comprehensively and point out errors.

Furthermore, the application purpose of the UGH library can be extended, if it could address the individual objects directly by means of a persistence layer without going via serialization classes. This omission of the serialization layer would mean that the complete document would not always have to be read and written to change/delete/update individual objects. Serialization classes would then only be used for import and export. Such a class library could also be used to directly build a repository, which, due to the identical API, can use all the tools that are currently applicable to METS files, for example.

As an example for further simplifications or standardizations of the UGH library, one could use proven analogies for the configuration of the number of metadata fields allowed per structural element (num attribute) and not use own conventions, for example the quantification syntax of regular expressions. Even though such changes will certainly result in some far-reaching adjustments of the UGH classes:Compatibility with earlier versions should be maintained at best.

