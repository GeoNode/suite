# Suite Documentation

This module builds all of the documentation for suite. All documentation is build with 
[Sphinx](http://sphinx-doc.org/).

This module is broken into multiple sub modules for each documentation project. To build the documentation included with suite invoke ant from this directory.

    % ant basic
    
To build documentation with additional enterprise components:

    % ant enterprise

Or to build a specific sub module change directory to that sub module and invoke ant.

    % cd usermanual
    % ant basic

# Building PDFs

The build will attempt to build PDF versions of the installation documentation
if the `pdflatex` command is available on the ``PATH``. If the command is
not available the build will skip PDF generation.

The ``pdflatex`` requires installing Latex which can be tricky depending on the
platform. On Ubuntu systems install the following packages:

    % apt-get install texlive-latex-recommended texlive-latex-extra texlive-fonts-recommended
