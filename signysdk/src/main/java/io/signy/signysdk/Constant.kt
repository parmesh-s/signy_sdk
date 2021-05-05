package io.signy.signysdk

import org.json.JSONObject

object Constant {
    @JvmStatic
    var CACHE_FOLDER_PREFIX = "cache"
    const val DOC_FILE_NAME: String = "doc.txt"

    const val FACE_MATCH_URL = "https://facematch.signy.io:4001/facematch"



    val OCR_URL = "https://ocr.signy.io/"
    var API_KEY = ""

//    LIB CONFIGS
   var SHOW_CONFIRM_DOCUMENT_FIELD_SCREEN:Boolean = true;


    var DOC_JSON:JSONObject? = null
}