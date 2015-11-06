# GeoExplorer

These instructions describe how to build GeoExplorer assuming you will be cloning the Suite repository.  You can also find download links on GitHub to get the source without using Git.

## Getting a copy of the application

To get a copy of the application source code, use [Git](http://git-scm.com/):

    you@prompt:~$ git clone git://github.com/opengeo/suite.git

## Dependencies

The GeoExplorer source directory contains what you need to run the application as a servlet with an integrated persistence layer.
You will need:

* [Apache Ant](http://ant.apache.org/bindownload.cgi) - 1.8+
* [Apache Ivy](http://ant.apache.org/ivy/download.cgi) - 2.3+

Before running in development mode or preparing the application for deployment, you need to install dependencies.  Do this by running `ant build` in the geoexplorer directory:

    you@prompt:~$ cd geoexplorer/
    you@prompt:~/geoexplorer$ ant build


## Running in development mode

The application can be run in development or distribution mode.  In development mode, individual scripts are available to a debugger.  In distribution mode, scripts are concatenated and minified.

To run the application in development mode, run `ant debug`:

    you@prompt:~$ cd geoexplorer
    you@prompt:~/geoexplorer$ ant debug

If the build succeeds, you'll be able to browse to the application at http://localhost:9080/.

By default, the application runs on port 9080.  To change this, you can set the `app.port` property as follows (setting the port to 8000):

    you@prompt:~/geoexplorer$ ant -Dapp.port=8000 debug

In addition, if you want to make a remote GeoServer available at the `/geoserver/` path, you can set the `app.proxy.geoserver` system property as follows:

    you@prompt:~/geoexplorer$ ant -Dapp.proxy.geoserver=http://example.com/geoserver/ debug


## Preparing the application for deployment

Running GeoExplorer as described above is not suitable for production because JavaScript files will be loaded dynamically.  Before moving your application to a production environment, run `ant build` and find the resulting `geoexplorer.war` in the `target` directory.  Move the `target/geoexplorer.war` file to your production environment (e.g. a  servlet container).

GeoExplorer writes to a geoexplorer.db when saving maps.  The location of this file is determined by the `GEOEXPLORER_DATA` value at runtime.  This value can be set as a servlet initialization parameter or a Java system property.

The `GEOEXPLORER_DATA` value must be a path to a directory that is writable by  the process that runs the application.  The servlet initialization parameter is given precedence over a system property if both exist.

As an example, if you want the geoexplorer.db file to be written to your `/tmp` directory, modify GeoExplorer's `web.xml` file to include the following:

    <init-param>
        <param-name>GEOEXPLORER_DATA</param-name>
        <param-value>/tmp</param-value>
    </init-param>

