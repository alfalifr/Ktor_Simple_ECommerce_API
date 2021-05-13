#!/bin/bash

./gradlew installDist

docker stop ktor-run
docker rm ktor-run

docker stop mysql1
docker rm mysql1

docker network rm mynet

docker network create mynet
docker build -t ktor-app .

docker run -d -it -p 3306:3306 --network mynet -e MYSQL_ROOT_PASSWORD=admin -e MYSQL_DATABASE=mytestdb -e MYSQL_USER=admin -e MYSQL_HOST=127.0.0.1 -e MYSQL_PASSWORD=abc123 --name mysql1 mysql:5.7
sleep 10
docker run -d -it -p 8080:8080 --network mynet --name ktor-run ktor-app