---
title: Vererbbare Metadatentypen
published: true
keywords:
    - UGH
---

Das Element `<inheirableMetadataTypes>` enthält all die Metadatentypen die von einer Struktureinheit auf unterliegende Struktureinheiten vererbt werden soll. Entsprechende Einträge werden in der Datenbank dupliziert und erlauben so einen schnellen Zugriff bzw. eine schnelle Filterung. Die Metadatentypen werden jedoch nur dann dupliziert, wenn die Struktureinheit kein eigenes Metadatum dieses Typs besitzt. Innerhalb das `<inheirableMetadataTypes>` Elements befindet sich für jeden Metadatentyp ein `<internalName>` Element.

_Beispiel: Vererbung der Digitalen Kollektion beim Datenbankimport_

```text
<inheirableMetadataTypes>
    <internalName>singleDigCollection</internalName>
</inheirableMetadataTypes>
```

