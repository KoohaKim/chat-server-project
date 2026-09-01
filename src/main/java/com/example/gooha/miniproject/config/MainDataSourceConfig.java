package com.example.gooha.miniproject.config;

import com.example.gooha.miniproject.sharding.ShardDataProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(
        basePackages = "com.example.gooha.miniproject.repository.main",
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
)
public class MainDataSourceConfig {
    private final ShardDataProperties properties;

    @Primary
    @Bean
    public DataSource mainDataSource() {
        ShardDataProperties.DataSourceProperties prop = properties.getMiniMain();
        System.out.println("MiniMain URL: " + prop.getUrl());
        System.out.println("MiniMain Max Pool Size: " + prop.getMaximumPoolSize());
        System.out.println("MiniMain Min Idle: " + prop.getMinimumIdle());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(prop.getUrl());
        config.setUsername(prop.getUsername());
        config.setPassword(prop.getPassword());
        config.setDriverClassName(prop.getDriverClassName());
        config.setMaximumPoolSize(prop.getMaximumPoolSize());
        config.setMinimumIdle(prop.getMinimumIdle());

        return new HikariDataSource(config);
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(mainDataSource())
                .packages("com.example.gooha.miniproject.entity.main")
                .persistenceUnit("main")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager mainTransactionManager(@Qualifier("mainEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
