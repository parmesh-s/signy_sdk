package io.signy.signysdk.others.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast

import org.json.JSONObject
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


fun getTimestamp(): Long {
    return Date().time
}
@Throws(ParseException::class)
fun formatYYMMDDDate(string: String): String {
    try {
        Log.v("OCCCRString", string)
        val format = SimpleDateFormat("dd/MM/yyyy")
        val year = Integer.parseInt(string.substring(0, 2))
        var finalYear = "20" + string.substring(0, 2)

        if (year > Date().year - 90) {
            finalYear = "19" + string.substring(0, 2)
        }
        Log.v("OCCCRFinalYear", finalYear)
        val Dob = format.parse(
            string.substring(4, 6) + "/" + string.substring(
                2,
                4
            ) + "/" + finalYear
        )
        return getDDMMYYYDate(Dob)
    } catch (e: Exception) {

    }

    return ""
}

fun getDDMMYYYDate(date: Date): String {
    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(date)
}


fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun getDDMMYYYDate(date: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(date)
}

fun getFormJson(): JSONObject {
    val formJSON = "{ \n" +
            "             \"metadata\":{ \n" +
            "                \"id\":\"cHhKQSts\",\n" +
            "                \"name\":\"Celebx\",\n" +
            "                \"version\":\"0.0.2\"\n" +
            "             },\n" +
            "             \"pages\":[ \n" +
            "                { \n" +
            "                   \"metadata\":{ \n" +
            "                      \"lastUpdated\":\"Sat Mar 16 2019 16:23:16 GMT0530 (India Standard Time)\"\n" +
            "                   },\n" +
            "                   \"fields\":[ \n" +
            "                        { \n" +
            "                         \"element\":\"paragraph\",\n" +
            "                         \"text\":\"Please upload your identity Document for verify your account\",\n" +
            "                         \"style\":{\n" +
            "                             \"fontSize\":14\n" +
            "                         }\n" +
            "                      },\n" +
            "                     { \n" +
            "                         \"element\":\"input\",\n" +
            "                         \"type\":\"file\",\n" +
            "                         \"label\":\"Identity Document\",\n" +
            "                         \"name\":\"identity_document\",\n" +
            "                         \"docType\":\"kyc\",\n" +
            "                         \"required\":true,\n" +
            "                         \"placeholder\":\"Identity Document\"\n" +
            "                      }\n" +
            "                   ] \n" +
            "                }\n" +
            "             ] \n" +
            "            }"
    return JSONObject(formJSON)
}


fun StringCapitalize(string: String): String {
    return string.substring(0, 1).toUpperCase() + string.substring(1)
}


fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

//
//fun prepareFilePart(partName: String, fileUri: String): MultipartBody.Part {
//    // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
//    // use the FileUtils to get the actual file by uri
//    val file = File(fileUri)
//
//    // create RequestBody instance from file
//    val requestFile = RequestBody.create(
//        MediaType.parse("multipart/form-data"),
//        file
//    )
//
//    // MultipartBody.Part is used to send also the actual file name
//    return MultipartBody.Part.createFormData(partName, file.name, requestFile)
//}