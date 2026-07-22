package com.fillinus.erp.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts Render.com's DATABASE_URL to a proper JDBC URL.
 *
 * Render injects one of these formats:
 *   postgres://user:password@host:port/dbname
 *   postgresql://user:password@host:port/dbname
 *
 * Spring/HikariCP needs:
 *   jdbc:postgresql://host:port/dbname
 *
 * This EnvironmentPostProcessor runs before any Spring beans are created,
 * so Flyway and JPA both receive the correct JDBC URL automatically.
 */
public class RenderDatabaseUrlConverter implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        String dbUrl = environment.getProperty("DATABASE_URL");
        if (dbUrl == null) return;

        // Handle both postgres:// and postgresql:// schemes
        String withoutScheme = null;
        if (dbUrl.startsWith("postgresql://")) {
            withoutScheme = dbUrl.substring("postgresql://".length());
        } else if (dbUrl.startsWith("postgres://")) {
            withoutScheme = dbUrl.substring("postgres://".length());
        }

        if (withoutScheme != null) {
            // withoutScheme = user:password@host:port/dbname
            String[] userInfoAndRest = withoutScheme.split("@", 2);
            if (userInfoAndRest.length == 2) {
                String userInfo   = userInfoAndRest[0]; // user:password
                String hostAndDb  = userInfoAndRest[1]; // host:port/dbname

                String[] credentials = userInfo.split(":", 2);
                String user     = credentials.length > 0 ? credentials[0] : "";
                String password = credentials.length > 1 ? credentials[1] : "";

                // Build proper JDBC URL
                String jdbcUrl = "jdbc:postgresql://" + hostAndDb + "?sslmode=require";

                Map<String, Object> props = new HashMap<>();
                props.put("DATABASE_URL", jdbcUrl);
                props.put("DB_USERNAME",  user);
                props.put("DB_PASSWORD",  password);

                environment.getPropertySources().addFirst(
                        new MapPropertySource("renderDatabaseConverter", props)
                );
            }
        }
    }
}
