package com.tamaka.tourism.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
@Order(2)
@RequiredArgsConstructor
public class SeedMigrationRunner implements CommandLineRunner {
    private static final String[] MIGRATIONS = {
            "V1__santa_marta_tourism_seed",
            "V2__taminaka_admin_account"
    };

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    @Value("${DIRECT_URL:}")
    private String directUrl;

    @Override
    public void run(String... args) {
        DataSource migrationDataSource = dataSource;
        JdbcTemplate migrationJdbcTemplate = jdbcTemplate;
        if (directUrl != null && !directUrl.isBlank()) {
            String jdbcDirectUrl = toJdbcUrl(directUrl);
            DriverManagerDataSource directDataSource = new DriverManagerDataSource();
            directDataSource.setUrl(jdbcDirectUrl);
            migrationDataSource = directDataSource;
            migrationJdbcTemplate = new JdbcTemplate(directDataSource);
        }

        migrationJdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_migrations (
                    id BIGSERIAL PRIMARY KEY,
                    migration_key VARCHAR(200) NOT NULL UNIQUE,
                    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        for (String migration : MIGRATIONS) {
            Integer applied = migrationJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM app_migrations WHERE migration_key = ?",
                    Integer.class,
                    migration
            );
            if (applied != null && applied > 0) {
                continue;
            }

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
            populator.addScript(new ClassPathResource("db/migration/" + migration + ".sql"));
            populator.execute(migrationDataSource);
            migrationJdbcTemplate.update("INSERT INTO app_migrations (migration_key) VALUES (?)", migration);
        }
    }

    private String toJdbcUrl(String url) {
        if (url.startsWith("jdbc:")) {
            return url;
        }
        if (url.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }
        if (url.startsWith("postgresql://")) {
            return "jdbc:postgresql://" + url.substring("postgresql://".length());
        }
        throw new IllegalArgumentException("Unsupported DIRECT_URL scheme");
    }
}
