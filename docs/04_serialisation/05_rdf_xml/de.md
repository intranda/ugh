---
title: RDF/XML
published: true
keywords:
    - UGH
    - RDF/XML
---

Serialisierungsklasse:

```text
 ugh.fileformats.exel.RDFFile (v1.2)
```

Das Serialisierungsformat RDF/XML ist aus Kompatibilitätsgründen Teil der UGH Bibliothek (beispielsweise zum Lesen alter RDF/XML-Bestände, die in ein neues Format überführt werden sollen), denn einige neuere Teile der Dokumentmodells können damit nicht mehr serialisiert werden und gingen somit bei einer Speicherung verloren. Dies betrifft momentan zum Beispiel die erweiterten Paginierungsarten „Spaltenzählung“ und „Blattzählung“. Daher sollte das Format nicht mehr zum internen Speichern genutzt werden!

