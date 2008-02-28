#!/bin/sh
echo "Cleaning Data for processing.."
mkdir $2/temp_phase1
./cleanhtmldirectory.pl $1 $2/temp_phase1
echo "Processing text.."
java TextProcessor $1 $2

#echo "Recognizing Entities.."
#cd /local/s/lingpipe/demos/generic/bin
#./corefall.sh $2
#echo "Starting Viewer.."
#cd ~/honors/EntityMapper
#java ClusterViewer $1 $2

