# -*- mode: ruby -*-
# vi: set ft=ruby :

# INSTRUCTIONS
# 1. Install virtualbox on your local machine
# 2. Install vagrant on your local machine
# 3. Make sure this repo is up-to-date and on the correct fork/branch.
# 4. `git submodule update --init --recursive`
# 3. `vagrant up`
# 4. When finished building (more than an hour), `vagrant ssh`.
#    The src directory is in /vagrant
#    The build output is in build_output.txt
# 5. To build again from the VM do `~/build`
#    To build again from the host do `vagrant ssh -c build`

Vagrant.configure(2) do |config|

  config.vm.provider :virtualbox do |vb, override|
    override.vm.box = 'ubuntu/trusty64'

    # Adjust these settings as needed.
    vb.memory = 3000
    vb.cpus = 4
  end

  config.vm.provider :aws do |aws, override|
    override.vm.box = 'dummy'
    override.vm.box_url = 'https://github.com/mitchellh/vagrant-aws/raw/master/dummy.box'
    aws.ami = "ami-d05e75b8"

    aws.instance_type = 'c4.2xlarge'
    aws.tags = {
      'Name' => "#{$AWS_KEYPAIR_NAME}-build-vagrant"
    }

    override.ssh.username = 'ubuntu'

    override.vm.synced_folder '.', '/vagrant', type: 'rsync' #, rsync__exclude: '.git/'
  end

  # Share host's maven cache. Comment the following line to disable.
  #config.vm.synced_folder '~/.m2', '/home/vagrant/.m2'

  config.vm.provision :shell, inline: <<SCRIPT

    export sudo DEBIAN_FRONTEND=noninteractive

    sudo apt-get update

    wget --no-check-certificate https://github.com/aglover/ubuntu-equip/raw/master/equip_java7_64.sh && bash equip_java7_64.sh
    sudo apt-get -y  install maven ant ivy
    sudo ln -s  /usr/share/java/ivy.jar /usr/share/ant/lib/ivy.jar
    sudo apt-get -y  install python-setuptools
    sudo easy_install jstools
    sudo apt-get -y  install python-pip python-dev
    sudo pip install -U Sphinx

    # the standard ubuntu node install doesnt work very well and is VERY old.  Use this one instead
    curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash -
    sudo  apt-get install -y nodejs

    sudo npm install -g bower
    sudo npm install -g grunt-cli
    sudo npm install -g gulp
    sudo apt-get -y install gdal-bin ### Do we need one from our own repo?

    echo """#!/bin/bash
      cd /vagrant
      echo 'Starting build.'
      (ant -v 2>&1) | tee build_output.txt""" > ~/build
    chmod a+x ~/build

    ~/build
SCRIPT
end
