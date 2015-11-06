.. _intro.installation.mac.uninstall:

Uninstallation
==============

This document describes how to uninstall OpenGeo Suite for Mac OS X. 

#. Shutdown GeoServer and PostGIS by selecting :guilabel:`Quit` from the GeoServer and PostGIS icons in the OS X menu bar.

   .. figure:: img/ext_quit.png

      Shutting down GeoServer

#. Download the free AppCleaner utility from `FreeMacSoft <http://www.freemacsoft.net/appcleaner/>`_. Extract the file and run it.

#. Drag the :guilabel:`Geoserver`, :guilabel:`GeoServer Extensions`, :guilabel:`PostGIS` and :guilabel:`PostGIS Utilities` applications into the AppCleaner window. Click the :guilabel:`Delete` button.

   .. figure:: img/appcleaner.png

      Uninstalling GeoServer

   .. warning:: This will delete your settings and data. If you do not want to delete your data:

      * Back up your GeoServer data directory by copying it to a safe location. You can find your data directory by running GeoServer and selecting :guilabel:`Open GeoServer Data Directory` from the GeoServer icon in the OS X menu bar.
      * Back up your PostGIS database to a safe location by following the instructions in the :ref:`dataadmin.pgDBAdmin.backup` section.

#. To uninstall the CLI tools, remove the ``/usr/local/opengeo`` directory. Open the :guilabel:`Terminal` application, and run the command::

     sudo rm -rf /usr/local/opengeo
