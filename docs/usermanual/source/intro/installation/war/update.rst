.. _intro.installation.war.update:

Updating
========

To update OpenGeo Suite applications to a newer version:

#. First, ensure GeoServer is configured with an external data directory. See the section on :ref:`intro.installation.war.install.deploy.extdatadir` for how to do this.

   .. warning:: Failure to use an external data directory may cause you to lose all of your data and settings!

#. Uninstall existing web applications. Please see the section on :ref:`intro.installation.war.uninstall` for more details.

#. Restart the application server.

#. Deploy new web applications. Please see the section on :ref:`intro.installation.war.install` for more details.

   .. note:: Be aware that any extensions that have been manually installed will need to be installed again.
