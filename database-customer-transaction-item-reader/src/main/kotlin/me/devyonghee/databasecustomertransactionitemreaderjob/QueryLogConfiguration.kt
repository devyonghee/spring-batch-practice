package me.devyonghee.databasecustomertransactionitemreaderjob

import com.p6spy.engine.spy.P6SpyOptions
import com.p6spy.engine.spy.appender.MessageFormattingStrategy
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class QueryLogConfiguration {

    @PostConstruct
    fun setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().apply {
            logMessageFormat = CustomMessageFormattingStrategy::class.java.getName()
        }
    }

    class CustomMessageFormattingStrategy : MessageFormattingStrategy {
        override fun formatMessage(
            connectionId: Int,
            now: String?,
            elapsed: Long,
            category: String?,
            prepared: String?,
            query: String?,
            url: String?,
        ): String {
            if (query.isNullOrBlank()) {
                return ""
            }
            return summary(query, connectionId, elapsed)
        }

        private fun summary(query: String, connectionId: Int, elapsed: Long): String {
            return """
                ----------------------------------------------------------------------------------------------------
                                                            QUERY LOG
                ----------------------------------------------------------------------------------------------------
                ${'\t'}${query}
                ----------------------------------------------------------------------------------------------------
                - Connection ID                           : $connectionId
                - Execution Time                          : $elapsed ms
                ----------------------------------------------------------------------------------------------------
                
                """
                .trimIndent()
        }
    }
}