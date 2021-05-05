### Features

- Document OCR
- Face Match
- Liveliness Test

## SIGNY SDK
                

### Dependency

Include the library in your App Gradle

    dependencies {
    	implementation 'com.signy_test:signy_sdk_test:1.4'
	}
    
Add maven in Project Gradle 

	repositories {
    	maven {
        	    url  "https://dl.bintray.com/signy/Signy_SDK/"
        	}
	}

### Get Document With OCR
	 SignySDK.getDocument(activity,--API_TOKEN--);

For get document back add onActivityResult method

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === SelectDocumentActivity.REQUEST_CODE && data != null) {
            val json = SignySDK.getDocJson(data.getStringExtra(SelectDocumentActivity.RESULT_DOC_DATA))
        }
    }

### Face Match 

	try {
                    SignySDK.compareFaces(Activity,--API_TOKEN--, ImageFile, ImageFile, object : OnFaceMatchComplete {
                        override fun onComplete(isFaceMatch: Boolean) {
                        }
                        override fun onFail() {
                        }
                    })
                } catch (e: Exception) {
				//handle Exception Message here
                }

### Liveliness 

	 SignySDK.startLiveliness(Activity)

For get liveliness result back add onActivityResult method

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === Liveliness.REQUEST_CODE && data != null) {
        val result =  data.getBooleanExtra(Liveliness.RESULT_FACE_MATCH_SUCCESS)
        }
    }
