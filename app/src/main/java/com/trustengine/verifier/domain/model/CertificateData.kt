package com.trustengine.verifier.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CertificateData(
    val certificateId: String,
    val verificationId: String,
    val ektpData: EKTPData,
    val verificationResult: VerificationResult,
    val ktpPhotoUri: Uri?,
    val selfieUri: Uri?,
    val generatedAt: Long = System.currentTimeMillis(),
    val pdfUri: Uri? = null
) : Parcelable