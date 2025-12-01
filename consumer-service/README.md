# StudyRooms Consumer

A lightweight Spring Boot service that authenticates against the StudyRooms REST API using JWT, fetches spaces and the authenticated user's reservations, and logs a short digest. It is meant to demonstrate a second service that consumes the API.

## Running

```bash
mvn -f consumer-service/pom.xml spring-boot:run \
  -Dspring-boot.run.arguments="--studyrooms.consumer.enabled=true --studyrooms.consumer.base-url=http://localhost:8080 --studyrooms.consumer.username=student --studyrooms.consumer.password=pass"
```

By default `studyrooms.consumer.enabled=false` so the process exits quickly without calling the main API. Update the properties above to point to your running StudyRooms instance and credentials.
