.. _intro.installation.redhat.uninstall:

Uninstalling
============

Uninstalling OpenGeo Suite is usually the recommended first step before :ref:`updating <intro.installation.ubuntu.majorupdate>` to a new major version or :ref:`upgrading <intro.installation.ubuntu.upgrade>` to OpenGeo Suite Enterprise.

The following procedure will uninstall OpenGeo Suite. In all cases, the data directory at ``/var/lib/opengeo/geoserver`` will not be removed.

.. note:: These commands should be run as root.

#. Determine which package you installed, one of ``opengeo``, ``opengeo-server``, or ``opengeo-client``. These "meta-packages" install other packages, so just uninstalling these won't remove OpenGeo Suite.

   .. code-block:: bash

      yum search opengeo

#. Uninstall the packages installed as dependencies:

   * For ``opengeo-server``:

     .. code-block:: bash

        yum remove postgis21-postgresql93 opengeo-tomcat geoserver geowebcache geoexplorer opengeo-dashboard opengeo-docs opengeo-server

   * For ``opengeo-client``:

     .. code-block:: bash

        yum remove postgis21 pgadmin3 postgis21-gui opengeo-client

   * For ``opengeo``:

     .. code-block:: bash

        yum remove postgis21-postgresql93 opengeo-tomcat geoserver geowebcache geoexplorer opengeo-dashboard opengeo-docs postgis21 pgadmin3 postgis21-gui opengeo

   .. note:: If you encounter any errors during the uninstall process, you may need to manually uninstall tomcat 6:

      .. code-block:: bash

         rpm -e --nodeps --noscripts opengeo-tomcat
         yum remove tomcat6


