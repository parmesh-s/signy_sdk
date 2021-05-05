package io.signy.signysdk.others.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import io.signy.signysdk.Constant.CACHE_FOLDER_PREFIX
import io.signy.signysdk.Constant.DOC_FILE_NAME
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow


fun moveFile(
    appFolderPath: String,
    oldPath: String,
    folderName: String,
    filePrefix: String
): String {
    val filename = oldPath.substring(oldPath.lastIndexOf("/") + 1)
    val newFile = dirChecker("$appFolderPath/$folderName") + "/" + filePrefix + filename
    return mvFile(oldPath, newFile)
}

fun copyFile(target: String, destination: String): String {
    try {
        val desFile = File(destination)
        desFile.createNewFile()
        return File(target).copyTo(desFile, true).path

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""

}

public fun mvFile(oldPath: String, newPath: String): String {
    val from = File(oldPath)
    val to = File(newPath)
    from.renameTo(to)
    return to.path
}

fun getRealPathFromURICamera(c: Context, contentURI: Uri): String? {
    val result: String?
    val cursor = c.contentResolver.query(contentURI, null, null, null, null)
    if (cursor == null) {
        result = contentURI.path
    } else {
        cursor.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        result = cursor.getString(idx)
        cursor.close()
    }
    return result
}

fun getTextFromFile(filePath: String): String {
    return File(filePath).readText()
}


fun createTxtFile(appFolder: File, folderName: String, fileName: String, data: String): String? {
    var appFolder = appFolder
    var folderName = folderName
    try {
        folderName = appFolder.path + "/" + folderName
        dirChecker(folderName)
        appFolder = File(folderName, fileName)
        if (!appFolder.exists()) {
            appFolder.createNewFile()
        }
        appFolder.writeText(data)
        return appFolder.path
    } catch (ignored: IOException) {
    }

    return null
}


fun getRealPathFromURIDevice(c: Context, contentURI: Uri): String {


    var filePath = ""
    val wholeID = DocumentsContract.getDocumentId(contentURI)
    // Split at colon, use second item in the array
    val id: String
    id = if (wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size > 1)
        wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
    else
        wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    val column = arrayOf(MediaStore.Images.Media.DATA)
    // where id is equal to
    val sel = MediaStore.Images.Media._ID + "=?"
    val cursor = c.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        column, sel, arrayOf(id), null
    )
    val columnIndex = cursor!!.getColumnIndex(column[0])
    if (cursor.moveToFirst()) {
        filePath = cursor.getString(columnIndex)
    }
    cursor.close()


    return filePath
}


fun dirChecker(_targetLocation: String): String {
    val file = File(_targetLocation)
    if (!file.exists())
        file.mkdirs()
    return file.path
}


fun saveBitmapToFile(appFolderPath: String, imgName: String, bitmap: Bitmap): File? {
    try {
        dirChecker(appFolderPath)

        val desFile = File("$appFolderPath/$imgName.jpg")
        desFile.createNewFile()

        val stream = FileOutputStream(desFile)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return desFile
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return null
}


fun reduceImageSize(f: File): ByteArray {
    var b: Bitmap? = null
    //Decode image size
    val o = BitmapFactory.Options()
    o.inJustDecodeBounds = true
    var fis: FileInputStream?
    try {
        fis = FileInputStream(f)
        BitmapFactory.decodeStream(fis, null, o)
        fis.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val IMAGE_MAX_SIZE = 1300
    var scale = 1
    if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
        scale = 2.0.pow(
            ceil(
                ln(
                    IMAGE_MAX_SIZE / o.outHeight.coerceAtLeast(o.outWidth).toDouble()
                ) / ln(0.5)
            ).toInt().toDouble()
        ).toInt()
    }
    //Decode with inSampleSize
    val o2 = BitmapFactory.Options()
    o2.inSampleSize = scale
    try {
        fis = FileInputStream(f)
        b = BitmapFactory.decodeStream(fis, null, o2)
        fis.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
//
//    val destFile = File(
//        appFolder, prefix
//                + getTimestamp() + ".jpeg"
//    )
    val baos = ByteArrayOutputStream()
    try {
//        val out = FileOutputStream(destFile)

        b!!.compress(Bitmap.CompressFormat.JPEG, 90, baos)

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return baos.toByteArray()
}


fun saveByteToFile(appFolder: File, bytes: ByteArray, prefix: String = "img_"): File {

    val destFile = File(
        appFolder, prefix
                + getTimestamp() + ".jpeg"
    )
    val bos = BufferedOutputStream(FileOutputStream(destFile))
    bos.write(bytes)
    bos.flush()
    bos.close()
    return destFile
}

fun getRotatedFilePath(appFolderPath: String, imageFile: File): File {
    try {
        val desFile = File(appFolderPath + "/" + CACHE_FOLDER_PREFIX + "/" + imageFile.name)
        desFile.createNewFile()

        var photoBitmap = BitmapFactory.decodeFile(imageFile.path)
        val stream = FileOutputStream(desFile)

        val imageRotation = getImageRotation(imageFile)

        if (imageRotation != 0)
            photoBitmap = getBitmapRotatedByDegree(photoBitmap, imageRotation)

        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

        return desFile
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return imageFile
}


fun deleteFile(filePath: String) {

    try {
        val f = File(filePath)
        if (f.exists()) {
            deleteRecursive(f)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun deleteRecursive(fileOrDirectory: File) {
    if (fileOrDirectory.isDirectory)
        for (child in fileOrDirectory.listFiles()!!)
            deleteRecursive(child)

    fileOrDirectory.delete()
}

private fun getImageRotation(imageFile: File): Int {

    var exif: ExifInterface? = null
    var exifRotation = 0

    try {
        exif = ExifInterface(imageFile.path)
        exifRotation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return if (exif == null)
        0
    else
        exifToDegrees(exifRotation)
}

private fun exifToDegrees(rotation: Int): Int {
    return when (rotation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

}

fun getBitmapRotatedByDegree(bitmap: Bitmap, rotationDegree: Int): Bitmap {
    val matrix = Matrix()
    matrix.preRotate(rotationDegree.toFloat())

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}


fun createTxtFolderOfDocument(
    file: File,
    documentName: String,
    extractedFields: HashMap<String, String>,
    documentImagePaths: HashMap<String, String>
): String? {
    val docJson = JSONObject()
    docJson.put("label", documentName);

    val docFieldJson = JSONArray()
    extractedFields.keys.forEach {
        val field = JSONObject()
        field.put("label", it)
        field.put("value", extractedFields[it])
        docFieldJson.put(field)
    }
    docJson.put("fields", docFieldJson);

    val imageArray = JSONArray()
    documentImagePaths.keys.forEach {
        val rotatedFile =
            getRotatedFilePath(file.path, File(documentImagePaths[it]))
        val reducedImagePath = reduceImageSize(rotatedFile)
        val field = JSONObject()
        field.put("label", it)
        field.put("image", Base64.encodeToString(reducedImagePath, Base64.DEFAULT))
        imageArray.put(field)
    }
    docJson.put("images", imageArray);

    return createTxtFile(
        file,
        documentName,
        DOC_FILE_NAME, docJson.toString()
    )

}