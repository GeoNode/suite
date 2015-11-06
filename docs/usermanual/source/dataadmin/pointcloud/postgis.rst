.. _dataadmin.pointcloud.postgis:

PostGIS integration
===================

The ``pointcloud_postgis`` extension adds functions that allow you to use Point Cloud with PostGIS, converting ``PcPoint`` and ``PcPatch`` to ``Geometry`` and doing spatial filtering on point cloud data. The ``pointcloud_postgis`` extension depends on both the ``postgis`` and ``pointcloud`` extensions, so they must be installed first:

.. code-block:: sql

    CREATE EXTENSION postgis;
    CREATE EXTENSION pointcloud;
    CREATE EXTENSION pointcloud_postgis;

The following is a list of functions that are available in databases with the ``pointcloud_postgis`` extension.

``PC_Intersects()``
-------------------
    
**PC_Intersects(p pcpatch, g geometry)** returns **boolean**

**PC_Intersects(g geometry, p pcpatch)** returns **boolean**

Returns true if the bounds of the patch intersect the geometry.

.. code-block:: sql

    SELECT PC_Intersects('SRID=4326;POINT(-126.451 45.552)'::geometry, pa)
    FROM patches WHERE id = 7;

.. code-block:: sql

    t

``PC_Intersection()``
---------------------

**PC_Intersection(pcpatch, geometry)** returns **pcpatch**

Returns a PcPatch which only contains points that intersected the geometry.

.. code-block:: sql

    SELECT PC_AsText(PC_Explode(PC_Intersection(
          pa, 
          'SRID=4326;POLYGON((-126.451 45.552, -126.42 47.55, -126.40 45.552, -126.451 45.552))'::geometry
    )))
    FROM patches WHERE id = 7;

.. code-block:: sql

                 pc_astext               
    --------------------------------------
     {"pcid":1,"pt":[-126.44,45.56,56,5]}
     {"pcid":1,"pt":[-126.43,45.57,57,5]}
     {"pcid":1,"pt":[-126.42,45.58,58,5]}
     {"pcid":1,"pt":[-126.41,45.59,59,5]}

``Geometry()``
--------------

**Geometry(pcpoint)** returns **geometry**

**pcpoint::geometry** returns **geometry**

Cast PcPoint to the PostGIS geometry equivalent, placing the x/y/z of the ``PcPoint`` into the x/y/z of the PostGIS point. 

.. code-block:: sql
 
    SELECT ST_AsText(PC_MakePoint(1, ARRAY[-127, 45, 124.0, 4.0])::geometry);

.. code-block:: sql
 
    POINT Z (-127 45 124)
