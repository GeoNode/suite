/**
 * @require plugins/Tool.js
 */

Ext.ns("myapp.plugins");

myapp.plugins.DrawBox = Ext.extend(gxp.plugins.Tool, {

  ptype: "myapp_drawbox",

  addActions: function() {
    return myapp.plugins.DrawBox.superclass.addActions.apply(this, [{
      text: "Draw box"
    }]);
  }

});

Ext.preg(myapp.plugins.DrawBox.prototype.ptype, myapp.plugins.DrawBox);