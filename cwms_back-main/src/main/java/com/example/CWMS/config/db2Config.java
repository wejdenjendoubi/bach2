package com.example.CWMS.config;



import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.CWMS.db2.repositories",
        entityManagerFactoryRef = "db2EntityManagerFactory",
        transactionManagerRef = "db2TransactionManager"
)
public class db2Config {

    // ✅ DataSource
    @Bean(name = "db2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DataSource db2DataSource() {
        return DataSourceBuilder.create().build();
    }

    // ✅ EntityManagerFactory
    @Bean(name = "db2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db2EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("db2DataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.app.db2.entities") // 🔴 VERY IMPORTANT
                .persistenceUnit("db2")
                .build();
    }

    // ✅ TransactionManager
    @Bean(name = "db2TransactionManager")
    public PlatformTransactionManager db2TransactionManager(
            @Qualifier("db2EntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }
}