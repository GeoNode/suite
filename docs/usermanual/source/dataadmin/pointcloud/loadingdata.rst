.. _dataadmin.pointcloud.loadingdata:

Loading data
============

The examples in the previous pages show how to form patches from arrays of doubles, and well-known binary. You can write your own loader using the uncompressed WKB format, or more simply you can load existing LIDAR files using the `PDAL <http://pointcloud.org>`_ processing and format conversion library.


Loading with PDAL
-----------------

PDAL is a LIDAR conversion library that includes some useful commandline utilities for automating LIDAR loading and processing. The PDAL package was included in the :ref:`dataadmin.pointcloud.install` directions.

.. note:: 

   OpenGeo Suite includes PDAL builds for Linux and OSX, but not yet for Windows. Future releases
   will include native Windows support.


Running ``pdal pipeline``
^^^^^^^^^^^^^^^^^^^^^^^^^

PDAL includes a `command line program <http://www.pdal.io/apps.html>`_ that allows both simple format translations and more complex "`pipelines <http://www.pdal.io/pipeline.html>`_" of transformation. The ``pdal translate`` does simple format transformations. In order to load data into Pointcloud we use a "PDAL pipeline", by calling ``pdal pipeline``. A pipeline combines a format reader, and format writer, with filters that can alter or group the points together. The `PDAL stage reference <http://www.pdal.io/stages/index.html>`_ provides a listing of available stages and configuration options.

PDAL pipelines are XML files, which nest together readers, filters, and writers into a processing chain that will be applied to the LIDAR data. 

To execute a pipeline file, run it through the ``pdal pipeline`` command::

    pdal pipeline --input pipelinefile.xml

Here is a simple example pipeline that reads a LAS file and writes into a Point Cloud-enabled database.

.. code-block:: xml

    <?xml version="1.0" encoding="utf-8"?>
    <Pipeline version="1.0">
        <Writer type="drivers.pgpointcloud.writer">
            <Option name="connection">host='localhost' dbname='pc' user='lidar'</Option>
            <Option name="table">sthsm</Option>
            <Option name="srid">26910</Option>
            <Filter type="filters.chipper">
                <Option name="capacity">600</Option>
                <Filter type="filters.cache">
                    <Reader type="drivers.las.reader">
                        <Option name="filename">/home/lidar/st-helens-small.las</Option>
                        <Option name="spatialreference">EPSG:26910</Option>
                    </Reader>
                </Filter>
            </Filter>
        </Writer>
    </Pipeline>

Point cloud storage of LIDAR works best when each "patch" of points consists of points that are close together, and when most patches do not overlap. In order to convert unordered data from a LIDAR file into patch-organized data in the database, we need to pass it through a filter to "chip" the data into compact patches. The "chipper" is one of the filters we need to apply to the data while loading.

Similarly, reading data from a Point Cloud-enabled database uses a point cloud reader and a file writer of some sort. This example reads from the database and writes to a CSV text file:

.. code-block:: xml

    <?xml version="1.0" encoding="utf-8"?>
    <Pipeline version="1.0">
        <Writer type="drivers.text.writer">
            <Option name="filename">/home/lidar/st-helens-small-out.txt</Option>
            <Option name="cache_block_size">32184</Option>
            <Option name="spatialreference">EPSG:26910</Option>
            <Reader type="drivers.pgpointcloud.reader">
                <Option name="connection">host='localhost' dbname='pc' user='lidar'</Option>
                <Option name="table">sthsm</Option>
                <Option name="column">pa</Option>
                <Option name="srid">26910</Option>
            </Reader>
        </Writer>
    </Pipeline>

Note that we do not need to chip the data stream when reading from the database, as the writer does not care if the points are blocked into patches or not.

You can use the "where" option to restrict a read to just an envelope, allowing partial extracts from a table:

.. code-block:: xml

    <?xml version="1.0" encoding="utf-8"?>
    <Pipeline version="1.0">
        <Writer type="drivers.las.writer">
            <Option name="filename">st-helens-small-out.las</Option>
            <Option name="spatialreference">EPSG:26910</Option>
            <Reader type="drivers.pgpointcloud.reader">
                <Option name="connection">dbname='pc' user='pramsey'</Option>
                <Option name="table">sthsm</Option>
                <Option name="column">pa</Option>
                <Option name="srid">26910</Option>
                <Option name="where">PC_Intersects(pa, ST_MakeEnvelope(560037.36, 5114846.45, 562667.31, 5118943.24, 26910))</Option>
            </Reader>
        </Writer>
    </Pipeline>


PDAL pgpointcloud reader/writer options
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The PDAL **drivers.pgpointcloud.writer** for Point Cloud takes the following options:

* **connection**: The PostgreSQL database connection string. E.g. `host=localhost user=username password=pw db=dbname port=5432`
* **table**: The database table create to write the patches to.
* **schema**: The schema to create the table in. [Optional]
* **column**: The column name to use in the patch table. [Optional: "pa"]
* **compression**: The patch compression format to use [Optional: "dimensional"]
* **overwrite**: Replace any existing table [Optional: true]
* **capacity**: How many points to store in each patch [Optional: 400]
* **srid**: The spatial reference id to store data in [Optional: 4326]
* **pcid**: An existing PCID to use for the point cloud schema [Optional]
* **pre_sql**: Before the pipeline runs, read and execute this SQL file or command [Optional]
* **post_sql**: After the pipeline runs, read and execute this SQL file or command [Optional]
 
The PDAL **drivers.pgpointcloud.reader** for PostgreSQL Pointcloud takes the following options:

* **connection**: The PostgreSQL database connection string. E.g. `host=localhost user=username password=pw db=dbname port=5432`
* **table**: The database table to read the patches from.
* **schema**: The schema to read the table from. [Optional] 
* **column**: The column name in the patch table to read from. [Optional: "pa"]
* **where**: SQL where clause to constrain the query [Optional]
* **spatialreference**: Overrides the database declared SRID [Optional]


Loading from WKB
----------------

If you are writing your own loading system and want to write into point cloud types, create well-known binary inputs in uncompressed format, :ref:`dataadmin.pointcloud.binaryformats.uncompressed`. If your schema indicates that your patch storage is compressed, Point Cloud will automatically compress your patch before storing it, so you can create patches in uncompressed WKB without worrying about the nuances of particular internal compression schemes.

The only issues to watch when creating WKB patches are ensuring the data you write is sized according to the schema (use the specified dimension type) and ensuring that the endianness of the data matches the declared endianness of the patch.
