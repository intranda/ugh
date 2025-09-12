---
title: Metadata
published: true
keywords:
    - UGH
---

Both content files and structural units can have metadata. A metadata is characterized by the fact that it has a specific type and value and is assigned to either a structural unit or a content file; it is a type-value pair that is used to describe the linked object. A metadata can only be assigned to one object at a time. The length and type of the metadata value are undefined. In principle, the API always assumes strings of any length. However, depending on the respective serialization classes, certain value types can be assumed, either because certain fields require values according to ISO conventions (for example, date, language, or country codes) or because database columns have certain maximum lengths. Currently, this problem must be handled by higher-level application layers.

A special type of metadata are persons and entities. They are not fundamentally different from conventional metadata, but the simple type-value scheme is supplemented with further characteristics. For example, a distinction is made between the first and last name of a person and there is a name to display (DisplayName) as well as a differentiation as to whether it is a natural or legal person (company or organisation). Corporations can also have further data in addition to the name: address, unit, census, city, name suffix. The metadata type for persons and corporations always stands for a specific role. Similar to a regular metadata, each person or entity has a type.
