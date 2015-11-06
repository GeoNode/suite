.. _dataadmin.pgGettingStarted.pgadmin:


Connecting to the PostgreSQL database with pgAdmin
==================================================

There are a number of front-end tools available for connecting to, and working with, the PostgreSQL database. Among the most popular are `psql <http://www.postgresql.org/docs/9.3/static/app-psql.html>`_, a command-line tool for querying the database, and the free and open source graphical tool `pgAdmin <http://www.pgadmin.org/>`_. 

Any data querying and manipulation you can do with ``pgAdmin`` can also be done at the command line with ``psql``.

.. note:: This section uses the graphical utility ``pgAdmin`` which may not be automatically present, depending on the type of installation of OpenGeo Suite. Please see the :ref:`intro.installation` section for information on how to install these tools for your platform.


Launching pgAdmin
-----------------

Depending on how OpenGeo Suite was installed pgAdmin may or may not have been installed. See the :ref:`intro.installation` section for installation instructions. Once installed follow the instructions below for your platform. 

Windows
^^^^^^^

On Windows pgAdmin can be launched from the :guilabel:`Start Menu`. 

.. figure:: img/pgadmin_win.png

   Launching pgAdmin on Windows

Mac OS X
^^^^^^^^

On OS X, the pgAdmin application is accessible from the installer image under :guilabel:`PostGIS Utilities`.

.. figure:: img/pgadmin_mac.png

   Launching pgAdmin on OS X

Linux
^^^^^

The pgAdmin application is installed via the ``pgadmin3`` package, and can be launched from the terminal with the ``pgadmin3`` command. On systems with graphical interfaces, ``pgAdmin III`` may be available from the Applications menu.

.. note:: Some extra steps need to performed before pgAdmin will be available from both local and remote hosts. Please see the section on :ref:`dataadmin.pgGettingStarted.firstconnect` for configuration information.

Working with pgAdmin
--------------------

Depending on how you installed OpenGeo Suite, you may already have one pre-configured PostGIS server, ``localhost:5432``, listed in the pgAdmin :guilabel:`Object browser`.

.. figure:: img/pgadmin_postgissrv.png

   PostGIS server in pgAdmin

If you don't have an entry for PostGIS listed, add a new server connection manually. Click :menuselection:`File --> Add Server` and complete the :guilabel:`New Server Registation` dialog box to register a new server. Ensure the Host is set to **localhost** and Port is **5432** (unless you have configured PostgreSQL for a different port). 

.. figure:: img/pgadmin_connectwindows.png

   Connection parameters on Windows / OS X

.. figure:: img/pgadmin_connectlinux.png

   Connection parameters on Linux


To connect to the PostGIS server, double-click the PostGIS server item and provide the password when prompted.

To view the databases in this instance, expand the :menuselection:`Databases` item. Double-click one of the listed databases to reveal the contents in the :guilabel:`Object browser`.  

.. figure:: img/pgadmin_treetable.png

   Navigating the database

When executing SQL queries, make sure you have the intended target database selected.  The SQL :guilabel:`Query` dialog box will confirm the current database selection.

.. figure:: img/pgadmin_querydb.png

   Querying a database

.. note:: If you are just installing OpenGeo Suite for the first time, there will only be a generic "postgres" database installed, with no tables, and you will need to :ref:`create a new spatial database <dataadmin.pgGettingStarted.createdb>`.
