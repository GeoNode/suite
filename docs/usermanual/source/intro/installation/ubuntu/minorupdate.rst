.. _intro.installation.ubuntu.minorupdate:

Updating a minor version
========================
   
This section describes how to perform a minor update from OpenGeo Suite 4.x to |version| on Ubuntu Linux.

.. note::

   * For new installations, please see the section on :ref:`intro.installation.ubuntu.install`.
   * For upgrading to **OpenGeo Suite Enterprise**, please see the section on :ref:`intro.installation.ubuntu.upgrade`.
   * For updating from a previous **major version** of OpenGeo Suite (3.x), please see the :ref:`intro.installation.ubuntu.majorupdate` section.

.. warning::

   While QGIS often paired with the rest of OpenGeo Suite, **please do not try to run QGIS on the same machine as OpenGeo Suite**, as package conflicts will occur (specifically with GDAL). If you install OpenGeo Suite on a machine that has QGIS, **QGIS may be automatically uninstalled!**

   Instead, use QGIS on a different machine and connect to OpenGeo Suite services from there.

.. include:: include/sysreq.txt

Pre-update process
------------------

This update will add the OpenGeo Suite package repository and then update the appropriate packages. See the :ref:`Packages <intro.installation.ubuntu.packages>` section for details about the possible packages to install.

.. warning:: Mixing repositories with is not recommended. If you already have a community (non-Boundless) repository that contains some of the components of OpenGeo Suite (such as PostgreSQL) please remove them before installing OpenGeo Suite.

The commands in this section require root privileges. 

#. Change to the ``root`` user:

   .. code-block:: bash

      sudo su - 

#. Import the Boundless GPG key:

   .. code-block:: bash

      wget -qO- https://apt.boundlessgeo.com/gpg.key | apt-key add - 

#. Add the OpenGeo Suite repository:

   * If updating **OpenGeo Suite** on **Ubuntu 12**:

     .. code-block:: bash

        echo "deb https://apt.boundlessgeo.com/suite/latest/ubuntu/ precise main" > /etc/apt/sources.list.d/opengeo.list

   * If updating **OpenGeo Suite** on **Ubuntu 14**:

     .. code-block:: bash

        echo "deb https://apt.boundlessgeo.com/suite/latest/ubuntu/ trusty main" > /etc/apt/sources.list.d/opengeo.list

   * If updating **OpenGeo Suite Enterprise** on **Ubuntu 12**:

     .. code-block:: bash

        echo "deb https://<username>:<password>@apt-ee.boundlessgeo.com/suite/latest/ubuntu/ precise main" > /etc/apt/sources.list.d/opengeo.list

     Making sure to replace ``<username>`` and ``<password>`` with the user name and password supplied to you after your purchase.

   * If updating **OpenGeo Suite Enterprise** on **Ubuntu 14**:

     .. code-block:: bash

        echo "deb https://<username>:<password>@apt-ee.boundlessgeo.com/suite/latest/ubuntu/ trusty main" > /etc/apt/sources.list.d/opengeo.list

     Making sure to replace ``<username>`` and ``<password>`` with the user name and password supplied to you after your purchase.

   .. note:: If you have OpenGeo Suite Enterprise and do not have a user name and password, please `contact us <http://boundlessgeo.com/about/contact-us/sales>`__.

Update process
--------------

#. Update the repository list:

   .. code-block:: bash

      apt-get update

#. Search for OpenGeo Suite packages to verify that the repository list is correct. If the command does not return any results, examine the output of the ``apt`` command for any errors or warnings.

   .. code-block:: bash

      apt-cache search opengeo

#. Run the following command to update to Tomcat 7:

   .. code-block:: bash

      apt-get upgrade opengeo-tomcat6- opengeo-tomcat7

#. Remove the existing ``libgdal`` package if it exists, as it can cause conflicts on update:

   .. code-block:: bash

      apt-get remove libgdal

#. You have options on what packages to update:

   .. note::  See the :ref:`Packages <intro.installation.ubuntu.packages>` section for details of individual packages.

   * To update typical server components:

     .. code-block:: bash

        apt-get upgrade opengeo-server geoserver-* postgis-* postgresql-*-postgis-* --only-upgrade

   * To update typical client components:

     .. code-block:: bash

        apt-get upgrade opengeo-client

   * To update typical client and server components:

     .. code-block:: bash

        apt-get upgrade opengeo geoserver-* postgis-* postgresql-*-postgis-* --only-upgrade


#. Update any other additional :ref:`packages <intro.installation.ubuntu.packages>` that you installed originally. For example, to update the :ref:`Boundless SDK <webapps.sdk>`:

   .. code-block:: bash

      apt-get upgrade opengeo-webapp-sdk

After update
------------

If you have changed the GeoServer data directory by setting ``GEOSERVER_DATA_DIR`` in ``/etc/defaults/tomcat7`` the update process will reset it back to the default location. (This change is often done as part of setting up a cluster.) Be sure to remember and re-make your change after the update.

The update is now complete. Please see the section on :ref:`intro.installation.ubuntu.postinstall` to continue.
