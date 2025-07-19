---
title: Konfiguration
published: true
---

Die Konfiguration erfolgt im Regelsatz innerhalb des `<Formats>` Elements. Dort kann ein `<Marc>` Element angelegt werden. Innerhalb dieses Elements sind die vier Elemente `<Person>`, `<Metadata>`, `<Group>` und `<DocStruct>` erlaubt, die jeweils mit einem internen Datentyp verknüpft werden müssen.

## Metadata

Mit Hilfe des `<Metadata>` Elements wird der Import von einfachen Metadaten konfiguriert. Innerhalb des Elements sind folgende Unterelemente erlaubt:

### Unterelement `<Name>`

Der interne Metadatenname. Dieses Element muss genau einmal vorhanden sein. Der Inhalt des Feldes muss einem Namen eines `<MetadataType>` Elements entsprechen.

### Unterelement `<field>`

Mit diesem Feld wird ein `datafield` eines MARC Records beschrieben. Dieses Feld ist wiederholbar und muss mindestens einmal existieren. Es wird in der [Sektion field](https://docs.intranda.com/ugh-de/4/4.1/4.1.2#4-1-2-3-field) genauer beschrieben.

### Unterelement `<identifierfield>`

Mit diesem Element kann das `subfield` definiert werden, das den Identifier des Metadatums enthält. Dieser Identifier kann auf Normadatenbanken wie die GND verweisen.

Dieses Feld ist optional.

### Unterelement `<identifierConditionField>`

In diesem Element kann ein regulärer Ausdruck definiert werden, den der Identifier erfüllen muss. Dies kann notwendig werden, wenn mehrere Identifier zu verschiedenen Datenbanken angegeben sind.

Dieses Feld ist optional.

### Unterelement `<identifierReplacement>`

In diesem Feld kann ein regulärer Ausdruck definiert werden, der auf den Identifier ausgeführt wird. Dies kann zum Beispiel genutzt werden, um einen unerwünschten Prefix zu entfernen.

Dieses Feld ist optional.

### Unterelement `<conditionField>`

In diesem Element kann der Code eines `<subfield>` definiert werden, dessen Inhalt auf eine in `<conditionValue>` definierte Bedingung überprüft werden soll. Das Metadatum wird nur dann erzeugt, wenn das `<subfield>` existiert und der Bedingung entspricht.

Dieses Feld ist optional. Wenn es existiert, muss auch `<conditionValue>` existieren.

### Unterelement `<conditionValue>`

Hier kann ein regulärer Ausdruck definiert werden. Der Inhalt des subfields, das in `<conditionField>` definiert wurde, muss diesen Ausdruck erfüllen, damit das Metadatum erzeugt wird. Hiermit kann zum Beispiel geprüft werden, ob eine Person oder Institution einer bestimmten Rolle entspricht.

Dieses Feld ist optional. Wenn es existiert, muss auch `<conditionField>` konfiguriert sein.

### Unterelement `<fieldReplacement>`

In diesem Feld kann ein regulärer Ausdruck definiert werden, der auf den Feldinhalt angewendet wird. Damit können zum Beispiel unerwünschte Klammern von Datumsangaben entfernt werden.

### Unterelement `<separateEntries>`

Dieses Feld steuert, wie mit mehrfach vorkommenden Feldern umgegangen werden soll. Es kann die Werte `true` und `false` enthalten.

Wurde true gesetzt, wird für jedes gefundene Feld ein eigenes Metadatum erstellt.

Wenn der Wert `false` ist, werden alle Inhalte in ein einzelnes Metadatum geschrieben, die einzelnen Werte werden dann durch den Inhalt des `<separator>` Feldes getrennt. Wenn das Feld nicht konfiguriert wurde, wird jedes Feld als eigenes Metadatum angelegt.

### Unterelement `<separator>`

Dieses Feld definiert die Zeichenfolge, die als Trennzeichen zwischen den Inhalten bei mehrfach vorkommenden Feldern genutzt werden soll, wenn `<separateEntries>` auf `false` gesetzt wurde. Fehlt dieses Feld, wird `Semikolon`, gefolgt von einem `Leerzeichen` genutzt.

## Person

Mit dem `<Person>` Element wird der Import von Personen konfiguriert. Innerhalb des Feldes sind alle Elemente erlaubt, die auch für `<Metadata>` möglich sind. Der einzige Unterschied der beiden Definitionen wird in [Sektion field](https://docs.intranda.com/ugh-de/4/4.1/4.1.2#4-1-2-3-field) beschrieben.

## field

Das `<field>` Element erlaubt die genaue Beschreibung zu einem einzelnen Metadatum innerhalb des MARC records.

### Unterelement `<fieldMainTag>`

Die `<datafield>` tag Nummer.

Das Feld muss genau einmal vorhanden sein

### Unterelement `<fieldInd1>`

Der Wert des `ind1` Attributes des `<datafield>` Elements. Hier kann eine Zahl, ein Leerzeichen oder `any` eingegeben werden.

Fehlt das Feld, wird `any` genutzt.

### Unterelement `<fieldInd2>`

Der Wert des `ind2` Attributes des `<datafield>` Elements. Hier kann eine Zahl, ein Leerzeichen oder `any` eingegeben werden.

Fehlt das Feld, wird `any` genutzt.

### Unterelement `<fieldSubTag>`

Der Wert des `code` Attributes des `<subfield>` Elements, in dem sich der zu importierende Text befindet.

Das Feld muss bei `<Metadata>` genau einmal vorhanden sein.

### Unterelement `<firstname>`

Der Wert des `code` Attributes des `<subfield>` Elements, in dem der Vorname einer Person enthalten ist.

Bei `<Person>` muss entweder `<firstname>` und `<lastname>` oder `<expansion>` einmal vorhanden sein. Dieses Feld ist wiederholbar. Wenn mehrere Felder konfiguriert wurden und vorhanden sind, werden die einzelnen Einträge in der angegebenen Reihenfolge durch den <separator> getrennt übernommen.

### Unterelement `<lastname>`

Der Wert des `code` Attributes des `<subfield>` Elements, in dem der Nachname einer Person enthalten ist.

Bei `<Person>` muss entweder `<firstname>` und `<lastname>` oder `<expansion>` einmal vorhanden sein. Dieses Feld ist wiederholbar. Wenn mehrere Felder konfiguriert wurden und vorhanden sind, werden die einzelnen Einträge in der angegebenen Reihenfolge durch den <separator> getrennt übernommen.

### Unterelement `<expansion>`

Der Wert des `code` Attributes des `<subfield>` Elements, in dem die Expansion des Namens enthalten ist. Beim Import wird der Name am `Komma` in Vorname und Nachname aufgeteilt.

Bei `<Person>` muss entweder `<firstname>` und `<lastname>` oder `<expansion>` einmal vorhanden sein. Wurden <firstname>, <lastname> und <expansion> angegeben, wird <expansion> nur dann ausgewertet, wenn kein Nachname gefunden werden konnte.

### Unterelement `<fieldMainName>`

Dieses Feld enthält den `code` des `<subfield>` Elements, aus dem der Hauptname der Körperschaft importiert werden soll. Das Feld ist wiederholbar, um verschiedene `code` angeben zu können. Wenn mehr als ein Feld gefunden wurde, wird der erste Wert importiert.

### Unterelement `<fieldSubName>`

Dieses Feld enthält den `code` des `<subfield>` Elements, aus dem weitere Namensangaben der Körperschaft importiert werden sollen. Das Feld ist wiederholbar, um verschiedene `code` angeben zu können. Wenn mehr als ein Feld gefunden wurde, wird jeder Wert separat importiert.

### Unterelement `<fieldPartName>`

Dieses Feld enthält den `code` des `<subfield>` Elements, aus dem zusätzliche Angaben zur Körperschaft importiert werden sollen. Das Feld ist wiederholbar, um verschiedene `code` angeben zu können. Wenn mehr als ein Feld gefunden wurde, werden alle Werte in ein gemeinsames Feld importiert, die einzelnen Einträge werden durch den in `<separator>` konfigurierten Wert getrennt.

## Group

In `<Group>` kann eine Metadatengruppe importiert werden. Das `<Group>` Element kann folgende Unterelemente enthalten:

### Unterelement `<Name>`

Der interne Name der Gruppe.

Dieses Element muss genau einmal vorhanden sein. Der Inhalt des Feldes muss einem Namen einer `<Group>` Definition entsprechen.

### Unterelement `<Metadata>`

Konfiguration eines Metadatums innerhalb der Gruppe. Die Konfiguration entspricht der `<Metadata>` Konfiguration. Es können nur Metadaten definiert werden, die innerhalb der Gruppe erlaubt sind.

Das Feld ist wiederholbar.

### Unterelement `<Person>`

Konfiguration einer Person innerhalb der Gruppe. Die Konfiguration entspricht der `<Person>` Konfiguration. Es können nur Personen definiert werden, die innerhalb der Gruppe erlaubt sind.

Das Feld ist wiederholbar.

## DocStruct

Mit Hilfe der `<DocStruct>` Definitionen können die einzelnen Dokumententypen definiert werden.

### Unterelement `<Name>`

Der interne Name des Strukturelements.

Dieses Element muss genau einmal vorhanden sein. Der Inhalt des Feldes muss dem Namen eines `<DocStrctType>` Elements entsprechen.

### Unterelement `<leader6>`

In diesem Feld kann der Wert gesetzt werden, der an sechster Position im `<leader>` erwartet wird.

Dieses Feld muss genau einmal existieren.

### Unterelement `<leader7>`

In diesem Feld kann der Wert gesetzt werden, der an siebter Position im `<leader>` erwartet wird.

Dieses Feld muss genau einmal existieren.

### Unterelement `<leader19>`

In diesem Feld kann der Wert gesetzt werden, der an der 19. Position im `<leader>` erwartet wird.

Dieses Feld ist optional.

### Unterelement `<field007_0>`

In diesem Feld kann der Wert gesetzt werden, der an nullter Position im `<controlfield tag=“007“>` erwartet wird.

Dieses Feld ist optional. Wenn das Feld einen Wert enthält, muss das `controlfield` auch existieren.

### Unterelement `<field007_1>`

In diesem Feld kann der Wert gesetzt werden, der an erster Position im `<controlfield tag=“007“>` erwartet wird.

Dieses Feld ist optional. Wenn das Feld einen Wert enthält, muss das `controlfield` auch existieren.

### Unterelement `<field008_21>`

In diesem Feld kann der Wert gesetzt werden, der an 21. Position im `<controlfield tag=“008“>` erwartet wird.

Dieses Feld ist optional. Wenn das Feld einen Wert enthält, muss das `controlfield` auch existieren.

## Corporate

Das `<Corporate>` Element dient zur Konfiguration des Imports von Körperschaften. Innerhalb des Feldes sind alle Elemente erlaubt, die auch für `<Metadata>` möglich sind. Der einzige Unterschied der beiden Definitionen wird in Sektion `field` beschrieben.
