#!/bin/sh
cp target/*.jar src/docker
docker build src/docker -t yogi:latest

