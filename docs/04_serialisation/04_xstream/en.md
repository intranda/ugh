---
title: XStream
published: true
---

Serialization class:

```text
 ugh.fileformats.mets.XStream (v1.2)
```

Serialization via XStream initially replaced the RDF/XML schema to be independent of mappings and additional entries in the rule set. XStream can completely serialize the document model, but has some disadvantages (see below), so this format will be removed from the UGH library in the future, but reading access will still be possible for compatibility reasons!

[XStream](http://xstream.codehaus.org/) serializes the complete Java object `DigitalDocument` in XStream XML format, so that all structure types, metadata, references, etc. of the document model are saved. Entries in the rule set or a mapping are not necessary here. However, since there are close relationships between the `DigitalDocument` and the ruleset (names of metadata, quantifiers, etc.), some parts of the ruleset are serialized first. After the serialized object is loaded, it is therefore compared with the current rule set so that changes in the rule set are mapped to the object.

Although XStream as a serialization format has the advantage that no further configuration is required to use it, it has the disadvantage that very large and complex XML files are created in a complex format, which can hardly or not at all be edited outside the UGH library - possibly by hand.

