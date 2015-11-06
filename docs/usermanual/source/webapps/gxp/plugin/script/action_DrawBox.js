/**
 * @require plugins/Tool.js
 * @require GeoExt/widgets/Action.js
 * @require OpenLayers/Control/DrawFeature.js
 * @require OpenLayers/Handler/RegularPolygon.js
 * @require OpenLayers/Layer/Vector.js
 * @require OpenLayers/Renderer/SVG.js
 * @require OpenLayers/Renderer/VML.js
 */

Ext.ns("myapp.plugins");

myapp.plugins.DrawBox = Ext.extend(gxp.plugins.Tool, {

  ptype: "myapp_drawbox",

  addActions: function() {
    var map = this.target.mapPanel.map;
    this.boxLayer = new OpenLayers.Layer.Vector(null, {displayInLayerSwitcher: false});
    map.addLayers([this.boxLayer]);
    // keep our vector layer on top so that it's visible
    map.events.on({
      addlayer: this.raiseLayer,
      scope: this
    });
    var action = new GeoExt.Action({
      text: "Draw box",
      toggleGroup: "draw",
      enableToggle: true,
      map: map,
      control: new OpenLayers.Control.DrawFeature(this.boxLayer,
        OpenLayers.Handler.RegularPolygon, {
          handlerOptions: {
            sides: 4,
            irregular: true
          }
        }
      )
    });
    return myapp.plugins.DrawBox.superclass.addActions.apply(this, [action]);
  },

  raiseLayer: function() {
    var map = this.boxLayer && this.boxLayer.map;
    if (map) {
      map.setLayerIndex(this.boxLayer, map.layers.length);
    }
  }

});

Ext.preg(myapp.plugins.DrawBox.prototype.ptype, myapp.plugins.DrawBox);