---
title: Metadata Type Mapping
published: true
---

However, the main part of the configuration for this serialization class is the mapping of the metadata types to specific tables and table columns. To understand the explanations described below, it is therefore useful to be familiar with the AGORA database schema.

For each metadata type there is a `<Metadata>` element. Within this element the table, column or even a value is selected. Each `<Metadata>` element must have a `<InternalName>` and `<TableName>` subelement. `<InternalName>` contains the internal name of the metadata type and `<TableName>` contains the name of the table in which the value of the metadata is to be stored. Depending on the database management system, it may be necessary to pay attention to upper and lower case. According to the relation between structural unit and metadata, there are several ways to store the metadata:

| Relation | Description |
| :--- | :--- |
| 1:1 | Metadata of the corresponding type may be used a maximum of once per structural unit the metadata is therefore stored in the same table like the structural unit. In the AGORA database schema, this is the `DOC` table for structural units of the logical structure and the `ISET` table for force units in the physical structure. All you have to do is the corresponding table column is specified in the element `<TableColumn>`1m, `1o` or `+`.<br/>*Example:*<br/>`<Metadata>`<br/>`<InternalName>CatalogIDDigital</InternalName>`<br/>`<TableName>Doc</TableName>`<br/>`<TableColumn>CatalogIDDigital</TableColumn>`<br/>`</Metadata>` |
| 1:n | The metadata can occur several times for a structural unit. In this case, there are two tables. The DocAttribute table contains the type of the metadata, the `DocAttribValue` table links the metadata value plus the metadata value to the table for the structural unit (DOC table). In this case, the value `DocAttribute` must be specified in `<TableName>`. The element `<FieldValue>` must then have the value of the internal attribute name as stored in the `DocAttribute` table. The element `<TableColumn>` does not exist.<br/>*Beispiel:*<br/>`<Metadata>`<br/>`<InternalName>PublisherName</InternalName> <TableName>DocAttribute</TableName>`<br/>`<FieldValue>Publisher</FieldValue>`<br/>`</Metadata>` |
| m:n | A metadata can occur several times for a structural unit. In contrast to the 1:n relationship, however, the metadata value is used several times for different force units. This model is used for the `DigitalCollection` and the `PlacePublication` table. If data is to be stored in one of these two tables, the `<Metadata>` element must contain only one `<TableName>` element. This must contain the value PlacePublication or DigitalCollection.<br/><br/>*Beispiel:*<br/>`<Metadata>`<br/>`<InternalName>PlaceOfPublication</InternalName> <TableName>PlacePublication</TableName>`<br/>`</Metadata>` |
| Personen | People are stored in a separate table. This is necessary because the additional characteristics of a person object have no place in the conventional metadata tables. The `<FieldValue>` element specifies in this case the role name that this person type has in the database. This is stored in the database in the `CreatorType` table. The element `<TableName>` must have the value `Creator`.<br/>Example:<br/>`<Metadata>`<br/>`<InternalName>Author</InternalName>`<br/>`<TableName>Creator</TableName>`<br/>`<FieldValue>AUTHOR</FieldValue>`<br/>`</Metadata>` |


In addition, not only the types can be converted, but also the values of a metadata. Value lists, which must be defined within the `<AGORADATABASE>` element, are used for this purpose. Such a value list can be used for the value conversion by inserting the `<ValueList>` element. This element must contain the name of the value list. It must be ensured that a corresponding value list with this name also exists.


_Beispiel: Nutzung einer Werteliste für die Wertkonvertierung_

```xml
<Metadata>
    <InternalName>DocLanguage</InternalName>
    <TableName>Doc</TableName>
    <TableColumn>IDLanguage</TableColumn>
    <ValueList>languagelist</ValueList>
</Metadata>
```

