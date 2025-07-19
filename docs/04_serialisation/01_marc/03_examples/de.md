---
title: Beispielkonfigurationen
published: true
---

## Beispiel: Mapping von Metadaten

Daten aus MARC XML:

```xml
<datafield tag="245" ind1="1" ind2="0">
    <subfield code="a">[Stammbuch Pauline Pichler]</subfield>
    <subfield code="h">Manuskript</subfield>
</datafield>
```

Mapping im Regelsatz zur Datenübernahme:

```xml
<Metadata>
    <Name>TitleDocMain</Name>
    <field>
        <fieldMainTag>245</fieldMainTag>
        <fieldSubTag>a</fieldSubTag>
        <fieldInd1>any</fieldInd1>
        <fieldInd2>0</fieldInd2>
    </field>
</Metadata>
```

## Beispiel: Entfernen von Klammern

Daten aus MARC XML:

```xml
<datafield tag="245" ind1="1" ind2="0">
    <subfield code="a">[Stammbuch Pauline Pichler]</subfield>
    <subfield code="h">Manuskript</subfield>
</datafield>
```

Mapping im Regelsatz zur Datenübernahme:

```xml
<Metadata><Name>TitleDocMain</Name>
    <field>
        <fieldMainTag>245</fieldMainTag>
        <fieldSubTag>a</fieldSubTag>
        <fieldInd1>any</fieldInd1>
        <fieldInd2>0</fieldInd2>
    </field>
    <fieldReplacement>s/\[(.+)\]/$1/g</fieldReplacement>
</Metadata>
```

## Beispiel: Übernahme von Normdaten

Daten aus MARC XML:

```xml
<datafield tag="650" ind1=" " ind2="7">
    <subfield code="0">(DE-601)106075438</subfield>
    <subfield code="0">(DE-588)4079163-4</subfield>
    <subfield code="a">Weltkrieg</subfield>
    <subfield code="9">g:1914-1918</subfield>
    <subfield code="2">gnd</subfield>
</datafield>
```

Mapping im Regelsatz zur Datenübernahme:

```xml
<Metadata>
    <Name>Classification</Name>
    <field>
        <fieldMainTag>650</fieldMainTag>
        <fieldSubTag>a</fieldSubTag>
        <fieldInd1>any</fieldInd1>
        <fieldInd2>any</fieldInd2>
    </field>
    <identifierField>0</identifierField>
    <identifierConditionField>/DE-588/</identifierConditionField>
    <identifierReplacement>s/\(.+\)//g</identifierReplacement>
</Metadata>
```

## Beispiel: Übernahme des Autors aus MARC XML

Daten aus MARC XML:

```xml
<datafield tag="100" ind1="1" ind2=" ">
    <subfield code="a">Klein, Felix</subfield>
    <subfield code="0">(DE-601)133790177</subfield>
    <subfield code="0">(DE-588)11856286X</subfield>
    <subfield code="4">aut</subfield>
</datafield>
```

Mapping im Regelsatz zur Datenübernahme:

```xml
<Person>
    <Name>Author</Name>
    <field>
        <fieldMainTag>100</fieldMainTag>
        <expansion>a</expansion>
        <fieldInd1>1</fieldInd1>
        <fieldInd1> </fieldInd1>
    </field>
    <identifierField>0</identifierField>
    <identifierConditionField>/DE-588/</identifierConditionField>
    <identifierReplacement>s/\(.+\)//g</identifierReplacement>
    <conditionField>4</conditionField>
    <conditionValue>/aut/</conditionValue>
</Person>
```

## Beispiel: Mapping einer Handschrift

Daten aus MARC XML:

```xml
<leader>xxxxxntm a22yyyyy c 4500</leader>
```

Mapping im Regelsatz zur Datenübernahme:

```xml
<DocStruct>
    <Name>Manuscript</Name>
    <leader6>t</leader6>
    <leader7>m</leader7>
</DocStruct>
```

## Beispiel: Mapping einer Karte

Daten aus MARC XML:

```xml
<leader>xxxxxnem a22yyyyy c 4500</leader>
<controlfield tag="001">840562748</controlfield>
<controlfield tag="003">DE-601</controlfield>
<controlfield tag="005">20151123080919.0</controlfield>
<controlfield tag="007">au auuun</controlfield>
```

Mapping im Regelsatz zur Datenübernahme:

```xml
<DocStruct>
    <Name>Map</Name>
    <leader6>e</leader6>
    <leader7>m</leader7>
    <field007_0>a</field007_0>
    <field007_1>u</field007_1>
    <field008_21> </field008_21>
</DocStruct>
```

## Beispiel: Angaben zum Verlag, Ort und Erscheinungsjahr

Daten aus MARC XML:

```xml
<datafield tag="260" ind1="3" ind2=" ">
    <subfield code="6">880-01</subfield>
    <subfield code="a">Tōkyō</subfield>
    <subfield code="b">Shōbunsha</subfield>
    <subfield code="c">2016</subfield>
</datafield>
```

Mapping im Regelsatz zur Datenübernahme:

```xml
<Group>
    <Name>Publishing</Name>
    <Metadata>
        <Name>PlaceOfPublication</Name>
        <field>
            <fieldMainTag>260</fieldMainTag>
            <fieldSubTag>a</fieldSubTag>
            <fieldInd1>3</fieldInd1>
        </field>
    </Metadata>
    <Metadata>
        <Name>PublisherName</Name>
        <field>
            <fieldMainTag>260</fieldMainTag>
            <fieldSubTag>b</fieldSubTag>
            <fieldInd1>3</fieldInd1>
        </field>
    </Metadata>
    <Metadata>
        <Name>PublicationYear</Name>
        <field>
            <fieldMainTag>260</fieldMainTag>
            <fieldSubTag>c</fieldSubTag>
            <fieldInd1>3</fieldInd1>
        </field>
    </Metadata>
</Group>
```

## Beispiel: Mapping von Körperschaften

```xml
<datafield tag="710" ind1="2" ind2=" ">
    <subfield code="a">Catholic Church.</subfield>
    <subfield code="b">Province of Baltimore (Md.).</subfield>
    <subfield code="b">Provincial Council</subfield>
    <subfield code="d">1869</subfield>
    <subfield code="n">10th</subfield>
    <subfield code="4">isb</subfield>
</datafield>
```

Mapping im Regelsatz zur Datenübernahme:

```xml
<Corporate>
    <Name>IssuingBody</Name>
    <field>
        <fieldMainTag>710</fieldMainTag>
        <fieldMainName>a</fieldMainName>
        <fieldSubName>b</fieldSubName>
        <fieldPartName>c</fieldPartName>
        <fieldPartName>n</fieldPartName>
        <fieldPartName>d</fieldPartName>
    </field>
    <conditionField>4</conditionField>
    <conditionValue>/isb/</conditionValue>
    <separator>: </separator>
</Corporate>
```
