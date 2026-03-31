package com.trustengine.verifier.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CaptureData(
    val ktpPhotoUri: Uri? = null,
    val selfieUri: Uri? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable