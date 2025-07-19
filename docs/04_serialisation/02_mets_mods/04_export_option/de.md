---
title: Erweiterte Möglicheiten des METS-Exports
published: true
---

Für diverse Möglichkeiten des zvdd/DFG-Viewer METS-Formats sind keine äquivalenten Daten in unserem Dokumentmodell vorgesehen. Für einen ordentlichen METS-Export sind diese Daten jedoch unerlässlich. Für die Festlegung von Werten der folgenden Felder sind in der Klasse `ugh.fileformats.mets.MetsModsImportExport` Setter- und Getter-Methoden implementiert – nähere Erläuterungen finden sich im bereits erwähnten zvdd/DFG-Viewer METS-Profil:

* Die Attribute `rightsOwner`, `rightsOwnerLogo`, `rightsOwnerSiteURL`, `digiprovReference`, `digiprovPresentation`, `digiprovReferenceAnchor` und `digiprovPresentationAnchor` setzen Werte für die administrative Metadaten-Sektion der METS-Datei, das sind die Metadaten zu Rechteinhaber, Urheber, Herkunft und Online-Präsentation – vergleiche Kapitel „Administrative Metadaten-Sektion“ im zvdd/DFG-Viewer METS-Profil.
* Das Attribut `purlUrl` setzt den Wert eines persistenten Identifiers, der in der METS-Datei als PURL für das gesamte Werk an der entsprechenden Stelle in der logischen StructMap gesetzt wird (siehe „structMap Anforderung 3: Komplexes Dokumentenmodell“).
* Das Attribut `contentIds` hingegen dient der Referenzierung der einzelnen Seiten der physischen Struktur. Momentan wird es als Pfad zur einzelnen Datei genutzt, an das noch der Dateiname angehängt wird (Dieses Feature ist noch experimentell).
* Wird am Ende eines solchen Metadadatums ein regulärer Ausdruck (in Perl5-Syntax) übergeben – in der Form `$REGEXP(s///)` – dann wird dieser reguläre Ausdruck auf den gesamten Wert angewendet, bevor er in das METS übernommen wird.

_Beispiel:_

Wird für das Metadatum `digiprovReference` beispielsweise der Wert `http://opac.sub.uni-goettingen.de/DB=1/PPN?PPN=PPN123456789` übergeben, und kommt das `PPN123456789` aus einem bestimmten Metadatenfeld und ist in einem bestimmten Kontext nicht beeinflussbar, kann durch `http://opac.sub.uni- goettingen.de/DB=1/PPN?PPN=PPN123456789$REGEXP(s/PPN=PPN/PPN/)` das `PPN` vor der eigentlichen Nummer entfernt werden.

Einer weiteren Erläuterung bedürfen die METS FileGroups, die per VirtualFileGroup übergeben werden. Eine jede VirtualFileGroup im Objekt FileSet des DigitalDocument wird als eine METS FileGroup exportiert wie in Kapitel „fileSec Anforderungen 2: File-Groups“ des METS-Profils erläutert. Diese können mit der folgenden Methode

```text
DigitalDocument.getFileSet().addVirtualFileGroup()
```

dem Dokumentmodell hinzugefügt werden. Alle benötigten Werte können innerhalb des `VirtualFileGroup` Objekts gesetzt werden, siehe hierzu die Implementierung im Quellcode der Klasse `UghConvert`.

## Normdaten

Normdaten können in UGH zu jedem Metadatum und jeder Person erfasst werden. Normdaten bestehen immer aus den drei Informationen Kürzel der Datenbank, URL der Datenbank und Wert innerhalb der Datenbank. Sie können mit der Methode`ugh.dl.Metadata.setAutorityFile(String authorityID, String authorityURI, String authorityValue)` gesetzt werden.

Der METS-Export erzeugt dann aus den Werten die Attribute `authority`, `authorityURI` und `valueURI`.

```xml
<mods:name type="personal" authority="gnd" authorityURI="http://d- nb.info/gnd/" 
     valueURI="http://d-nb.info/gnd/116733721">
     <mods:namePart>Mann, Monika</mods:namePart>
     <mods:role>
          <mods:roleTerm type="code" authority="marcrelator">aut</mods:roleTerm>
     </mods:role>
</mods:name>
```

```xml
<mods:subject>
    <mods:topic authority="gnd" authorityURI="http://d-nb.info/gnd/" 
        valueURI="http://d-nb.info/gnd/4077445-4">Silicium</mods:topic>
</mods:subject>
```

## Persistente Identifier

Das Attribut `contentIDs` des zvdd/DFG-Viewer METS-Formats wird beim METS-Export automatisch erzeugt, sofern das Metadatum `_urn` für die Struktureinheit existiert.

