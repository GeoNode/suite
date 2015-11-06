.. _intro.installation.redhat.install:

Installing
==========

This section describes how to perform an installation of **OpenGeo Suite** |version| on Red hat-based Linux distributions. These instructions should be followed if your system does not have OpenGeo Suite installed.

.. note::

   * For upgrading to **OpenGeo Suite Enterprise**, please see the section on :ref:`intro.installation.redhat.upgrade`.
   * For updating from a previous **minor version** of OpenGeo Suite (4.x), please see the :ref:`intro.installation.redhat.minorupdate` section.
   * For updating from a previous **major version** of OpenGeo Suite (3.x), please see the :ref:`intro.installation.redhat.majorupdate` section.

.. warning:: While QGIS often paired with the rest of OpenGeo Suite, it is not currently bundled as a package by Boundless. **We do not recommend running QGIS on the same machine as OpenGeo Suite**, as package conflicts will occur. Instead, you can use QGIS on a different machine and connect to OpenGeo Suite services from there.

.. _intro.installation.redhat.install.sysreq:

.. include:: include/sysreq.txt

Pre-installation process
------------------------

This installation will add the OpenGeo Suite package repository and then install the appropriate packages. See the :ref:`Packages <intro.installation.redhat.packages>` section for details about the possible packages to install.

.. warning:: Mixing repositories is not recommended. If you already have a community (non-Boundless) repository that contains some of the components of OpenGeo Suite (such as PostgreSQL) please remove them before installing OpenGeo Suite.

The commands in this section require root privileges.

#. Change to the ``root`` user:

   .. code-block:: bash

      sudo su -

#. Download the Boundless and Fedora GPG keys:

   .. code-block:: bash

      wget https://yum.boundlessgeo.com/RPM-GPG-KEY-OpenGeo_Suite
      wget https://getfedora.org/static/0608B895.txt

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

Installation process
--------------------

#. Search for OpenGeo Suite packages to verify that the repository list is correct. If the command does not return any results, examine the output of the ``yum`` command for any errors or warnings.

   .. code-block:: bash

      yum search opengeo

#. You have options on what packages to install:

   .. note::  See the :ref:`Packages <intro.installation.redhat.packages>` section for details of individual packages.

   * To install typical server components:

     .. code-block:: bash

        yum install opengeo-server

   * To install typical client components:

     .. code-block:: bash

        yum install opengeo-client

   * To install typical client and server components:

     .. code-block:: bash

        yum install opengeo

#. Restart the Suite

     .. code-block:: bash

        service tomcat restart

After installation
------------------

Installation is now complete. Please see the section on :ref:`intro.installation.redhat.postinstall` to continue.
