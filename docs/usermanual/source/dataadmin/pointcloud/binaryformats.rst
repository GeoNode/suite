.. _dataadmin.pointcloud.binaryformats:

Binary formats
==============

In order to preserve some compactness in dump files and network transmissions, the binary formats need to retain their native compression. All binary formats are hex-encoded before output. 

The ``PcPoint`` and ``PcPatch`` binary formats start with a common header, which provides:

* The endianness flag, to allow portability between architectures
* The ``pcid`` number, to look up the schema information in the ``pointcloud_formats`` table

The patch binary formats have additional standard header information:

* The compression number, which indicates how to interpret the data
* The number of points in the patch


Point binary
------------

::

    byte:     endianness (1 = NDR, 0 = XDR)
    uint32:   pcid (key to POINTCLOUD_SCHEMAS)
    uchar[]:  pointdata (interpret relative to pcid)

.. _dataadmin.pointcloud.binaryformats.uncompressed: 

Patch binary (uncompressed)
---------------------------

::

    byte:         endianness (1 = NDR, 0 = XDR)
    uint32:       pcid (key to POINTCLOUD_SCHEMAS)
    uint32:       0 = no compression
    pointdata[]:  interpret relative to pcid

Patch binary (dimensional)
--------------------------

::

    byte:          endianness (1 = NDR, 0 = XDR)
    uint32:        pcid (key to POINTCLOUD_SCHEMAS)
    uint32:        2 = dimensional compression
    uint32:        npoints
    dimensions[]:  dimensionally compressed data for each dimension

Each compressed dimension starts with a byte, that gives the compression type, and then a ``uint32`` that gives the size of the segment in bytes.

::

    byte:           dimensional compression type (0-3)
    uint32:         size of the compressed dimension in bytes
    data[]:         the compressed dimensional values

There are four possible compression types used in dimensional compression:

* No compression = 0
* Run-length compression = 1
* Significant bits removal = 2
* Deflate = 3

No compression
^^^^^^^^^^^^^^

For dimensional compression 0 (no compression) the values just appear in order. The length of words in this dimension must be determined from the schema document.

::

    word[]:

Run-length compression
^^^^^^^^^^^^^^^^^^^^^^

For run-length compression, the data stream consists of a set of pairs: a byte value indicating the length of the run, and a data value indicating the value that is repeated.

::

     byte:          number of times the word repeats
     word:          value of the word being repeated
     ....           repeated for the number of runs

The length of words in this dimension must be determined from the schema document.

Significant bits removal
^^^^^^^^^^^^^^^^^^^^^^^^

Significant bits removal starts with two words. The first word just gives the number of bits that are "significant", that is the number of bits left after the common bits are removed from any given word. The second word is a bitmask of the common bits, with the final, variable bits zeroed out.

::

     word1:          number of variable bits in this dimension
     word2:          the bits that are shared by every word in this dimension
     data[]:         variable bits packed into a data buffer

Deflate dimension
^^^^^^^^^^^^^^^^^

Where simple compression schemes fail, general purpose compression is applied to the dimension using ``zlib``. The data area is a raw ``zlib`` buffer suitable for passing directly to the ``inflate()`` function. The size of the input buffer is given in the common dimension header. The size of the output buffer can be derived from the patch metadata by multiplying the dimension word size by the number of points in the patch.

Patch binary (GHT)
------------------

::

    byte:          endianness (1 = NDR, 0 = XDR)
    uint32:        pcid (key to POINTCLOUD_SCHEMAS)
    uint32:        1 = GHT compression
    uint32:        npoints
    uint32:        GHT data size
    uint8:         GHT data

GHT patches are much like dimensional patches, except their internal structure is more opaque. Use ``LibGHT`` to read the GHT data buffer out into a GHT tree in memory.
