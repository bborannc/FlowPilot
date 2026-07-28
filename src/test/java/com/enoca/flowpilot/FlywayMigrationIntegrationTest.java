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

@SpringBootTest
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
    }

    @Autowired
    private Flyway flyway;

    @Test
    @DisplayName("Sıfır PostgreSQL konteyneri üzerinde tüm Flyway migration'ları başarıyla çalışmalıdır")
    void shouldApplyAllMigrationsSuccessfullyOnCleanDatabase() {
        // Flyway'in uyguladığı migration bilgilerini alıyoruz
        var migrationInfo = flyway.info();

        // 1. Tüm migration'ların (V1, V2, V3) başarıyla uygulandığını doğruluyoruz
        assertThat(migrationInfo.applied()).hasSize(3);

        // 2. Veritabanının en son sürümde (v3) olduğunu doğruluyoruz
        assertThat(migrationInfo.current().getVersion().getVersion()).isEqualTo("3");
    }
}
