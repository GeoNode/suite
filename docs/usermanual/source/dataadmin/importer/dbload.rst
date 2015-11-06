.. _dataadmin.importer.dbload:

Loading data into a database via the Layer Importer
===================================================

With the Layer Importer, it is possible not only to publish data to GeoServer with ease, but also to load the data into a database first and then publish the data, all in a single process.

Background
----------

Serving data from a spatial database is usually recommended, due to advantages in concurrent access, editing, and speed. That said, data is often retrieved and transferred in "flat" spatial files such as shapefiles.

Recognizing this, the Layer Importer can perform a two step process during a spatial file import:

* Load the data into a database
* Publish the newly-created database tables as layers in GeoServer

All that is required is that the database store be set up in advance, and then be the target of the import.

Procedure
---------

In this example, we'll load a single shapefile into a PostGIS database, and then publish that table with GeoServer, but be aware that **this process works with any valid spatial file and database store type in GeoServer**.

Create a database store
~~~~~~~~~~~~~~~~~~~~~~~

The first step is to add the store that will be the target of the data loading. The database that we will be using must already be created, though it need not be empty.

#. In the GeoServer web interface, navigate to :menuselection:`Data --> Stores --> Add a new store`.

#. Select the database type that you would like to use..

#. Enter in the connection parameters.

   .. figure:: img/dbload_storepage.png

      Store connection parameters (workspace = import, store = postgres)

#. Click :guilabel:`Save` when done.

#. You will see a list of layers (if any) inside the store. It is not necessary to do anything here, as we'll move over to the Layer Importer to publish layers.

Import the spatial file
~~~~~~~~~~~~~~~~~~~~~~~

Now with the database store created, we can use the Layer Importer to load data into the database, and then into GeoServer.

#. Navigate to :menuselection:`Data -> Import Data` to bring up the Layer Importer.

#. Select the button for :guilabel:`Spatial Files`.

#. In the section titled :guilabel:`Configure the data source`, enter the path to the spatial file to load.

   .. note:: As with all Importer operations, this file must be locally accessible to the machine where GeoServer is running.

#. In the section titled :guilabel:`Specify the target for the import`, specify the workspace and store that represents the database loaded in the previous step.

   .. figure:: img/dbload_import.png

      Importer settings to load into a database

#. Click :guilabel:`Next`.

#. The file(s) will be enumerated and the list will be displayed on the next page. As with a normal import process, select the layer(s) you wish to load and then click :guilabel:`Import`. 

At this point, the files will be loaded into the database and then subsequently published as layers in GeoServer.
