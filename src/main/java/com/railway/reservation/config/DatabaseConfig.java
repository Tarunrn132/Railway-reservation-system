package com.railway.reservation.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${DB_URL:#{null}}")
    private String dbUrl;

    @Value("${DB_USERNAME:#{null}}")
    private String dbUsername;

    @Value("${DB_PASSWORD:#{null}}")
    private String dbPassword;

    @Bean
    @Primary
    @Profile("prod")
    public DataSource productionDataSource() {
        String rawUrl = databaseUrl != null && !databaseUrl.trim().isEmpty() ? databaseUrl.trim() : dbUrl;

        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            log.info("No explicit DATABASE_URL/DB_URL provided in prod profile, using standard JDBC configuration.");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/railway_reservation");
            config.setDriverClassName("org.postgresql.Driver");
            config.setUsername(dbUsername != null ? dbUsername : "railway_user");
            config.setPassword(dbPassword != null ? dbPassword : "password");
            return new HikariDataSource(config);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.postgresql.Driver");

        // Handle standard Render / Heroku URI format: postgres://user:password@host:port/dbname
        if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
            try {
                URI uri = new URI(rawUrl);
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }

                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, path);
                hikariConfig.setJdbcUrl(jdbcUrl);

                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    hikariConfig.setUsername(parts[0]);
                    hikariConfig.setPassword(parts[1]);
                } else if (userInfo != null) {
                    hikariConfig.setUsername(userInfo);
                    if (dbPassword != null) {
                        hikariConfig.setPassword(dbPassword);
                    }
                }

                log.info("Successfully parsed Render PostgreSQL URI into JDBC URL: jdbc:postgresql://{}:{}/{}", host, port, path);
            } catch (Exception e) {
                log.warn("Could not parse DATABASE_URL as URI, treating as direct JDBC string: {}", e.getMessage());
                hikariConfig.setJdbcUrl(rawUrl);
                if (dbUsername != null) hikariConfig.setUsername(dbUsername);
                if (dbPassword != null) hikariConfig.setPassword(dbPassword);
            }
        } else {
            // Direct JDBC URL
            hikariConfig.setJdbcUrl(rawUrl);
            if (dbUsername != null) hikariConfig.setUsername(dbUsername);
            if (dbPassword != null) hikariConfig.setPassword(dbPassword);
        }

        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setConnectionTimeout(20000);
        hikariConfig.setPoolName("RailReserveProdHikariPool");

        return new HikariDataSource(hikariConfig);
    }
}
