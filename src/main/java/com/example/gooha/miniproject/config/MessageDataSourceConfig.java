package com.example.gooha.miniproject.config;

import com.example.gooha.miniproject.sharding.MessageShardKeySelector;
import com.example.gooha.miniproject.sharding.ShardDataProperties;
import com.example.gooha.miniproject.sharding.ShardingDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(
        basePackages = "com.example.gooha.miniproject.repository.message",
        entityManagerFactoryRef = "messageEntityManagerFactory",
        transactionManagerRef = "messageTransactionManager"
)
public class MessageDataSourceConfig {
    private final ShardDataProperties properties;

    public DataSource createDataSource(ShardDataProperties.DataSourceProperties prop) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(prop.getUrl());
        config.setUsername(prop.getUsername());
        config.setPassword(prop.getPassword());
        config.setDriverClassName(prop.getDriverClassName());
        config.setMaximumPoolSize(prop.getMaximumPoolSize());
        config.setMinimumIdle(prop.getMinimumIdle());

        // 로그 추가
        System.out.println("DataSource URL: " + prop.getUrl());
        System.out.println("Maximum Pool Size: " + prop.getMaximumPoolSize());
        System.out.println("Minimum Idle: " + prop.getMinimumIdle());

        return new HikariDataSource(config);
    }

    @Bean
    public DataSource dataSource() {
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(0, createDataSource(properties.getMiniMessage0()));
        dataSourceMap.put(1, createDataSource(properties.getMiniMessage1()));

        MessageShardKeySelector messageShardKeySelector = new MessageShardKeySelector();
        return new LazyConnectionDataSourceProxy(new ShardingDataSource(dataSourceMap, messageShardKeySelector));
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean messageEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource())
                .packages("com.example.gooha.miniproject.entity.message")
                .persistenceUnit("message")
                .build();
    }

    @Bean
    public PlatformTransactionManager messageTransactionManager(@Qualifier("messageEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
