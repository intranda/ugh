---
title: Configuration
published: true
---

The configuration takes place in the rule set within the `<format>` element. A `<Marc>` element can be created there. Within this element the four elements `<Person>`, `<Metadata>`, `<Group>` and `<DocStruct>` are allowed, each of which must be linked to an internal data type.

## Metadata

The `<Metadata>` element is used to configure the import of simple metadata. Within the element the following sub-elements are allowed:

### Subelement `<Name>`

The internal metadata name. This element must exist exactly once. The content of the field must correspond to a name of a `<MetadataType>` element.

### Subelement `<field>`

This field is used to describe a `datafield` of a MARC record. This field can be repeated and must exist at least once. It is described in detail in the section [field](/en/other/ugh/serialisation/marc/configuration#field).

### Subelement `<identifierfield>`

This element can be used to define the `subfield` containing the identifier of the metadata. This identifier can refer to norm databases like the GND.

This field is optional.

### Subelement `<identifierConditionField>`

In this element, a regular expression can be defined that the identifier must satisfy. This may become necessary if several identifiers are specified for different databases.

This field is optional.

### Subelement `<identifierReplacement>`

In this field a regular expression can be defined which is executed on the identifier. This can be used, for example, to remove an unwanted prefix.

This field is optional.

### Subelement `<conditionField>`

In this element the code of a `<subfield>` can be defined, whose content is to be checked for a condition defined in `<conditionValue>`. The metadata is only created if the `<subfield>` exists and matches the condition.

This field is optional. If it exists, `<conditionValue>` must also exist.

### Subelement `<conditionValue>`

A regular expression can be defined here. The content of the subfield defined in `<conditionField>` must match this expression in order for the metadata to be generated. This can be used, for example, to check whether a person or institution corresponds to a particular role.

This field is optional. If it exists, `<conditionField>` must also be configured.

### Subelement `<fieldReplacement>`

In this field a regular expression can be defined which is applied to the field content. This can be used, for example, to remove unwanted brackets from dates.

### Subelement `<separateEntries>`

This field controls how to deal with fields that occur more than once. It can contain the values `true` and `false`.

If `true` was set, a separate metadata is created for each field found.

If the value is `false`, all contents are written to a single metadata, the individual values are then separated by the contents of the `<separator>` field. If the field is not configured, each field is created as a separate metadata.

### Subelement `<separator>`

This field defines the character string to be used as a separator between the contents of fields with multiple occurrences if `<separateEntries>` is set to `false`. If this field is missing, `semicolon` followed by a `space` is used.

## Person

The `<Person>` element is used to configure the import of persons. Within the field all elements are allowed, which are also possible for `<Metadata>`. The only difference between the two definitions is described in section [field](/en/other/ugh/serialisation/marc/configuration#field).

## field

The `<field>` element allows the exact description of a single metadata within the MARC record.

### Subelement `<fieldMainTag>`

The `<datafield>` tag number.

The field must exist exactly once.

### Subelement `<fieldInd1>`

The value of the `ind1` attribute of the `<datafield>` element. Here you can enter a number, a space or `any`.

If the field is missing, `any` is used.

### Subelement `<fieldInd2>`

The value of the `ind2` attribute of the `<datafield>` element. Here you can enter a number, a space or `any`.

If the field is missing, `any` is used.

### Subelement `<fieldSubTag>`

The value of the `code` attribute of the `<subfield>` element containing the text to be imported.

The field must exist exactly once for `<Metadata>`.

### Subelement `<firstname>`

The value of the `code` attribute of the `<subfield>` element containing the first name of a person.

With `<person>` either `<firstname>` and `<lastname>` or `<expansion>` must exist once. This field is repeatable. If several fields have been configured and are present, the individual entries are taken over in the specified order separated by the `<separator>`.

### Subelement `<lastname>`

The value of the `code` attribute of the `<subfield>` element containing the last name of a person.

With `<person>` either `<firstname>` and `<lastname>` or `<expansion>` must exist once. This field is repeatable. If several fields have been configured and are present, the individual entries are taken over in the specified order separated by the `<separator>`.

### Subelement `<expansion>`

The value of the `code` attribute of the `<subfield>` element containing the expansion of the name. During import, the name is split into first name and last name at the `comma`.

With `<person>` either `<firstname>` and `<lastname>` or `<expansion>` must exist once. If `<firstname>`, `<lastname>` and `<expansion>` were specified, `<expansion>` is only evaluated if no last name could be found.

### Subelement `<fieldMainName>`

This field contains the `code` of the `<subfield>` element from which the main name of the corporation is to be imported. The field is repeatable to allow different `code` to be specified. If more than one field is found, the first value will be imported.

### Subelement `<fieldSubName>`

This field contains the `code` of the `<subfield>` element from which further name details of the corporation are to be imported. The field is repeatable to allow different `code` to be specified. If more than one field is found, each value will be imported separately.

### Subelement `<fieldPartName>`

This field contains the `code` of the `<subfield>` element from which additional information about the corporation is to be imported. The field is repeatable to allow different `codes` to be specified. If more than one field is found, all values are imported into a common field, the individual entries are separated by the value configured in `<separator>`.

## Group

In `<Group>` a metadata group can be imported. The `<Group>` element can contain the following subelements:

### Subelement `<Name>`

The internal name of the group.

This element must exist exactly once. The content of the field must correspond to a name in a `<Group>` definition.

### Subelement `<Metadata>`

Configuration of a metadata within the group. The configuration corresponds to the `<Metadata>` configuration. Only metadata that is allowed within the group can be defined.

The field is repeatable.

### Subelement `<Person>`

Configuration of a person within the group. The configuration corresponds to the `<person>` configuration. Only persons that are allowed within the group can be defined.

The field is repeatable.

## DocStruct

With the help of the `<DocStruct>` definitions the individual document types can be defined.

### Subelement `<Name>`

The internal name of the force element.

This element must exist exactly once. The content of the field must correspond to the name of a `<DocStrctType>` element.

### Subelement `<leader6>`

In this field you can set the value that is expected at the sixth position in the `<leader>`.

This field must exist exactly once.

### Subelement `<leader7>`

In this field you can set the value that is expected at the seventh position in the `<leader>`.

This field must exist exactly once.

### Subelement `<leader19>`

In this field you can set the value that is expected at the 19th position in the `<leader>`.

This field is optional.

### Subelement `<field007_0>`

In this field the value can be set which is expected at zero position in the `<controlfield tag="007">`.

This field is optional. If the field contains a value, the `controlfield` must also exist.

### Subelement `<field007_1>`

In this field you can set the value that is expected at the first position in the `<controlfield tag="007">`.

This field is optional. If the field contains a value, the `controlfield` must also exist.

### Subelement `<field008_21>`

In this field you can set the value that is expected at the 21st position in the `<controlfield tag="008">`.

This field is optional. If the field contains a value, the `controlfield` must also exist.


## Corporate

The `<Corporate>` element is used to configure the import of corporate bodies. Within the field, all elements are allowed that are also possible for `<Metadata>`. The only difference between the two definitions is described in section `field`.
