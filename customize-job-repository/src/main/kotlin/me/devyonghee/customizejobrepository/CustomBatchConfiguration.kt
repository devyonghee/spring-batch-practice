package me.devyonghee.customizejobrepository

import javax.sql.DataSource
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.support.DatabaseType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.transaction.PlatformTransactionManager

class CustomBatchConfiguration(
    @Qualifier("repositoryDataSource") private val dataSource: DataSource,
    @Qualifier("batchTransactionManager") private val transactionManager: PlatformTransactionManager,
) : DefaultBatchConfiguration() {

    override fun jobRepository(): JobRepository {
        // 빈 정의로 호출하지 않기 때문에 afterPropertiesSet, object 직접 호출 필요
        return JobRepositoryFactoryBean().apply {
            setDatabaseType(DatabaseType.MYSQL.productName)
            setTablePrefix("FOO_")
            setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ")
            setDataSource(dataSource)
            afterPropertiesSet()
        }.`object`
    }

    override fun getTransactionManager(): PlatformTransactionManager {
        return transactionManager
    }

    override fun jobExplorer(): JobExplorer {
        return JobExplorerFactoryBean().apply {
            setDataSource(dataSource)
            setTablePrefix("FOO_")
            afterPropertiesSet()
        }.`object`
    }
}