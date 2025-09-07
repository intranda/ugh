---
title: Introduction
published: true
keywords:
    - UGH
    - MARC
---

The format **MARC** (**MA**chine-**R**eadable **C**ataloging) is a data format developed for the exchange of bibliographic data. The format contains metadata on a single structural unit. Structure data cannot be described via the format. Therefore, the format can only be used to import records, the writing of metadata in MARC format is not provided in UGH.

The present UGH extension expects as input a record in MARCXML format, as described by the [Library of Congress](http://www.loc.gov/marc/bibliographic/).

The extension cannot access a catalog itself; the record must already be available locally. It can be passed to the UGH extension either as a file or as a string.

The MARC XML format consists of three areas. Each `<record>` contains exactly one `<leader>` element. Here are some coded details of the document type. This is followed by a series of `<controlfield>` elements. `001` contains the identifier of the data set, `007` and `008` contain further information on the document type. This is followed by `<datafield>` elements, which in turn contain a list of `<subfield>`.

```xml
<record xmlns="http://www.loc.gov/MARC21/slim">
    <leader>xxxxxntm a22yyyyy c 4500</leader>
    <controlfield tag="001">722181507</controlfield>
    <controlfield tag="003">DE-601</controlfield>
    <controlfield tag="005">20150128152124.0</controlfield>
    <controlfield tag="008">120823m18611871gw 000 0 lat d</controlfield>
    <datafield tag="035" ind1=" " ind2=" ">
        <subfield code="a">(DE-599)GBV722181507</subfield>
    </datafield>
    <datafield tag="040" ind1=" " ind2=" ">
        <subfield code="a">GBVCP</subfield>
        <subfield code="b">ger</subfield>
        <subfield code="c">GBVCP</subfield>
        <subfield code="e">rakwb</subfield>
    </datafield>
    <datafield tag="041" ind1="0" ind2=" ">
        <subfield code="a">lat</subfield>
        <subfield code="a">ger</subfield>
    </datafield>
    <datafield tag="100" ind1="1" ind2=" ">
        <subfield code="a">Pichler, Pauline</subfield>
        <subfield code="e">Besitzer</subfield>
        <subfield code="0">(DE-601)722181795</subfield>
        <subfield code="0">(DE-588)1025374479</subfield>
    </datafield>
    <datafield tag="245" ind1="1" ind2="0">
        <subfield code="a">[Stammbuch Pauline Pichler]</subfield>
        <subfield code="h">Manuskript</subfield>
    </datafield> 
    ...
</record>
```

