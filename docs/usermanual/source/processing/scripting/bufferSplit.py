from geoserver.wps import process
from geoscript.process import Process
from geoscript.geom import *

buffer = Process.lookup('geo:buffer')
split = Process.lookup('geo:splitPolygon')

@process(
  title = 'Buffer and Split',
  description = 'Buffers a geometry and splits the resulting polygon.',
  inputs = {
    'geom': (Geometry, 'The geometry to buffer'),
    'dist': (float, 'The distance by which to buffer the target geometry'),
    'line': (LineString, 'The line used for splitting the buffered geometry')
  },
  outputs = {
    'result': (Geometry, 'The buffered and split geometry')
  }
)
def run(geom, dist, line):

  buffered = buffer.run(geom=geom, distance=dist)['result']
  return split.run(polygon=buffered, line=line)['result']
