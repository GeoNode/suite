/**
 * Add all your dependencies here.
 *
 * @require Popup.js
 * @require LayersControl.js
 */

// ========= config section ================================================
var url = '/geoserver/ows?';
var featurePrefix = 'usa';
var featureType = 'states';
var featureNS = 'http://census.gov';
var srsName = 'EPSG:900913';
var geometryName = 'the_geom';
var geometryType = 'MultiPolygon';
var fields = ['STATE_NAME', 'STATE_ABBR'];
var layerTitle = 'States';
var infoFormat = 'application/vnd.ogc.gml/3.1.1'; // can also be 'text/html'
var center = [-10764594.758211, 4523072.3184791];
var zoom = 3;
// =========================================================================

// override the axis orientation for WMS GetFeatureInfo
var proj = new ol.proj.Projection({
  code: 'http://www.opengis.net/gml/srs/epsg.xml#4326',
  axis: 'enu'
});
ol.proj.addEquivalentProjections([ol.proj.get('EPSG:4326'), proj]);

// create a GML format to read WMS GetFeatureInfo response
var format = new ol.format.GML({featureNS: featureNS, featureType: featureType});

// create a new popup with a close box
// the popup will draw itself in the popup div container
// autoPan means the popup will pan the map if it's not visible (at the edges of the map).
var popup = new app.Popup({
  element: document.getElementById('popup'),
  closeBox: true,
  autoPan: true
});

// the tiled WMS source for our local GeoServer layer
var wmsSource = new ol.source.TileWMS({
  url: url,
  params: {'LAYERS': featurePrefix + ':' + featureType, 'TILED': true},
  serverType: 'geoserver'
});

// create a vector layer to contain the feature to be highlighted
var highlight = new ol.layer.Vector({
  style: new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: '#00FFFF',
      width: 3
    })
  }),
  source: new ol.source.Vector()
});

// when the popup is closed, clear the highlight
$(popup).on('close', function() {
  highlight.getSource().clear();
});

// create the OpenLayers Map object
// we add a layer switcher to the map with two groups:
// 1. background, which will use radio buttons
// 2. default (overlays), which will use checkboxes
var map = new ol.Map({
  controls: ol.control.defaults().extend([
    new app.LayersControl({
      groups: {
        background: {
          title: "Base Layers",
          exclusive: true
        },
        'default': {
          title: "Overlays"
        }
      }
    })
  ]),
  // add the popup as a map overlay
  overlays: [popup],
  // render the map in the 'map' div
  target: document.getElementById('map'),
  // use the Canvas renderer
  renderer: 'canvas',
  layers: [
    // MapQuest streets
    new ol.layer.Tile({
      title: 'Street Map',
      group: "background",
      source: new ol.source.MapQuest({layer: 'osm'})
    }),
    // MapQuest imagery
    new ol.layer.Tile({
      title: 'Aerial Imagery',
      group: "background",
      visible: false,
      source: new ol.source.MapQuest({layer: 'sat'})
    }),
    // MapQuest hybrid (uses a layer group)
    new ol.layer.Group({
      title: 'Imagery with Streets',
      group: "background",
      visible: false,
      layers: [
        new ol.layer.Tile({
          source: new ol.source.MapQuest({layer: 'sat'})
        }),
        new ol.layer.Tile({
          source: new ol.source.MapQuest({layer: 'hyb'})
        })
      ]
    }),
    new ol.layer.Tile({
      title: layerTitle,
      source: wmsSource
    }),
    highlight
  ],
  // initial center and zoom of the map's view
  view: new ol.View({
    center: center,
    zoom: zoom
  })
});

// register a single click listener on the map and show a popup
// based on WMS GetFeatureInfo
map.on('singleclick', function(evt) {
  var viewResolution = map.getView().getResolution();
  var url = wmsSource.getGetFeatureInfoUrl(
      evt.coordinate, viewResolution, map.getView().getProjection(),
      {'INFO_FORMAT': infoFormat});
  if (url) {
    if (infoFormat == 'text/html') {
      popup.setPosition(evt.coordinate);
      popup.setContent('<iframe seamless frameborder="0" src="' + url + '"></iframe>');
      popup.show();
    } else {
      $.ajax({
        url: url,
        success: function(data) {
          var features = format.readFeatures(data);
          highlight.getSource().clear();
          if (features && features.length >= 1 && features[0]) {
            var feature = features[0];
            var html = '<table class="table table-striped table-bordered table-condensed">';
            var values = feature.getProperties();
            var hasContent = false;
            for (var key in values) {
              if (key !== 'the_geom' && key !== 'boundedBy') {
                html += '<tr><td>' + key + '</td><td>' + values[key] + '</td></tr>';
                hasContent = true;
              }
            }
            if (hasContent === true) {
              popup.setPosition(evt.coordinate);
              popup.setContent(html);
              popup.show();
            }
            feature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
            highlight.getSource().addFeature(feature);
          } else {
            popup.hide();
          }
        }
      });
    }
  } else {
    popup.hide();
  }
});
