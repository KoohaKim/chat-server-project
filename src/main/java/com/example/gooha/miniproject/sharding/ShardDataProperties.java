package com.example.gooha.miniproject.sharding;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class ShardDataProperties {
    private DataSourceProperties miniMessage0;
    private DataSourceProperties miniMessage1;
    private DataSourceProperties miniMain;

    @Getter
    @Setter
    public static class DataSourceProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private int maximumPoolSize;
        private int minimumIdle;
    }
}
