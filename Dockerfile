FROM openjdk:11

ADD target/scala-**/checkersOne.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]