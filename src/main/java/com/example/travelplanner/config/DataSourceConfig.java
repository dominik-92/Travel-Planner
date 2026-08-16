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
            int atPos = url.indexOf('@');
            if (atPos >= 0) {
                int slashPos = url.indexOf('/', atPos);
                if (slashPos > atPos) {
                    String hostPart = url.substring(atPos + 1, slashPos);
                    if (!hostPart.contains(":")) {
                        url = url.substring(0, slashPos) + ":5432" + url.substring(slashPos);
                    }
                }
            }
        }
        properties.setUrl(url);
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
