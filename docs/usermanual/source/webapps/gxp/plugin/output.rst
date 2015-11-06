.. _webapps.gxp.plugin.output:

Plugin with output only
=======================

This example will create a new plugin that will display the perimeter and area of all the boxes that are drawn on the map. 

Creating a basic panel
----------------------

#. The first step is to create a plugin that outputs some static text. Create a file called :file:`BoxInfo.js` in the :file:`plugins` directory.

#. Open this file in a text editor and add the following:

   .. literalinclude:: script/output_BoxInfo_initial.js
      :language: javascript

  This plugin will only implement the ``addOutput`` function to create a panel with a title and some content.

Connecting to the application
-----------------------------

#. To add the BoxInfo plugin to the application, edit :file:`src/app/app.js` and include the dependency at the top of the file:

   .. literalinclude:: script/output_app.js
      :language: javascript
      :lines: 16

#. Add a container for the output to the ``items`` section of the ``portalConfig`` definition in :file:`src/app/app.js`:

   .. literalinclude:: script/output_app.js
      :language: javascript
      :lines: 39-45

#. In the ``tools`` section, add an entry for the "boxinfo" tool and direct its output to the south panel:

   .. literalinclude:: script/output_app.js
      :language: javascript
      :lines: 77-78,80-81

#. Restart the SDK and reload the application in the browser to see the results:

   .. figure:: img/output_boxinfo.png

      Adding an information box to the application

Adding dynamic content
----------------------

#. To connect this panel to dynamic content, it needs a reference to the vector ``boxLayer`` that is created by the ``DrawBox`` tool. This reference is established by attaching an ``id`` to the DrawBox tool in :file:`app.js`. The BoxInfo tool will then reference this ``id`` value. Add the ``id`` to :file:`app.js` after ``ptype: "myapp_drawbox"`` and before ``actiontarget: "map.tbar"``. 

   .. literalinclude:: script/output_app.js
      :language: javascript
      :lines: 73-77
      :emphasize-lines: 3

#. Add the reference to the boxinfo config, between ``ptype: "myapp_boxinfo"`` and ``outputTarget: "southpanel"``:

   .. literalinclude:: script/output_app.js
      :language: javascript
      :lines: 77-81
      :emphasize-lines: 3


#. Now, switching back over to :file:`BoxInfo.js`, replace the ``addOutput`` function of the BoxInfo tool with the following code. With this change, the application will depict information about the box that has been drawn.

   .. literalinclude:: script/output_BoxInfo.js
      :language: javascript
      :lines: 7-26

   In the above code, the ``boxTool`` string identifier finds the DrawBox tool so that it can get a reference to its ``boxLayer`` property. 

#. Add the following code below the code added in the previous step:

   .. literalinclude:: script/output_BoxInfo.js
      :language: javascript
      :lines: 28-33

   When a feature is added to the ``boxLayer``, the code adds a panel to the output container. The content is generated using an ``Ext.Template``.

#. Reload the application as before. Draw a few boxes on the map and verify that the container at the bottom will display information about the boxes:

   .. figure:: img/output_boxinfo_area.png

      Box info showing area and perimeter

   .. note:: To adjust the output, use the ``tplText`` parameter and the ``outputConfig`` section of the tool in :file:`src/app/app.js`. For example, the following code would display only the area and turn off autoscrolling:

      .. code-block:: javascript

         ptype: "myapp_boxinfo",
         boxTool: "drawbox",
         tplText: "AREA: {area}",
         outputTarget: "southpanel",
         outputConfig: {
           title: "Box info",
           autoScroll: false
         }

Download the :download:`BoxInfo.js <script/output_BoxInfo.js>` and :download:`app.js <script/output_app.js>` files created in this section.
