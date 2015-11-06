# parses the build.properties file making it available to sphinx config files
import os
from StringIO import StringIO
from ConfigParser import ConfigParser

__all__ = ['suite_version', 'suite_version_short', 'gs_version', 'gs_version_short', 
           'gwc_version', 'gwc_version_short', 'pg_version', 'pg_version_short']

bpfile = open(os.path.join(os.path.dirname(__file__), 'build.properties'))
buf = StringIO()
buf.write('[build]')
buf.write(bpfile.read())
buf.seek(0, os.SEEK_SET)

cp = ConfigParser()
cp.readfp(buf)

def clean_snapshot(ver):
    return ver.replace('-SNAPSHOT', '.x')

def short_version(ver):
    return ver.split('-')[0]

suite_version = clean_snapshot(cp.get('build', 'suite.version'))
suite_version_short = short_version(cp.get('build', 'suite.version'))

gs_version = clean_snapshot(cp.get('build', 'gs.version'))
gs_version_short = short_version(cp.get('build', 'gs.version'))

gwc_version = clean_snapshot(cp.get('build', 'gwc.version'))
gwc_version_short = short_version(cp.get('build', 'gwc.version'))

pg_version = clean_snapshot(cp.get('build', 'pg.version'))
pg_version_short = short_version(cp.get('build', 'pg.version'))