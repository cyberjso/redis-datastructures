#!/bin/bash

docker rm redis-server

docker run --network host --name redis-server -d redis  
