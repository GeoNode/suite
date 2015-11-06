.. _dataadmin.mongodb.jsonschema:

JSON schema format
==================

Examples of JSON schema mapping
-------------------------------

Consider the following GeoJSON feature:

.. code-block:: json

  {
      "type": "Feature",
      "geometry": {
          "type": "Point",
          "coordinates": [ 43.092738, -89.526719 ]
      },
      "properties": {
          "name": "Jos. A. Bank",
          "type": "Men's Clothing Store"
      }
  }


This document could be described with the following JSON schema:

.. code-block:: json

  {
      "typeName": "places",
      "userData": {
          "collection": "places"
       },
      "geometryDescriptor": {
          "localName": "location",
          "crs": {
              "properties": {
                  "name": "urn:ogc:def:crs:EPSG:4326"
              },
              "type": "name"
          }
      },
      "attributeDescriptors": [
          {
              "localName": "location",
              "type": {
                  "binding": "com.vividsolutions.jts.geom.Point"
              },
              "userData": {
                  "encoding": "GeoJSON",
                  "mapping": "geometry"
              }
          },
          {
              "localName": "name",
              "type": {
                  "binding": "java.lang.String"
              },
              "userData": {
                  "mapping": "properties.name"
              }
          },
          {
              "localName": "type",
              "type": {
                  "binding": "java.lang.String"
              },
              "userData": {
                  "mapping": "properties.type"
              }
          }
      ]
  }


A WFS DescribeFeatureType request derived from the JSON schema would be similar to this:

.. code-block:: xml

   <xsd:schema xmlns:gml="http://www.opengis.net/gml" xmlns:opengeo="http://boundlessgeo.com/opengeo"xmlns:xsd="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified" targetNamespace="http://boundlessgeo.com/opengeo">
     <xsd:import namespace="http://www.opengis.net/gml" schemaLocation="http://localhost:8080/geoserver/schemas/gml/2.1.2/feature.xsd"/>
     <xsd:complexType name="placesType">
       <xsd:complexContent>
         <xsd:extension base="gml:AbstractFeatureType">
           <xsd:sequence>
             <xsd:element maxOccurs="1" minOccurs="0" name="geometry" nillable="true" type="gml:PointPropertyType"/>
             <xsd:element maxOccurs="1" minOccurs="0" name="name" nillable="true" type="xsd:string"/>
             <xsd:element maxOccurs="1" minOccurs="0" name="type" nillable="true" type="xsd:string"/>
           </xsd:sequence>
         </xsd:extension>
       </xsd:complexContent>
     </xsd:complexType>
     <xsd:element name="places" substitutionGroup="gml:_Feature" type="opengeo:placesType"/>
   </xsd:schema>


A WFS GetFeature request would produce a response like this:

.. code-block:: xml

   <wfs:FeatureCollection xmlns="http://www.opengis.net/wfs" xmlns:wfs="http://www.opengis.net/wfs"xmlns:opengeo="http://boundlessgeo.com/opengeo" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://boundlessgeo.com/opengeo http://localhost:8080/geoserver/opengeo/wfs?service=WFS&version=1.0.0&request=DescribeFeatureType&typeName=opengeo%3Aplaces http://www.opengis.net/wfs http://localhost:8080/geoserver/schemas/wfs/1.0.0/WFS-basic.xsd">
     <gml:boundedBy>
       <gml:null>unknown</gml:null>
     </gml:boundedBy>
     <gml:featureMember>
       <opengeo:places fid="5320f579f644cc8970f26ea3">
         <opengeo:geometry>
           <gml:Point srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
             <gml:coordinates xmlns:gml="http://www.opengis.net/gml" decimal="." cs="," ts=" ">43.092738,-89.526719</gml:coordinates>
           </gml:Point>
         </opengeo:geometry>
         <opengeo:name>Jos. A. Bank</opengeo:name>
         <opengeo:type>Men's Clothing Store</opengeo:type>
       </opengeo:places>
     </gml:featureMember>
   </wfs:FeatureCollection>

JSON schema definition
----------------------

The schema is a JSON object with the root level fields ``typeName``, ``userData``, ``geometryDescriptor`` object, and an ``attributeDescriptors`` object array.


* ``typeName``: This is the type name for the schema. With a MongoDB schema store this is the key used for schema retrieval. With file stores this field will match the suffix of the file name. The value of this field is also used as the MongoDB collection name in the absence of an collection key in the userData object.
* ``userData``: An optional object containing an optional key, collection.
* ``collection``: When present, its value is used to select the collection to apply schema mapping to. With this method one can generate additional schemas, or views, to a single MongoDB document collection. In the absence of an explicit ``collection`` key the collection is selected with the root level typeName value. If a ``collection`` key exists and its value represents a non-existent or non-compliant MongoDB document collection the schema is ignored.
* ``geometryDescriptor``: The geometry descriptor contains two fields: ``crs`` and ``localName``.

  * ``crs``: Uses the `Named CRS <http://geojson.org/geojson-spec.html#named-crs>`_ format from the `GeoJSON Format Specification 1.0 <http://geojson.org/geojson-spec.html>`_. Note that while a CRS can be defined the only supported CRS for MongoDB GeoJSON encoded geometries is **WGS84**.
  * ``localName``: Specifies the property name for the geometry attribute. It must match the ``localName`` for an entry in the ``attributeDesciptors`` array.

* ``attributeDescriptors``: An array of one or more objects with the following fields:

  * ``localName``: A required value representing a unique name identifying the attribute. Used as the document field mapping in the absence of an explicit ``mapping`` key in the ``userData`` object.
  * ``minOccurs``: An optional field with an integer value representing the minimum occurrence of the value for a feature. options are 0 (default) or 1.
  * ``maxOccurs``: An optional field with an integer value representing the maximum occurrence of the value for a feature. Defaults to 1.
  * ``defaultValue``: An optional field with the default value for an attribute if it is not specified for a feature. The type of the value must match that specified in the type binding.
  * ``type``: A required object containing a single field: ``binding``.

    * ``binding``: A required field referencing the name of the Java type this attribute is to be represented with. Given the absence of type enforcement in MongoDB document fields care should be taken with this field. A "best effort" at conversion will be attempted but if conversion for an attribute value fails no value will be output. With type uncertainty a binding of ``java.lang.String`` should be specified as all literal types can be converted to it.

  * ``userData``: An object with additional data to aid in mapping and type conversion for the attribute.

* ``mapping``: Contains the path to the value in a MongoDB document. Nested fields can be delimited using a period (``.``). In the absence of this field the attribute ``localName`` will be used to extract the feature attribute value from a MongoDB document. 
* ``encoding``: Used to specify the encoding for a mapped field. Currently this field is currently only in use for attribute descriptors describing geometries and is always "GeoJSON". While not currently required it should be specified for forward compatibility.
