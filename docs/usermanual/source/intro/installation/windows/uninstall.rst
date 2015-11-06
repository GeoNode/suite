.. _intro.installation.windows.uninstall:

Uninstallation
==============

This document describes how to uninstall OpenGeo Suite for Windows. 

#. Shutdown all OpenGeo Suite services by navigating to :menuselection:`Start Menu --> All Programs --> OpenGeo Suite` and using the :guilabel:`Stop` shortcuts. Alternately, services can be stopped from the Windows :guilabel:`Services` dialog, and stopping both the **OpenGeo Jetty** and **OpenGeo PostgreSQL** services.

   .. figure:: img/startstop_services.png

      Starting and stopping OpenGeo Suite services

#. Navigate to :menuselection:`Start Menu --> Programs --> OpenGeo Suite --> Uninstall`.

   .. note:: Uninstallation is also available via the standard Windows program removal workflow (**Programs and Features** Control Panel entry for Windows 7/Vista.)

#. Click :guilabel:`Uninstall` to start the uninstallation process.

   .. figure:: img/uninstall.png

      Ready to uninstall OpenGeo Suite

#. Uninstalling will not delete your settings and data. Should you wish to delete this directory, you will need to do it manually. The uninstallation process will display the location of your settings directory. By default, this directory is located at :file:`C:\\ProgramData\\Boundless\\OpenGeo`.

   .. figure:: img/undatadir.png

      Location of data and settings

#. The uninstallation will proceed.

   .. figure:: img/uninstalling.png

      Uninstalling OpenGeo Suite

#. When finished, click :guilabel:`Close`.
