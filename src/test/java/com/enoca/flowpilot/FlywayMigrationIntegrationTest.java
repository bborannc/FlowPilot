package com.enoca.flowpilot;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration"
})
@Testcontainers
class FlywayMigrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dynamic_approval_test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired(required = false)
    private Flyway flyway;

    @Test
    @DisplayName("Sıfır PostgreSQL konteyneri üzerinde tüm Flyway migration'ları başarıyla çalışmalıdır")
    void shouldApplyAllMigrationsSuccessfullyOnCleanDatabase() {
        // Eğer bean otomatik enjekte edilmediyse doğrudan konteyner bağlantısıyla Flyway'i ayağa kaldıralım
        if (flyway == null) {
            flyway = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations("classpath:db/migration")
                    .load();
            flyway.migrate();
        }

        var migrationInfo = flyway.info();

        // 1. V1, V2 ve V3 migration'larının uygulandığını doğrula
        assertThat(migrationInfo.applied()).hasSize(3);

        // 2. Veritabanının en son sürümde (3) olduğunu doğrula
        assertThat(migrationInfo.current().getVersion().getVersion()).isEqualTo("3");
    }
}