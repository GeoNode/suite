.. _sysadmin.jndi:

Working with JNDI
-----------------

Applications such as GeoServer are in position to work with database connection pools set up by the application server. The Java Naming and Directory Interface (JNDI) can be used to create a connection pool for a JDBC data source.

These instructions will set up a JNDI connection pool on the Tomcat application server using a PostgreSQL data source:

.. note::

   Tomcat is the application server used in:

   * OpenGeo Suite for Ubuntu Linux
   * OpenGeo Suite for Red Hat-based Linux

   Tomcat can also be used with:

   * OpenGeo Suite for Application Servers

#. Copy the JDBC Driver for your database to :file:`<TOMCAT_HOME>/lib`. The JDBC driver can often be found on the website of your DBMS provider, or in the installed version of your database. 

   .. note:: The PostgreSQL JDBC Driver can be found at the `PostgreSQL web site <http://jdbc.postgresql.org/>`_ or in the :file:`geoserver/WEB-INF/lib` directory.

#. Remove the JDBC Driver from the :file:`WEB-INF/lib` directory inside the GeoServer webapps directory. It should be named :file:`postgresql-X.X-XXX.jdbc3.jar`.

#. Register the connection pool with the Tomcat Java Naming and Directory Interface (JNDI). This is done by adding a ``<Resource\>`` entry to the :file:`TOMCAT_HOME/conf/context.xml` configuration file. For example, given a PostgreSQL database named ``test`` with a user name and password of ``admin``, the configuration would look like this:

   .. code-block:: xml

      <Context>
       ...
        <Resource name="jdbc/postgres" auth="Container" type="javax.sql.DataSource"
                  driverClassName="org.postgresql.Driver"
                  url="jdbc:postgresql://localhost:5432/test"
                  username="admin" password="admin"
                  maxActive="20" maxIdle="10" maxWait="-1"/>
      </Context>

   .. note:: For more information about the possible parameters and their values refer to the `DBCP documentation <http://commons.apache.org/dbcp/configuration.html>`_. 

#. When adding a store in GeoServer, select the :guilabel:`JNDI` option. Enter the following information:

   * The :guilabel:`jndiReferenceName` used by the application server.

   * The :guilabel:`schema` used by the database.

    Using the configuration example above, the :guilabel:`jndiReferenceName` would be ``java:comp/env/jdbc/postgres``.

For more information, including how to configure JNDI for Oracle and SQL Server data sources, see the `GeoServer user manual <../../geoserver/tutorials/tomcat-jndi/tomcat-jndi.html>`_.
