.. _dataadmin.mongodb.notes:

Implementation Notes
====================

Layer bounding box calculation
------------------------------

Generating a bounding box for a layer using the "Compute from data" link on Layer Edit page will result in a full table scan.

Multigeometry support
---------------------

Multigeometry support is valid for MongoDB versions 2.5 and newer. To alleviate issues, when writing to a MongoDB collection the GeoServer will convert MultiGeometry instances to the non-multi variant when possible (e.g. a multipolygon instance encoding a single polygon).

Self-intersecting polygons
--------------------------

MongoDB will reject self-intersecting polygons when writing to an indexed field or on index creation.

Coordinate Reference System support
-----------------------------------

All ``2dsphere`` indexes and spatial operations assume the WGS84 ellipsoid. All indexed GeoJSON data stored in a MongoDB document collection is assumed to be referenced with the  WGS84 coordinate reference system.

Spatial query Limitations
-------------------------

Any query with a predicate geometry **spanning 180 degrees** will result in a full table scan. Issues can arise when geometries are encountered that span past 180 degrees longitude. GeoServer will split the geometry at this line resulting in multiple geometries that must be related with an OR operation. MongoDB versions tested through 2.4.9 do not support more than one operation on a spatial index nested in an `$or operation <http://docs.mongodb.org/manual/reference/operator/query/or/>`_. To perform the query the data is filtered in GeoServer and not the MongoDB instance.

Any query with a predicate "within", "intersects", or "bounding box" geometry **spanning a hemisphere** along as axis will result in the use of the smaller of the complementary geometries along that axis. This is a limitation of the MongoDB `$geoWithin <http://docs.mongodb.org/manual/reference/operator/query/geoWithin/>`_ and `$geoIntersects <http://docs.mongodb.org/manual/reference/operator/query/geoIntersects/>`_ operations.

