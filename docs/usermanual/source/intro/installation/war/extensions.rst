.. _intro.installation.war.extensions:

Extensions
==========

GeoServer :ref:`extensions <intro.extensions>` are distributed as a separate archive. Each extension is contained in a directory inside the archive. Files contained in these directories can be manually extracted and installed.

.. tabularcolumns:: |p{5cm}|p{7cm}|
.. list-table::
   :header-rows: 1
   :widths: 30 70
   :class: non-responsive

   * - Directory
     - Description
   * - ``arcsde``
     - ArcSDE middleware extension for GeoServer
   * - ``app-schema``
     - Application Schema support
   * - ``cluster``
     - Clustering extension for GeoServer. Use with ``jdbcconfig``.
   * - ``csw``
     - Catalogue Service for Web (CSW) extension for GeoServer
   * - ``db2``
     - DB2 database extension for GeoServer
   * - ``geopkg``
     - GeoPackage extension for GeoServer
   * - ``jdbcconfig``
     - Database catalog and configuration extension for GeoServer. Use with ``cluster``.
   * - ``inspire``
     - Additional WMS and WFS metadata configuration for INSPIRE compliance
   * - ``mongodb``
     - MongoDB data format extension for GeoServer
   * - ``oracle``
     - Oracle database extension for GeoServer
   * - ``scripting``
     - Scripting extension for GeoServer
   * - ``sqlserver``
     - SQL Server database extension for GeoServer
   * - ``wps``
     - Web Processing Service (WPS) extension for GeoServer

Installation
------------

To install an extension:

#. Extract the contents of the OpenGeo Suite extensions archive.

#. Locate the directory that contains the extension to install (such as :file:`wps` for the WPS extension).

#. Copy the files in that directory to the GeoServer library path. This path is often :file:`webapps/geoserver/WEB-INF/lib`, where :file:`webapps` is the application server's web applications directory.

   .. note:: You may be warned about overwriting existing files. This is okay.

#. Restart the application server.

#. Confirm that the extension was installed properly. Typically, the feature will be displayed on the GeoServer Welcome page, or as an available data source.

   .. figure:: img/wps.png
        
      WPS extension installed
