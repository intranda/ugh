---
title: Database configuration
published: true
---

The configuration is located in the enclosing `<AGORADATABASE>` element, which is subordinate to the `<Formats>` element. First, some basic parameters of the database must be specified. These are in detail:

| Parameter | Description |
| :--- | :--- |
| `<databaseURL>` | URL of the database to be accessed. |
| `<databaseUser>` | User who has appropriate read/write access to the database. |
| `<databasePassword>` | Password of the user. |
| `<databaseDriver>` | Name of the class of the JDBC driver that is responsible for communication with the database. |

