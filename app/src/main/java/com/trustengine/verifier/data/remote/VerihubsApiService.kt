package com.trustengine.verifier.data.remote

import com.trustengine.verifier.domain.model.VerificationResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VerihubsApiService {
    
    @Multipart
    @POST("v1/identity/verify")
    suspend fun verifyIdentity(
        @Header("Authorization") apiKey: String,
        @Part("nik") nik: RequestBody,
        @Part("name") name: RequestBody,
        @Part ktpImage: MultipartBody.Part,
        @Part selfieImage: MultipartBody.Part
    ): Response<VerihubsVerificationResponse>
    
    @Multipart
    @POST("v1/face/compare")
    suspend fun compareFaces(
        @Header("Authorization") apiKey: String,
        @Part image1: MultipartBody.Part,
        @Part image2: MultipartBody.Part
    ): Response<VerihubsFaceCompareResponse>
    
    @Multipart
    @POST("v1/liveness/check")
    suspend fun checkLiveness(
        @Header("Authorization") apiKey: String,
        @Part selfieImage: MultipartBody.Part
    ): Response<VerihubsLivenessResponse>
}

data class VerihubsVerificationResponse(
    val success: Boolean,
    val data: VerificationData?,
    val message: String?
)

data class VerificationData(
    val verificationId: String,
    val status: String,
    val confidenceScore: Float,
    val faceMatchScore: Float,
    val livenessScore: Float,
    val documentAuthenticity: Float,
    val riskLevel: String,
    val timestamp: Long
)

data class VerihubsFaceCompareResponse(
    val success: Boolean,
    val data: FaceCompareData?,
    val message: String?
)

data class FaceCompareData(
    val similarity: Float,
    val isMatch: Boolean,
    val threshold: Float
)

data class VerihubsLivenessResponse(
    val success: Boolean,
    val data: LivenessData?,
    val message: String?
)

data class LivenessData(
    val isLive: Boolean,
    val confidence: Float,
    val spoofingScore: Float
)