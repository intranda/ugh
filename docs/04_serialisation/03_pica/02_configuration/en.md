---
title: Configuration
published: true
---

The PICA+ configuration takes place within the enclosing `<PicaPlus>` element, which is directly subordinate to the `<Formats>` element. Within the `<PicaPlus>` element, the type-specific elements `<Person>`, `<Metadata>` and `<DocStruct>` are used, each of which maps an internal data type. As with the RDF/XML format, a 1:1 mapping is always performed here.

The characteristics of the PICA+ structure are used for the mapping. Within these type-specific elements, the following sub-elements are permitted:

| Tag | Description |
| :--- | :--- |
| `<picaMainTag>` | The PICA+ field number. This element must exist exactly once. |
| `<picaSubTag>` | The PICA+ field separator within the field specified with `<picaMainTag>`. The content of this subfield corresponds to the value of the metadata specified in the `<name>` element. Within the `<Metadata>` and `<DocStruct>` element, this field must exist exactly once. Different rules apply to the `<Person>` element (see below). |
| `<valueCondition>` | Conditional mapping: By specifying a regular expression (Perl5 syntax), a condition can be specified here. Only if this condition applies to the content of the PICA+ field defined above, the value is assigned to the internal metadata. This field is optional, see example below. |
| `<name>` | The internal metadata name. This element must also exist exactly once. |
| `<valueRegExp>` | Subsequent editing of PICA+ field values: By specifying a regular expression (Perl5 syntax), the value of the metadata can be manipulated. This field is optional, see example below. |

_Example: Mapping of PICA+ fields_

```xml
<Metadata>
      <picaMainTag>021A</picaMainTag>
      <picaSubTag>a</picaSubTag>
      <Name>TitleDocMain</Name>
</Metadata>
```

_Example: Use of conditional assignments_

```xml
<Metadata>
    <picaMainTag>007S</picaMainTag>
    <picaSubTag>0</picaSubTag>
    <ValueCondition>/^VD17/</ValueCondition>
    <Name>CatalogFieldVDseventeen</Name>
    <ValueRegExp>s/^VD17\s(.*)/$1/</ValueRegExp>
</Metadata>
<Metadata>
    <picaMainTag>007S</picaMainTag>
    <picaSubTag>0</picaSubTag>
    <ValueCondition>/^VD18/</ValueCondition>
    <Name>CatalogFieldVDeighteen</Name>
    <ValueRegExp>s/^VD18\s(.*)/$1/</ValueRegExp>
</Metadata>
```

The value of `picaMainTag 007s` and `picaSubTag 0` is assigned to the internal metadata `CatalogFieldVDseventeen` only if its content starts with `DVD17`. If the content of the PICA+ field starts with `Vd18`, it will be assigned to the internal metadata `CatalogFieldVDeighteen`.

If one of the conditions is met, the `VD17` or the `VD18` in front of the actual VD17 or VD18 number (including the space) is also removed.

_Example: Manipulation of metadata during import_

```xml
<Metadata>
    <picaMainTag>003@</picaMainTag>
    <picaSubTag>0</picaSubTag>
    <Name>CatalogIDDigital</Name>
    <ValueRegExp>s/(.*)/PPN$1/</ValueRegExp>
</Metadata>
```

This regular expression adds the string `PN` to the value of PICA+ (PPN).

Since persons have other characteristics and are not just a simple type-value pair, a `<picaSubTag>` can exist within the `<person>` element for each of these characteristics. The corresponding attribute `type` specifies the corresponding characteristic. The following values are valid for this attribute:

| Attribut | Description |
| :--- | :--- |
| `firstname` | First name of the person |
| `lastname` | Last name of the person |
| `identifier` | Identifier of the person (e.g. from an authority file) |
| `expansion` | First and last name can also be extracted from the pica field "Expansion of the attachment form". |

_Example: Mapping of persons_

```xml
<Person>
<picaMainTag>028A</picaMainTag> <Name>Author</Name>
<picaSubTag type="firstname">d</picaSubTag> <picaSubTag type="lastname">a</picaSubTag> <picaSubTag type="identifier">9</picaSubTag> <picaSubTag type="expansion">8</picaSubTag>
</Person>
```

Corporations also consist of several fields. They are defined within a `<Corporate>` element. Here, similar to the persons, the `type` attribute for `<picaSubTag>` is available to define into which field a value is to be imported. The following values are possible:

| Attribute | Description |
|-- |-- |
| `mainName` | This field contains the `code` of the `<subfield>` element from which the main name of the corporation is to be imported. The field is repeatable to allow different `code` to be specified. If more than one field is found, the first value will be imported. |
| `subName` | This field contains the `code` from which further name details of the corporation are to be imported. The field is repeatable to allow different `code` to be specified. If more than one field is found, each value is imported separately. |
| `partName` | This field contains the `code` from which to import censuses, locations or dates for the corporation. The field is repeatable to allow different `codes` to be specified. If more than one field is found, all values are imported into a common field, the individual entries are separated by the value configured in `<separator>`. |
| `identifier` |  This field contains the `code` of the `<subfield>` element in which a standard data identifier of the entity has been defined. |

_Beispiel: Mapping von Körperschaften_

```xml
<Corporate>
    <Name>Corporation</Name>
    <picaMainTag>029F</picaMainTag>
    <picaSubTag type="mainName">a</picaSubTag>
    <picaSubTag type="subName">b</picaSubTag>
    <picaSubTag type="partName">c</picaSubTag>
    <picaSubTag type="partName">d</picaSubTag>
    <picaSubTag type="partName">n</picaSubTag>
    <picaSubTag type="identifier">7</picaSubTag>
</Corporate>
```

Since the type of the respective document structure is dependent on the value of a field, there is another mandatory element within the element `<DocStrct>`. Only if the element specified with `<picaMainTag>` and `<picaSubTag>` has the content defined in `<picaContent>`, a corresponding document structure is created and the metadata is assigned to it. The type of the document structure is specified in the element `<name>`.

This can also result in an n:1 mapping, i.e. several types from the pica system can be assigned to an internal type. This is necessary because the letter combination of the PICA+ format contains not only the bibliographic type but also the appearance (printed, digital, microform). Accordingly, a mapping must be made for each letter combination.

_Example: Mapping of a MultiVolume work_

```xml
<DocStruct>
      <picaMainTag>002@</picaMainTag>
      <picaSubTag>0</picaSubTag>
      <picaContent>Oc</picaContent>
      <Name>MultivolumeWork</Name>
</DocStruct>
<DocStruct>
      <picaMainTag>002@</picaMainTag>
      <picaSubTag>0</picaSubTag>
      <picaContent>Ac</picaContent>
      <Name>MultivolumeWork</Name>
</DocStruct>
```

For the mapping of document structures, it should be noted that only the first letters defined in the rule set are compared. This is virtually equivalent to a truncated search. Since in the example above only two letters are specified in the field `<picaContent>`, only the first two letters in the PICA+ file are taken into account. The comparison is also case-sensitive.
