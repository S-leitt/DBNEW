package com.example.dbnew.config;

import com.example.dbnew.context.DataSourceKey;
import com.example.dbnew.context.DynamicDataSourceContextHolder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@MapperScan(basePackages = "com.example.dbnew.mapper")
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource mysqlDataSource(@Qualifier("mysqlDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.sqlserver")
    public DataSourceProperties sqlServerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource sqlServerDataSource(@Qualifier("sqlServerDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.postgres")
    public DataSourceProperties postgresDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource postgresDataSource(@Qualifier("postgresDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    public AbstractRoutingDataSource routingDataSource(@Qualifier("mysqlDataSource") DataSource mysql,
                                                       @Qualifier("sqlServerDataSource") DataSource sqlServer,
                                                       @Qualifier("postgresDataSource") DataSource postgres) {
        Map<Object, Object> targets = new HashMap<>();
        targets.put(DataSourceKey.MYSQL, mysql);
        targets.put(DataSourceKey.SQLSERVER, sqlServer);
        targets.put(DataSourceKey.POSTGRES, postgres);

        AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return DynamicDataSourceContextHolder.getDataSource();
            }
        };
        routing.setDefaultTargetDataSource(mysql);
        routing.setTargetDataSources(targets);
        return routing;
    }

    @Bean
    @Primary
    public DataSourceTransactionManager transactionManager(AbstractRoutingDataSource routingDataSource) {
        return new DataSourceTransactionManager(routingDataSource);
    }

    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(AbstractRoutingDataSource routingDataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(routingDataSource);
        return factoryBean.getObject();
    }
}
