package io.signy.signysdk.activity.addDocument.fieldExtractor

object Fields {
    var panCardFields: List<String> = listOf("Name", "Pancard", "Date of Birth")
    var aadhaarCardFields: List<String> = listOf("Name", "Date of Birth", "Gender", "Aadhaar No")
    var emiratesIDFIelds: List<String> = listOf(
        "Name",
        "Date of Birth",
        "Gender",
        "Emirates Card No",
        "Nationality",
        "Emirates Id Expiry Date"
    )
    var passportFields: List<String> = listOf(
        "Name",
        "Passport Number",
        "Date of Birth",
        "Nationality",
//        "Passport Issue Date",
        "Passport Expiry Date",
        "Gender"
    )
    var licenceFields: List<String> = listOf(
        "Name",
        "Date of Birth",
        "DL No",
        "Issue Date",
        "Expiry Date"
    )

    var voterId: List<String> = listOf(
        "Voter ID",
        "Name"
    )

}
