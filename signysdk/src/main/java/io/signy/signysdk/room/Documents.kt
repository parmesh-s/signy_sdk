package io.signy.signysdk.room

import io.signy.signysdk.model.Document


public fun getDocumentList(): MutableList<Document> {
    val documentList = mutableListOf<Document>()
    documentList.add(Document("Passport", "India"))
    documentList.add(Document("Pancard", "India"))
    documentList.add(Document("Aadhaar Card", "India"))
//    documentList.add(Document("Driving Licence", "India"))
    return documentList
}