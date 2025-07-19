---
title: Definition of metadata groups
published: true
---

A metadata group is defined within the `<Group>` element. This element must be the only child of the `<Name>` element, which contains the internal name of the metadata type. The internal name is also used to reference the metadata type from the serialization configuration. It must not contain any spaces.

Furthermore the `<Group>` element can contain any number of `<language>` elements as a child. These `<language>` elements translate the internal name of the metadata type into the respective language specified in the name attribute. Therefore it is useful to define at least one `<language>` element per metadata type so that its value can be displayed and used in the user interface. There may only be one &lt;language&gt; element per language and `<Group>` element.

In addition, the `<group>` element must contain at least one `<metadata>` element. These elements can be used to add the metadata to the group. To do this, the element must contain the internal metadata name of a `<MetadataType>` definition.

In addition, the `<metadata>` element may contain the attribute `num` to indicate the frequency of use in the context of the group.

```xml
<Group>
      <Name>Title</Name>
      <language name="de">Titel</language>
      <language name="en">Title</language>
      <metadata num="1o">NonSort</metadata>
      <metadata num="1m">SortingTitle</metadata>
      <metadata num="*">Subtitle</metadata>      
</Group>
```

The attribute `num` must contain one of the following values:

| Value of the attribute | Meaning of the attribute
|-- |-- |
| `*` | no time or any number of times (0...n) 
| `+` | once or as often as desired (1...n) 
| `1o` | no time or exactly once (0...1) 
| `1m` | exactly once (1) 

If no attribute `num` is specified, the value `*` is assumed by default.
