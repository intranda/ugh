---
title: Definition of metadata types
published: true
keywords:
    - UGH
---

A metadata type is defined within the `<MetadataType>` element. This element must be the only child of the `<Name>` element, which contains the internal name of the metadata type. The internal name is also used to reference the metadata type from the serialization configuration. It must not contain any spaces.

## Multilingualism

Furthermore the `<MetadataType>` element can contain any number of `<language>` elements as a child. These `<language>` elements translate the internal name of the metadata type into the respective language specified in the name attribute. Therefore, it makes sense to define at least one `<language>` element per metadata type so that its value can be displayed and used in the user interface. There can only be one `<language>` element per language and `<MetadataType>` element.

_Example for the definition of the main title "TitleDocMain":_

```xml
<MetadataType>
        <Name>TitleDocMain</Name>
        <language name="de">HauptTitel</language>
        <language name="en">Main title</language>
</MetadataType>
```

The order of the `<language>` elements does not matter, since they are called internally via the API using the values in the attribute `name`. If no language is defined, the API returns a NULL value and the application should use the internal name of the metadata type.

## Persons

If the defined field is not a metadata but a person, an attribute `type` with the value `person` must be added to the `<MetadataType>` element.

_Example for the definition of a person:_

```xml
<MetadataType type="person">
        <Name>Author</Name>
        <language name="en">Author</language>
        <language name="de">Autor</language>
</MetadataType>
```

## Corporate bodies

Corporate bodies are defined in a very similar way. For this purpose, the attribute `type` is assigned the value `corporate`.

_Example for the definition of a corporate body:_

```xml
  <MetadataType normdata="true" type="corporate">
    <Name>IssuingBody</Name>
    <language name="de">Herausgebendes Organ</language>
    <language name="en">Issuing body</language>
  </MetadataType>
```

## Identifiers

If the anchor is to be saved in a separate file, a metadata is required that allows a reference between the anchor file and the file of the underlying structural unit. Therefore, in this case, the metadata type to be used for this purpose must have an attribute `type` with the value `identifier`.

_Example for the definition of an identifier for the above referencing:_

```xml
<MetadataType type="identifier">
    <Name>CatalogIDDigital</Name>
    <language name="de">PPN (digital)</language>
    <language name="en">PPN</language>
</MetadataType>
```

## Internal metadata types

Metadata types that begin with an underscore `_` are so-called `internal metadata types`. You can assign values to these metadata types when you create activities, for example, by means of an OPAC import defined in the rules set or by a definition in Goobi's `projects.xml` project configuration file. These `internal metadata types` are not returned with the structure type return lists - for example, `DocStruct.getAllVisibleMetadata()`. For example, they are not visible to users in the Goobi Metadata Editor and cannot be created as new metadata.

_Example for the definition of an internal metadata type:_

```xml
<MetadataType>
    <Name>_uccID</Name>
    <language name="en">UCC ID</language>
    <language name="de">UCC ID</language>
</MetadataType>
```

## Authority data

_Example for the definition of a metadata with authority data:_

Metadata with the attribute `normdata="true"` has additional fields to describe a standard record by a URL, an identifier and the name of the database.

```xml
<MetadataType normdata="true">
    <Name>Classification</Name>
    <language name="en">Classification</language>
    <language name="de">Klassifizierung </language>
</MetadataType>
```

For definitions of persons, the attribute `namepart` can be set in addition to the `normdata` attribute described above. This enables additional fields to be activated in which further information such as life data or form of attachment can be entered.

_Example for the definition of a person with additional fields:_

```xml
<MetadataType type="person" namepart="true">
        <Name>Author</Name>
        <language name="en">Author</language>
        <language name="de">Autor</language>
</MetadataType>
```

## Validation using regular expressions

For metadata types, validation rules can be defined as regular expressions. These validation rules can then be displayed and applied within a user interface. In the case of Goobi workflow, for example, this ensures that metadata can be validated correctly before a task with write permission on the metadata is completed by a user.

In addition, it is possible to create individual error messages that are displayed to the user in the event of a violation of the regular expression. Here, the placeholder `{}` can be used to use the entered value as part of the error message. If no individual error message exists for the user's currently selected language, a standard message is displayed instead.

_Example of a validation rule for a metadata type:_

```xml
<MetadataType>
    <Name>DocLanguage</Name>
    <language name="de">Sprache</language>
    <language name="en">Language</language>
    <language name="es">Idioma</language>
    <validationExpression>[a-z]{3}</validationExpression>
    <validationErrorMessage name="de">Der Wert muss einem dreistelligen iso 639 code entsprechen. Gefunden wurde jedoch '{}'.</validationErrorMessage>
    <validationErrorMessage name="en">The value '{}' does not correspond to a three-letter iso 639 code.</validationErrorMessage>    
</MetadataType>
```

## Access-restricted metadata

Metadata can be access-restricted. The `allowAccessRestriction` attribute can be assigned for this purpose. An option to set access restriction can be offered in a user interface for metadata with this specification. If the metadata is access-restricted, it is exported, but with the specification `shareable=‘no’`. This specification can then be evaluated by portals and suppress the display for unauthorised persons.


Example for the definition of access-protected metadata:_

```xml
<MetadataType allowAccessRestriction="true">
    <Name>Classification</Name>
    <language name="en">Classification</language>
    <language name="de">Klassifizierung </language>
</MetadataType>
```
