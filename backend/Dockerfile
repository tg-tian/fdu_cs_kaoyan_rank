FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml backend/pom.xml
COPY backend/.mvn backend/.mvn
COPY backend/src backend/src
COPY proto proto
RUN cd backend && mvn -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/backend/target/*.jar /app/app.jar
EXPOSE 6666
ENTRYPOINT ["java","-jar","/app/app.jar"]
