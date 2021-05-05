package io.signy.signysdk.others.interfaces

interface OnFaceMatchComplete {
    fun onComplete(b: Boolean);
    fun onFail()
}