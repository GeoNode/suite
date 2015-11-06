.. _qgis.explorer:

OpenGeo Explorer plugin for QGIS
================================

The OpenGeo Explorer plugin for QGIS is used to configure the components of OpenGeo Suite through QGIS.

**OpenGeo Explorer allows you to connect QGIS to PostGIS databases and GeoServer instances**. This means that data, layers, and styles can be pushed to and pulled from external servers.

For example, you can create a project in QGIS, including local layers, and then publish the entire project to a GeoServer instance, converting QGIS styles to GeoServer styles, uploading the data, and even converting the data to PostGIS if desired. In addition, data and styles can easily be loaded from PostGIS or GeoServer right into the local QGIS project.

**With OpenGeo Explorer, QGIS becomes a front-end for OpenGeo Suite**. Given the native integration with QGIS, publishing data on the web can be as simple as dragging and dropping.

.. image:: tutorial/img/project.png

This section describes how to use the OpenGeo Explorer plugin for QGIS.

.. toctree::
   :maxdepth: 1

   install
   tutorial/index
   usage/index
   actions/index
   metadata/index
   config/index
   caveats/index
