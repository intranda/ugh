---
title: Einführung
published: true
keywords:
    - UGH
    - RDF/XML
---

Das RDF/XML-Format ist das ursprünglich für das AGORA Dokument-Management-System (AGORA DMS) entwickelte Datenformat, welches von diesem für den Import unterstützt wird. Ferner generiert das dazugehörige Meta- und Strukturdatenerfassungsprogramm AGORA Editor dieses Format. Es handelt sich also um kein generisches RDF-basiertes XML-Format, sondern vielmehr um eine ganz spezielle Ausprägung, um digitalisierte Dokumente zu beschreiben, wobei davon ausgegangen wird, dass jede Seite als einzelnes Image gespeichert wird. Um Daten in diesem Format übernehmen und in das DMS importieren zu können, wird das entsprechende Datenformat von UGH unterstützt.

Das RDF/XML-Format bildet nicht das komplette Dokumentmodell ab. Während die logische Struktur mit ihren Metadaten frei konfigurierbar ist, ist die physische Struktur nur stark eingeschränkt erhalten. Seiten werden zu so genannten Seitenbereichen mit identischer Paginierung zusammengefasst (Paginierungssequenzen), hierdurch kann ein logisches Strukturelement lediglich eine Startseite und eine Endseite besitzen. Unterbrechungen im Seitenverlauf sind nicht möglich.

Damit das Mapping entsprechend funktioniert, sind die Strukturtypen für die physische Struktur vorgegeben. Das oberste physische Strukturelement muss vom Typ `BoundBook` sein. Die darunterliegenden Seiten sind vom Typ `page`. Bei abweichenden Typen besteht Gefahr, dass die Paginierungssequenzen nicht erzeugt werden können.

Ferner kennt das RDF/XML-Format keine Inhaltsdateien. Um entsprechende Objekte intern zu erzeugen, müssen die Imagedateien einer konkreten Namenskonvention erfolgen: 8-stellig aufsteigend beginnend mit 1 (mit vorangestellten Nullen) und 3 Stellen für die Endung `tif`, beispielsweise `00000001.tif`.

