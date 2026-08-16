package com.example.travelplanner.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String url = properties.getUrl();
        if (url != null && !url.startsWith("jdbc:")) {
            url = "jdbc:" + url;
        }
        if (url != null) {
            int protocolEnd = url.indexOf("://");
            if (protocolEnd >= 0) {
                String afterProtocol = url.substring(protocolEnd + 3);
                int atPos = afterProtocol.indexOf('@');
                if (atPos >= 0) {
                    String credentials = afterProtocol.substring(0, atPos);
                    String hostAndDb = afterProtocol.substring(atPos + 1);
                    int colonPos = credentials.indexOf(':');
                    if (colonPos >= 0) {
                        properties.setUsername(credentials.substring(0, colonPos));
                        properties.setPassword(credentials.substring(colonPos + 1));
                    }
                    int slashPos = hostAndDb.indexOf('/');
                    if (slashPos > 0 && !hostAndDb.substring(0, slashPos).contains(":")) {
                        hostAndDb = hostAndDb.substring(0, slashPos) + ":5432" + hostAndDb.substring(slashPos);
                    }
                    url = url.substring(0, protocolEnd + 3) + hostAndDb;
                }
            }
        }
        properties.setUrl(url);
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
