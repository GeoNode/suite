.. _intro.installation.redhat.upgrade:

Upgrading to OpenGeo Suite Enterprise
=====================================

This section describes how to upgrade OpenGeo Suite to **OpenGeo Suite Enterprise** on Red Hat-based systems.

.. note:: OpenGeo Suite Enterprise can only be obtained through `Boundless <http://boundlessgeo.com>`_. Please `contact us <http://boundlessgeo.com/about/contact-us/sales/>`__ for information on how to purchase OpenGeo Suite Enterprise.

On a system that already has OpenGeo Suite:

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

#. Add the OpenGeo Suite Enterprise repository by replacing the file :file:`/etc/yum.repos.d/OpenGeo.repo` with the following contents::

     [opengeo]
     name=opengeo
     baseurl=https://<username>:<password>@yum-ee.boundlessgeo.com/suite/v45/<OS>/$releasever/$basearch
     enabled=1
     gpgcheck=1
     gpgkey=https://<username>:<password>@yum-ee.boundlessgeo.com/RPM-GPG-KEY-OpenGeo_Suite,https://getfedora.org/static/0608B895.txt

   Make sure to replace each instance of ``<username>`` and ``<password>`` with the user name and password supplied to you after your purchase. Also, replace ``<OS>`` with either ``centos`` or ``rhel`` based on your distribution.

   .. note:: If you have OpenGeo Suite Enterprise and do not have a user name and password, please `contact us <http://boundlessgeo.com/about/contact-us/sales>`__.

#. Update the repository list:

   .. code-block:: bash

      yum update

#. Clean the metadata for existing packages:

   .. code-block:: bash

      yum clean metadata

#. Now you can reinstall the existing packages, which will now include the OpenGeo Suite Enterprise components. Currently all of the Enterprise-specific components are contained in the ``geoserver`` package:

   .. code-block:: bash

      yum reinstall geoserver

#. You can now install additional packages as well, such as new :ref:`extensions <intro.extensions>`. For example, to install the :ref:`MongoDB <dataadmin.mongodb>` extension:

   .. code-block:: bash

      yum install geoserver-mongodb

   .. note:: See the :ref:`Packages <intro.installation.redhat.packages>` section for details about individual packages.

#. Restart the Suite

   .. code-block:: bash

      service tomcat restart

After upgrade
-------------

The upgrade is now complete. Please see the section on :ref:`intro.installation.redhat.postinstall` to continue.
