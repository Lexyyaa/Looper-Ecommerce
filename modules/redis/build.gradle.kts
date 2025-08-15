plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-redis")
    //cache
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.springframework.boot:spring-boot-starter-cache")

    testFixturesImplementation("com.redis:testcontainers-redis")
    testFixturesImplementation("org.testcontainers:testcontainers")
    testFixturesImplementation("org.testcontainers:junit-jupiter")
}
