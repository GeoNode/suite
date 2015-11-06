.. _intro.installation.war.install:

Installing
==========

OpenGeo Suite is compatible with a number of application servers, among them Jetty, Apache Tomcat, JBoss, and WebSphere. The most popular and most recommended application server is `Apache Tomcat <http://tomcat.apache.org/>`_.

This section will show how to install OpenGeo Suite for Application Servers. **As application server deployments can vary widely, these are just guidelines**, and your specific setup may differ from those detailed below.

System requirements
-------------------

OpenGeo Suite requires the use of **Java 7** or higher. You may use either a JRE or a JDK from Oracle or the OpenJDK project.

.. for reference http://docs.geoserver.org/latest/en/user/installation/java.html

Other system requirements are as follows:

* :ref:`Windows system requirements <intro.installation.windows.install.sysreq>`
* :ref:`OS X system requirements <intro.installation.mac.install.sysreq>`
* :ref:`Ubuntu system requirements <intro.installation.ubuntu.install.sysreq>`
* :ref:`Red Hat / CentOS system requirements <intro.installation.redhat.install.sysreq>`

.. _intro.installation.war.install.deploy:

Deploying web applications
--------------------------

The following steps should be followed to deploy the web applications:

Increasing available memory
~~~~~~~~~~~~~~~~~~~~~~~~~~~

We recommend the following memory allocations in Tomcat:

* Maximum heap size: 1 GB
* Maximum PermGen size: 128 MB

The Java options for these settings are: ``-Xmx1024m -XX:MaxPermSize=128m``.

Add these options to your application server and then restart.

Tomcat-specific deployment
~~~~~~~~~~~~~~~~~~~~~~~~~~

The use of the `Tomcat Management Console <http://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html>`_ is optional and provides an administrative front end for monitoring Tomcat that can also be used to deploy web applications.

To deploy applications using Tomcat Management Console:

#. Open the Management Console (often available at ``http://localhost:8080/manager/html``).

#. Locate the :guilabel:`Deploy` heading and click the :guilabel:`Browse` button.

   .. figure:: img/deploy-browse.png
        
      Deploying a web application

#. Select the web application file to deploy.

#. Click :guilabel:`Deploy`. The WAR file will be uploaded and unpacked into the :file:`webapps` folder.

#. Repeat this process as needed for every web application to be deployed.
   
.. note::

   The Tomcat Management Console has a default upper limit on the size of WAR files that can be deployed. This will need to be changed in order to deploy many of the OpenGeo Suite web applications. To change this, open the :file:`webapps/manager/WEB-INF/web.xml` file in a text editor, and edit the ``*multipart-config`` parameters as follows:
   
   .. code-block:: xml
      :emphasize-lines: 3,4
      
       <multipart-config>
         <!-- 260 MB max -->
         <max-file-size>262144000</max-file-size>
         <max-request-size>262144000</max-request-size>
         <file-size-threshold>0</file-size-threshold>
       </multipart-config>

You can also perform a manual deployment in Tomcat by copying individual WAR files to the :file:`webapps` directory. (You may need to restart Tomcat.) Each WAR file will be unpacked into a corresponding directory, so :file:`geoserver.war` will be unpacked to :file:`webapps/geoserver`, etc.

.. _intro.installation.war.install.deploy.extdatadir:

Externalizing the GeoServer data directory
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

GeoServer includes a built-in data directory used to manage configuration information. To facilitate updating and prevent data loss, **it is highly recommended to move the data directory to a location external to the application**.

#. Stop the application server.

#. Move the :file:`data` directory inside :file:`webapps/geoserver` to an external location. Here are some suggested locations:
   
   * **Linux**: :file:`/var/lib/opengeo/geoserver`
   * **Windows**: :file:`C:\\ProgramData\\Boundless\\OpenGeo\\geoserver`
   * **OS X**: :file:`/Users/opengeo/geoserver_data`

#. Open :file:`geoserver/WEB-INF/web.xml` in a text editor.

#. Change the ``GEOSERVER_DATA_DIR`` parameter to point to the new directory location.

#. Save the file and restart the application server.

Externalizing the GeoWebCache configuration and cache 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

By default, GeoWebCache stores the cache and cache configuration information in the temporary storage folder of the application server. To prevent data loss, it is highly recommended to move the data directory to a permanent location external to the application server.

#. Stop the application server.

#. Move the :file:`geowebcache/geowebcache.xml` file from ``geowebcache`` to an external location. Here are some suggested locations:
   
   * **Linux**: :file:`/var/lib/opengeo/geowebcache/geowebcache.xml`
   * **Windows**: :file:`C:\\ProgramData\\Boundless\\OpenGeo\\geowebcache\\geowebcache.xml`
   * **OS X**: :file:`/Users/opengeo/geowebcache_data/geowebcache.xml`

#. Open :file:`geowebcache/WEB-INF/geowebcache-core-context.xml` in a text editor and modify the constructor argument with the new path to :file:`geowebcache.xml` (just the path, not including the file name:

   .. code-block:: xml
      :emphasize-lines: 3
      
      <bean id="gwcXmlConfig" class="org.geowebcache.config.XMLConfiguration">
        <constructor-arg ref="gwcAppCtx" />
        <constructor-arg value="/var/lib/opengeo/geowebcache" />

#. Next, move the cache directory. Here are some suggested locations:

   * **Linux**: :file:`/var/cache/geowebcache`
   * **Windows**: :file:`C:\\ProgramData\\Boundless\\OpenGeo\\geowebcache`
   * **OS X**: :file:`/Users/opengeo/geowebcache_data`

#. Open :file:`geowebcache/WEB-INF/web.xml` in a text editor and configure the ``GEOWEBCACHE_CACHE_DIR`` location. 
   
   .. code-block:: xml
      :emphasize-lines: 3
      
      <context-param>
        <param-name>GEOWEBCACHE_CACHE_DIR</param-name>
        <param-value>/var/cache/geowebcache</param-value>
      </context-param>

#. Save all files and restart the application server.
