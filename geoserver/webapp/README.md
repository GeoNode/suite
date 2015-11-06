# GeoServer Web Application

This module contains the GeoServer web application. It is comprised of a backend api on the Java/GeoServer side, and an angular based client. 

*Note: for instructions on getting this running with Composer see the [Composer](https://github.com/boundlessgeo/composer) project page.*

## Building

Invoke ant to build the module:

    ant build

This will result in a deployable GeoServer war file.

## Developing

To run the backend import this module into your Java IDE and run
the `Start` class. By default the backend will start up on port 8080.

### Tips and Tricks

Ant build targets:

    Main targets:

     build  Builds project
     help   Print help
     serve  Runs GeoServer
