package com.trustengine.verifier.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VerificationResult(
    val isVerified: Boolean,
    val confidenceScore: Float,
    val message: String,
    val verificationId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: VerificationDetails? = null
) : Parcelable

@Parcelize
data class VerificationDetails(
    val faceMatchScore: Float,
    val livenessScore: Float,
    val documentAuthenticity: Float,
    val riskLevel: RiskLevel
) : Parcelable

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}