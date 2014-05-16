#!/bin/bash

PYTHON=python2.7
SCALA=scala-2.10

inputResolution=720x480
outputResolution=fullscreen
environmentResolution=720x480
framesPerSecBirds=24
framesPerSecCam=24
popSize=400
birdsSpeed=1.5
vision=10
visionObstacle=10
minimumSeparation=0.8
maxAlignTurn=10
maxCohereTurn=3
maxSeparateTurn=1.5
birdWidth=1.5
birdLength=1.2
dataFile="/tmp/fgmask"

baseDir=`pwd`

trap "kill 0" SIGHUP SIGINT SIGTERM

cd InterfaceCamera/Projection
$PYTHON Projection.py $framesPerSecCam $dataFile &
cd $baseDir
$SCALA -cp target/scala-2.10/flocking_2.10-0.1.jar flocking.Interface $inputResolution $outputResolution $environmentResolution $framesPerSecBirds $framesPerSecCam $birdsSpeed $dataFile $vision $visionObstacle $minimumSeparation $maxAlignTurn $maxCohereTurn $maxSeparateTurn $birdWidth $birdLength $popSize &

wait
