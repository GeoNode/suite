.. _intro.installation.redhat.minorupdate:

Updating a minor version
========================

This section describes how to perform a minor update from OpenGeo Suite 4.x to |version| on Red Hat-based Linux distributions.

.. note::

   * For new installations, please see the section on :ref:`intro.installation.redhat.install`.
   * For upgrading to **OpenGeo Suite Enterprise**, please see the section on :ref:`intro.installation.redhat.upgrade`.
   * For updating from a previous **major version** of OpenGeo Suite (3.x), please see the :ref:`intro.installation.redhat.majorupdate` section.

.. warning::

   While QGIS often paired with the rest of OpenGeo Suite, **please do not try to run QGIS on the same machine as OpenGeo Suite**, as package conflicts will occur (specifically with GDAL). If you install OpenGeo Suite on a machine that has QGIS, **QGIS may be automatically uninstalled!**

   Instead, use QGIS on a different machine and connect to OpenGeo Suite services from there.

.. include:: include/sysreq.txt

Pre-update process
------------------

This process will add the OpenGeo Suite package repository and then update the appropriate packages. See the :ref:`Packages <intro.installation.redhat.packages>` section for details about the possible packages to be updated.

.. warning:: Mixing repositories is not recommended. If you already have a community (non-Boundless) repository that contains some of the components of OpenGeo Suite (such as PostgreSQL) please remove them before installing OpenGeo Suite.

The commands in this section require root privileges.

#. Change to the ``root`` user:

   .. code-block:: bash

      sudo su -

#. Download the Boundless and Fedora GPG keys:

   .. code-block:: bash

      wget https://yum.boundlessgeo.com/RPM-GPG-KEY-OpenGeo_Suite
      wget https://getfedora.org/static/0608B895.txt

   .. note:: This step and the next two (all regarding the keys/certificates) may be skipped if your system already has this in place.

#. Verify the fingerprint of each certificate:

   .. code-block:: bash

      gpg --quiet --with-fingerprint ./RPM-GPG-KEY-OpenGeo_Suite

   ::

      pub  2048R/E2DD3C77 2015-06-05 OpenGeo Suite <contact@boundlessgeo.com>
            Key fingerprint = DEF2 E3C7 75C7 5037 97F5  9111 6E26 79DC E2DD 3C77
      sub  2048R/D8FEF4BF 2015-06-05

   .. code-block:: bash

      gpg --quiet --with-fingerprint ./0608B895.txt

   ::

      pub  4096R/0608B895 2010-04-23 EPEL (6) <epel@fedoraproject.org>
            Key fingerprint = 8C3B E96A F230 9184 DA5C  0DAE 3B49 DF2A 0608 B895

#. Import the Boundless and Fedora GPG keys:

   .. code-block:: bash

      rpm --import ./RPM-GPG-KEY-OpenGeo_Suite
      rpm --import ./0608B895.txt

#. Add the OpenGeo Suite repository by creating the file :file:`/etc/yum.repos.d/OpenGeo.repo` and adding the following contents:

   * For **OpenGeo Suite**::

        [opengeo]
        name=opengeo
        baseurl=https://yum.boundlessgeo.com/suite/latest/<OS>/$releasever/$basearch
        enabled=1
        gpgcheck=1
        gpgkey=https://yum.boundlessgeo.com/RPM-GPG-KEY-OpenGeo_Suite,https://getfedora.org/static/0608B895.txt

     Make sure to replace ``<OS>`` with either ``centos`` or ``rhel`` based on your distribution.

   * For **OpenGeo Suite Enterprise**::

        [opengeo]
        name=opengeo
        baseurl=https://<username>:<password>@yum-ee.boundlessgeo.com/suite/latest/<OS>/$releasever/$basearch
        enabled=1
        gpgcheck=1
        gpgkey=https://<username>:<password>@yum-ee.boundlessgeo.com/RPM-GPG-KEY-OpenGeo_Suite,https://getfedora.org/static/0608B895.txt

     Make sure to replace each instance of ``<username>`` and ``<password>`` with the user name and password supplied to you after your purchase. Also, replace ``<OS>`` with either ``centos`` or ``rhel`` based on your distribution.

     .. note:: If you have OpenGeo Suite Enterprise and do not have a user name and password, please `contact us <http://boundlessgeo.com/about/contact-us/sales>`__.

Update process
--------------

#. Search for OpenGeo Suite packages to verify that the repository list is correct. If the command does not return any results, examine the output of the ``yum`` command for any errors or warnings.

   .. code-block:: bash

      yum search opengeo

#. Run the following commands to remove the previous OpenGeo Suite Tomcat package:

   .. code-block:: bash

      rpm -e --nodeps --noscripts opengeo-tomcat
      yum remove tomcat6

#. You have options on what packages to update:

   .. note::  See the :ref:`Packages <intro.installation.redhat.packages>` section for details of individual packages.

   * To update typical server components:

     .. code-block:: bash

        yum upgrade opengeo-server geoserver-*

   * To update typical client components:

     .. code-block:: bash

        yum upgrade opengeo-client

   * To update typical client and server components:

     .. code-block:: bash

        yum upgrade opengeo geoserver-*

  .. note:: If you encounter an error where GDAL failed to update, you may need to manually uninstall ``gdal-filegdb``:

      .. code-block:: bash

         yum remove gdal-filegdb

#. Be sure to update any additional :ref:`packages <intro.installation.redhat.packages>` that you installed originally. For example, to update the :ref:`Boundless SDK <webapps.sdk>`:

   .. code-block:: bash

      yum upgrade opengeo-webapp-sdk

#. Restart the Suite

   .. code-block:: bash

      service tomcat restart

After update
------------

The update is now complete. Please see the section on :ref:`intro.installation.redhat.postinstall` to continue.
