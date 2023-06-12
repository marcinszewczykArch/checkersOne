FROM openjdk:11

ADD target/scala-2.13/checkersOne.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]