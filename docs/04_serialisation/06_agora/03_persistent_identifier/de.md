---
title: Definition des Persistenten Identifiers in der Datenbank
published: true
keywords:
    - UGH
---

Weiterhin sind einige funktional relevante Angaben zu machen. So legt das Element `<persistentIdentifier>` zum Beispiel fest, welcher Metadatentyp zur persistenten Identifizierung einer Struktureinheit gilt. Dieses Element muss genau einmal vorhanden sein, da anhand dessen überprüft wird, ob ein Dokument bereits im Repository importiert wurde und so gegebenenfalls die Serialisierung abgelehnt werden muss.

_Beispiel: Konfiguration der PPN als persistenter Identifier:_

```xml
<persistentIdentifier>CatalogIDDigital</persistentIdentifier>
```

