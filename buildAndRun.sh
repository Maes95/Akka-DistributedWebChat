#!/usr/bin/env bash

rm -rf target/
./activator dist
unzip target/universal/webchat-1.0.zip -d target/universal/
./target/universal/webchat-1.0/bin/webchat -Dakka.cluster.seed-nodes.0=akka.tcp://application@127.0.0.1:8000

#./webchat-1.0/bin/webchat -Dakka.cluster.seed-nodes.0=akka.tcp://application@172.31.38.247:8000