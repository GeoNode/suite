.. _webmaps.basic.load:

Load your data with GeoExplorer
===============================

The first step is to load data into OpenGeo Suite. These are many tools and methods to accomplish this (see the :ref:`dataadmin` section), but the simplest way is to use **GeoExplorer**. GeoExplorer allows you to create web maps based on layers served through GeoServer and a variety of other sources. When deployed alongside GeoServer and PostGIS, GeoExplorer will convert spatial files into PostGIS tables, and then serve that data through GeoServer. This process is seamless; all you need to do is supply the data.

#. First, make sure OpenGeo Suite is running.

#. Run GeoExplorer by clicking :guilabel:`GeoExplorer` in the :ref:`intro.dashboard` or by navigating to its direct URL (such as ``http://localhost:8080/geoexplorer``).

   .. figure:: img/load_geoexplorer.png

      *GeoExplorer*

#. Click :guilabel:`Add layers` on the :guilabel:`Layers` panel toolbar, and then click :guilabel:`Upload layers`.

   .. figure:: img/load_uploadmenu.png

      *Uploading layers*

#. For security reasons, you will be asked to log in. Enter the administrative user name and password for GeoServer. The default is ``admin`` / ``geoserver``.

   .. figure:: img/load_login.png

      *Login to GeoExplorer using GeoServer credentials*

   .. note:: For security reasons, it is strongly recommended that you change the admin password. See the `Security <../../geoserver/webadmin/security/>`_ section of the GeoServer reference for more information on changing passwords.

   .. todo:: Also link to master password tutorial.

#. A dialog box will be displayed on the left side of the screen. There are boxes for :guilabel:`Title` and :guilabel:`Description`. You can add those fields in, or you can leave them to be auto-populated by GeoExplorer in the next steps.

   .. figure:: img/load_metadata.png

      *Title and Description fields for new layer*

#. The third box is for the location of your data files. GeoExplorer accepts the following file types:

   * Shapefile archive in ``zip`` format or equivalent, including all required files (``.shp``, ``.shx``, ``.dbf``, ``.prj``)
   * GeoTIFF

   .. note:: Uploading just the ``.shp`` file is not sufficient, as it does not contain all of the required information.

   Click the yellow folder icon to open the file select dialog box for your operating system. Select the file to upload, and click :guilabel:`OK`.

   .. figure:: img/load_fileselected.png

      *File selected and ready for upload*

#. If your data does not include a ``.prj`` file, select the :guilabel:`Options` check box and enter the coordinate reference system ID in the :guilabel:`CRS` field. 

   .. figure:: img/load_crs.png

      *Manually declaring a CRS*

#. When done, click :guilabel:`Upload`.

   .. figure:: img/load_uploadbutton.png

      *Click the Upload button to continue*

#. Your data will be uploaded.

   .. figure:: img/load_progress.png

      *Progress bar during upload*

#. Your file will be loaded as a database table, and this table will be loaded into GeoServer as a Layer. You should immediately see your layer in the Map Window.

   .. note:: On some systems, the data may not be loaded into the database. Instead, the shapefile will be copied to the GeoServer data directory, and a new shapefile store will be created. You can verify this by navigating to the Stores page in the GeoServer admin interface and searching for the newly created store.

   .. figure:: img/load_success.png

      *A successfully loaded layer*

#. If you wish to load more data, you may repeat these steps. Otherwise, continue on to :ref:`webmaps.basic.style`.

.. todo:: Bulk layer loading is available through the GeoServer Layer Importer.

Optional: Adding hosted base layers
-----------------------------------

GeoExplorer displays a default base layer (`MapQuest OpenStreetMap <http://open.mapquest.com/>`_) but many others are available. To select another base layer:

#. Click the :guilabel:`Add layers` on the :guilabel:`Layers` panel toolbar, and then click :guilabel:`Add layers`.

   .. figure:: img/load_addlayers.png

      *Adding layers*

#. In the :guilabel:`View available data from` list, select an alternate data source, such as :guilabel:`Google Layers` or :guilabel:`MapBox Layers`.

   .. figure:: img/load_hostedlayersource.png

      *Hosted layer sources*

#. A list of hosted layers will be shown. Click the layer you would like to add as a base layer, and click :guilabel:`Add layers`.

   .. figure:: img/load_hostedlayer.png

      *Selecting a hosted layer*

#. The base layer will be added to the map. Only one base layer is visible at one time, so you may need to drag the layers around in the Layers List to view them properly.

   .. figure:: img/load_hostedlayeradded.png

      *Hosted layer added to the map*
