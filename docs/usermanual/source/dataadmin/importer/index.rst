.. _dataadmin.importer:

GeoServer Layer Importer
========================

OpenGeo Suite includes a Layer Importer for GeoServer, providing another option for loading and publishing a variety of vector and raster format data. You can either access the Layer Importer through the GeoServer Web Interface, or programmatically through the Layer Importer REST API.

The Layer Importer differs from the standard GeoServer interface for loading data in a few ways.

* The Layer Importer loads stores and publishes layers in a single step, populating the fields with intelligent defaults.
* It also operates on multiple files at once.
* Finally, the Layer Importer creates a unique style (:term:`SLD`) for every layer loaded.

This section describes how to use the Layer Importer.

.. toctree::
   :maxdepth: 2

   guireference
   guiexample
   dbload
   formats
