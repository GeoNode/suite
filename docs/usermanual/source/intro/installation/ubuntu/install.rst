.. _intro.installation.ubuntu.install:

Installing
==========
   
This section describes how to perform an installation of **OpenGeo Suite** |version| on Ubuntu Linux. These instructions should only be followed if your system does not have OpenGeo Suite installed.

.. note::

   * For upgrading to **OpenGeo Suite Enterprise**, please see the section on :ref:`intro.installation.ubuntu.upgrade`.
   * For updating from a previous **minor version** of OpenGeo Suite (4.x), please see the :ref:`intro.installation.ubuntu.minorupdate` section.
   * For updating from a previous **major version** of OpenGeo Suite (3.x), please see the :ref:`intro.installation.ubuntu.majorupdate` section.

.. warning::

   While QGIS often paired with the rest of OpenGeo Suite, **please do not try to run QGIS on the same machine as OpenGeo Suite**, as package conflicts will occur (specifically with GDAL). If you install OpenGeo Suite on a machine that has QGIS, **QGIS may be automatically uninstalled!**

   Instead, use QGIS on a different machine and connect to OpenGeo Suite services from there.

.. _intro.installation.ubuntu.install.sysreq:

.. include:: include/sysreq.txt

Pre-installation process
------------------------

This installation will add the OpenGeo Suite package repository and then install the appropriate packages. See the :ref:`Packages <intro.installation.ubuntu.packages>` section for details about the possible packages to install.

.. warning:: Mixing repositories is not recommended. If you already have a community (non-Boundless) repository that contains some of the components of OpenGeo Suite (such as PostgreSQL) please remove them before installing OpenGeo Suite.

The commands in this section require root privileges. 

#. Change to the ``root`` user:

   .. code-block:: bash

      sudo su - 

#. Import the Boundless GPG key:

   .. code-block:: bash

      wget -qO- https://apt.boundlessgeo.com/gpg.key | apt-key add - 

#. Add the OpenGeo Suite repository:

   * If installing **OpenGeo Suite** on **Ubuntu 12**:

     .. code-block:: bash

        echo "deb https://apt.boundlessgeo.com/suite/latest/ubuntu/ precise main" > /etc/apt/sources.list.d/opengeo.list

   * If installing **OpenGeo Suite** on **Ubuntu 14**:

     .. code-block:: bash

        echo "deb https://apt.boundlessgeo.com/suite/latest/ubuntu/ trusty main" > /etc/apt/sources.list.d/opengeo.list

   * If installing **OpenGeo Suite Enterprise** on **Ubuntu 12**:

     .. code-block:: bash

        echo "deb https://<username>:<password>@apt-ee.boundlessgeo.com/suite/latest/ubuntu/ precise main" > /etc/apt/sources.list.d/opengeo.list

     Making sure to replace ``<username>`` and ``<password>`` with the user name and password supplied to you after your purchase.

   * If installing **OpenGeo Suite Enterprise** on **Ubuntu 14**:

     .. code-block:: bash

        echo "deb https://<username>:<password>@apt-ee.boundlessgeo.com/suite/latest/ubuntu/ trusty main" > /etc/apt/sources.list.d/opengeo.list

     Making sure to replace ``<username>`` and ``<password>`` with the user name and password supplied to you after your purchase.

   .. note:: If you have OpenGeo Suite Enterprise and do not have a user name and password, please `contact us <http://boundlessgeo.com/about/contact-us/sales>`__.

Installation process
--------------------

#. Update the repository list:

   .. code-block:: bash

      apt-get update

#. Search for OpenGeo Suite packages to verify that the repository list is correct. If the command does not return any results, examine the output of the ``apt`` command for any errors or warnings.

   .. code-block:: bash

      apt-cache search opengeo

#. You have options on what packages to install:

   .. note::  See the :ref:`Packages <intro.installation.ubuntu.packages>` section for details of individual packages.

   * To install typical server components:

     .. code-block:: bash

        apt-get install opengeo-server

   * To install typical client components:

     .. code-block:: bash

        apt-get install opengeo-client

   * To install typical client and server components:

     .. code-block:: bash

        apt-get install opengeo

After installation
------------------

Installation is now complete. Please see the section on :ref:`intro.installation.ubuntu.postinstall` to continue.
