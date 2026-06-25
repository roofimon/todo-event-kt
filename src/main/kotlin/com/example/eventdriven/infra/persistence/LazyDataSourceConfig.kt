package com.example.eventdriven.infra.persistence

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import javax.sql.DataSource

/**
 * Makes the H2 connection lazy, mirroring the deferred RabbitMQ connection: the
 * application boots without opening any database connection, and a physical
 * connection is acquired only when a statement actually runs (first repository
 * call). Combined with `ddl-auto=none`, an explicit dialect, disabled boot-time
 * JDBC metadata access, and a lazy H2 `INIT` script (see application.properties),
 * nothing touches the database at startup.
 */
@Configuration(proxyBeanMethods = false)
class LazyDataSourceConfig {

    /** The real pool. `minimum-idle=0` + `initialization-fail-timeout=-1` keep it from connecting at startup. */
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    fun realDataSource(properties: DataSourceProperties): HikariDataSource =
        properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()

    /**
     * Defers acquiring a physical connection until a statement is actually executed.
     * `defaultAutoCommit` is set so the proxy doesn't open an extra probe connection
     * to discover it — important when the DB is down, so failure isn't paid twice.
     */
    @Bean
    @Primary
    fun dataSource(realDataSource: HikariDataSource): DataSource =
        LazyConnectionDataSourceProxy(realDataSource).apply { setDefaultAutoCommit(true) }
}
