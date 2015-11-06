.. _intro.installation.ubuntu.upgrade:

Upgrading to OpenGeo Suite Enterprise
=====================================

This section describes how to upgrade OpenGeo Suite to **OpenGeo Suite Enterprise** on Ubuntu.

.. note:: OpenGeo Suite Enterprise can only be obtained through `Boundless <http://boundlessgeo.com>`_. Please `contact us <http://boundlessgeo.com/about/contact-us/sales/>`__ for information on how to purchase OpenGeo Suite Enterprise.

On a system that already has OpenGeo Suite:

#. Change to the ``root`` user:

   .. code-block:: bash

      sudo su - 

#. Remove the OpenGeo Suite repository entry, which is typically inside either ``/etc/apt/sources.list.d/opengeo.list`` or ``/etc/apt/sources.list``. Open the file and delete the line or lines that refer to OpenGeo Suite.

#. Add the OpenGeo Suite Enterprise repository. Make sure to replace ``<username>`` and ``<password>`` with the user name and password supplied to you after your purchase:

   * If installing on Ubuntu 12:

     .. code-block:: bash

        echo "deb https://<username>:<password>@apt-ee.boundlessgeo.com/suite/v45/ubuntu/ precise main" > /etc/apt/sources.list.d/opengeo.list

   * If installing on Ubuntu 14:

     .. code-block:: bash

        echo "deb https://<username>:<password>@apt-ee.boundlessgeo.com/suite/v45/ubuntu/ trusty main" > /etc/apt/sources.list.d/opengeo.list

   .. note:: If you have OpenGeo Suite Enterprise and do not have a user name and password, please `contact us <http://boundlessgeo.com/about/contact-us/sales>`__.

#. Update the repository list:

   .. code-block:: bash

      apt-get update

#. Now you can upgrade the installed packages, which will now include the OpenGeo Suite Enterprise components:

   .. code-block:: bash

      apt-get upgrade

#. You can now install additional packages as well, such as new :ref:`extensions <intro.extensions>`. For example, to install the :ref:`MongoDB <dataadmin.mongodb>` extension:

   .. code-block:: bash

      apt-get install geoserver-mongodb

   .. note:: See the :ref:`Packages <intro.installation.ubuntu.packages>` section for details about individual packages.

After upgrade
-------------

The upgrade is now complete. Please see the section on :ref:`intro.installation.ubuntu.postinstall` to continue.
