#!/bin/bash

#
# METVIEWER_DOCKER_TAG DockerHub tag 
#
if [ -z ${METVIEWER_DOCKER_TAG+x} ]; then
  echo "ERROR: METVIEWER_DOCKER_TAG must be set to the DockerHub dtcenter/metviewer tag to be run!"
  exit 1
else
  echo "METVIEWER_DOCKER_TAG = ${METVIEWER_DOCKER_TAG}"
fi

#
# MYSQL_DIR local path for MySQL data
#
if [ -z ${MYSQL_DIR+x} ]; then
  echo "ERROR: MYSQL_DIR must be set to the local path for MySQL data!"
  exit 1
else
  echo "MYSQL_DIR = ${MYSQL_DIR}"
fi

#
# METVIEWER_DATA local path for MET data to be loaded 
#
if [ -z ${METVIEWER_DATA+x} ]; then
  echo "ERROR: METVIEWER_DATA must be set to the local path for MET data to be loaded!"
  exit 1
else
  echo "METVIEWER_DATA = ${METVIEWER_DATA}"
fi

#
# METVIEWER_DIR local path for METviewer output
#
if [ -z ${METVIEWER_DIR+x} ]; then
  echo "ERROR: METVIEWER_DIR must be set to the local path for METviewer output data!"
  exit 1
else
  echo "METVIEWER_DIR = ${METVIEWER_DIR}"
fi

