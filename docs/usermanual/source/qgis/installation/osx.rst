.. _qgis.installation.osx:

Installing QGIS on Mac OS X
===========================


This section will describe how to install QGIS on Mac OS X.

Unlike all of the other components of OpenGeo Suite which can be installed from a single installer package, QGIS is broken out into its own package and must be installed separately.

.. warning:: If you have the `QGIS community version <https://www.qgis.org/en/site/index.html>`_, uninstall it and remove ``/Libraries/Frameworks/GDAL.frameworks`` from your ``PATH`` variable. The QGIS community version for Mac OS X depends on GDAL frameworks which conflict with GeoWebCache for Application Servers.

Prerequisites
-------------

QGIS can run on any recent hardware/software combination.

Install
-------

#. Open the provided DMG archive. There will be a single :file:`QGIS.app` and an alias to :file:`Applications`.

   .. figure:: img/osx_qgis-app.png

      QGIS.app

#. Drag this icon into the :guilabel:`Applications` folder.

   .. figure:: img/osx_qgis-install.png

      QGIS copied to Applications folder

QGIS is now installed, and can be run like any normal application.

.. note:: :file:`QGIS.app` can be installed anywhere in the filesystem of your startup volume, not just in :file:`/Applications`, and it will still work correctly. However, you may have issues with the embedded GRASS installation, within :file:`QGIS.app`, if it is installed on other volumes.

Uninstall
---------

To uninstall QGIS, delete the icon from the :guilabel:`Applications` folder, or from where it was installed.
