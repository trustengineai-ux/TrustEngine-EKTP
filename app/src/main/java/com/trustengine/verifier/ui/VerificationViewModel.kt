package com.trustengine.verifier.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trustengine.verifier.domain.model.EKTPData
import com.trustengine.verifier.domain.model.VerificationResult
import com.trustengine.verifier.domain.model.CaptureData
import com.trustengine.verifier.domain.model.CertificateData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor() : ViewModel() {
    
    private val _currentStep = MutableStateFlow(VerificationStep.HOME)
    val currentStep: StateFlow<VerificationStep> = _currentStep.asStateFlow()
    
    private val _ektpData = MutableStateFlow<EKTPData?>(null)
    val ektpData: StateFlow<EKTPData?> = _ektpData.asStateFlow()
    
    private val _captureData = MutableStateFlow(CaptureData())
    val captureData: StateFlow<CaptureData> = _captureData.asStateFlow()
    
    private val _verificationResult = MutableStateFlow<VerificationResult?>(null)
    val verificationResult: StateFlow<VerificationResult?> = _verificationResult.asStateFlow()
    
    private val _certificateData = MutableStateFlow<CertificateData?>(null)
    val certificateData: StateFlow<CertificateData?> = _certificateData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    fun startVerification() {
        _currentStep.value = VerificationStep.NFC_SCAN
        resetData()
    }
    
    fun setEKTPData(data: EKTPData) {
        _ektpData.value = data
        _currentStep.value = VerificationStep.CAMERA_CAPTURE
    }
    
    fun setCaptureData(data: CaptureData) {
        _captureData.value = data
        _currentStep.value = VerificationStep.VERIFICATION
    }
    
    fun setVerificationResult(result: VerificationResult) {
        _verificationResult.value = result
        if (result.isVerified) {
            _currentStep.value = VerificationStep.CERTIFICATE
        } else {
            _errorMessage.value = result.message
        }
    }
    
    fun setCertificateData(data: CertificateData) {
        _certificateData.value = data
    }
    
    fun goToStep(step: VerificationStep) {
        _currentStep.value = step
    }
    
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    fun setError(message: String?) {
        _errorMessage.value = message
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    private fun resetData() {
        _ektpData.value = null
        _captureData.value = CaptureData()
        _verificationResult.value = null
        _certificateData.value = null
        _errorMessage.value = null
    }
    
    fun reset() {
        resetData()
        _currentStep.value = VerificationStep.HOME
    }
}

enum class VerificationStep {
    HOME,
    NFC_SCAN,
    CAMERA_CAPTURE,
    VERIFICATION,
    CERTIFICATE
}