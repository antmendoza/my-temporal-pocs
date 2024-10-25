#!/usr/bin/env bash

host="http://localhost:3000"

for uid in $(curl -s "${host}/api/search" | jq -r '.[] | .uid'); do
    dashboard="$(curl -s "${host}/api/dashboards/uid/${uid}")"
    name=$(echo "${dashboard}" | jq -r '.meta.slug')
    echo "${dashboard}" | jq '.dashboard' > ${name}.json
done