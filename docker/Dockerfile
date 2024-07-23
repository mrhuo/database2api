FROM openjdk:8-jre
WORKDIR /usr/app
VOLUME /usr/app/data
COPY ./database2api.jar /usr/app/database2api.jar
CMD ["java", "-jar", "-Dfile.encoding=utf-8", "database2api.jar"]