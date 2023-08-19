package me.devyonghee.customeritemwriter.flatfile

import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLEventWriter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.item.xml.StaxWriterCallback
import org.springframework.stereotype.Component

@Component
class CustomerXmlHeaderCallback : StaxWriterCallback {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun write(writer: XMLEventWriter) {
        try {
            with(writer) {
                XMLEventFactory.newInstance().also {
                    add(it.createStartElement("", "", "identification"))
                    add(it.createStartElement("", "", "author"))
                    add(it.createAttribute("name", "dennis"))
                    add(it.createEndElement("", "", "author"))
                    add(it.createEndElement("", "", "identification"))
                }
            }
        } catch (e: Exception) {
            log.error("xml error occurred", e)
        }
    }
}