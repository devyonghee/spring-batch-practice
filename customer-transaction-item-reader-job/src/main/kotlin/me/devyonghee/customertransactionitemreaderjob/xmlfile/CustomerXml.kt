package me.devyonghee.customertransactionitemreaderjob.xmlfile

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlElementWrapper
import jakarta.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "customer")
@XmlAccessorType(XmlAccessType.FIELD)
data class CustomerXml(
    var firstName: String? = null,
    var middleInitial: String? = null,
    var lastName: String? = null,
    var addressNumber: String? = null,
    var street: String? = null,
    var city: String? = null,
    var state: String? = null,
    var zipCode: String? = null,
    @XmlElement(name = "transaction")
    @XmlElementWrapper(name = "transactions")
    var transaction: List<TransactionXml>? = null,
)
