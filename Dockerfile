FROM maven:3.5.4-jdk-8 AS build  

COPY . /
COPY datasource-observer-app/src/main/resources/h2_Jobstore.mv.db /

RUN mvn -f /pom.xml clean install -DskipTests


FROM openjdk:8  
COPY --from=build /datasource-observer-app/target/datasource-observer-app-0.0.1-SNAPSHOT.jar /datasource-observer-app/datasource-observer-app-0.0.1-SNAPSHOT.jar 
COPY --from=build /h2_Jobstore.mv.db /
EXPOSE 5672
ENTRYPOINT ["java","-jar","/datasource-observer-app/datasource-observer-app-0.0.1-SNAPSHOT.jar"]