/**
 * Add all your dependencies here.
 *
 * @require LayersControl.js
 * @require TransactionHandler.js
 * @require FeatureTable.js
 * @require WFSBBOXLoader.js
 */

// ========= config section =============================================
var url = '/geoserver/wfs?';
var featurePrefix = 'usa';
var featureType = 'states';
var featureNS = 'http://census.gov';
var srsName = 'EPSG:3857';
var geometryName = 'the_geom';
var geometryType = 'MultiPolygon';
var fields = ['STATE_NAME', 'STATE_ABBR'];
var layerTitle = 'States';
var center = [-10764594.758211, 4523072.3184791];
var zoom = 3;
// ======================================================================

// this is the callback function that will get called when the features are in
var loadFeatures = function(response) {
  vectorSource.addFeatures(vectorSource.readFeatures(response));
};

// create a WFS BBOX loader helper
var BBOXLoader = new app.WFSBBOXLoader({
  url: url,
  featurePrefix: featurePrefix,
  featureType: featureType,
  srsName: srsName,
  callback: loadFeatures
});

// create a source to fetch the WFS features using a BBOX loader
var vectorSource = new ol.source.ServerVector({
  format: new ol.format.GeoJSON({
    defaultDataProjection: srsName,
    geometryName: geometryName
  }),
  loader: $.proxy(BBOXLoader.load, BBOXLoader),
  strategy: ol.loadingstrategy.createTile(new ol.tilegrid.XYZ({
    maxZoom: 19
  })),
  projection: 'EPSG:3857'
});

// create a vector layer that uses the source
var vector = new ol.layer.Vector({
  title: layerTitle,
  source: vectorSource,
  style: new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'rgba(0, 0, 255, 1.0)',
      width: 2
    })
  })
});

// create the map
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
    vector
  ],
  // initial center and zoom of the map's view
  view: new ol.View({
    center: center,
    zoom: zoom
  })
});

// create a WFS transaction helper which can help us draw new features,
// modify existing features and delete existing features.
var transaction = new app.TransactionHandler({
  source: vector.getSource(),
  geometryType: geometryType,
  geometryName: geometryName,
  srsName: srsName,
  featureNS: featureNS,
  featureType: featureType,
  url: url,
  map: map
});

// create a feature table that will represent our features in a tabular form
var table = new app.FeatureTable({
  id: 'features',
  fields: fields,
  showFeatureId: true,
  source: vector.getSource(),
  map: map, 
  container: 'features-container',
  select: transaction.getSelect(),
  offset: 37
});

// set table height based on responsive panel size
var resizeTableHeight = function() {
  var _window = $(window);
  var window_h = _window.height(), 
      window_w =  _window.width(), 
      navbar_h = $('.navbar').height(),
      table_container = $('#features-container'),
      table = $('#features');

  if (window_w < 768) { // table is beneath map
    var table_height = window_h - $('#map').height();

  } else { // table is right of map
    var table_height = $('#map').height(); 
  }
  table.height(table_height);
  table_container.height(table_height);
}

var turnOnCursor = function() {
  $('#map').addClass('drawcursor');
  $('#draw-btn').addClass('active');
}

var turnOffCursor = function() {
  $('#map').removeClass('drawcursor');
  $('#draw-btn').removeClass('active');
}

// delete the selected feature
var deleteFeature = function() {
  transaction.deleteSelected();
};

// draw a new feature
var drawFeature = function() {
  transaction.activateInsert();
  turnOnCursor();

  transaction.draw_.on('drawend', turnOffCursor, this);
};


// add layers control collapse button
var addLayersControlBtn = function() {
  var layersControl =  $("#map .layers-control");

  var btnString = '<button type="button" data-toggle="dropdown" class="layers-control-btn btn btn-default btn-sm"><i class="glyphicon glyphicon-minus"></button>';
  var $btn = $($.parseHTML(btnString));
  $btn.css('bottom', layersControl.height()-2);
  $('#map').append($btn);

  $btn.click(function () {
    var iconDiv = $('.layers-control-btn > i.glyphicon');

    if (iconDiv.hasClass('glyphicon-minus')) {
      iconDiv.removeClass('glyphicon-minus')
              .addClass('glyphicon-plus');
    } else {
      iconDiv.removeClass('glyphicon-plus')
              .addClass('glyphicon-minus');
    }
    layersControl.toggle();
  });

}

$(document).ready(function() {

  addLayersControlBtn();

  // add resize listener for table height, enables scroll-y
  // on table for responsive and full view
  var rszTimer;

  $(window).resize(function(e) {
    clearTimeout(rszTimer);
    rszTimer = setTimeout(resizeTableHeight(), 100);
  }).resize(); // call on first load

});
