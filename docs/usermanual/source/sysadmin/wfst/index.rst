.. _sysadmin.wfst:

Allowing read-write access to vector data through transactions
==============================================================

OpenGeo Suite contains a full Web Feature Service (WFS) implementation. This also includes support for WFS Transactions (WFS-T).

However, as unrestricted write access to data is a security risk, the WFS in OpenGeo Suite is set to "Basic" by default. This means that the only requests that will be accepted are:

* GetCapabilites
* DescribeFeatureType
* GetFeature

**To enable WFS-T, you will need to change the WFS Service Level in GeoServer to Transactional** (or Complete).

#. Open the GeoServer admin interface.

#. Under the :guilabel:`Services` section, click :guilabel:`WFS`.

   .. figure:: img/wfslink.png

      WFS in the Services menu

#. Scroll down to :guilabel:`Service Level`. Click the box next to :guilabel:`Transactional`.

   .. figure:: img/wfst-basic.png

      Default setting: WFS-T not allowed

   .. figure:: img/wfst-transactional.png

      WFS-T allowed

#. Click :guilabel:`Save`.

If you are using **the default data directory** shipped with OpenGeo Suite, you can easily test that transactions are enabled:

#. On the left side of the page, click :guilabel:`Demos` and then click :guilabel:`Demo Requests`.

#. Select the :guilabel:`WFS_transactionInsert.xml` request.

   .. note:: The request will need to be modified to produce output. Read the comments at the top of the request for instructions.

   .. figure:: img/transactiondemo.png

      Transaction demo

#. Enter your administrative user name and password in the appropriate fields at the bottom.

#. Click :guilabel:`Submit`.

#. The request should execute successfully.

   .. figure:: img/wfstsuccess.png

      A successful transaction

   .. note:: The :guilabel:`WFS_transactionDelete.xml` request will undo this insert.
