package io.signy.signysdk.model

data class DocumentField(
    var label: String,
    var value: String,
//    var cameFrom: String,
//    var docId: Long = 0,
    var verificationStatus: ArrayList<String>
)