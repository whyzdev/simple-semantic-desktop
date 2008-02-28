#!/bin/sh
echo "Backing up old data (if there's any).."
rm -rf $2/backup
mkdir $2/backup
mv  $2/* $2/backup
echo "Cleaning Data for processing.."
mkdir $2/temp_phase1
./cleanhtmldirectory.pl $1/ $2/temp_phase1/
echo "Processing text.."
java -Xms512m -Xmx2048m ssd.TextProcessor $1/ $2/
#echo "Recognizing Entities.."
#cd ~/honors/libs/lingpipe/demos/generic/bin
#./corefall.sh $2/
echo "Starting Viewer.."
cd ~/honors/SSD.2b
java -Xms512m -Xmx2048m clusterviewer.ClusterViewer $1/ $2/

