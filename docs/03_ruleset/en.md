---
title: Rule set
published: true
keywords:
    - UGH
---

The document model described above is quite general, it does not specify any metadata or structure types. To be able to create instances of structural units and metadata at all, it is necessary to define corresponding types in a rule set. The rule set also contains corresponding sections for the serialization information of the respective serialization classes.

Technically, the rule set is loaded by the application during the first API call and only the part that defines the meta and structure types is interpreted. Only when corresponding classes for serializing the data are called is the corresponding section for configuring the serialization interpreted (since this is only implemented in the corresponding serialization classes). This means that if the configuration is incorrect, errors can also occur during runtime. These are thrown as exceptions by the `UGH library` and must be caught by the application.

The rule set itself is implemented as an XML file. The root node in each ruleset is the  element. Within this `<Preferences>` element are all definitions of the metadata and structure types. It is important to note that metadata types must be defined before groupings and structure types.

