# reverie-backend

This repository holds the source code for the reverie API. It is responsible for journal creation, listing, insights generation and chats.

It is written in Spring Boot using Java.


### application.properties

Create this file in `src/main/resources` directory with the following content.

```
spring.application.name=reverie
spring.datasource.url=jdbc:postgresql://localhost:5432/reverie
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto = update
spring.jpa.show-sql = true
spring.datasource.hikari.auto-commit = false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web.reactive.function.client=TRACE

huggingface.api.token=
api.key=
```

Obtain hugging face API token from hugging face, set the API key as set in the frontend. In the datasource fields, fill the postgres credentials.