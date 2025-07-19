---
title: UghConvert
published: true
---

To convert the serialisation formats of the used document model described above between each other independently of an external application, UghConvert can be used. It is a Java application that can read and write the respective formats at command line level with the supplied scripts for Windows and Linux systems. Only a complete version of the UGH library (ughCLI) and a Java runtime environment in version 1.5 is required for execution.

UghConvert reads in a `DigitalDocument` in a given serialisation format - if necessary by specifying the rule set used - and then serialises it again in a likewise given format. Data can also be read only, read in one format and saved again in the same format.

## The command line

The script is called as follows, whereby the path to the Java Runtime Environment must be set:

```bash
java -jar ughCLI-1.6.jar
```

The basic parameters are the following:

| Parameter | Description |
| :--- | :--- |
| `-c`, `--config <file>` | The path to the rule set file used. |
| `-r`, `--read <format>` | The format to read: The following are possible: `mets`, `rdf`, `xstream` or `picaplus`. As a METS variant, only the internal METS format can be read here, as described above. |
| `-w`, `--write <format>` | The format to write: The following serialization formats can be used: `mets`, `dvmets`, `rdf` or `xstream`. A distinction is made here between the internal METS format (`mets`) and the zvdd/DFG viewer METS (`dvmets`) |
| `-i,` `--input <file>` | The path to the source file, in the format specified with `r`. |
| `-o`, `--output <file>` | The path where the target file is to be saved; it is serialized in the format specified in `-o`. |
| `-v`, `--verbose` | This parameter outputs the structural and metadata of the document when it is read. |
| `-h`, `--help` | Outputs a help for the syntax. |
| `-q`, `--quiet` | Avoids any kind of output, except errors. |
| `-V`, `--version` | Outputs the versions of each `fileformat` implementation used by UGH. |

_Example: Reading an XStream file with output of structural and metadata_

```bash
java -jar ughCLI-1.6.jar -c digizeit.xml -i meta.xstream.xml -r xstream -v
```

_Example: Converting an XStream file to an (internal) METS file_

```bash
java -jar ughCLI-1.6.jar -c vd17_nova.xml -i meta.xstream.xml -r xstream -o meta.mets.xml -w mets
```

Example: Converting an RDF file into an (internal) METS file, including output of structural and metadata

```bash
java -jar ughCLI-1.6.jar -c gdz.xml -i meta.rdf.xml -r rdf -o meta.mets.xml -w mets -v
```

## Administrative metadata section

For the conversion into the zvdd/DFG Viewer METS format, some more values are configurable, which can be divided into three groups (see also "Peculiarities of the METS export"). One of them contains the data for the administrative metadata section `v:rights` and `v:links`.

| Parameter | Description |
| :--- | :--- |
| `-mro`, `--metsrightsowner <owner>` | The author of the digitised material. |
| `-mrl`, `--metsrightslogo <url>` | A URL of the author's logo; this logo is displayed accordingly by the DFG Viewer and the zvdd portal |
| `-mru`, `--metsrightsurl <url>` | A URL of the homepage of the author. |
| `-mrc`, `--metsrightscontact <url>` | A URL to a contact form of the author's homepage or alternatively an e-mail address. This is intended to enable the DFG Viewer to contact the author directly. |
| `-mdr`, `--metsdigiprovreference <url>` | A URL to the catalogue entry of the digitised material - or to the subordinate structure, for example a volume - if one exists. |
| `-mdra`, `--metsdigiprovreferenceanchor <url>` | A URL to the catalogue entry of the digitised material - or to the superordinate structure, for example a journal - if one exists. |
| `-mdp`, `--metsdigiprovpresentation <url>` | A URL to the online presentation of the digitised material - or to the subordinate structure, for example a volume - if one exists. |
| `-mdpa`, `--metsdigiprovpresentationanchor <url>` | A URL to the online presentation of the digitised material - or to the superordinate structure, for example a journal - if one exists. |

## File groups (FileGroups)

Data for the file groups required for the DFG Viewer (`FileGroups`), at least `FileGroups` with the names `MIN` and `DEFAULT` are required:

| Parameter | Description |
| :--- | :--- |
| `-fmin`, `--minfilesuffix <filesuffix>` | The file extension of the file names in the FileGroup `MIN`. |
| `-mmin`, `--minmimetype <mimetype>` | The mimetype of the files in the FileGroup `MIN`. |
| `-pmin`, `--minpath <path>` | The path of the files in the FileGroup `MIN`, the file name is taken from the properties of the object `ContentFile`. |
| `-smin`, `--minidsuffix <idSuffix>` | The extension of the XML ID used in the METS file for the FileGroup `MIN`. |

All other specifications are equivalent for the file groups `DEFAULT`, `MAX`, `DOWNLOAD`, `LOCAL`, `PRESENTATION` and `THUMBS`. The file group `LOCAL` is created automatically, but can be overwritten with your own values if required. The parameters for the other file groups are set equivalent to `minfilesuffix`, `minmimetype`, `minpath` and `minidsuffix`, for example `defaultpath`, `maxpath`, `downloadpath`, `localpath`, `presentationpath` and `thumbspath`. All parameters can be found in the help of UghConvert:

```bash
java -jar ughCLI-1.6.jar -h
```

For the configuration of the extended parameters (administrative metadata and file groups) the long parameters (`--mimidsufffix`, `--minmimetype`, etc.) should be used.

_Example: Extract from a METS file (FileGroups) From the information_

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

_the following METS FileGroup `DEFAULT` gets generated..._

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

_...and the following administrative METS metadata section:_

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

