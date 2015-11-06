.. _intro.installation.ubuntu.majorupdate:

Updating a major version
========================

This section will show how to update OpenGeo Suite 3.x to |version| on Ubuntu Linux.

.. note::

   * For new installations, please see the section on :ref:`intro.installation.ubuntu.install`.
   * For upgrading to **OpenGeo Suite Enterprise**, please see the section on :ref:`intro.installation.ubuntu.upgrade`.
   * For updating from a previous **minor version** of OpenGeo Suite (4.x), please see the :ref:`intro.installation.ubuntu.minorupdate` section.

.. warning:: This update is **not-backward compatible**. Irreversible changes are made to the data so that they can't be used with versions 3.x and below of OpenGeo Suite.

Back up PostGIS databases
~~~~~~~~~~~~~~~~~~~~~~~~~

.. note:: The commands in this section require root privileges. 

The first step of the update process is to back up your existing PostGIS data. 

#. Change to the ``root`` user:

   .. code-block:: bash

      sudo su - 

#. Ensure PostgreSQL is running.

   .. code-block:: bash

      service postgresql start

#. Change to the ``postgres`` user:

   .. code-block:: bash

      su - postgres

#. To backup PostGIS a separate utility is used. `Download <http://repo.opengeo.org/suite/releases/pgupgrade/postgis_upgrade-4.0.zip>`_  the archive and extract it to a temporary directory. By default, the backup files created from using this script will be saved into this same directory:

   .. code-block:: console

      mkdir -p /tmp/opengeo_backup/pg_backup
      cd /tmp/opengeo_backup/pg_backup
      wget http://repo.opengeo.org/suite/releases/pgupgrade/postgis_upgrade-4.0.zip
      unzip postgis_upgrade-4.0.zip

   .. warning:: The :file:`/tmp` directory is not recommended for long term backups, as it can often be purged as part of normal system activity.

#. Run the backup command:

   .. code-block:: console

      perl postgis_upgrade.pl backup 

   .. note:: For more information about supported options run ``perl postgis_upgrade.pl --help``. 

#. The script will run and create a number of files:

   * Compressed dump files for every database backed up (:file:`<database>.dmp`)
   * SQL output of server roles

#. Exit back to the ``root`` user:

   .. code-block:: bash

      exit

#. The PostGIS data backup process is complete. 

Back up GeoServer data directory
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The next step is to back up the GeoServer data directory, located at :file:`/var/lib/opengeo/geoserver`. 

#. Shutdown the Tomcat service:

   .. code-block:: bash

      service tomcat7 stop

#. Copy the old data directory to :file:`/tmp`:

   .. code-block:: bash

      cp -r /var/lib/opengeo/geoserver /tmp/opengeo_backup

Uninstall old version
~~~~~~~~~~~~~~~~~~~~~

You may now uninstall the old version of OpenGeo Suite.

The package(s) to remove depend on what was installed. Please see the section on :ref:`intro.installation.ubuntu.uninstall` for details.

.. note:: If unsure, run the following command to see the relevant list of packages:

   .. code-block:: bash

      dpkg --get-selections | grep opengeo 

Install new version
~~~~~~~~~~~~~~~~~~~

You may now install the new version of OpenGeo Suite. See the :ref:`new installation <intro.installation.ubuntu.install>` section for details.

Restore PostGIS databases
~~~~~~~~~~~~~~~~~~~~~~~~~

#. Ensure PostgreSQL is running:

   .. code-block:: bash

      service postgresql start

#. Change to the ``postgres`` user:

    .. code-block:: console
 
       su - postgres

#. Restore your PostGIS data by running the utility again with the "restore" argument:

   .. code-block:: console

      cd /tmp/opengeo_backup/pg_backup
      perl postgis_upgrade.pl restore 

#. Your databases and roles will be restored. You can verify that the databases were created and data restored by running ``psql -l`` on the command line.

Restore GeoServer data directory
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The default GeoServer data directory location for OpenGeo Suite is located at :file:`/var/lib/opengeo/geoserver`. 

#. Go back to the root account:

   .. code-block:: bash

      exit
   
#. Stop the Tomcat service:

   .. code-block:: bash

      service tomcat7 stop

#. Rename the new default data directory:

   .. code-block:: bash

      mv /var/lib/opengeo/geoserver /var/lib/opengeo/geoserver.old

#. Restore the original data directory:

   .. code-block:: console

      cp -r /tmp/opengeo_backup/geoserver /var/lib/opengeo/geoserver

#. Ensure proper permissions on the restored copy:

   .. code-block:: console

      chown -R tomcat7 /var/lib/opengeo/geoserver

#. Start the Tomcat service:

   .. code-block:: bash

      service tomcat7 start

After update
------------

Update is now complete. Please see the section on :ref:`intro.installation.ubuntu.postinstall` to continue.
