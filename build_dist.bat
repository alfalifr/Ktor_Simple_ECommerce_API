echo Starting build...
call gradlew installDist

if errorlevel 1 goto error

docker stop ktor-run
docker rm ktor-run

docker stop mysql1
docker rm mysql1

docker network rm mynet

docker network create mynet
docker build -t ktor-app .

docker run -d -it -p 3306:3306 --network mynet -e MYSQL_ROOT_PASSWORD=admin -e MYSQL_DATABASE=mytestdb -e MYSQL_USER=admin -e MYSQL_HOST=127.0.0.1 -e MYSQL_PASSWORD=abc123 --name mysql1 mysql:5.7
timeout 10 /nobreak
docker run -d -it -p 8080:8080 --network mynet --name ktor-run ktor-app

if errorlevel 0 goto ok

:error
echo error

:ok
echo ok

rem docker build -t ktor-app .
rem docker run -it -p 8080:8080 --network mysql1 --name ktor-run ktor-app
rem docker run -t -p 8080:8080 --name ktor-run ktor-app
rem docker run -d -it --network app_network -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=database_name --name mysql1 mysql/mysql-server
rem docker run -d -it --network app_network --name mysql1 mysql/mysql-server
rem docker run -d -it -p 8080:8080 --network app_network --name ktor-run ktor-app

rem docker run -d -it --network app_network --name mysql_name mysql