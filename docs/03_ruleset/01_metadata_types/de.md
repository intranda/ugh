---
title: Definition von Metadatentypen
published: true
keywords:
    - UGH
---

Ein Metadatentyp wird innerhalb des `<MetadataType>` Elements definiert. Dieses Element muss als einziges Kind das `<Name>` Element besitzen, welches den internen Namen des Metadatentyps enthält. Der interne Name wird auch zur Referenzierung auf den Metadatentyp aus der Serialisierungskonfiguration heraus genutzt. Er darf keine Leerzeichen enthalten.

## Mehrsprachigkeit

Weiterhin kann das `<MetadataType>` Element beliebig viele `<language>` Elemente als Kind enthalten. Diese `<language>` Elemente übersetzen den internen Namen des Metadatentyps in die jeweilige Sprache, die im Attribut name angegeben ist. Daher ist es sinnvoll, mindestens ein `<language>` Element pro Metadatentyp zu definieren, damit dessen Wert in der Benutzeroberfläche angezeigt und genutzt werden kann. Pro Sprache und `<MetadataType>` Element darf es nur ein einziges `<language>` Element geben.

_Beispiel für die Definition des Haupttitels „TitleDocMain“:_

```xml
<MetadataType>
		<Name>TitleDocMain</Name>
		<language name="de">HauptTitel</language>
		<language name="en">main title</language>
</MetadataType>
```

Die Reihenfolge der `<language>` Elemente spielt keine Rolle, da sie intern über die API anhand der Werte im Attribut `name` aufgerufen werden. Ist keine Sprache definiert, so gibt die API einen NULL-Wert zurück und die Applikation sollte den internen Namen des Metadatentyps nutzen.

## Personen

Handelt es sich bei dem definierten Feld nicht um ein Metadatum, sondern um eine Person, so ist dem `<MetadataType>` Element ein Attribut `type` mit dem Wert `person` hinzuzufügen.

_Beispiel für die Definition einer Person:_

```xml
<MetadataType type="person">
		<Name>Author</Name>
		<language name="en">Author</language>
		<language name="de">Autor</language>
</MetadataType>
```

## Körperschaften

Körperschaften werden ganz ähnlich definiert. Hierzu wird das Attribut `type` mit dem Wert `corporate` belegt.

_Beispiel für die Definition einer Körperschaft:_

```xml
  <MetadataType normdata="true" type="corporate">
    <Name>IssuingBody</Name>
    <language name="de">Herausgebendes Organ</language>
    <language name="en">Issuing body</language>
  </MetadataType>
```

## Identifier

Soll der Anker in einer separaten Datei gespeichert werden, wird zwingend ein Metadatum benötigt, das eine Referenzierung von Ankerdatei und der Datei der unterliegenden Struktureinheit ermöglicht. Daher muss in diesem Fall der Metadatentyp, der hierfür genutzt werden soll, ein Attribut `type` mit dem Wert `identifier` besitzen.

_Beispiel für die Festlegung eines Identifiers für die oben genannte Referenzierung:_

```xml
<MetadataType type="identifier">
    <Name>CatalogIDDigital</Name>
    <language name="de">PPN (digital)</language>
    <language name="en">PPN</language>
</MetadataType>
```

## Interne Metadatentypen

Metadatentypen, die mit einem Unterstrich `_` beginnen, sind sogenannte `interne Metadatentypen`. Diese können zum Beispiel beim Anlegen von Vorgängen mit Werten versehen werden - etwa durch einen OPAC-Import, der im Regelsatz festgelegt ist oder durch eine Definition in der Projekt-Konfigurationsdatei `projects.xml` von Goobi. Diese `internen Metadatentypen` werden nicht mit den Rückgabelisten für Strukturtypen - zum Beispiel `DocStruct.getAllVisibleMetadata()` - zurückgegeben. So sind sie beispielsweise im Goobi-Metadateneditor für Nutzer nicht sichtbar und auch nicht als neue Metadaten anlegbar.

_Beispiel für die Definition eines internen Metadatentyps:_

```xml
<MetadataType>
    <Name>_uccID</Name>
    <language name="en">UCC ID</language>
    <language name="de">UCC ID</language>
</MetadataType>
```

## Normdaten

Metadaten, bei denen das Attribut `normdata=“true“` gesetzt ist, besitzen weitere Felder, um einen Normdatensatz durch eine URL, einen Identifier und den Namen der Datenbank zu beschreiben.

_Beispiel für die Definition eines Metadatums mit Normdaten:_

```xml
<MetadataType normdata="true">
    <Name>Classification</Name>
    <language name="en">Classification</language>
    <language name="de">Klassifizierung </language>
</MetadataType>
```

Bei Definitionen von Personen kann zusätzlich zum weiter oben beschriebenen Attribut `normdata` auch das Attribut `namepart` gesetzt werden. Dadurch können zusätzliche Felder freigeschaltet werden, in denen weitere Informationen wie Lebensdaten oder Ansetzungsform erfasst werden können.

_Beispiel für die Definition einer Person mit zusätzlichen Feldern:_

```xml
<MetadataType type="person" namepart="true">
		<Name>Author</Name>
		<language name="en">Author</language>
		<language name="de">Autor</language>
</MetadataType>
```

## Validierung mittels regulärer Ausdrücke

Für Metadatentypen können Validierungsregeln als reguläre Ausdrücke definiert werden. Diese Validierungsregeln können dann innerhalb einer Nutzeroberfläche angezeigt und angewendet werden. Im Fall von Goobi workflow wird so beispielsweise sichergestellt, dass die Metadaten korrekt validiert werden könnnen, bevor eine Aufgabe mit schreibender Berechtigung auf die Metadaten durch einen Nutzer abgeschlossen wird.

Zusätzlich ist es möglich, individuelle Fehlermeldungen zu erstellen, die dem Nutzer im Falle eines Verstoßes gegen den regulären Ausdruck angezeigt werden. Hierbei kann der Platzhalter `{}` verwendet werden, um den eingetragenen Wert als Teil der Fehlermeldung zu nutzen. Wenn für die aktuell gewählte Sprache des Nutzers keine individuelle Fehlermeldung existiert, wird stattdessen eine Standard-Meldung angezeigt.

_Beispiel für eine Validierungsregel für einen Metadatentyp:_

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

## Zugriffsgeschützte Metadaten

Metadaten können zugriffsgeschützt sein. Hierzu kann das Attribut `allowAccessRestriction` vergeben werden. Für Metadaten mit dieser Angabe kann in einer Nutzeroberfläche eine Option zum Setzen des Zugriffsschutzes angeboten werden. Wenn das Metadatum zugriffsgeschützt ist, wird es zwar exportiert, jedoch mit der Angabe `shareable="no"` versehen. Diese Angabe kann dann von Portalen dann ausgewertet werden und die Anzeige für unberechtigte Personen unterdrücken.


_Beispiel für die Definition von zugriffsgeschützten Metadaten:_

```xml
<MetadataType allowAccessRestriction="true">
    <Name>Classification</Name>
    <language name="en">Classification</language>
    <language name="de">Klassifizierung </language>
</MetadataType>
```