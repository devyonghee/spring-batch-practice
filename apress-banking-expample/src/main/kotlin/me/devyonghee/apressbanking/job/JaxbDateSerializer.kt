package me.devyonghee.apressbanking.job

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.text.SimpleDateFormat
import java.util.Date

object JaxbDateSerializer : XmlAdapter<String, Date>() {

    private val DATE_FORMAT: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override fun unmarshal(v: String?): Date {
        return DATE_FORMAT.parse(v)
    }

    override fun marshal(v: Date?): String {
        return DATE_FORMAT.format(v)
    }

}

