.. _dataadmin.pgDBAdmin.movedir:

Moving the location of the PostGIS database
===========================================

One does not normally need to change the location of the PostgreSQL/PostGIS database on the file system, but it can be done.

By default, the database is typically located in the following locations:

.. list-table::
   :header-rows: 1
   :stub-columns: 1

   * - Operating system
     - File path
   * - Windows
     - :file:`C:\\ProgramData\\Boundless\\OpenGeo\\pgsql\\9.3`
   * - OS X
     - :file:`~/Library/Application\\ Support/PostGIS`
   * - Ubuntu Linux
     - :file:`/var/lib/postgresql/9.3/main`
   * - Red Hat / CentOS Linux
     - :file:`/var/lib/pgsql/9.3/data`

Please see the instructions specific to your operating system.

Windows
-------

#. Open Windows Services (typically :menuselection:`Control Panel --> Administrative Tools --> Services` or :menuselection:`Start --> Run --> services.msc`) and stop the :guilabel:`OpenGeo PostgreSQL` service.

   .. note:: You can also use the :guilabel:`Stop` shortcut for :guilabel:`PostGIS` in the Start Menu.

#. Open the file :file:`pg_config.cmd`, typically located in :file:`C:\\Program Files (x86)\\Boundless\\OpenGeo\\bin`, in a text editor. (For permissions reasons, you may wish to open the text editor as an administrator.)

#. Change the line::

     set pg_data_dir=%data%\pgsql\%pg_version%\

   to::

      set pg_data_dir=<NEWPATH>

   where ``<NEWPATH>`` would be the desired path (such as ``D:\pgdata``)

#. Start the :guilabel:`OpenGeo PostgreSQL` service.

OS X
----

#. Quit the PostGIS application using the menu.

#. In a terminal, move the location of the data directory::

      sudo mv ~/Library/Application\ Support/PostGIS <NEWPATH>

   where ``<NEWPATH>`` would be the desired path (such as ``~/pgdata``)

#. Create a symlink from the old path the new one::

     ln -s ~/Library/Application\ Support/PostGIS <NEWPATH>

#. Launch PostGIS.

Ubuntu Linux
------------

There are two ways to accomplish this in Ubuntu: **use a symlink** or **edit the configuration file**.

Using a symlink:

#. Stop the PostgreSQL service::

     sudo service postgresql stop

#. Move the location of the data directory::

     sudo mv /var/lib/postgresql/9.3/main <NEWPATH>

   where ``<NEWPATH>`` would be the desired path (such as ``/opt/pgdata``)

#. Create a symlink from the old path the new one::

     ln -s /var/lib/postgresql/9.3/main <NEWPATH>

#. Start the PostgreSQL service::

     sudo service postgresql start

Editing the configuration file:

#. Stop the PostgreSQL service::

     sudo service postgresql stop

#. Move the location of the data directory::

     sudo mv /var/lib/postgresql/9.3/main <NEWPATH>

   where ``<NEWPATH>`` would be the desired path (such as ``/opt/pgdata``)

#. Open :file:`/etc/postgresql/9.3/main/postgresql.conf` in a text editor (with super user privileges).

#. Find the line that reads ``data_directory=/var/lib/postgresql/9.3/main`` and change the path to the ``<NEWPATH>`` determined above.

#. Save and close the file.

#. Start the PostgreSQL service::

     sudo service postgresql start

Red Hat / CentOS Linux
----------------------

There are two ways to accomplish this in Red Hat / CentOS: **use a symlink** or **edit the configuration file**.

Using a symlink:

#. Stop the PostgreSQL service::

     sudo service postgresql-9.3 stop

#. Move the location of the data directory::

      sudo mv /var/lib/pgsql/9.3/data <NEWPATH>

   where ``<NEWPATH>`` would be the desired path (such as ``/opt/pgdata``)

#. Create a symlink from the old path the new one::

      ln -s /var/lib/pgsql/9.3/data <NEWPATH>

#. Start the PostgreSQL service::

      sudo service postgresql-9.3 start

Editing the configuration file:

#. Stop the PostgreSQL service::

     sudo service postgresql-9.3 stop

#. Move the location of the data directory::

      sudo mv /var/lib/pgsql/9.3/data <NEWPATH>

   where ``<NEWPATH>`` would be the desired path (such as ``/opt/pgdata``)

#. Open :file:`/etc/rc.d/init.d/postgresql-9.3` in a text editor (with super user privileges).

#. Find the line that starts with ``PGDATA`` and and change the path to the ``<NEWPATH>`` determined above.

#. Save and close the file.

#. Start the PostgreSQL service::

     sudo service postgresql start
