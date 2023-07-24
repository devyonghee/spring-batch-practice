package me.devyonghee.accounttransactionjob

import org.springframework.batch.item.ItemProcessor

class TransactionApplierProcessor(
    private val transactionDao: TransactionDaoSupport,
) : ItemProcessor<AccountSummary, AccountSummary> {

    override fun process(item: AccountSummary): AccountSummary {
        return item.apply { currentBalance += sumTransactionAmount(item) }
    }

    private fun sumTransactionAmount(item: AccountSummary): Double {
        return transactionDao.getTransactionsByAccountNumber(item.accountNumber)
            .sumOf {
                it.amount
            }
    }
}
