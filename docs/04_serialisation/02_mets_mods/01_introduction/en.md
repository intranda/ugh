---
title: Introduction
published: true
keywords:
    - UGH
    - METS
    - MODS
---

The METS/MODS XML format can completely serialize the internal document model, both read and write methods are implemented. Since it is only a subset of METS - not all METS elements are used - a description of the format on which this implementation is based is available on the [DFG Viewer](http://dfg-viewer.de/): The DFG Viewer METS Profile in Version 2.0.

:::info
**As PDF:**  
[http://dfg-viewer.de/fileadmin/groups/dfgviewer/METSAnwendungsprofil2.0.pdf ](http://dfg-viewer.de/fileadmin/groups/dfgviewer/METS_Anwendungsprofil_2.0.pdf%20)

**As XML-Profile:**  
[http://dfg-viewer.de/fileadmin/groups/dfgviewer/METSAnwendungsprofil2.0.xml](http://dfg-viewer.de/fileadmin/groups/dfgviewer/METS_Anwendungsprofil_2.0.xml)
:::

The structure types are always serialized in METS, the metadata in the MODS format of the \[DFG Viewer profile\] ([http://dfg-viewer.de/fileadmin/groups/dfgviewer/MODSAnwendungsprofil1.0.pdf)(\`ugh.fileformat.mets](http://dfg-viewer.de/fileadmin/groups/dfgviewer/MODS_Anwendungsprofil_1.0.pdf%29%28`ugh.fileformat.mets). MetsModsImportExport`) or in a Goobi namespace within the MODS extension tag (`ugh.fileformat.mets.MetsMods\`). This separation is necessary because a mapping of the metadata types of the document model to the MODS types AND REVERSE is mandatory for storage in the MODS application profile. However, since the metadata types can be freely defined in the rule set, mapping to MODS is always possible, but not in the other direction in the case of different authors, for example - for whom there is not necessarily a different correspondence in MODS - but then not in the other direction.

For the complete serialisation of the document model, the `Mets-mods` class can be used, while the `Mets-modsImportExport` class initially supports export, so that METS files can be exported in the DFG Viewer METS profile. Read access to files with metadata in the MODS application profile is already implemented, but is subject to certain restrictions, which are explained in the chapter "Mapping for Import".

