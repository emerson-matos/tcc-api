#FROM gradle:7.4.2-jdk17

##RUN gradle bootJar --no-daemon
##
FROM openjdk:17.0.2
WORKDIR /home/gradle/src/build/libs
##
EXPOSE 8080
EXPOSE 5005
#
ENTRYPOINT ["ls"]
#COPY build/libs/*.jar application.jar
##-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005","-Djava.security.egd=file:/dev/./urandom", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom","-jar","hotels-api-0.0.1-SNAPSHOT.jar"]
##ENTRYPOINT ["gradle","bootRun", "--debug-jvm"]