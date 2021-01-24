import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j", "log4j-api", properties["version.log4j"].toString())
    implementation("org.apache.logging.log4j", "log4j-core", properties["version.log4j"].toString())
    implementation("org.slf4j", "slf4j-simple", properties["version.slf4j"].toString())

    implementation("org.springframework", "spring-core", properties["version.spring"].toString())
    testImplementation("org.springframework", "spring-test", properties["version.spring"].toString())

    implementation("org.springframework.data", "spring-data-jpa", properties["version.spring.data"].toString())
    implementation("javax.validation", "validation-api", properties["version.javax.validation"].toString())
    implementation("javax.xml.bind", "jaxb-api", properties["version.jaxb.api"].toString())
    implementation("org.glassfish.jaxb", "jaxb-runtime", properties["version.jaxb.runtime"].toString())
    implementation("org.hibernate", "hibernate-core", properties["version.hibernate"].toString())
    implementation("org.hibernate", "hibernate-entitymanager", properties["version.hibernate"].toString())
    implementation("org.postgresql", "postgresql", properties["version.postgres"].toString())
    implementation("org.flywaydb", "flyway-core", properties["version.flyway"].toString())

    testImplementation("org.hibernate", "hibernate-testing", properties["version.hibernate"].toString())

    testImplementation("org.junit.jupiter", "junit-jupiter", properties["version.junit"].toString())
    testImplementation("org.testcontainers", "testcontainers", properties["version.testcontainers"].toString())
    testImplementation("org.testcontainers", "postgresql", properties["version.testcontainers"].toString())
    testImplementation("org.testcontainers", "junit-jupiter", properties["version.testcontainers"].toString())

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events = setOf(
            TestLogEvent.PASSED,
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED
        )
    }
}
