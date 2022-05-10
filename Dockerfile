FROM adoptopenjdk/openjdk16:debian

RUN apt update && apt upgrade -y
RUN apt install -y wget
RUN mkdir -p ~/.postgresql
RUN wget "https://storage.yandexcloud.net/cloud-certs/CA.pem" -O ~/.postgresql/root.crt
RUN chmod 0600 ~/.postgresql/root.crt

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY target/*.jar app.jar

EXPOSE 8089

CMD ["java", "-jar", "app.jar"]