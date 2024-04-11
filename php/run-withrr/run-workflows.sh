#!/bin/sh


for i in {1..5} ; do

  for x in {1..10}; do tctl workflow start --tq "default" --wt "Parent.greet" --input '31'; done

  sleep 30

done


