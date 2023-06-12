FROM openjdk:11

COPY ./target/scala-**/checkersOne.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]