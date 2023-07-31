package ru.otus.support.db

import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories


@EnableR2dbcRepositories
@Configuration
class DbConfiguration : AbstractR2dbcConfiguration() {
    override fun connectionFactory(): ConnectionFactory = H2ConnectionFactory(
        H2ConnectionConfiguration.builder()
            .url("mem:testdb;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4")
            .username("sa")
            .build()
    )
}