Loading and publishing GeoPackage data
======================================

OpenGeo Suite supports data saved in the OGC `GeoPackage <http://www.geopackage.com>`_ specification.

.. todo:: Say more about what this is.

Installing GeoPackage support
-----------------------------

GeoPackage support isn't enabled by default, so it must be separately installed.

Installing GeoPackage support is the same as most :ref:`OpenGeo Suite Extensions <intro.extensions>`.

Windows
~~~~~~~

The GeoPackage extension can be added during the installation process. On the :guilabel:`Components` page, check the box for :guilabel:`GeoPackage` in the section named :guilabel:`GeoServer Extensions`.

   .. figure:: img/geopackage_components.png

      GeoPackage in the components list (OpenGeo Suite)

   .. figure:: img/geopackage_enterprise_components.png

      GeoPackage in the components list (OpenGeo Suite Enterprise)

If OpenGeo Suite has already been installed and you wish to install GeoPackage support at a later time, simply run the installer once more and uncheck all other items except for :guilabel:`GeoPackage`. This will add GeoPackage support to the existing OpenGeo Suite installation.

OS X
~~~~

The GeoPackage extension can be added to OpenGeo Suite by copying the contents of the :file:`geopackage` extension folder (not the folder itself) to the GeoServer library folder. The GeoServer library folder can be found by selecting :guilabel:`Open Webapps Directory` from the GeoServer menu and then navigating to :file:`geoserver/WEB-INF/lib`.

   .. figure:: ../../intro/installation/mac/img/ext_webappsmenu.png

      Opening the webapps directory from the GeoServer menu

Once copied, GeoServer will need to be restarted to take effect.

Ubuntu Linux
~~~~~~~~~~~~

The GeoPackage extension can be added to OpenGeo Suite by installing the ``geoserver-geopackage`` package:

.. code-block:: console

   apt-get install geoserver-geopackage

.. note:: This command will need to be run as root or with :command:`sudo`.

Red Hat Linux
~~~~~~~~~~~~~

The GeoPackage extension can be added to OpenGeo Suite by installing the ``geoserver-geopackage`` package:

.. code-block:: console

   yum install geoserver-geopackage

.. note:: This command will need to be run as root or with :command:`sudo`.

Application servers
~~~~~~~~~~~~~~~~~~~

The GeoPackage extension can be added to OpenGeo Suite by copying the contents of the :file:`geopackage` extension directory (not the directory itself) to the GeoServer library directory. If GeoServer is installed at :file:`/opt/tomcat/webapps/geoserver`, the GeoServer library directory will be found at :file:`/opt/tomcat/webapps/geoserver/WEB-INF/lib`.

After copying files, the application server or GeoServer will need to be restarted.

Verifying installation
----------------------

To verify that the extension has been installed properly:

#. Log in to the GeoServer web interface.

#. Click :guilabel:`Stores` then :guilabel:`Add new store`.

#. In the list of :guilabel:`Vector data stores`, you should see two entries for GeoPackage, one in :guilabel:`Vector Data Stores` and one in :guilabel:`Raster Data Stores`.

   .. figure:: img/geopackage_vectorstores.png

      GeoPackage in the list of vector stores

   .. figure:: img/geopackage_rasterstores.png

      GeoPackage in the list of raster stores

If you don't see these entries, the extension did not install completely.

.. todo:: Add info about publishing a layer.
