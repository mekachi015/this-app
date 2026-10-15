   FROM maven:3.9-eclipse-temurin-17 AS build
   WORKDIR /workspace
   COPY pom.xml .
   COPY src ./src
   RUN mvn -B -DskipTests package

   FROM eclipse-temurin:17-jre
   WORKDIR /app
   COPY --from=build /workspace/target/This_App_Backend-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 10000
   CMD ["sh", "-c", "exec java -Dserver.port=${PORT:-10000} -jar /app/app.jar"]