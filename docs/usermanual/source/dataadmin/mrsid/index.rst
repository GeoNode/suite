.. _dataadmin.mrsid:

Enabling MrSID image support
============================

The OpenGeo Suite comes with support for publishing data from many formats supported by the `Geospatial Data Abstraction Library <http://gdal.org>`_ (GDAL). These formats include DTED, EHdr, AIG, ENVIHdr, and much more.

`MrSID <http://www.gdal.org/frmt_mrsid.html>`_ is available as an optional extension as well, but due to licensing issues, it is not enabled by default. The steps to enable support will differ depending on how the OpenGeo Suite is installed, and on what platform.

.. note:: MrSID support is only available with `OpenGeo Suite Enterprise <http://boundlessgeo.com/solutions/opengeo-suite/>`_. For more information on OpenGeo Suite Enterprise, please `contact us <http://boundlessgeo.com/about/contact-us/sales/>`_.

Find the method of installation below and continue there.

Installation
------------

Ubuntu
~~~~~~

.. note:: By default, OpenGeo Suite for Applications Servers (WAR bundle) will not show **any** of the GDAL image formats at all. To enable GDAL image formats in this type of installation, please see the section on :ref:`dataadmin.gdal`. This section assumes that the GDAL image formats are already enabled.

MrSID support is provided by a separate package called **gdal-mrsid**.

#. Install the following package from the OpenGeo repository. (See :ref:`intro.installation.ubuntu.install` for instructions on how to add the OpenGeo repository)::

      apt-get install gdal-mrsid

   .. note::  You must run these commands as root or use :command:`sudo`.

#. Restart Tomcat.

Continue at the :ref:`dataadmin.mrsid.verify` section.

CentOS / Red Hat
~~~~~~~~~~~~~~~~

.. note:: By default, the OpenGeo Suite for Application Servers will not show **any** of the GDAL image formats at all. To enable GDAL image formats in this type of install, please see the section on :ref:`dataadmin.gdal`. This section assumes that the GDAL image formats are already enabled.

MrSID support is provided by a separate package called **gdal-mrsid**.

#. Install the following package from the OpenGeo repository. (See :ref:`intro.installation.redhat.install` for instructions on how to add the OpenGeo repository)::

      yum install gdal-mrsid

   .. note::  You must run this command as root or use :command:`sudo`.

#. Copy :file:`/usr/lib/gdal.jar` (:file:`/usr/lib64/gdal.jar` on 64-bit installs) to :file:`/usr/share/tomcat6/webapps/geoserver/WEB-INF/lib/gdal-1.8.1.jar`.

   .. note:: You may need to replace ``tomcat6`` with ``tomcat5`` in the above path depending on the server configuration.

#. Restart Tomcat.

Continue reading at the :ref:`dataadmin.mrsid.verify` section.

Windows (Installer)
~~~~~~~~~~~~~~~~~~~

MrSID support is enabled during the installation process.

#. Navigate through the installer until you reach the :guilabel:`Choose Components` page.

#. Scroll down to the :guilabel:`GeoServer Extensions` option, and expand the tree.

#. Check the box for :guilabel:`GDAL Image Formats`.

   .. note:: See the section on :ref:`intro.installation.windows.components` for more information.

#. Click :guilabel:`Next` and continue the installation process.

.. note:: If you have already installed OpenGeo Suite but not this component, you and can re-run the installer and select only this option.

Continue reading at the :ref:`dataadmin.mrsid.verify` section.

Windows (Application server)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

#. Navigate to:  http://data.boundlessgeo.com/gdal_support/ .

#. Download the file that matches the version of the OpenGeo Suite (e.g. :file:`mrsid_win_30.zip` for version 3.0).

   .. note:: This file requires 32-bit Java/Tomcat.

#. Extract the contents of this archive to the location where the GDAL libraries were extracted (see :ref:`dataadmin.gdal`).

#. Restart Tomcat.

Continue reading at the :ref:`dataadmin.mrsid.verify` section.


Mac OS X
~~~~~~~~

Currently, MrSID support is not available on OS X.

.. Mac OS X (Installer)
.. ~~~~~~~~~~~~~~~~~~~~

.. MrSID support is enabled via a separate installation package, available in the OpenGeo Suite bundle. In addition to the standard :file:`OpenGeo Suite Installer.pkg` file, there is also an :file:`OpenGeo Suite Extensions.mpkg` file.

.. .. figure:: img/mrsid_mac_ext.png
      
..    OpenGeo Suite Extensions contain MrSID support

.. #. Double click on this file to install MrSID after the OpenGeo Suite installation process has completed. Click :guilabel:`Next` to continue.

..    .. figure:: img/mrsid_mac_welcome.png
      
..       OpenGeo Suite Extensions contain the MrSID extension

.. #. Select the target disk, which should be the same as where the OpenGeo Suite was installed. Click  :guilabel:`Next` again.

.. #. On the following page, check the box that says :guilabel:`MrSID Support for OpenGeo Suite`, then click :guilabel:`Next`.

..    .. figure:: img/mrsid_mac_components.png
      
..       Check this box to install MrSID support

.. #. Please wait while the installation proceeds.

..    .. figure:: img/mrsid_mac_progress.png
      
..       Installation

.. #. You will receive confirmation that the installation was successful. 

..     .. figure:: img/mrsid_mac_success.png

..        MrSID support successfully installed

.. Continue reading at the :ref:`dataadmin.mrsid.verify` section.

.. Mac OS X (Application server)
.. ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. #. Navigate to:  http://data.boundlessgeo.com/gdal_support/

.. #. Download the file that matches the version of the OpenGeo Suite (e.g. :file:`mrsid_osx_30.zip` for version 3.0).

.. #. Extract the contents of the archive into :file:`/usr/local/lib/`.

.. #. Restart Tomcat.

.. Continue reading at the :ref:`dataadmin.mrsid.verify` section.


.. _dataadmin.mrsid.verify:

Verification
------------

#. To verify that the MrSID extension was installed properly, navigate to the GeoServer web admin interface and log in with administrator credentials.

#. Click on :guilabel:`Stores` and then :guilabel:`Add new Store`. There should be a :guilabel:`MrSID` option under :guilabel:`Raster Data Formats`.

   .. figure:: img/mrsid_verify.png
      
      Verifying that MrSID is an option in the Raster Data Sources

