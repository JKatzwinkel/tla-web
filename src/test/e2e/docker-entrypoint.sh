#!/bin/bash

( [ -d node_modules/ ] || npm install ) && cypress run "$@"
