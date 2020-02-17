# build image
FROM maven:3.5.4-jdk-8 AS build  

COPY . /
COPY datasource-observer-app/src/main/resources/h2_Jobstore.mv.db /

RUN mvn -f /pom.xml clean install -DskipTests -Dapp.finalName=datasource-observer-app

# runnable image
FROM adoptopenjdk/openjdk8:alpine

COPY --from=build /datasource-observer-app/target/datasource-observer-app.jar /app.jar 
COPY --from=build /h2_Jobstore.mv.db /
EXPOSE 5672
ENTRYPOINT ["java","-jar","/app.jar"]
