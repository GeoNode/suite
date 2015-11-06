.. _sysadmin.manager:

Using Tomcat Manager
====================

.. note::

   This section only applies to the following versions of OpenGeo Suite:

   * OpenGeo Suite for Ubuntu Linux
   * OpenGeo Suite for Red Hat Linux
   * OpenGeo Suite for Application Servers (when using **Tomcat**).

   OpenGeo Suite for Windows and OS X use `Jetty <http://www.eclipse.org/jetty/>`_, not Tomcat.

`Apache Tomcat <http://tomcat.apache.org>`_ is the application server used on Linux-based installations of OpenGeo Suite. While Apache Tomcat has a graphical manager for loading and managing web applications, **the Tomcat Manager is not loaded with OpenGeo Suite by default**.

This section will show you how to install and perform basic tasks with Tomcat Manager.

.. note:: The Tomcat service used by OpenGeo Suite is pulled in from standard repository sources, and is not specific to OpenGeo Suite. The same is true for the Tomcat Manager.

Installing via package manager
------------------------------

Ubuntu Linux
^^^^^^^^^^^^

The Tomcat Manager can be installed with the ``tomcat7-admin`` package through the standard Ubuntu package manager.

#. In a terminal, enter the following command:

   .. code-block:: console

      sudo apt-get install tomcat7-admin

#. Restart Tomcat:

   .. code-block:: console

      sudo service tomcat7 restart

#. To verify that the package was installed correctly, navigate to ``http://localhost:8080/manager`` (or substitute the location of the root web application URL) and you will see a request for credentials:

   .. figure:: img/tomcatcreds.png

      Tomcat manager asking for credentials

#. Click :guilabel:`Cancel` for now.

#. Continue below at the :ref:`sysadmin.manager.access` section.

Red Hat-based Linux
^^^^^^^^^^^^^^^^^^^

The Tomcat Manager can be installed with the ``tomcat-host-manager`` package through the standard Red Hat package manager.

#. In a terminal, enter the following command:

   .. code-block:: console

      sudo yum install tomcat-host-manager

#. Restart Tomcat:

   .. code-block:: console

      sudo service tomcat7 restart

#. To verify that the package was installed correctly, navigate to ``http://localhost:8080/manager/html`` (or substitute the location of the root web application URL) and you will see a request for credentials.

#. Click :guilabel:`Cancel` for now.

#. Continue below at the :ref:`sysadmin.manager.access` section.

.. _sysadmin.manager.access:

Allowing access to Tomcat Manager
---------------------------------

To access Tomcat Manager, you will need to create a user in Tomcat with the credentials to access it. The simplest way to create a user in Tomcat is as follows:

#. Open :file:`tomcat-users.xml` (typically found in :file:`/etc/tomcat7`) in a text editor.

#. In the ``<tomcat-users>`` block, add the following line:

   .. code-block:: xml

     <user username="admin" password="tomcat" roles="manager-gui"/> 

   This will create a user with the name of ``admin`` and a password of ``tomcat``. Feel free to substitute your own credentials.

   .. warning:: Make sure this line is not inside a block that is commented out, otherwise it will have no effect. 

#. Save and close the file.

#. Restart Tomcat.

#. Navigate back to ``http://localhost:8080/manager/html`` and verify that the credentials allow access to Tomcat Manager.

   .. figure:: img/tomcatmanager.png

      Tomcat Manager

For more details on setting up a user in Tomcat, please see the `Tomcat Manager documentation <http://tomcat.apache.org/tomcat-7.0-doc/security-manager-howto.html>`_.

Deploying an application using Tomcat Manager
---------------------------------------------

While it is usually acceptable to deploy a web application by copying the ``WAR`` file to the Tomcat ``webapps`` directory, you may wish to deploy the application via the Tomcat Manager.

To do this:

#. Log in to Tomcat Manager.

#. Scroll down to the section titled :guilabel:`Deploy`.

   .. figure:: img/tomcatdeploy.png

      The Deploy section of Tomcat Manager

#. Click :guilabel:`Choose File` and then select the web application file to deploy.

#. Click :guilabel:`Deploy`.

#. The application will be uploaded to the server and be deployed. It will also automatically start if possible.

Increasing the maximum file size for uploads
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Tomcat Manager usually maintains a file size limit for uploaded files (typically 50MB). This can cause large applications to fail during the upload process::

  The request was rejected because its size (134888150) exceeds the configured maximum (52428800) 

To change/remove this limit:

#. Open the :file:`web.xml` in the :file:`WEB-INF` directory associated with Tomcat Manager. 

   .. note:: This file is often located at :file:`/usr/share/tomcat7-admin/manager/WEB-INF/web.xml`.

#. Scroll down to the block that contains the file size limit:

   .. code-block:: xml 
      :emphasize-lines: 3

      <multipart-config>
        <!-- 50MB max -->
        <max-file-size>52428800</max-file-size>
        <max-request-size>52428800</max-request-size>
        <file-size-threshold>0</file-size-threshold>
      </multipart-config>

#. Change the ``<max-file-size>`` line to contain a larger value, or remove the line entirely:

   .. code-block:: xml 
      :emphasize-lines: 3

      <multipart-config>
        <!-- 200MiB max -->
        <max-file-size>200000000</max-file-size>
        <max-request-size>52428800</max-request-size>
        <file-size-threshold>0</file-size-threshold>
      </multipart-config>

#. Save and close the file.

#. Restart Tomcat.

.. note:: Without changing this limit, it is still possible to deploy large applications by copying them to the Tomcat :file:`webapps` folder. In most cases, the application will automatically be deployed.
