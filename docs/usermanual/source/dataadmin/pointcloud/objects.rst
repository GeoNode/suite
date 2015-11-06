.. _dataadmin.pointcloud.objects:

Objects
=======

The following are objects as known to databases with the Point Cloud extension.

PcPoint
-------

The basic point cloud type is a ``PcPoint``. Every point has a certain number of dimensions, but at a minimum an X and Y coordinate that place it in space.

Points can be rendered in a human-readable JSON form using the ``PC_AsText(pcpoint)`` function (see :ref:`dataadmin.pointcloud.functions`). The ``pcid`` is the foreign key reference to the ``pointcloud_formats`` table, where the meaning of each dimension in the ``pt`` array of doubles is explained. The underlying storage of the data might not be double, but by the time it has been extracted, scaled, and offset, it is representable as doubles.

.. code-block:: javascript

    {
        "pcid" : 1,
          "pt" : [0.01, 0.02, 0.03, 4]
    }


PcPatch
-------

The structure of database storage is such that storing billions of points as individual records in a table is not an efficient use of resources. Instead, we collect a group of ``PcPoints`` into a ``PcPatch``. Each patch should hopefully contain points that are near together. 

Instead of a table of billions of single ``PcPoint`` records, a collection of LIDAR data can be represented in the database as a much smaller collection (tens of millions) of ``PcPatch`` records. 

Patches can be rendered into a human-readable JSON form using the ``PC_AsText(pcpatch)`` function (see :ref:`dataadmin.pointcloud.functions`).  The ``pcid`` is the foreign key reference to the ``pointcloud_formats`` table.

.. code-block:: javascript

    {
        "pcid" : 1,
         "pts" : [
                  [0.02, 0.03, 0.05, 6],
                  [0.02, 0.03, 0.05, 8]
                 ]
    }
