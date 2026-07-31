# Step 1: Build stage using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Tomcat runtime stage
FROM tomcat:10.1-jdk17

# Disable Tomcat shutdown port (set to -1 to ignore health checks hitting shutdown port)
RUN sed -i 's/port="8005" shutdown="SHUTDOWN"/port="-1" shutdown="SHUTDOWN"/' /usr/local/tomcat/conf/server.xml

# Remove default ROOT application and copy your WAR file as ROOT.war
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]