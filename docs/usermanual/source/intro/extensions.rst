.. _intro.extensions:

OpenGeo Suite extensions
========================

OpenGeo Suite comes with a number of optional extensions or "add-ons". These add functionality to OpenGeo Suite, but may not be needed by most users. We recommend that you choose only the extensions that you need.

These optional components can be selected in different ways depending on your operating system:

* **Windows**: Extensions can be selected during the :ref:`Windows installation process <intro.installation.windows.components>` or by rerunning the installer after OpenGeo Suite is installed.
* **OS X**: Extensions can be added by :ref:`manually copying the extension files <intro.installation.mac.install.extensions>` after OpenGeo Suite is installed.
* **Ubuntu Linux**: Extensions can be installed via :ref:`packages <intro.installation.ubuntu.packages>`.
* **Red Hat-based Linux**: Extensions can be installed by via :ref:`packages <intro.installation.redhat.packages>`.

For users of OpenGeo Suite for Application Servers, please `contact Boundless <http://boundlessgeo.com/about-us/contact/>`_ to receive access to the extensions and information on how to install them.

.. note:: Most of these extensions only available with `OpenGeo Suite Enterprise <http://boundlessgeo.com/solutions/opengeo-suite/>`_. For more information on OpenGeo Suite Enterprise, please `contact us <http://boundlessgeo.com/about/contact-us/sales/>`_.
   

.. _intro.extensions.appschema:

App Schema
----------

.. note:: OpenGeo Suite Enterprise only.

The Application Schema (or App Schema) extension allows the ability to apply a mapping from a simple data store such as a shapefile or database table to one or more complex feature types, conforming to a GML application schema.

Once this extension is added, GeoServer can be configured with mapping files defining how content is to be processed as expected by a given application schema.

For more information, please see the `GeoServer Application Schema documentation <../geoserver/data/app-schema/>`_.

.. _intro.extensions.arcsde:

ArcSDE
------

.. note:: OpenGeo Suite Enterprise only.

The ArcSDE extension adds the ability for GeoServer to publish data from ArcSDE sources. Once this extension is added, GeoServer will show ArcSDE as one of its available data sources when adding a new vector or raster store.

Please visit the `ArcSDE 8.1 Service Pack 1 Readme <http://downloads2.esri.com/support/downloads/ao_/SP1_downloads/ArcSDE_sp1_readme.html>`_ for more information.


.. _intro.extensions.cloudwatch:

CloudWatch
----------

.. note:: OpenGeo Suite Enterprise only.

The CloudWatch extension adds the ability for GeoServer to share performance information with the `Amazon CloudWatch <http://aws.amazon.com/cloudwatch/>`_ monitoring service.

For more information, please see the :ref:`sysadmin.cloudwatch` section.


.. _intro.extensions.clustering:

Clustering
----------

.. note:: OpenGeo Suite Enterprise only.

The Clustering extension for GeoServer adds the ability to automatically set up multiple instances of GeoServer that can pool resources.

This extension consists of two parts:

* Database-backed configuration
* Server connection and communication

.. note::

   On Windows, this extension is known simply as "Clustering". However, on Linux, it is divided into two separate packages, both required:

   * ``geoserver-jdbcconfig``
   * ``geoserver-clustering``

For more information about Clustering, please see the :ref:`sysadmin.clustering` section.

Scripts for setting up clusters either on Amazon Web Services (AWS) or locally-hosted virtual machines are available to OpenGeo Suite Enterprise clients. Please `contact Boundless <http://boundlessgeo.com/about-us/contact/>`_ to access these scripts.


.. _intro.extensions.csw:

CSW
---

The CSW extension adds the ability for GeoServer to publish information conforming to the Catalog Service for Web (CSW) protocol.

For more information about CSW in GeoServer, please see the `GeoServer CSW documentation <../geoserver/extensions/csw/>`_.

.. warning:: The installation instructions in the GeoServer documentation do not apply to users of OpenGeo Suite. Users of OpenGeo Suite can install the extension in the standard way as indicated above.


.. _intro.extensions.db2:

DB2
---

.. note:: OpenGeo Suite Enterprise only.

The DB2 extension adds the ability for GeoServer to publish data from DB2 databases. Once this extension is added, GeoServer will show DB2 as one of its available data sources when adding a new vector store.


.. _intro.extensions.gdal:

GDAL Image Formats
------------------

.. note:: OpenGeo Suite Enterprise only.

The GDAL Image Formats extension adds the ability for GeoServer to publish data from extra raster data sources, through the use of `GDAL <http://www.gdal.org/>`_. These formats include, but are not limited to DTED, EHdr, AIG, and ENVIHdr.

The instructions for enabling these formats may require a few additional steps from the standard extension installation instructions as indicated above. Please see the section on :ref:`installing GDAL image formats <dataadmin.gdal>` for more information.


.. _intro.extensions.geomesa:

GeoMesa
-------

.. note:: OpenGeo Suite Enterprise only.

The GeoMesa extension allows GeoServer to publish data from GeoMesa data stores. `GeoMesa <http://geomesa.org>`_ is a database built on top of `Apache Accumulo <https://accumulo.apache.org/>`_. 

For more information, please see the :ref:`dataadmin.geomesa` section.


.. _intro.extensions.geopackage:

GeoPackage
----------

The GeoPackage extension adds the ability for GeoServer to publish data from `GeoPackage <http://www.geopackage.org/>`_ sources (a data format based on `SQLite <http://www.sqlite.org/>`_). Once this extension is added, GeoServer will show GeoPackage as one of its available data sources when adding a new store.


.. _intro.extensions.script:

GeoScript
---------

.. note:: OpenGeo Suite Enterprise only.

The GeoScript extension adds support for the use of Python in GeoServer. Spatial capabilities are added to Python allowing for the quick generation of custom processes.

For more information on scripting in OpenGeo Suite, please see the :ref:`processing.scripting` section.


.. _intro.extensions.inspire:

INSPIRE
-------

.. note:: OpenGeo Suite Enterprise only.

The INSPIRE extension provides additional metadata information for WMS and WFS services as required by the European Commission's `INSPIRE directive <http://inspire.ec.europa.eu>`__.

For information on configuring WMS and WFS services, please see the `GeoServer INSPIRE documentation <../geoserver/extensions/inspire/>`_.


.. _intro.extensions.mongodb:

MongoDB
-------

.. note:: OpenGeo Suite Enterprise only.

The MongoDB extension adds the ability for GeoServer to publish data from `MongoDB <http://www.mongodb.org/>`_ sources. Once this extension is added, GeoServer will show MongoDB as one of its available data sources when adding a new store.

For more information on using MongoDB, see the section on :ref:`dataadmin.mongodb`.

.. note:: This extension will only work when connecting to MongoDB databases at version 2.4 and above.


.. _intro.extensions.oracle:

Oracle
------

.. note:: OpenGeo Suite Enterprise only.

The :ref:`Oracle extension <dataadmin.oracle>` adds the ability for GeoServer to publish data from Oracle Spatial databases. Once this extension is added, GeoServer will show Oracle as one of its available data sources when adding a new vector store.


.. _intro.extensions.sqlserver:

SQL Server
----------

.. note:: OpenGeo Suite Enterprise only.

The SQL Server extension adds the ability for GeoServer to publish data from MS SQL Server databases (2008 and above only). Once this extension is added, GeoServer will show SQLServer as one of its available data sources when adding a new vector stores.

   
.. _intro.extensions.wps:

WPS
---

The WPS extension adds the ability for GeoServer to support and publish the Web Processing Service (WPS). WPS is a protocol for hosting and executing geospatial processes, bringing geospatial analysis to the client/server model.

For more information on WPS in OpenGeo Suite, please see the :ref:`processing` section.
