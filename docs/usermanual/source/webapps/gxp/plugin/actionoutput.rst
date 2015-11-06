.. _webapps.gxp.plugin.actionoutput:

Plugin with action and output
=============================

This final example will modify the DrawBox plugin created previously to also display a pop-up window after the box has been drawn on the map.

#. Open the :file:`DrawBox.js` file in a text editor.

#. Add an event listener to the ``DrawFeature`` control to display a pop-up containing the area of the box by replacing the existing control with the following code.

   .. literalinclude:: script/actionoutput_DrawBox.js
      :language: javascript
      :lines: 33-44

#. Add the ``displayPopup`` function to the :file:`DrawBox.js` file, which will create the output in the form of a ``GeoExt.Popup``. Insert it before the ``raiseLayer`` function:

   .. literalinclude:: script/actionoutput_DrawBox.js
      :language: javascript
      :lines: 49-60

#. Add the dependency for the ``GeoExt.Popup`` in the top of the :file:`DrawBox.js` file:

   .. literalinclude:: script/actionoutput_DrawBox.js
      :language: javascript
      :lines: 10

#. Open :file:`app.js` and add an ``outputTarget`` for the ``DrawBox`` tool, in between ``id: "drawbox",`` and ``actionTarget: "map.tbar"``:

   .. literalinclude:: script/actionoutput_app.js
      :language: javascript
      :lines: 73-78
      :emphasize-lines: 4

#. Restart the SDK and reload the application in the browser. Test the functionality by drawing boxes. After drawing a box there will now be a pop-up window at the feature's location containing the area of the box drawn. The units are in square meters, as the geometry is using Google Web Mercator projection.

   .. figure:: img/actionoutput_popup.png

      Popup showing area of drawn box

Download the :download:`DrawBox.js <script/actionoutput_DrawBox.js>` and :download:`app.js <script/actionoutput_app.js>` files created in this section. If you also download :download:`BoxInfo.js <script/output_BoxInfo.js>` from the previous section, you will have all the files to recreate this application.

