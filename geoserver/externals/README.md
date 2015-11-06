# GeoServer External Dependencies for Suite

This module builds all of the upstream projects from the GeoServer ecosystem required by suite. This includes:

* GeoTools
* GeoWebCache
* Community GeoServer
* Suite specific GeoServer extensions
* GeoScript

Running `ant -p` will illustrate the different build targets corresponding to each of the individual projects.

    % ant -p

    Main targets:

     build               Build all projects
     build-geoscript-js  Build GeoScript JS
     build-geoscript-py  Build GeoScript PY
     build-gs            Build GeoServer
     build-gs-exts       Build GeoServer Exts
     build-gt            Build GeoTools
     build-gwc           Build GeoWebCache
     help                Print help
    Default target: build

Additional build flags can be passed to the corresponding maven builds with the following build properties.

* `gt.flags`
* `gwc.flags`
* `gs.flags`
* `gs-exts.flags`
* `geoscript-js.flags`
* `geoscript-py.flags`

For example a common scenario is resuming a build from a specific module:

    ant -Dgt.flags="-rf modules/library/render" build-gt