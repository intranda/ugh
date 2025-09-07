---
title: UghConvert
published: true
keywords:
    - UGH
    - METS
    - MODS
---

Um die oben beschriebenen Serialisierungsformate des genutzten Dokumentmodells unabhängig von einer externen Applikation zwischen einander zu konvertieren, kann UghConvert genutzt werden. Es handelt sich dabei um eine Java-Applikation, die auf Kommandozeilen-Ebene mit mitgelieferten Skripten für Windows- und Linux-Systeme die jeweiligen Formate lesen und schreiben kann. Zur Ausführung wird lediglich eine vollständige Version der UGH Bibliothek (ughCLI) sowie ein Java-Runtime-Environment in Version 1.5 benötigt. 

UghConvert liest ein `DigitalDocument` in einem gegebenen Serialisierungsformat ein – wenn nötig unter Angabe des genutzten Regelsatzes – und serialisiert es anschließend wieder in einem ebenfalls gegebenen Format. Es können Daten auch nur gelesen werden sowie in einem Format gelesen und im selben Format wieder gespeichert werden.

## Die Kommandozeile

Das Skript wird wie folgt aufgerufen, wobei der Pfad zum Java Runtime Environment gesetzt sein muss:

```bash
java -jar ughCLI-1.6.jar
```

Die grundlegenden Parameter sind die folgenden:

| Parameter | Beschreibung |
| :--- | :--- |
| `-c`, `--config <file>` | Der Pfad zur genutzten Regelsatzdatei. |
| `-r`, `--read <format>` | Das zu lesende Format: Hier kommen die folgenden in Frage: `mets`, `rdf`, `xstream` oder `picaplus`. Als METS Variante kann hier - wie schon oben beschrieben - zunächst nur das interne METS-Format gelesen werden. |
| `-w`, `--write <format>` | Das zu schreibende Format: Die folgenden Serialisierungsformate kommen hierfür in Frage: `mets`, `dvmets`, `rdf` oder `xstream`, wobei hier zwischen dem internen METS-Format (`mets`) und dem zvdd/DFG-Viewer- METS unterschieden wird (`dvmets`). |
| `-i,` `--input <file>` | Der Pfad zur Quelldatei, im Format wie mit `-r` angegeben. |
| `-o`, `--output <file>` | Der Pfad, unter der die Zieldatei gespeichert werden soll; diese wird im Format wie unter `-o` angegeben serialisiert. |
| `-v`, `--verbose` | Mit diesem Parameter werden die Struktur- und Metadaten des Dokuments beim Lesen ausgegeben. |
| `-h`, `--help` | Gibt eine Hilfe zur Syntax aus. |
| `-q`, `--quiet` | Vermeidet jegliche Art der Ausgabe, Fehler jedoch ausgenommen. |
| `-V`, `--version` | Gibt die Versionen der einzelnen von UGH genutzten `Fileformat` Implementierungen aus. |

_Beispiel: Lesen einer XStream-Datei mit Ausgabe der Struktur- und Metadaten_

```bash
java -jar ughCLI-1.6.jar -c digizeit.xml -i meta.xstream.xml -r xstream -v
```

_Beispiel: Konvertieren einer XStream-Datei in eine (interne) METS-Datei_

```bash
java -jar ughCLI-1.6.jar -c vd17_nova.xml -i meta.xstream.xml -r xstream -o meta.mets.xml -w mets
```

Beispiel: Konvertieren einer RDF-Datei in eine (interne) METS-Datei, incl. Ausgabe der Struktur- und Metadaten

```bash
java -jar ughCLI-1.6.jar -c gdz.xml -i meta.rdf.xml -r rdf -o meta.mets.xml -w mets -v
```

## Administrative Metadatensektion

Für die Konvertierung in das zvdd/DFG-Viewer METS-Format sind noch einige weitere Werte konfigurierbar, die sich in drei Gruppen einteilen lassen (siehe auch “Eigenheiten des METS- Exports”). Eine davon enthält die Daten für die administrative Metadaten-Sektion `dv:rights` und `dv:links`.

| Parameter | Beschreibung |
| :--- | :--- |
| `-mro`, `--metsrightsowner <owner>` | Der Urheber des Digitalisats. |
| `-mrl`, `--metsrightslogo <url>` | Eine URL des Logos des Urhebers; dieses Logo wird durch den DFG-Viewer sowie das zvdd-Portal entsprechend angezeigt. |
| `-mru`, `--metsrightsurl <url>` | Eine URL der Homepage des Urhebers. |
| `-mrc`, `--metsrightscontact <url>` | Eine URL zu einem Kontaktformular der Homepage des Urhebers oder alternativ eine E- Mail-Adresse. Hiermit soll vom DFG-Viewer ein direkter Kontakt zum Urheber ermöglicht werden. |
| `-mdr`, `--metsdigiprovreference <url>` | Eine URL auf den Katalogeintrag des Digitalisats - oder auf die untergeordnete Struktur, zum Beispiel einen Band - falls eine solche existiert. |
| `-mdra`, `--metsdigiprovreferenceanchor <url>` | Eine URL auf den Katalogeintrag des Digitalisats - oder auf die Übergeordnete Struktur, zum Beispiel eine Zeitschrift - wenn eine solche existiert. |
| `-mdp`, `--metsdigiprovpresentation <url>` | Eine URL auf die Online-Präsentation des Digitalisats - oder auf die untergeordnete Struktur, zum Beispiel einen Band - falls eine solche existiert. |
| `-mdpa`, `--metsdigiprovpresentationanchor <url>` | Eine URL auf die Online-Präsentation des Digitalisats - oder auf die übergeordnete Struktur, zum Beispiel eine Zeitschrift - falls eine solche existiert. |

## Dateigruppen (FileGroups)

Daten für die für den DFG-Viewer benötigten Dateigruppen (`FileGroups`), mindestens erforderlich sind `FileGroups` mit den Namen `MIN` und `DEFAULT`.

| Parameter | Beschreibung |
| :--- | :--- |
| `-fmin`, `--minfilesuffix <filesuffix>` | Die Dateiendung der Dateinamen in der FileGroup `MIN`. |
| `-mmin`, `--minmimetype <mimetype>` | Der Mimetype der Dateien in der FileGroup `MIN`. |
| `-pmin`, `--minpath <path>` | Der Pfad der Dateien in der FileGroup `MIN`, der Dateiname wird aus den Eigenschaften des Objekts `ContentFile` genommen. |
| `-smin`, `--minidsuffix <idSuffix>` | Die Endung der XML ID, die in der METS-Datei für die FileGroup `MIN` verwendet wird. |

Alle weiteren Angaben sind für die Dateigruppen `DEFAULT`, `MAX`, `DOWNLOAD`, `LOCAL`, `PRESENTATION` und `THUMBS` äquivalent. Die Dateigruppe `LOCAL` wird automatisch erzeugt, kann jedoch bei Bedarf mit eigenen Werten überschrieben werden. Die Parameter für die weiteren Dateigruppen sind äquivalent zu `minfilesuffix`, `minmimetype`, `minpath` und `minidsuffix` festgelegt, zum Beispiel `defaultpath`, `maxpath`, `downloadpath`, `localpath`, `presentationpath` und `thumbspath`. Alle Parameter finden sich in der Hilfe von UghConvert:

```bash
java -jar ughCLI-1.6.jar -h
```

Für die Konfiguration der erweiterten Parameter (administrative Metadaten und Dateigruppen) sollten die langen Parameter (`--mimidsufffix`, `--minmimetype`, etc.) verwendet werden.

_Beispiel: Ausschnitt aus einer METS-Datei (FileGroups) Aus dem Angaben_

```bash
java -jar ughCLI-1.6.jar
-c vd17_nova.xml
-i meta.rdf.xml
-r rdf
-o meta.mets.xml
-w dvmets
--default filesuffix .jpg
-–defaultmimetype image/jpeg
--defaultpath /pfad/zu/den/bildern/
--defaultidsuffix _DEF
--metsrightsowner "SUB Göttingen"
--metsrightslogo http://gdz.sub.uni-goettingen.de/logo_gdz_dfgv.png 
--metsrightsowner mailto:dfg-viewer@gdz.sub.uni-goettingen.de 
--metsrightsurl http://gdz.sub.uni-goettingen.de 
--metsdigiprovreference
http://opac.sub.uni-goettingen.de/DB=1/PPN?PPN=590628720 
--metsdigiprovpresentation
 
http://resolver.sub.uni-goettingen.de/purl?PPN590628720
```

_ergibt sich die folgende METS FileGroup `DEFAULT`..._

```bash
<mets:fileGrp USE="DEFAULT">
      
      <mets:file ID="FILE_0000_DEF" MIMETYPE="image/jpeg">
            <mets:FLocat LOCTYPE="URL" xlink:href="/pfad/zu/den/bildern/00000001.jpg" xmlns:xlink="http://www.w3.org/1999/xlink" />
      </mets:file>
      
      <mets:file ID="FILE_0001_DEF" MIMETYPE="image/jpeg">
            <mets:FLocat LOCTYPE="URL" xlink:href="/pfad/zu/den/bildern/00000002.jpg" xmlns:xlink="http://www.w3.org/1999/xlink" />
      </mets:file>
      
      ...
      
</mets:fileGrp>
```

_...und folgende administrative METS Metadatensektion:_

```bash
<mets:amdSec ID="AMD">
    <mets:rightsMD ID="RIGHTS">
        <mets:mdWrap MDTYPE="OTHER" MIMETYPE="text/xml" OTHERMDTYPE="DVRIGHTS">
            <mets:xmlData>
                <dv:rights xmlns:dv="http://dfg-viewer.de/">
                    <dv:owner>SUB Göttingen</dv:owner>
                    <dv:ownerLogo>
                        http://gdz.sub.uni-
 
goettingen.de/logo_gdz_dfgv.png
                    </dv:ownerLogo>
                    <dv:ownerSiteURL>
                        http://gdz.sub.uni-goettingen.de
                    </dv:ownerSiteURL>
                
    <dv:ownerContact>
                        mailto:dfg-viewer@gdz.sub.uni-goettingen.de
                    </dv:ownerContact>
                </dv:rights>
            </mets:xmlData>
        </mets:mdWrap>
    </mets:rightsMD>

    <mets:digiprovMD ID="DIGIPROV">
        <mets:mdWrap MDTYPE="OTHER" MIMETYPE="text/xml" OTHERMDTYPE="DVLINKS">
            <mets:xmlData>
                <dv:links xmlns:dv="http://dfg-viewer.de/">
                    <dv:reference>
                        http://opac.sub.uni-goettingen.de/DB=1/PPN?PPN=590628720
                    </dv:reference>
                    <dv:presentation>
                        http://resolver.sub.uni-goettingen.de/purl?PPN590628720
                    </dv:presentation>
                </dv:links>
            </mets:xmlData>
        </mets:mdWrap>
    </mets:digiprovMD>
</mets:amdSec>
```

