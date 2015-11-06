.. _sysadmin.clustering.autoaws:

How to set up an OpenGeo Suite cluster on AWS
=============================================

This page will show how to automatically set up and configure a cluster of servers on Amazon Web Services (AWS).

This script is available to Enterprise clients only. Please `contact us <http://boundlessgeo.com/about/contact-us/sales/>`__ to become an Enterprise client.

Prerequisites
-------------

* Machine running the script must use Ubuntu, Red Hat-based Linux or Mac OS X
* Python 2.7 or higher (not guaranteed on Python 3)
* Ansible 1.6.2 or higher (installed via `pip <https://pypi.python.org/pypi/pip>`_)
* Java JDK 7 or higher (not a JRE)
* AWS account with EC2 access
* Super user access

Setup
-----

Packages
^^^^^^^^

#. *Mac OS X only:* Install ``pip``: 

   .. code-block:: bash
      
      sudo easy_install pip

#. Ansible must be installed via ``pip``. If you have Ansible installed through your standard package manager, run :command:`apt-get remove --purge ansible` first before running :command:`pip install ansible`.

#. Install the ``python-dev`` package through your package manager. For example, on Ubuntu:

   .. code-block:: bash

      apt-get install python-dev

#. Install `boto <https://pypi.python.org/pypi/boto/>`_ with ``pip``:

   .. code-block:: bash

      pip install boto

Java
^^^^

#. Set the ``$JAVA_HOME`` variable to the location of Java. For example:

   .. code-block:: bash

      export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-i386/jre

   .. note:: The command :command:`which java` may not give the true path. For more information on how to find the real location of Java, see the `Amazon EC2 documentation <http://docs.aws.amazon.com/AWSEC2/latest/CommandLineReference/set-up-ec2-cli-linux.html#java_runtime_linux>`_.

#. To verify, type the following command:

   .. code-block:: bash

      $JAVA_HOME/bin/java -version

EC2 tools
^^^^^^^^^

#. Download and install the `EC2 API tools <http://aws.amazon.com/developertools/351>`_.

#. Extract the EC2 API tools into :file:`/usr/local/ec2/ec2-api-tools-a.b.c.d`, where ``a.b.c.d`` is the specific version of the EC2 API tools.

#. Add the path where you extracted the EC2 API tools to the ``$EC2_HOME`` environment variable. For example:

   .. code-block:: bash

      export EC2_HOME=/usr/local/ec2/ec2-api-tools-1.6.14.1

#. Add the :file:`$EC2_HOME/bin` path to the $PATH:

   .. code-block:: bash

      export PATH=$PATH:$EC2_HOME/bin

#. Verify that the EC2 tools are on your path by entering the following command:

   .. code-block:: bash

      ec2-describe-regions

   If you see a list of regions, your EC2 interface is correct::

      REGION  eu-west-1 ec2.eu-west-1.amazonaws.com
      REGION  sa-east-1 ec2.sa-east-1.amazonaws.com
      REGION  us-east-1 ec2.us-east-1.amazonaws.com
      REGION  ap-northeast-1  ec2.ap-northeast-1.amazonaws.com
      REGION  us-west-2 ec2.us-west-2.amazonaws.com
      REGION  us-west-1 ec2.us-west-1.amazonaws.com
      REGION  ap-southeast-1  ec2.ap-southeast-1.amazonaws.com
      REGION  ap-southeast-2  ec2.ap-southeast-2.amazonaws.com

For more information about setting up the EC2 tools, please see the `Amazon documentation <http://docs.aws.amazon.com/AWSEC2/latest/CommandLineReference/set-up-ec2-cli-linux.html#setting_up_ec2_command_linux>`_.

EC2 remote access
^^^^^^^^^^^^^^^^^

In order to be able to use the EC2 tools, you will need to have AWS access keys. These consist of a pair of credentials, called an **Access Key** and a **Secret Access Key**.

#. Retrieve these keys (or make a new pair) by `logging into your AWS account <https://console.aws.amazon.com/iam/home?#security_credential>`_.

#. Set these keys as local environment variables:

   .. code-block:: bash

      export AWS_ACCESS_KEY=your_access_key
      export AWS_SECRET_KEY=your_secret_access_key

You will also need to have a `key pair <http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html>`_ set up on your EC2 account.

#. To create a key pair, navigate to the `EC2 key pairs page <https://console.aws.amazon.com/ec2/v2/home?#KeyPairs:>`_.

#. Click :guilabel:`Create key pair`.

#. You will then be asked to download a file (with the extension ``.pem``). Save this file in a safe place.

Misc
^^^^

The following are other setup tasks that don't fall under any specific category.

#. Add another environment variable:

   .. code-block:: bash

      export ANSIBLE_HOST_KEY_CHECKING=False

#. Open :file:`$HOME/ssh_config` in a text editor.

#. Add the following line to the bottom of the file::

     StrictHostKeyChecking no

#. Save and close the file.

Clustering script
^^^^^^^^^^^^^^^^^

#. Download and extract the clustering script archive to a directory.

   .. note:: If you are an Enterprise client and do not have the script, please `let us know <http://boundlessgeo.com/about/contact-us/>`_.

#. Open the file :file:`roles/aws/vars/main.yml` in a text editor.

#. Change the line that starts with ``aws_keypair`` to contain the name of your key file (omitting the ``.pem`` extension)::

     aws_keypair: key  

#. Save and close the file.

Launching the cluster
---------------------

With setup complete, you can now launch the cluster.

#. Run the following command from the root of the clustering directory:

   .. code-block:: bash

      ansible-playbook aws-launch.yml -i hosts.aws -e "use_aws=true" --private-key=key.pem

   substituting the name and path of the key file as downloaded in a previous step for :file:`key.pem`.

#. During the script, there will be a pause where you are asked to set up your SSH configuration. Add the following to the :file:`$HOME/.ssh/config`

   .. code-block:: bash
     
      Host 10.1.2?.*
         IdentityFile key.pem
         User ubuntu
         Port 22
         ProxyCommand ssh -o "ControlMaster no" -p 22 -i key.pem ec2-user@INSTANCE_IP -W %h:%p
 
   subsituting the the name of the key file for :file:`key.pem`, and the IP given by the script for ``INSTANCE_IP``.
   
#. Details on the cluster created, including AWS-specific information, will be available in the log file :file:`/tmp/informationoutput`.

Troubleshooting
---------------

* If you encounter errors while running the script, you can run the script in "debug mode" by appending ``-vvvv`` to the command. The individual commands run by the script will be displayed in the terminal.

* Try running the script again. Sometimes, due to issues with AWS connectivity, a script may fail the first time but succeed the second. (There is a fix checked into the latest development version of Ansible which should fix a lot of these problems)

Shutting down the cluster
-------------------------

For security reasons, there is no script to shut down a cluster.

To shut down the cluster:

#. Log into your `AWS EC2 console <https://console.aws.amazon.com/ec2/v2/home>`_.

#. Click :guilabel:`Instances`.

#. Find the instances generated by the script. If you have many instances, look at the :guilabel:`Launch Time` or the :guilabel:`Key Pair`. You can also match up by IP address.

#. :guilabel:`Terminate` these instances.

#. Open the `AWS VPC console <https://console.aws.amazon.com/vpc/home>`_.

#. Find the VPC (or VPCs) created by the script and :guilabel:`Delete` them.

#. Open the `AWS RDS console <https://console.aws.amazon.com/rds/home>`_.

#. Find the RDS instances created by the script and :guilabel:`Delete` them.
