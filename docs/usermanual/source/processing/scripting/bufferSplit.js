var Process = require("geoscript/process").Process;

var buffer = Process.get("geo:buffer");
var split = Process.get("geo:splitPolygon");

exports.process = new Process({
  title: "Buffer and Split",
  description: "Buffers a geometry and splits the resulting polygon",
  inputs: {
    geom: {
      type: "Geometry",
      title:"Target Geometry",
      description: "The geometry to buffer"
    },
    dist: {
      type: "Number",
      title: "Buffer Distance",
      description: "The distance by which to buffer the target geometry"
    },
    line: {
      type: "LineString",
      title: "Splitter Line",
      description: "The line used for splitting the buffered geometry"
    }
  },
  outputs: {
    result: {
      type: "Geometry",
      title: "Result",
      description: "The buffered and split geometry"
    }
  },
  run: function(inputs) {
    var buffered = buffer.run({
      geom: inputs.geom, distance: inputs.dist
    });
        
    return split.run({polygon: buffered.result, line: inputs.line});
  }
});
