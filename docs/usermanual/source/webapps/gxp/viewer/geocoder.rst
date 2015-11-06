.. _webapps.gxp.viewer.geocoder:

Adding a Google Geocoder search field
=====================================

This section describes how to add the ability to add the Google Geocoder to the viewer application.

.. warning:: Before adding Google components to your applications, make sure that the `Google Terms of Use <https://developers.google.com/terms/>`_ allow you to do so.

Navigate to the :file:`src/app/app.js` in the :file:`myviewer` directory. Open up this file in a text editor. In the `API documentation <../../../sdk-api/>`_, find the `gxp.plugins.GoogleGeocoder <../../../sdk-api/lib/plugins/GoogleGeocoder.html>`_ tool. This provides the geocoding functionality.

This plugin requires the Google Maps v3 API to be present in the application. Add the following script tag to :file:`index.html` in your app directory:

.. code-block:: html

    <script src="http://maps.google.com/maps/api/js?v=3.6&amp;sensor=false"></script>

Now open up :file:`app.js` again, and add the tool configuration for this plugin. We want the geocoder field to show up in the map's top toolbar:

.. code-block:: javascript

    {
        ptype: "gxp_googlegeocoder",
        outputTarget: "map.tbar",
        outputConfig: {
            emptyText: "Search for a location ..."
        }
    }

Add the plugin to the list of dependencies in :file:`app.js`. The file name is :file:`plugins/GoogleGeocoder.js`.

.. code-block:: javascript

   * @require plugins/GoogleGeocoder.js

 Restart the application and reload the browser.  The Google Geocoder will now be in the viewer:

.. figure:: img/viewer_geocoder.png

