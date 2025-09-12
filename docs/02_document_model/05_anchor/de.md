---
title: Anker
published: true
keywords:
    - UGH
---

Als Anker (Anchor) wird eine Dokumentstruktur verstanden, die eher virtueller Natur ist und andere Struktureinheiten zu einer Gruppe zusammenfügen kann. Solche Anker sind beispielsweise die Strukturen Mehrbändiges Werk oder Zeitschrift, welches alle Bände des Werkes bzw. alle Bände der Zeitschrift zusammenhält. Der Anker selber besitzt keine Inhaltsdateien – also kein Imageset mit Bild-Dateien, er besteht lediglich aus einer Struktureinheit, die über deskriptive Metadaten verfügt. Das wesentliche Metadatum ist ein Identifier, mit welchem der Anker eindeutig identifiziert werden kann.

Prinzipiell gibt es zwei Möglichkeiten, den Anker abzuspeichern:

* In derselben Datei wie die unterliegende Struktureinheit. In diesem Fall wird die Struktureinheit, die als Anker dient, redundant in jeder Metadatendatei gespeichert. Das RDF/XML Format beispielsweise speichert die Dokumentstruktur des Ankers als oberste Struktureinheit in der Datei. 
* In einer separaten Datei, so dass aus unterliegenden Struktureinheiten auf den Anker verwiesen werden muss. Für den Import in Datenbanken und Repositories bedeutet dies, dass der Anker immer zuerst importiert werden muss. Um die Referenzierung zu gewährleisten, enthält der Anker einen Identifier, der im Verweis in der unterliegenden Struktureinheit genutzt wird.

