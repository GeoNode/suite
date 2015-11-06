.. _sysadmin.backup:


Backing up
==========

Backing up is a vital task of any system administrator.  It should be done on a regular basis, and especially before doing any in-place upgrades.  This section discusses how to backup the various components of OpenGeo Suite.

.. _sysadmin.backup.geoserver:

Backing up the GeoServer Data Directory
---------------------------------------

The GeoServer data directory is located by default here:

.. list-table::
   :header-rows: 1

   * - Installation Target
     - GeoServer Data directory
   * - Windows
     - ``C:\\ProgramData\\Boundless\\OpenGeo\\geoserver``
   * - Mac
     - ``~/Library/Application Support/GeoServer/data_dir``
   * - Linux
     - ``/var/lib/opengeo/geoserver``
   * - War Install
     - ``<SERVLET_ROOT>/webapps/geoserver/data/``

In general, the directory can safely be copied or archived to another location, and can be restored by reversing the process.  Please be sure to shut down GeoServer before making any backups or restores.

.. _sysadmin.backup.postgis:

Backing up the PostGIS database system
--------------------------------------

.. note:: Please see the :ref:`intro.installation` section for your particular operating system if backing up in preparation for an upgrade to OpenGeo Suite 4.x.

The PostGIS databases can be backed up with the built-in utilities `pg_dump <http://www.postgresql.org/docs/9.3/static/app-pgdump.html>`_ (for a single database) and `pg_dumpall <http://www.postgresql.org/docs/9.3/static/app-pg-dumpall.html>`_ (for multiple databases).

Miscellaneous backup tasks
--------------------------

GeoExplorer saves its map configurations in a file called :file:`geoexplorer.db`.  
By default this file is located at:

.. list-table::

   * - Windows
     - ``C:\\ProgramData\\Boundless\\OpenGeo\\geoexplorer``
   * - Mac
     - ``~/Library/Application Support/GeoServer/data_dir``
   * - Linux
     - ``/var/lib/opengeo/geoexplorer``

This file can be safely backed up and restored as necessary without any special utilities.

Custom content
--------------

Make sure to backup all data and custom applications hosted on the server but not contained in the GeoServer data directory. The details are dependent on the content and where it was placed.
