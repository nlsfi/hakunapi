FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build

COPY . /build

RUN mvn -DskipTests -pl webapp-jakarta/hakunapi-simple-webapp-jakarta -am clean package

FROM tomcat:jdk21-openjdk
ENV CATALINA_OUT=/dev/stdout

# Copy the built WAR from the build stage
COPY --from=build /build/webapp-jakarta/hakunapi-simple-webapp-jakarta/target/*.war /usr/local/tomcat/webapps/features.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
