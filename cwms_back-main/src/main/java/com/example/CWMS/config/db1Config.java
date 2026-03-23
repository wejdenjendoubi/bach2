package com.example.CWMS.config;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.CWMS.db1.repositories",
        entityManagerFactoryRef = "db1EntityManagerFactory",
        transactionManagerRef = "db1TransactionManager"
)
public class db1Config {

    // ✅ DataSource
    @Primary
    @Bean(name = "db1DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db1")
    public DataSource db1DataSource() {
        return DataSourceBuilder.create().build();
    }

    // ✅ EntityManagerFactory
    @Primary
    @Bean(name = "db1EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db1EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("db1DataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.app.db1.entities") // 🔴 VERY IMPORTANT
                .persistenceUnit("db1")
                .build();
    }

    // ✅ TransactionManager
    @Primary
    @Bean(name = "db1TransactionManager")
    public PlatformTransactionManager db1TransactionManager(
            @Qualifier("db1EntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }
}