.. _dataadmin.mongodb.store:

Adding a store
==============

The process for adding a MongoDB store to GeoServer is the same as for any other file or database store.

#. From the GeoServer web interface, click :guilabel:`Stores`, then :guilabel:`Add a new store`.

#. If the MongoDB extension was installed correctly, you will see an entry for :guilabel:`MongoDB`.

   .. figure:: img/mongodb_storelink.png

      MongoDB in the list of data sources

#. Click the entry for :guilabel:`MongoDB`.

#. Fill out the following form: Enter the following parameters:

   * :guilabel:`Workspace`: Workspace that will contain the MongoDB store.

   * :guilabel:`Data source name`: Internal name of the store.

   * :guilabel:`Description`: Internal description of the store.

   *  :guilabel:`Enabled`: Toggles whether the store is enabled (and all its layers published) or not.

   * :guilabel:`data_store`: Specifies the MongoDB instance and database to connect to.

     .. note::

        This field requires a MongoDB client URI as specified by the `MongoDB Manual <http://docs.mongodb.org/manual/reference/connection-string/>`_. A typical URI will be of the form: ``mongodb://example.com:27017/database`` The URI must include a database, but the database will be created if it doesn't yet exist. Write access is only required if the database needs to be created or if other write operations such as WFS Transactions are expected. If needed, credentials can be supplied with the MongoDB client URI, in the form:  ``mongodb://username:password@example.com:27017/database``. 

   * :guilabel:`schema_store`: Designates the storage for inferred and manually defined schemas.

     .. note::

        This field can accept either a mongodb or file URI. It must be an absolute path of the form ``file:///``, so if using a Windows path, it will need to be converted to this form: ``file:///C:/data/schemas/database`` If Linux: ``file:///opt/schemas/database/``. The directory will be created if it does not exist, in which case write permissions will be necessary.

        The database and collection names are optional. If missing, the database name will default to ``geotools`` and the collection name to ``schemas``. The database and collection must be writable using the credentials provided with the URI. Schemas are stored as MongoDB documents or files adhering to the JSON schema format with the schema "Type Name" (typeName) as the key.

   .. figure:: img/mongodb_storepage.png

      MongoDB store parameters

#. When finished, click :guilabel:`Save`.

The store will be created, and any compliant MongoDB document collections can now be exposed as layers through standard GeoServer processes.

