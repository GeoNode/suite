Ext.ns("myapp.plugins");

myapp.plugins.BoxInfo = Ext.extend(gxp.plugins.Tool, {

  ptype: "myapp_boxinfo",

  boxTool: null,

  tplText: 'Area: {area}, Perimeter: {length}',

  title: "Box info",

  addOutput: function(config) {
    if (this.boxTool !== null) {
      var layer = this.target.tools[this.boxTool].boxLayer;
      layer.events.on({
        featureadded: this.addFeature,
        scope: this
      });
      this.tpl = new Ext.Template(this.tplText);
    }
    return myapp.plugins.BoxInfo.superclass.addOutput.call(this, Ext.apply({
      title: this.title,
      autoScroll: true
    }, config));
  },
  
  addFeature: function(evt) {
    var geom = evt.feature.geometry,
      output = this.output[0];
    output.add({html: this.tpl.applyTemplate({area: geom.getArea(), length: geom.getLength()})});
    output.doLayout();
  }

});

Ext.preg(myapp.plugins.BoxInfo.prototype.ptype, myapp.plugins.BoxInfo);