package com.example.spring.testing;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(IntegrationTestConfiguration.class)
@Testcontainers
public abstract class IntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> container =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"))
            .withUsername("postgres-test")
            .withPassword("postgres-test");

    @DynamicPropertySource
    private static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("database.url", container::getJdbcUrl);
        registry.add("database.user", container::getUsername);
        registry.add("database.password", container::getPassword);
    }

    @BeforeAll
    public static void beforeEach(ApplicationContext context) {
        Flyway flyway = context.getBean(Flyway.class);
        MigrateResult result = flyway.migrate();
        assertNotEquals(0, result.migrationsExecuted);
    }
}
