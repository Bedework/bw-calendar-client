#!/bin/bash

repo=~/bw-git/

cd $repo

install() {
  git clone git@github.com:Bedework/$1.git
  cd $1/
  mvn -q clean package 
  mvn -q install
  cd ..
}

#install "bw-ws"
#install "bw-util"
#install "bw-access"
#install "bw-synch"
#install "bw-timezone-server"
#install "bw-webdav"
#install "bw-caldav"
#install "bw-carddav"
#install "bw-self-registration"
install "bw-calendar-engine"
