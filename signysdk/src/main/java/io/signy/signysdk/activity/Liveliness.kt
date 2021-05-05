package io.signy.signysdk.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import io.signy.signysdk.Constant
import io.signy.signysdk.R
import io.signy.signysdk.SignySDK
import io.signy.signysdk.others.helpers.getBitmapRotatedByDegree
import io.signy.signysdk.others.helpers.saveBitmapToFile
import io.signy.signysdk.others.helpers.showToast
import io.signy.signysdk.others.interfaces.DialogListener
import io.signy.signysdk.others.interfaces.OnFaceMatchComplete
import io.signy.signysdk.others.interfaces.PermissionListener
import io.signy.signysdk.others.utils.faceDetector.MyFaceDetector
import io.signy.signysdk.others.utils.faceDetector.OnBitmapReceived
import io.signy.signysdk.others.views.DialogSuccessORFail
import kotlinx.android.synthetic.main.signy_sdk_activity_liveliness.*
import java.io.File
import java.io.IOException

class Liveliness : BaseActivity(), OnBitmapReceived {

    companion object {
        private const val REQUEST_CODE = 202;
        var INTENT_FILE_PATH = "INTENT_FILE_PATH"
        var RESULT_FACE_MATCH_SUCCESS = "RESULT_FACE_MATCH_SUCCESS"
        fun startActivityForResult(a: Activity, faceFilePath: String? = null) {
            val i = Intent(a, Liveliness::class.java)
            if (faceFilePath != null)
                i.putExtra(INTENT_FILE_PATH, faceFilePath)
            a.startActivityForResult(
                i, REQUEST_CODE
            )
        }
    }

    override fun bitmapReceived(bitmap: Bitmap) {
        frameBitmap = bitmap
    }

    val handler = Handler()
    private lateinit var frameBitmap: Bitmap
    private var leftEyeOpenProbability = -1.0
    private var rightEyeOpenProbability = -1.0
    private var blinkCount = 0
    private var dialogSuccessORFail: DialogSuccessORFail? = null
    private var mCameraSource: CameraSource? = null

    private var isFaceMatchSuccess: Boolean? = null
    private val maxEyeCloseProbabilityLimit = 0.3
    private var isLivelinessStart = false
    val faceImagePaths = mutableListOf<String>()
    private val RC_HANDLE_GMS = 9001
    var intentFacePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signy_sdk_activity_liveliness)
        intentFacePath = intent.getStringExtra(INTENT_FILE_PATH);
        if (intentFacePath == null)
            isFaceMatchSuccess = false
        setUpSuccessDialog()

        btnStart.setOnClickListener {
            isLivelinessStart = true
            btnStart.visibility = View.GONE
            tvTitleMsg.text = getString(R.string.signy_sdk_blink_msg)
            tvBlinkCount.text = "$blinkCount / 3"
            tvBlinkCount.visibility = View.VISIBLE

        }

        if (checkAndRequestPermission(arrayOf(Manifest.permission.CAMERA),
                object : PermissionListener {
                    override fun OnPermissionApprove() {
                        createCameraSource()
                    }
                })
        )
            createCameraSource()


    }


    private fun setUpSuccessDialog() {
        if (dialogSuccessORFail == null) {
            dialogSuccessORFail = DialogSuccessORFail(this, object : DialogListener {
                override fun onOk(msg: String) {

                    val intent = Intent()
                    intent.putExtra(RESULT_FACE_MATCH_SUCCESS, isFaceMatchSuccess)
                    setResult(REQUEST_CODE, intent)
                    finish()//finishing activity


                }

                override fun onCancel() {
                    isFaceMatchSuccess = null
                    blinkCount = 0
                    tvBlinkCount.text = "$blinkCount / 3"
                    faceImagePaths.clear()
                    startCameraSource()
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        this.mPreview?.stop()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        startCameraSource()
        faceImagePaths.clear()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCameraSource != null) {
            mCameraSource!!.release()
        }
    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the qrCodeData detector to detect small barcodes
     * at long distances.
     */
    private fun createCameraSource() {

        val context = applicationContext
        val detector = FaceDetector.Builder(context)
            .setProminentFaceOnly(true)

            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build()

        val myFaceDetector = MyFaceDetector(detector, this)
        myFaceDetector.setProcessor(
            MultiProcessor.Builder<Face>(GraphicFaceTrackerFactory())
                .build()
        )

        if (!myFaceDetector.isOperational) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w("FaceTAg", "Face detector dependencies are not yet available.")
        }
        val displayMetrics = DisplayMetrics()

        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        mCameraSource = CameraSource.Builder(context, myFaceDetector)

            .setRequestedPreviewSize(width, height)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedFps(30.0f)
            .build()
    }


    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {

        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg =
                GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (mCameraSource != null) {
            try {
                mPreview?.start(mCameraSource)
            } catch (e: IOException) {
                Log.e("FragmentActivity.TAG", "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }

        }
    }


    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private inner class GraphicFaceTrackerFactory : MultiProcessor.Factory<Face> {

        override fun create(face: Face): Tracker<Face> {
            return GraphicFaceTracker()
        }
    }


    private fun showResult() {
        try {
            if (isFaceMatchSuccess != null && blinkCount > 2) {
//                Saving User Profile Link
                runOnUiThread {
                    pb.visibility = View.GONE
//                            hud?.dismiss()

                    var message = "Liveliness Test Complete"
                    var status = 1
                    dialogSuccessORFail!!.show()
                    if (intentFacePath != null) {
                        status = 0
                        message = "Face does not match with document"
                        if (isFaceMatchSuccess!!) {
                            status = 1
                            message = "Face match with document"
                        }
                    }

//                if (livelinessSuccess!! && facematchSuccess!!) {
//                    status = 1
//                    message = "Face match with document \n Liveliness test Pass"
//                } else {
//                    if (livelinessSuccess!! && !facematchSuccess!!) {
//                        status = 0
//                        message = "Face does not match with document \n Liveliness test Pass"
//                    } else if (!livelinessSuccess!! && facematchSuccess!!) {
//                        status = 0
//                        message = "Face match with document \n Liveliness test Fail"
//                    }
//                }
                    dialogSuccessORFail!!.setContent(status, message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private inner class GraphicFaceTracker internal constructor() :
        Tracker<Face>() {


        /**
         * Start tracking the detected face instance within the face overlay.
         */
        override fun onNewItem(faceId: Int, item: Face) {

        }


        override fun onUpdate(detectionResults: Detector.Detections<Face>, face: Face) {

            if (isLivelinessStart && isEyeBlinked(face) && blinkCount < 3) {
                blinkCount++
                tvBlinkCount.text = "$blinkCount / 3"

                //                    Capturing 3 image after first blink
                if (blinkCount == 1) {

                    var finalI = 0
                    handler.postDelayed(object : Runnable {

                        override fun run() {
                            val imgUrl = saveBitmapToFile(
                                getAppFolder().path,

                                "ProfileView_$finalI",
                                getBitmapRotatedByDegree(frameBitmap, -90)
                            )!!.path
                            finalI++
                            faceImagePaths.add(imgUrl)

                            if (finalI < 3) {
                                handler.postDelayed(this, 500)
                            } else if (intentFacePath != null)
                                runOnUiThread {
                                    aiMatch()
                                }
                        }


                    }, 700)
                } else if (blinkCount > 2) {
                    runOnUiThread {
                        pb.visibility = View.VISIBLE
                        mPreview?.stop()
                    }

                    showResult()
                }
            }
        }

        private fun aiMatch() {
            SignySDK.compareFaces(
                this@Liveliness,
                Constant.API_KEY,
                File(intentFacePath),
                File(faceImagePaths[0]),
                object : OnFaceMatchComplete {
                    override fun onComplete(b: Boolean) {
                        isFaceMatchSuccess = b
                        showResult()
                    }

                    override fun onFail() {
                        showToast("Error while comparing face")
                    }
                })
        }


        private fun isEyeBlinked(face: Face): Boolean {
            val currentLeftEyeOpenProbability = face.isLeftEyeOpenProbability
            val currentRightEyeOpenProbability = face.isRightEyeOpenProbability
//            Log.v("Right Eye",currentRightEyeOpenProbability.toString());
//            Log.v("Left Eye",currentLeftEyeOpenProbability.toString());

            if (currentLeftEyeOpenProbability.toDouble() == -1.0 || currentRightEyeOpenProbability.toDouble() == -1.0) {
                return false
            }

            return if (leftEyeOpenProbability > 0.9 || rightEyeOpenProbability > 0.9) {
                var blinked = false
                if (currentLeftEyeOpenProbability < maxEyeCloseProbabilityLimit && currentRightEyeOpenProbability < maxEyeCloseProbabilityLimit) {
                    blinked = true
                }
                leftEyeOpenProbability = currentLeftEyeOpenProbability.toDouble()
                rightEyeOpenProbability = currentRightEyeOpenProbability.toDouble()
                blinked
            } else {
                leftEyeOpenProbability = currentLeftEyeOpenProbability.toDouble()
                rightEyeOpenProbability = currentRightEyeOpenProbability.toDouble()
                false
            }
        }


        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        override fun onMissing(detectionResults: Detector.Detections<Face>) {

        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        override fun onDone() {

        }
    }


}
