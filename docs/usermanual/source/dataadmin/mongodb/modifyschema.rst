.. _dataadmin.mongodb.modifyschema:

Modifying or adding a schema or view
====================================

.. todo:: Unclear how necessary this section is.

Schemas can be modified by interacting with the schema store. Multiple schemas, or views, can be created for a single MongoDB document collection by creating a new, unique, ``typeName`` and specifying the ``collection`` under the root-level ``userData`` object. If a view is added it will be available as a new layer name listed when adding a new layer against the store. If a schema is modified it can be realized by editing the layer for the schema and selecting :guilabel:`Reload feature type` on the layer data page.

File URI schema stores
----------------------

For the directory-based schema store, edit the JSON document with the ``typeName`` requiring modification. The document must be compliant with the JSON schema format. Schemas are written by the store without indenting. Indenting can be introduced to ease editing without side effects. Non-ASCII characters are not allowed.

MongoDB URI schema stores
-------------------------

Using a MongoDB document manipulation tool, update or insert the schema document in the collection maintaining the document in a form that follows the JSON schema format. The JSON files contained in the file schema store are in a format that can be inserted into a MongoDB schema store if the ``typeName`` in the file is unique to the document collection as enforced by an index.

