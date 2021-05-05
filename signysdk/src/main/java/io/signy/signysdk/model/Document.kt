package io.signy.signysdk.model

class Document(
    var name: String,
    var country: String,
    var type: String = "kyc",
    var key: String = ""

) {
    init {
        this.key = type.trim().toLowerCase().replace(" ", "_") + name.trim().toLowerCase().replace(
            " ",
            "_"
        ) + country.trim().toLowerCase().replace(" ", "_")
    }
}