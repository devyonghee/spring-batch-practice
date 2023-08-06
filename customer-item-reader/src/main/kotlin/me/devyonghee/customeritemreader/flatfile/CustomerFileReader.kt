package me.devyonghee.customeritemreader.flatfile

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream
import org.springframework.core.io.Resource

class CustomerFileReader(
    private val delegate: ResourceAwareItemReaderItemStream<out Any>,
) : ResourceAwareItemReaderItemStream<Customer?> {

    private var currentItem: Any? = null

    override fun setResource(resource: Resource) {
        delegate.setResource(resource)
    }

    override fun read(): Customer? {
        if (currentItem == null) {
            readAndAssignCurrentItem()
        }

        val customer: Customer? = currentItem as Customer?
        currentItem = null
        customer?.also {
            while (readAndAssignCurrentItem() is Transaction) {
                customer.addTransaction(currentItem as Transaction)
                currentItem = null
            }
        }
        return customer
    }

    private fun readAndAssignCurrentItem(): Any? {
        val item = delegate.read()
        currentItem = item
        return item
    }

    override fun close() {
        delegate.close()
    }

    override fun open(executionContext: ExecutionContext) {
        delegate.open(executionContext)
    }

    override fun update(executionContext: ExecutionContext) {
        delegate.update(executionContext)
    }
}