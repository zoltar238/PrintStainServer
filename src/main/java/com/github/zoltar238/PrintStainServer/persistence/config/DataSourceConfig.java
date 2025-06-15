package com.github.zoltar238.PrintStainServer.persistence.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.primary.url:jdbc:postgresql://localhost:5432/printstain}")
    private String primaryUrl;

    @Value("${spring.datasource.secondary.url:jdbc:postgresql://printstain-db:5432/printstain}")
    private String secondaryUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.auto-commit:false}")
    private boolean autoCommit;

    @Value("${spring.datasource.hikari.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSource primaryDataSource = createDataSource(primaryUrl);

        // Try to connect to the primary database
        try (Connection connection = primaryDataSource.getConnection()) {
            log.info("Successfully connected to primary database at {}", primaryUrl);
            return primaryDataSource;
        } catch (SQLException e) {
            log.warn("Failed to connect to primary database at {}: {}", primaryUrl, e.getMessage());
            log.info("Attempting to connect to secondary database at {}", secondaryUrl);

            // Try to connect to the secondary database
            DataSource secondaryDataSource = createDataSource(secondaryUrl);
            try (Connection connection = secondaryDataSource.getConnection()) {
                log.info("Successfully connected to secondary database at {}", secondaryUrl);
                return secondaryDataSource;
            } catch (SQLException ex) {
                log.error("Failed to connect to secondary database at {}: {}", secondaryUrl, ex.getMessage());
                log.error("Could not connect to any database. Returning primary DataSource configuration, but connection will likely fail.");
                return primaryDataSource;
            }
        }
    }

    private DataSource createDataSource(String url) {
        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();

        // Configure Hikari connection pool
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setAutoCommit(autoCommit);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        dataSource.setConnectionTimeout(connectionTimeout);

        return dataSource;
    }
}
