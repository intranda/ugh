---
title: Metadatentyp-Mapping
published: true
---

Hauptbestandteil der Konfiguration für diese Serialisierungsklasse ist jedoch das Mapping der Metadatentypen in bestimmte Tabellen und Tabellenspalten. Für das Verständnis der unten beschriebenen Erläuterungen ist die Kenntnis des AGORA-Datenbankschemas daher sinnvoll.

Für jeden Metadatentyp existiert ein `<Metadata>` Element. Innerhalb dieses Elements wird die Tabelle, die Spalte oder sogar ein Wert ausgewählt. Jedes `<Metadata>` Element muss ein Unterelement `<InternalName>` und `<TableName>` besitzen. `<InternalName>` enthält den internen Namen des Metadatentyps und `<TableName>` enthält den Namen der Tabelle, in welcher der Wert des Metadatums gespeichert werden soll. Je nach Datenbankmanagementsystem ist hier evtl. auf Groß/Kleinschreibung zu achten. Entsprechend der Relation zwischen Struktureinheit und Metadatum gibt es mehrere Möglichkeiten, das Metadatum zu speichern:

| Relation | Beschreibung |
| :------- | :----------- |
| 1:1      | Metadaten des entsprechenden Typs dürfen max. einmal pro Struktureinheit auftreten, das Metadatum wird daher in der gleichen Tabelle gespeichert wie die Struktureinheit. Dies ist in dem AGORA-Datenbankschema die `DOC`-Tabelle für Struktureinheiten der logischen Struktur und die `ISET`-Tabelle für Struktureinheiten der physischen Struktur. Hierzu muss lediglich die entsprechende Tabellenspalte im Element `<TableColumn>` angegeben werden. Zu beachten ist jedoch, dass die entsprechende Relation zwischen Strukturtyp und Metadatum im Regelsatz (num-Attribut) auf `1m`, `1o` oder `+` steht.<br/>*Beispiel:*<br/>`<Metadata>`<br/>`<InternalName>CatalogIDDigital</InternalName>`<br/>`<TableName>Doc</TableName>`<br/>`<TableColumn>CatalogIDDigital</TableColumn>`<br/>`</Metadata>` |
| 1:n      | Das Metadatum kann mehrmals für eine Struktureinheit vorkommen. In diesem Fall gibt es zwei Tabellen. Die DocAttribute-Tabelle enthält den Typ des Metadatums, die `DocAttribValue`-Tabelle verknüpft zzgl. des Metadatumwerts mit der Tabelle für die Struktureinheit (DOC-Tabelle). In diesem Fall ist in `<TableName>` der Wert `DocAttribute` anzugeben. Das Element `<FieldValue>` muss dann den Wert des internen Attributnamen besitzen, wie er in der `DocAttribute` Tabelle gespeichert ist. Das Element `<TableColumn>` existiert nicht.<br/>*Beispiel:*<br/>`<Metadata>`<br/>`<InternalName>PublisherName</InternalName> <TableName>DocAttribute</TableName>`<br/>`<FieldValue>Publisher</FieldValue>`<br/>`</Metadata>` |
| m:n      | Ein Metadatum kann mehrmals für eine Struktureinheit vorkommen. Im Gegensatz zu der 1:n Verknüpfung wird allerdings der Metadatenwert mehrmals für unterschiedliche Struktureinheiten verwendet. Dieses Modell findet für die `DigitalCollection`- und die `PlacePublication`-Tabelle Anwendung. Sollen Daten in einer dieser beiden Tabellen gespeichert werden, muss das `<Metadata>` Element lediglich ein `<TableName>` Element enthalten. Dieses muss den Wert PlacePublication oder DigitalCollection enthalten.<br/>*Beispiel:*<br/>`<Metadata>`<br/>`<InternalName>PlaceOfPublication</InternalName> <TableName>PlacePublication</TableName>`<br/>`</Metadata>` |
| Personen | Personen werden in einer eigenen Tabelle gespeichert. Dies ist notwendig, da die zusätzlichen Merkmale eines Person-Objekts keinen Platz in den herkömmlichen Metadatentabellen haben. Das `<FieldValue>` Element gibt in diesem Fall den Rollennamen an, den dieser Personentyp in der Datenbank besitzt. Dieser wird in der Datenbank in der `CreatorType`-Tabelle gespeichert. Das Element `<TableName>` muss dazu zwingend den Wert `Creator` besitzen.<br/>Beispiel:<br/>`<Metadata>`<br/>`<InternalName>Author</InternalName>`<br/>`<TableName>Creator</TableName>`<br/>`<FieldValue>AUTHOR</FieldValue>`<br/>`</Metadata>` |

Zusätzlich können nicht nur die Typen konvertiert werden, sondern auch die Werte eines Metadatums. Dazu dienen Wertelisten, die innerhalb des `<AGORADATABASE>` Elements definiert sein müssen. Eine solche Werteliste kann für die Wertkonvertierung durch einfügen des Elements `<ValueList>` genutzt werden. Dieses Element muss den Namen der Werteliste enthalten. Es ist sicherzustellen, dass eine entsprechende Werteliste mit diesem Namen auch existiert.

_Beispiel: Nutzung einer Werteliste für die Wertkonvertierung_

```xml
<Metadata>
    <InternalName>DocLanguage</InternalName>
    <TableName>Doc</TableName>
    <TableColumn>IDLanguage</TableColumn>
    <ValueList>languagelist</ValueList>
</Metadata>
```

