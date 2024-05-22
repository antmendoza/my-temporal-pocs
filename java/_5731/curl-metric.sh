#!/bin/bash

curl -L -X POST 'https://jky94838.live.dynatrace.com/api/v2/metrics/ingest' \
-H 'Authorization: Api-Token dt0c01.O4QJXGRDLPPNHKP6QZ6HI75A.GP3XSHNEDM5VFGS7TA37WUP2ASNXOIKTFWMSYRDTCYXZOBVXZUOKX6K6JQNSYRFX' \
-H 'Content-Type: text/plain' \
--data-raw 'cpu.temperature2,dt.entity.host=HOST-06F288EE2A930951,cpu=1 55'