#FROM mysql/mysql-server
#FROM mysql-client
#FROM mysql:5.7
#RUN mysql -uroot
#CMD mysql -uroot

FROM openjdk:8-jdk
EXPOSE 8080:8080
#RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -yq mysql-server
#RUN apt-get update && apt-get install -y mysql-client
RUN mkdir /app
COPY ./build/install/simple-ecommerce/ /app/
WORKDIR /app/bin
#CMD ["./docker"]
#CMD ["ls"]
CMD ["./simple-ecommerce"]