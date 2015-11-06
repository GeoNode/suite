.. _dataadmin.gdal:

Enabling GDAL image formats support
===================================

.. note:: This document only concerns the extra image formats that can be made available in GeoServer via GDAL. The GDAL binaries, such as :command:`gdalinfo` are installed separately.

The OpenGeo Suite comes with support for publishing data from many formats supported by the `Geospatial Data Abstraction Library <http://gdal.org>`_ (GDAL).  These formats include DTED, EHdr, AIG, ENVIHdr, and much more.

.. note:: GDAL image formats support is only available with `OpenGeo Suite Enterprise <http://boundlessgeo.com/solutions/opengeo-suite/>`_. For more information on OpenGeo Suite Enterprise, please `contact us <http://boundlessgeo.com/about/contact-us/sales/>`_.

.. note:: See the :ref:`intro.installation` section for more information on the various ways to install OpenGeo Suite.

Installation
------------

Ubuntu packages
~~~~~~~~~~~~~~~

.. note:: GDAL image formats are only available on Linux using Boundless packages. Installing with OpenGeo Suite for Application Servers (via a WAR bundle) is not supported.

#. Install the following package from the Boundless repository.  (See :ref:`intro.installation.ubuntu.install` for instructions on how to add the OpenGeo repository):

   .. code-block:: console

      apt-get install geoserver-gdal

   .. note:: You must run this command as root or use :command:`sudo`.

#. Once the package is installed, restart Tomcat to allow the changes to take effect:

   .. code-block:: console

      service tomcat7 restart

Continue reading at the :ref:`dataadmin.gdal.verify` section.

CentOS / Red Hat
~~~~~~~~~~~~~~~~

.. note:: GDAL image formats are only available on Linux using Boundless packages. Installing with OpenGeo Suite for Application Servers (via a WAR bundle) is not supported.

#. Install the following package from the Boundless repository.  (See :ref:`intro.installation.redhat.install` for instructions on how to add the Boundless repository):

   .. code-block:: console

      yum install geoserver-gdal

   .. note::  You must run this command as root or use :command:`sudo`.

#. Once the package is installed, restart Tomcat to allow the changes to take effect:

   .. code-block:: console

      service tomcat7 restart

Continue reading at the :ref:`dataadmin.gdal.verify` section.

Windows installer
~~~~~~~~~~~~~~~~~

The GDAL image formats can be added during the :ref:`installation <intro.installation.windows.install>` process. On the :guilabel:`Components` page, check the box for :guilabel:`GDAL Image Formats` in the section named :guilabel:`GeoServer Extensions`.

If OpenGeo Suite has already been installed and you wish to install GDAL image formats at a later time, simply run the installer once more and uncheck all other items except for :guilabel:`GDAL Image Formats`. This will add support to the existing OpenGeo Suite installation.

Windows application server
~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note:: This section is for installing GDAL image formats on a Windows-based installation of OpenGeo Suite for Application Servers (WAR bundle).

#. Navigate to http://data.boundlessgeo.com/gdal_support/.

#. Download the file with a version number that most closely matches the version of OpenGeo Suite. For example, the file :file:`gdal_win_suite_40.zip` would be appropriate for OpenGeo Suite 4.x.

   .. note:: This file requires a 32-bit Java/Tomcat.

#. Extract the file :file:`gdal-A.B.C.jar` (where A.B.C is a version number) from the archive. Copy into :file:`<TOMCAT_HOME>\\webapps\\geoserver\\WEB-INF\\lib\\`, where :file:`<TOMCAT_HOME>` is the location where Tomcat is installed (such as :file:`C:\\Program Files\\Tomcat\\`).

#. If Tomcat is installed as a service:

   #. Extract the full contents of the archive to :file:`<TOMCAT_HOME>\\bin\\`.

   #. Restart Tomcat.

#. If Tomcat is not installed as a service:

   #. Extract the full contents of the archive to any folder (such as :file:`C:\\Program Files\\GDAL\\`).

   #. Edit the file :file:`<TOMCAT_HOME>\\bin\\setenv.bat` and add the following line::

         set PATH='C:\Program Files\GDAL\;%PATH%'
 
      replacing :file:`C:\\Program Files\\GDAL\\` with the path where the archive was extracted.

   #. Restart Tomcat.

Continue reading at the :ref:`dataadmin.gdal.verify` section.

OS X installer
~~~~~~~~~~~~~~

The GDAL image formats extension can be added to OpenGeo Suite by copying the contents of the :file:`gdal` extension folder (not the folder itself) to the GeoServer library folder. The GeoServer library folder can be found by selecting :guilabel:`Open Webapps Directory` from the GeoServer menu and then navigating to :file:`geoserver/WEB-INF/lib`.

   .. figure:: ../../intro/installation/mac/img/ext_webappsmenu.png

      Opening the webapps directory from the GeoServer menu

Once copied, GeoServer will need to be restarted to take effect.


OS X application server
~~~~~~~~~~~~~~~~~~~~~~~

.. note:: This section is for installing GDAL image formats on an OS X-based installation of OpenGeo Suite for Application Servers (WAR bundle).

#. Navigate to:  http://data.boundlessgeo.com/gdal_support/

#. Download the file with a version number that most closely matches the version of OpenGeo Suite. For example, the file :file:`gdal_osx_suite_40.zip` would be appropriate for OpenGeo Suite 4.x.

#. Extract the contents of the archive into :file:`/usr/local/lib/`.  The path may not exist, so it may need to be created first.

#. Create (or edit) a file called :file:`setenv.sh` located in $TOMCAT_HOME/bin, and add the following line::

      export DYLD_LIBRARY_PATH=/usr/local/lib:$DYLD_LIBRARY_PATH`

#. Restart Tomcat.

Continue reading at the :ref:`dataadmin.gdal.verify` section.


.. _dataadmin.gdal.verify:

Verification
------------

#. To verify that the GDAL image formats were enabled properly, navigate to the GeoServer web admin interface and log in with administrator credentials.

   .. note:: Please see the GeoServer reference documentation for more information about the GeoServer web admin interface.
   
#. Click on :guilabel:`Stores` and then :guilabel:`Add new Store`.  There should be many raster image formats, such as DTED, EHdr, AIG, and ENVIHdr in the list of formats.

   .. figure:: img/gdal_verify.png
      
      Verifying that GDAL image formats are available
