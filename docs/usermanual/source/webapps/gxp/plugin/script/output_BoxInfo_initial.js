Ext.ns("myapp.plugins");

myapp.plugins.BoxInfo = Ext.extend(gxp.plugins.Tool, {

  ptype: "myapp_boxinfo",

  addOutput: function(config) {
    return myapp.plugins.BoxInfo.superclass.addOutput.call(this, Ext.apply({
      title: "Box info",
      html: "This is where the box info will be shown"
    }, config));
  }

});

Ext.preg(myapp.plugins.BoxInfo.prototype.ptype, myapp.plugins.BoxInfo);