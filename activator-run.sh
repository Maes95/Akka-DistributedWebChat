#!/usr/bin/env bash

./activator -Dhttp.port=$1 -Dakka.cluster.seed-nodes.0=akka.tcp://application@127.0.0.1:8000 run