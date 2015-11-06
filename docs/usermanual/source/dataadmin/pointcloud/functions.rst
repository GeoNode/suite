.. _dataadmin.pointcloud.functions:

Functions
=========

The following is a list of functions that are available in databases with the Point Cloud extension.

``PC_MakePoint()``
------------------

**PC_MakePoint(pcid integer, vals float8[])** returns **pcpoint**

Given a valid ``pcid`` schema number and an array of doubles that matches the schema, construct a new ``pcpoint``.

.. code-block:: sql

    SELECT PC_MakePoint(1, ARRAY[-127, 45, 124.0, 4.0]);

.. code-block:: sql

    010100000064CEFFFF94110000703000000400

Insert some test values into the ``points`` table.

.. code-block:: sql

    INSERT INTO points (pt)
    SELECT PC_MakePoint(1, ARRAY[x,y,z,intensity])
    FROM (
      SELECT  
      -127+a/100.0 AS x, 
        45+a/100.0 AS y,
             1.0*a AS z,
              a/10 AS intensity
      FROM generate_series(1,100) AS a
    ) AS values;

``PC_AsText()``
---------------

**PC_AsText(p pcpoint)** returns **text**
    
Return a JSON version of the data in that point.

.. code-block:: sql

    SELECT PC_AsText('010100000064CEFFFF94110000703000000400'::pcpoint);

.. code-block:: sql

    {"pcid":1,"pt":[-127,45,124,4]}

``PC_AsBinary()``
-----------------

**PC_AsBinary(p pcpoint)** returns **bytea**

Return the OGC "well-known binary" format for the point.

.. code-block:: sql

    SELECT PC_AsBinary('010100000064CEFFFF94110000703000000400'::pcpoint);

.. code-block:: sql

    \x01010000800000000000c05fc000000000008046400000000000005f40

``PC_Get()``
------------

**PC_Get(pt pcpoint, dimname text)** returns **numeric**

Return the numeric value of the named dimension. The dimension name must exist in the schema.

.. code-block:: sql

    SELECT PC_Get('010100000064CEFFFF94110000703000000400'::pcpoint, 'Intensity');

.. code-block:: sql

    4

``PC_Patch()``
--------------

**PC_Patch(pts pcpoint[])** returns **pcpatch**

Aggregate function that collects a result set of ``pcpoint`` values into a ``pcpatch``.

.. code-block:: sql

    INSERT INTO patches (pa)
    SELECT PC_Patch(pt) FROM points GROUP BY id/10;

``PC_NumPoints()``
------------------

**PC_NumPoints(p pcpatch)** returns **integer**

Return the number of points in this patch.

.. code-block:: sql

    SELECT PC_NumPoints(pa) FROM patches LIMIT 1;

.. code-block:: sql

    9     

``PC_Envelope()``
-----------------

**PC_Envelope(p pcpatch)** returns **bytea**

Return the OGC "well-known binary" format for *bounds* of the patch. Useful for performing intersection tests with geometries.
 
.. code-block:: sql

    SELECT PC_Envelope(pa) FROM patches LIMIT 1;

.. code-block:: sql

    \x0103000000010000000500000090c2f5285cbf5fc0e17a
    14ae4781464090c2f5285cbf5fc0ec51b81e858b46400ad7
    a3703dba5fc0ec51b81e858b46400ad7a3703dba5fc0e17a
    14ae4781464090c2f5285cbf5fc0e17a14ae47814640

``PC_AsText()``
---------------

**PC_AsText(p pcpatch)** returns **text**

Return a JSON version of the data in that patch.

.. code-block:: sql

    SELECT PC_AsText(pa) FROM patches LIMIT 1;

.. code-block:: sql

    {"pcid":1,"pts":[
     [-126.99,45.01,1,0],[-126.98,45.02,2,0],[-126.97,45.03,3,0],
     [-126.96,45.04,4,0],[-126.95,45.05,5,0],[-126.94,45.06,6,0],
     [-126.93,45.07,7,0],[-126.92,45.08,8,0],[-126.91,45.09,9,0]
    ]}

``PC_Uncompress()``
-------------------

**PC_Uncompress(p pcpatch)** returns **pcpatch**

Returns an uncompressed version of the patch (compression type "none"). In order to return an uncompressed patch on the wire, this must be the outer function with return type ``pcpatch`` in your SQL query. All other functions that return ``pcpatch`` will compress output to the schema-specified compression before returning.

.. code-block:: sql

    SELECT PC_Uncompress(pa) FROM patches 
       WHERE PC_NumPoints(pa) = 1;

.. code-block:: sql

    01010000000000000001000000C8CEFFFFF8110000102700000A00 

``PC_Union()``
--------------

**PC_Union(p pcpatch[])** returns **pcpatch**

Aggregate function merges a result set of ``pcpatch`` entries into a single ``pcpatch``.

.. code-block:: sql

    -- Compare npoints(sum(patches)) to sum(npoints(patches))
    SELECT PC_NumPoints(PC_Union(pa)) FROM patches;
    SELECT Sum(PC_NumPoints(pa)) FROM patches;

.. code-block:: sql

    100 

``PC_Intersects()``
-------------------

**PC_Intersects(p1 pcpatch, p2 pcpatch)** returns **boolean**

Returns true if the bounds of ``p1`` intersect the bounds of ``p2``.

.. code-block:: sql

    -- Patch should intersect itself
    SELECT PC_Intersects(
             '01010000000000000001000000C8CEFFFFF8110000102700000A00'::pcpatch,
             '01010000000000000001000000C8CEFFFFF8110000102700000A00'::pcpatch);

.. code-block:: sql

    t

``PC_Explode()``
----------------

**PC_Explode(p pcpatch)** returns **SetOf[pcpoint]**

Set-returning function, converts patch into result set of one point record for each point in the patch.

.. code-block:: sql

    SELECT PC_AsText(PC_Explode(pa)), id 
    FROM patches WHERE id = 7;

.. code-block:: sql

                  pc_astext               | id 
    --------------------------------------+----
     {"pcid":1,"pt":[-126.5,45.5,50,5]}   |  7
     {"pcid":1,"pt":[-126.49,45.51,51,5]} |  7
     {"pcid":1,"pt":[-126.48,45.52,52,5]} |  7
     {"pcid":1,"pt":[-126.47,45.53,53,5]} |  7
     {"pcid":1,"pt":[-126.46,45.54,54,5]} |  7
     {"pcid":1,"pt":[-126.45,45.55,55,5]} |  7
     {"pcid":1,"pt":[-126.44,45.56,56,5]} |  7
     {"pcid":1,"pt":[-126.43,45.57,57,5]} |  7
     {"pcid":1,"pt":[-126.42,45.58,58,5]} |  7
     {"pcid":1,"pt":[-126.41,45.59,59,5]} |  7

``PC_PatchAvg()``
-----------------

**PC_PatchAvg(p pcpatch, dimname text)** returns **numeric**

Reads the values of the requested dimension for all points in the patch and returns the *average* of those values. Dimension name must exist in the schema.

.. code-block:: sql

    SELECT PC_PatchAvg(pa, 'intensity') 
    FROM patches WHERE id = 7;

.. code-block:: sql

    5.0000000000000000

``PC_PatchMax()``
-----------------

**PC_PatchMax(p pcpatch, dimname text)** returns **numeric**

Reads the values of the requested dimension for all points in the patch and returns the *maximum* of those values. Dimension name must exist in the schema.

.. code-block:: sql

    SELECT PC_PatchMax(pa, 'x') 
    FROM patches WHERE id = 7;

.. code-block:: sql

    -126.41

``PC_PatchMin()``
-----------------

**PC_PatchMin(p pcpatch, dimname text)** returns **numeric**

Reads the values of the requested dimension for all points in the patch and returns the *minimum* of those values. Dimension name must exist in the schema.

.. code-block:: sql

    SELECT PC_PatchMin(pa, 'y') 
    FROM patches WHERE id = 7;

.. code-block:: sql

    45.5

``PC_FilterGreaterThan()``
--------------------------

**PC_FilterGreaterThan(p pcpatch, dimname text, float8 value)** returns **pcpatch**

Returns a patch with only points whose values are greater than the supplied value for the requested dimension.

.. code-block:: sql

    SELECT PC_AsText(PC_FilterGreaterThan(pa, 'y', 45.57)) 
    FROM patches WHERE id = 7;

.. code-block:: sql

    {"pcid":1,"pts":[[-126.42,45.58,58,5],[-126.41,45.59,59,5]]}

``PC_FilterLessThan()``
-----------------------

**PC_FilterLessThan(p pcpatch, dimname text, float8 value)** returns **pcpatch**

Returns a patch with only points whose values are less than the supplied value for the requested dimension.

``PC_FilterBetween()``
----------------------

**PC_FilterBetween(p pcpatch, dimname text, float8 value1, float8 value2)** returns **pcpatch**

Returns a patch with only points whose values are between the supplied values for the requested dimension.

``PC_FilterEquals()``
---------------------

**PC_FilterEquals(p pcpatch, dimname text, float8 value)** returns **pcpatch**

Returns a patch with only points whose values are the same as the supplied values for the requested dimension.
