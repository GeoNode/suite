.. _intro.installation.war.postinstall:

After installation: Working with OpenGeo Suite for Application Servers
======================================================================

.. note:: For more information about setting up OpenGeo Suite, please see the :ref:`sysadmin` section.

Working with GeoServer
----------------------

Enabling spatial reference systems with Imperial units
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A fix is available for spatial reference systems measured in Imperial units (feet). This setting is recommended for all users, and strongly recommended for those working with **US State Plane** projections measured in feet.

To enable this fix:

#. Add the following parameter to your application server settings:

   .. code-block:: bash

      -Dorg.geotoools.render.lite.scale.unitCompensation=true

   .. note:: On Tomcat, 7, it is typical for this file to be located at :file:`/etc/tomcat7/server.xml`.

#. Restart the application server.

Update GeoJSON output
^^^^^^^^^^^^^^^^^^^^^

GeoServer GeoJSON output is now provided in x/y/z order as required by the specification. In addition, the ``crs``  output has changed to support full URN representation of spatial reference systems:

   .. code-block:: json

      "crs": {
         "type": "name",
         "properties": {
            "name": "urn:ogc:def:crs:EPSG::4326"
         }
      }

.. note::

   Previously, the output was:

      .. code-block:: json

         "crs": {
            "type": "EPSG",
            "properties": {
               "code": "4326"
            }
         }

To restore the previous ``crs`` representation for compatibility reasons (especially when working with OpenLayers 3):

#. Add the following context parameter to your GeoServer :file:`WEB-INF/web.xml` file:

   .. code-block:: xml

       <context-param>
           <param-name>GEOSERVER_GEOJSON_LEGACY_CRS</param-name>
           <param-value>true</param-value>
       </context-param>

#. Restart the application server.
