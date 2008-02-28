#!/bin/sh
echo "Cleaning Data for processing.."
java CleanTextForNER $1 $2
echo "Recognizing Entities.."
cd /local/s/lingpipe/demos/generic/bin
./corefall.sh $2
echo "Starting Viewer.."
cd ~/honors/EntityMapper
java ClusterViewer $1 $2

