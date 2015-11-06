.. _webapps.gxp.viewer.featureinfo:

Adding a WMS GetFeatureInfo tool
================================

This section describes how to add WMS Get Feature Info functionality to the viewer application.

Navigate to the :file:`src/app/app.js` in the :file:`myviewer` directory. Open up this file in a text editor. In the `API documentation <../../../sdk-api/>`_, find the `gxp.plugins.WMSGetFeatureInfo <../../../sdk-api/lib/plugins/WMSGetFeatureInfo.html>`_ tool. This provides the GetFeatureInfo functionality.

The ``ptype`` for ``gxp.plugins.WMSGetFeatureInfo`` is ``gxp_wmsgetfeatureinfo``, so we will add an entry in the ``tools`` configuration of :file:`app.js`:

.. code-block:: javascript

    {
        ptype: "gxp_wmsgetfeatureinfo"
    }

As the next step we need to add the new tool to our build profile, so we add a line for :file:`plugins/WMSGetFeatureInfo.js` to the list of dependencies at the top of our :file:`app.js` file. 

.. code-block:: javascript

    * @require plugins/WMSGetFeatureInfo.js

Now restart the application, and reload the application in your browser. You should now see an extra tool button in the map's toolbar:

.. figure:: img/viewer_gfi.png

Click on this button to activate the tool, and click on a state. You should then get a pop-up displaying the information about that state using WMS GetFeatureInfo:

.. figure:: img/viewer_gfi_popup.png

Let's say you want to influence the way that the pop-up looks like, for example to increase its width. Open up the :file:`app.js` again, and add a section called ``outputConfig`` to your tool configuration:

.. code-block:: javascript

    {
        ptype: "gxp_wmsgetfeatureinfo",
        outputConfig: {
            width: 400
        }
    }

Reload the application in the browser, and check that the pop-up now has a width of 400 pixels:

.. figure:: img/viewer_gfi_popupwidth.png

If we want to influence the sequence of tools in the toolbar, e.g. having the WMS GetFeatureInfo tool as the second button, open up :file:`app.js`, and configure an ``actionTarget`` with an ``index``:

.. code-block:: javascript

    {
        ptype: "gxp_wmsgetfeatureinfo",
        outputConfig: {
            width: 400
        },
        actionTarget: {
            target: "map.tbar",
            index: 1
        }
     }

The button is now the second button in the toolbar:

.. figure:: img/viewer_gfi_index.png

