OpenGeo Suite README
====================

These instructions describe how to build platform independent components of OpenGeo Suite.

## Prerequisites

The following base software packages are required.

* Java 7 - [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html) - 1.7+ or [Open JDK 7](http://openjdk.java.net/install/)
* [Apache Ant](http://ant.apache.org/bindownload.cgi) - 1.8+
* [Apache Ivy](http://ant.apache.org/ivy/download.cgi) - 2.3+
* [Git](http://git-scm.com/) - 1.7.10+*

Some modules require additional packages:

* [Apache Maven](http://maven.apache.org/download.html) - 3.0+
* [JSTools](https://github.com/whitmo/jstools) - Latest
* [Sphinx](http://sphinx.pocoo.org/) - 1.0+ (the full build requires LaTeX support)
* [NodeJS](http://nodejs.org/) - Latest version
  * [Bower](http://bower.io/) - `npm install -g bower`
  * [Grunt](http://gruntjs.com/) - `npm install -g grunt-cli`
  * [Gulp](http://gulpjs.com/) - `npm install -g gulp`

Ensure that all the above are installed so that the associated executables are on the 
`PATH` of the user building the suite. 

## Developer Guide

If you just want to build suite locally, and do not intend to contribute changes, you can skip this section and proceed to the [Quickstart](#quickstart).

### Getting Started

1. [Fork](https://guides.github.com/activities/forking/) the suite projuct to your github account. This fork will be used to stage your changes.

1. Clone your fork of suite in the desktop:

        % git clone git://github.com/<yourusername>/suite.git suite
        % cd suite

1. Add an upstream remote pointing to the boundless suite project:

        % git remote add upstream https://www.github.com/boundlessgeo/suite.git suite

1. This remote will be used to update your fork of suite to the latest from the boundless suie repository. Any time you want to get the latest changes, simply pull from the upstream remote:

        % git pull upstream master

### Submodules

Suite includes code from several other projects by way of [Git Submodules](https://git-scm.com/book/en/v2/Git-Tools-Submodules). Most suite submodules are in [geoserver/externals](https://github.com/boundlessgeo/suite/tree/master/geoserver/externals). Submodules function like a link to a specific revision of a project. When using git on the desktop, submodules behave like single files as long as you are outside of them. However, they can be traversed like directories and once inside a submodule, git behaves as if you are in a checkout of the submodule project itself. 

1. When inside a submodule, you can update the revision it links to by pulling from a remote. Many submodules link to a specific branch, so make sure you get the right one (ask a developer if you are unsure)!

        % cd geoserver/externals/geoserver
        % git pull origin 2.7.x

2. In order for this update to be reflected in the suite project, it must be commited like any other change:

        % cd ../
        % git add geoserver
        % git commit -m "update geoserver submodule"

3. You can then push this change to your fork and create a pull request, like any other commit.

### Release Branches

During regular development, suite changes are commited to the master branch. Prior to a release, a release branch (of the form `r4.7`) is created. Any changes should be made against that branch instead, and backported to master if necessary. 

1. When commiting a change to the release branch, note the commit id:

        [r4.7 0c66de5] update geoserver submodule

2. To backport this commit to master, switch to the master branch and use `cherry-pick` to copy the commit. Remember to push your change up to the suite repository:

        % git checkout master
        % git cherry-pick 0c66de5
        % git push upstream master

Certain submodules (mainly geoserver) will also have release specific branches. If you are updating a submodule on the release branch, first check if it has its own branch for this release (usually of the form `suite-4.7`). Ask a developer if you are unsure.

### What's next

To build suite, go to step 2 of the [Quickstart](#quickstart).

For more information about the build system, see the [Build System Overview](#build-system-overview).

For information on the individual components that comprise suite, follow the links in the [Modules](#modules) section.

## Quickstart

1. Clone the repository:

        % git clone git://github.com/boundlessgeo/suite.git suite
        % cd suite

1. Initialize submodule dependencies:

        % git submodule update --init --recursive

1. Do a full build:

        % ant

1. Or build the module of your choice:

        % cd docs
        % ant 

## Build System Overview

The suite repository is made up a number of modules (ie projects). During development 
typically modules are built individually as opposed to all at once. The primary build 
tool for suite is Ant. For some modules the ant script delegates to the modules native build tool such as Maven or Sphinx. 

All top level modules have a `build.xml` that defines the following targets:

1. `build` - Builds the project, the result of this is something deployable in the development environment. This target is the default.
1. `clean` - Cleans the project deleting all build artifacts. 
1. `assemble` - Assembles the project into a zip archive suitable for deployment in production. This is the artifact consumed by installer builders. 
1. `publish` - Publishes the zip archive to a final location.
1. `all` - Runs all the above targets.

Building for development purposes typically looks like:

    ant clean build

Building for deployment purposes typically looks like:

    ant clean build assemble

Or to build everything:

    ant all

The [build](build) directory contains common build files used by modules.

 * [common.xml](build/common.xml) - Common ant targets used by module 
 build files. Every module build file imports this file as the first step.
 * [build.properties](build/build.properties) - Default build properties that can
 be overridden on a global or per module basis.

### Build Properties

Many aspects of the suite build such as file locations, executable names, etc... are 
parameterized into build properties. The default [build.properties](build/build.properties)
contains a list of default values. Often these default properties must be overridden to 
cater to the environment (eg. Windows vs Unix) or to cater to specifics of a particular module. 

There are two ways to override build properties: 

1. The first is to specify them directly to the ant build command with the Java system property (-D) syntax. For example:

          % ant -Dsuite.build_cat=release build

1. Creating a file named `local.properties` either at the global level or at the module level. The global `local.properties` is located in the [build](build) directory next to 
`build.properties`. Module specific `local.properties` files are located next to the module `build.xml` file. Naturally the module specific local properties file overrides properties from its global counterpart. 

Using any combination of the above method it should never be necessary to modify the `build.properties` file directly. 

## Modules

The suite repository is composed of the following modules:

* [apps](apps/README.md)
* [dashboard](dashboard/README.md)
* [docs](docs/README.md)
* [geoexplorer](geoexplorer/README.md)
* [geoserver](geoserver/README.md)
* [geowebcache](geowebcache/README.md)
* [jetty](jetty/README.md)
* [recipes](recipes/README.md)
* [webapp-sdk](sdk/README.md)

Consult the module README files for module specific information. 
