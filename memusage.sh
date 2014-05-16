#!/bin/bash

$@ & 

pid=$!

while true
do
  sleep 0.1
  #cat /proc/$pid/status
  ps -p $pid -o vsize
done
