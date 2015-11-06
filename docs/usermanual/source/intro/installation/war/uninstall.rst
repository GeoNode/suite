.. _intro.installation.war.uninstall:

Uninstallation
==============

To uninstall OpenGeo Suite for Application Servers:

#. Undeploy all of the deployed applications. A full list would include:

   * ``dashboard``
   * ``opengeo-docs``
   * ``geoexplorer``
   * ``geoserver``
   * ``geowebcache``

#. Delete any related WAR files if they still exist.

#. Delete the GeoServer data directory and GeoWebCache cache directory, if they were set up to be external to the applications. For the recommended locations of these directories, please see the section on :ref:`intro.installation.war.install.deploy`.
