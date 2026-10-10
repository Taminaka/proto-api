package com.tamaka.tourism.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
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
    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_migrations (
                    id BIGSERIAL PRIMARY KEY,
                    migration_key VARCHAR(200) NOT NULL UNIQUE,
                    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        for (String migration : MIGRATIONS) {
            Integer applied = jdbcTemplate.queryForObject(
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
            populator.execute(dataSource);
            jdbcTemplate.update("INSERT INTO app_migrations (migration_key) VALUES (?)", migration);
        }
    }
}
