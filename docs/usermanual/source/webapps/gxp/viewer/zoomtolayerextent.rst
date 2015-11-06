.. _webapps.gxp.viewer.layerextent:

Adding a Zoom to Layer Extent tool
==================================

After adding a layer, it would be useful to be able to zoom to its full extent. This is what the **Zoom to Layer Extent** does.  We will add this tool to the toolbar on top of the layer tree, and to the context menu that appears when we right-click on a layer in the tree.

Navigate to the :file:`src/app/app.js` in the :file:`myviewer` directory. Open up this file in a text editor. In the `API documentation <../../../sdk-api/>`_, find the `gxp.plugins.ZoomToLayerExtent <../../../sdk-api/lib/plugins/ZoomToLayerExtent.html>`_ tool. This provides the Zoom to Layer Extent functionality.

The ``ptype`` for ``gxp.plugins.ZoomToLayerExtent`` is ``gxp_zoomtolayerextent``, so we will add an entry to the list of dependencies at the top of :file:`app.js`:

.. code-block:: javascript

    * @require plugins/ZoomToLayerExtent.js

Now find the ``tools`` section in :file:`app.js` and add the following tool configuration:

.. code-block:: javascript

    {
        ptype: "gxp_zoomtolayerextent",
        actionTarget: ["tree.tbar", "tree.contextMenu"]
    }

The ``actionTarget`` property tells the plugin where to place its buttons. In this case we want it in the top toolbar of the layertree (``tree.tbar``), and in the tree's context menu (``tree.contextMenu``).

Now restart the application as before, and reload the application in your browser.  There will now be an extra tool button in the tree's toolbar.

Select a layer and click on this button to zoom to the layer's extent.

