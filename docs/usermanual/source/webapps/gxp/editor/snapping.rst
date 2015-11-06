.. _webapps.gxp.editor.snapping:

Snapping features
=================

Snapping is the act of making features automatically touch when they are approximately close to each other. When editing, it might make sense to use snapping. For example, it might make sense to snap to the states layer, so when digitizing a new state, we make sure it aligns well with another state.

In the `API documentation <../../../sdk-api/>`_, find the `gxp.plugins.SnappingAgent <../../../sdk-api/lib/plugins/FeatureEditor.html>`_ tool. This provides the snapping functionality. The ``ptype`` for ``gxp.plugins.SnappingAgent`` is ``gxp_snappingagent``. 

 Open up :file:`app.js` and add a tool to the ``tools`` section to configure a snapping agent:

.. code-block:: javascript

    {
        ptype: "gxp_snappingagent",
        id: "snapping-agent",
        targets: [{
            source: "local",
            name: "usa:states"
        }]
    }

This creates a snapping agent that will load the ``usa:states`` data using a BBOX Strategy and hooking it up with an OpenLayers snapping control.

Now we hook up our feature editor with the snapping agent. Replace the existing feature editor tool code block from the previous section:

.. code-block:: javascript

    {
        ptype: "gxp_featureeditor",
        featureManager: "states_manager",
        autoLoadFeature: true
    }

with the following:

.. code-block:: javascript

    {
        ptype: "gxp_featureeditor",
        featureManager: "states_manager",
        autoLoadFeature: true,
        snappingAgent: "snapping-agent"
    }

Also, add :file:`plugins/SnappingAgent.js` to the list of dependencies at the top of :file:`app.js`:

.. code-block:: javascript

    * @require plugins/SnappingAgent.js

Finally, restart the application and reload the browser.

Zoom in at the west coast of the USA and create a new state. When you get close to the border of an existing state, you will see that the polygon is being snapped to that border. 

.. note:: It is possible that the new state will not show up directly in the GeoServer WMS layer.  This is usually caused by the layer's bounding box being configured too narrowly for the layer. By recomputing the bounding boxes after the transaction, or by enlarging them manually, the new state should show up after a refresh.

