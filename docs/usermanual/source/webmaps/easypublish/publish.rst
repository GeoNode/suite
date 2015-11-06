.. _webmaps.basic.publish:

Compose and publish your map
============================

With your layers :ref:`styled <webmaps.basic.style>` and :ref:`edited <webmaps.basic.edit>`, you are at last ready to compose and publish your map. Map composition involves loading all the layers you wish to publish, determining layer order, and determining map extent and positioning. Once this is accomplished, you can save your map in a format that makes it easy to embed in any web page.

Map composition
---------------

#. Make sure all of the layers that you wish to include in your map are loaded in the Layers Panel. This can include local GeoServer layers, remote WMS layers, and other hosted services such as MapQuest or Google.

#. In the Layers Panel, click and drag the layers up and down to adjust rendering order. The layers that are on the top of the list will be drawn last (and thus "on top" of the other layers).

   .. figure:: img/publish_drag.png
      :align: center

      *Drag layers to reorder*

#. Use the zoom and pan tools to set the default map view.

   .. figure:: img/publish_compose.png

      *A finished map*


Save the map view
-----------------

You can save your map composition for later, to be accessed by a URL bookmark.

#. Click :guilabel:`Map` on the GeoExplorer toolbar and click :guilabel:`Save map` to generate a URL.

   .. figure:: img/publish_savemapbutton.png

      *The Save map tool will generate a URL*

#. When navigating to this URL at a later time, the map will return to this exact placement.

   .. figure:: img/publish_savemap.png

      *Copy this URL to save your map*


Publish via embedded map
------------------------

GeoExplorer allows you to publish your map view by generating a small block of HTML code that can be embedded in any web page.

#. Click :guilabel:`Map` on the GeoExplorer toolbar and click :guilabel:`Publish map`.

   .. figure:: img/publish_publishmapbutton.png

      *The Publish map tool will generate HTML*

#. Choose the tools you would like to have available in your embedded map:

   .. figure:: img/publish_tools.png

      *Selecting the tools for the published map*

#. A dialog box will show some HTML code that can be copied and included in a web page in order to embed the map. You can change the values for the map size in the :guilabel:`Map Size` list, or by changing the :guilabel:`Height` and :guilabel:`Width` values. The changes will automatically be reflected in the HTML.

   .. figure:: img/publish_html.png

      *HTML for embedding a map*

#. Copy and paste this code into any web page or HTML-source to embed your map.

   To try this out create a file :file:`sample.html`:
   
   .. code-block:: html
   
      <html>
      <head>
         <title>GeoExplorer Sample</title>
      </head>
      <body>
         <h1>Sample</h1>
         <p>
         Sample of an embedded GeoExplorer map.
         </p>
         <!-- iframe snippet goes here -->
      </body>
   
   .. note:: URLs that contain "localhost" will not be accessible to anyone other than those on the local machine. Please deploy the OpenGeo Suite onto a server with a web-accessible URL if you would like to share this map beyond your local system. The :ref:`sysadmin` section has more information about deployment.

