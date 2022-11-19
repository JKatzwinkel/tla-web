#!/bin/bash

url=${url:-$1}
timeout=${timeout:-$2}
interval=${interval:-2}

until curl -sf $url; do
  sleep ${interval} && timeout=$((timeout - interval))
  if [[ ${timeout} -lt 0 ]]; then
    exit 1
  fi
done

exit 0
