#!/bin/sh


for i in {1..5} ; do

  for x in {1..20}; do tctl workflow start --tq "default" --wt "Parent.greet" --input '720'; done

  for x in {1..20}; do tctl workflow start --tq "default" --wt "Parent.greet" --input '660'; done

  sleep 600

done


