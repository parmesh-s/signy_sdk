package io.signy.signysdk

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.SparseArray
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.error.VolleyError
import com.android.volley.request.SimpleMultiPartRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import io.signy.signysdk.Constant.API_KEY
import io.signy.signysdk.Constant.CACHE_FOLDER_PREFIX
import io.signy.signysdk.Constant.FACE_MATCH_URL
import io.signy.signysdk.activity.Liveliness
import io.signy.signysdk.activity.addDocument.SelectDocumentActivity
import io.signy.signysdk.others.helpers.*
import io.signy.signysdk.others.interfaces.OnFaceMatchComplete
import org.json.JSONObject
import java.io.File


object SignySDK {
    public fun getDocument(a: Activity, apiKey: String) {
        API_KEY = apiKey;
        SelectDocumentActivity.startActivityForResult(a, "docType")
    }


    public fun startLiveliness(a: Activity,  apiKey: String = "",faceFilePath: File? = null) {
        API_KEY = apiKey;

        Liveliness.startActivityForResult(a, faceFilePath?.path)
    }

    public fun getDocJson(filePath: String): String {
        return getTextFromFile(filePath)
    }


    public fun compareFaces(
        a: Activity,
        apiKey: String,
        faceImage1: File,
        faceImage2: File,
        listener: OnFaceMatchComplete
    ) {

        API_KEY = apiKey;
        val byte1 = reduceImageSize(getRotatedFilePath(getAppFolder(a).path, faceImage1));


        var detector: FaceDetector? = FaceDetector.Builder(a)
            .setTrackingEnabled(false)


            .build()


        val img1Face =
            getFacesFromImage(
                a,
                detector!!,
                BitmapFactory.decodeByteArray(byte1, 0, byte1.size),
                "img1"
            )
                ?: throw java.lang.Exception("could not found face in faceImage1")

        val byte2 = reduceImageSize(getRotatedFilePath(getAppFolder(a).path, faceImage2));
        val img2Face =
            getFacesFromImage(
                a,
                detector!!,
                BitmapFactory.decodeByteArray(byte2, 0, byte2.size),
                "img2"
            )
                ?: throw java.lang.Exception("could not found face in faceImage2")

        detector.release();
        detector = null;
        val smr = object : SimpleMultiPartRequest(
            Request.Method.POST, FACE_MATCH_URL,
            Response.Listener { response: String? ->
                try {
                    val jObj = JSONObject(response)
                    listener.onComplete(true)
                } catch (e: Exception) {
                    listener.onComplete(false)
                }

            }, Response.ErrorListener { error: VolleyError ->

                listener.onComplete(false)

            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $API_KEY"
                return headers
            }
        }
        //The file param name is "profile-pic". and Content-type is form-data
        smr.addFile("file1", img1Face.path)
        smr.addFile("file2", img2Face.path)
        smr.retryPolicy = DefaultRetryPolicy(
            60 * 1000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        val mRequestQueue =
            Volley.newRequestQueue(a.applicationContext)
        mRequestQueue.add(smr);
    }

    private fun checkMaxValue(value: Float, max: Float): Float {
        return (if (value > max) max else value)
    }

    private fun checkMinValue(value: Float): Float {
        return (if (value < 0) 0f else value)
    }


    private fun getFacesFromImage(
        c: Context,
        detector: FaceDetector,
        img: Bitmap,
        imgName: String = "img1"
    ): File? {


        val frame: Frame = Frame.Builder().setBitmap(img).build()
        val faces: SparseArray<Face> = detector.detect(frame)
        if (faces.size() == 0)
            return null;
        else if (faces.size() > 1)
            throw Exception("Image contain more than 1 face")

        val face = faces[0]
        var faceImages = img;
        if (face != null) {

            val x = checkMinValue(face.position.x - 15)
            val y = checkMinValue(face.position.y - 15)
            var x2 = checkMaxValue(face.width + 30, img.width.toFloat())
            if (x2 + x > img.width) {
                x2 = face.width
            }
            var y2 = checkMaxValue(face.height + 30, img.height.toFloat())
            if (y2 + y > img.height) {
                y2 = face.height
            }


            faceImages = Bitmap.createBitmap(
                img,
                x.toInt(),
                y.toInt(),
                x2.toInt(),
                y2.toInt()
            )
        }
        return saveBitmapToFile(getAppFolder(c).path, imgName, faceImages)


    }

    private fun getAppFolder(a: Context): File {
        val f = File(dirChecker(a.filesDir.path + "/AppData"))
        dirChecker(a.filesDir.path + "/AppData/" + CACHE_FOLDER_PREFIX)
        return f

    }
}