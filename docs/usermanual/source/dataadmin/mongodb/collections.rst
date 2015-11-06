.. _dataadmin.mongodb.collections:

Publishing collections
======================

Some notes about publishing collections:

* A compliant collection must have one or more spatial indexes of type ``2dsphere``. Any collection without an indexed field of type ``2dsphere`` will be ignored.

* When examining compliant collections, the schema store will be queried for an existing schema by using the collection name as the schema name. If a schema exists it is utilized otherwise it is inferred from the collection and cached to the schema store for reuse.

* When generating a schema, objects and values in arrays are ignored. Indexes on arrays are also ignored. The only exception is the arrays of coordinates used by geometries, which are parsed as such. 

* A new collection can be created in the database by selecting the "Create new feature type link" when selecting a layer to publish. This should not be utilized to manually define a schema for an existing collection.

* A collection must have its underlying coordinate system set to **EPSG:4326** (WGS84, latitude/longitude).

Supported types
---------------

* The following GeoJSON geometry encodings are valid:

  * Point
  * LineString
  * Polygon
  * MultiPoint
  * MultiLineString
  * MultiPolygon

  .. note:: GeoJSON multigeometry variants are only supported for MongDB version 2.5 and newer.

* The following Java equivalents of BSON types are valid:

  * String
  * Double
  * Long
  * Integer
  * Boolean
  * Date

.. todo:: Took out section on inferred mapping, pending verification on whether it's germane.
