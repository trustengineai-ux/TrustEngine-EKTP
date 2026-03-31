package com.trustengine.verifier

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.trustengine.verifier.domain.model.CertificateData
import com.trustengine.verifier.nfc.NFCEKTPReader
import com.trustengine.verifier.pdf.CertificatePDFGenerator
import com.trustengine.verifier.ui.VerificationStep
import com.trustengine.verifier.ui.VerificationViewModel
import com.trustengine.verifier.ui.screens.CameraScreen
import com.trustengine.verifier.ui.screens.CertificateScreen
import com.trustengine.verifier.ui.screens.HomeScreen
import com.trustengine.verifier.ui.screens.NFCScreen
import com.trustengine.verifier.ui.screens.VerificationScreen
import com.trustengine.verifier.ui.theme.TrustEngineTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: VerificationViewModel by viewModels()
    
    @Inject
    lateinit var nfcReader: NFCEKTPReader
    
    @Inject
    lateinit var pdfGenerator: CertificatePDFGenerator
    
    private var nfcAdapter: NfcAdapter? = null
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        setContent {
            TrustEngineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentStep by viewModel.currentStep.collectAsState()
                    val ektpData by viewModel.ektpData.collectAsState()
                    val captureData by viewModel.captureData.collectAsState()
                    val verificationResult by viewModel.verificationResult.collectAsState()
                    val certificateData by viewModel.certificateData.collectAsState()
                    
                    when (currentStep) {
                        VerificationStep.HOME -> {
                            HomeScreen(
                                onStartVerification = {
                                    viewModel.startVerification()
                                }
                            )
                        }
                        
                        VerificationStep.NFC_SCAN -> {
                            NFCScreen(
                                nfcReader = nfcReader,
                                onEKTPRead = { data ->
                                    viewModel.setEKTPData(data)
                                },
                                onBack = {
                                    viewModel.reset()
                                },
                                onError = { message ->
                                    viewModel.setError(message)
                                }
                            )
                        }
                        
                        VerificationStep.CAMERA_CAPTURE -> {
                            CameraScreen(
                                onCaptureComplete = { data ->
                                    viewModel.setCaptureData(data)
                                },
                                onBack = {
                                    viewModel.goToStep(VerificationStep.NFC_SCAN)
                                }
                            )
                        }
                        
                        VerificationStep.VERIFICATION -> {
                            ektpData?.let { ektp ->
                                VerificationScreen(
                                    ektpData = ektp,
                                    captureData = captureData,
                                    onVerificationComplete = { result ->
                                        viewModel.setVerificationResult(result)
                                        
                                        // Create certificate data
                                        val certData = CertificateData(
                                            certificateId = UUID.randomUUID().toString(),
                                            verificationId = result.verificationId,
                                            ektpData = ektp,
                                            verificationResult = result,
                                            ktpPhotoUri = captureData.ktpPhotoUri,
                                            selfieUri = captureData.selfieUri
                                        )
                                        viewModel.setCertificateData(certData)
                                    },
                                    onRetry = {
                                        viewModel.goToStep(VerificationStep.CAMERA_CAPTURE)
                                    }
                                )
                            }
                        }
                        
                        VerificationStep.CERTIFICATE -> {
                            certificateData?.let { cert ->
                                CertificateScreen(
                                    certificateData = cert,
                                    pdfGenerator = pdfGenerator,
                                    onNewVerification = {
                                        viewModel.reset()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Enable NFC foreground dispatch
        nfcAdapter?.let { adapter ->
            val intent = android.app.PendingIntent.getActivity(
                this,
                0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                android.app.PendingIntent.FLAG_MUTABLE
            )
            adapter.enableForegroundDispatch(this, intent, null, null)
        }
    }
    
    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        
        // Handle NFC intent
        if (intent?.action in listOf(
                NfcAdapter.ACTION_NDEF_DISCOVERED,
                NfcAdapter.ACTION_TECH_DISCOVERED,
                NfcAdapter.ACTION_TAG_DISCOVERED
            )
        ) {
            val tag: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                Log.d(TAG, "NFC Tag discovered: ${it.id?.contentToString()}")
                nfcReader.setTag(it)
                
                // Trigger NFC read
                lifecycleScope.launch {
                    nfcReader.readEKTPData()
                        .onSuccess { ektpData ->
                            viewModel.setEKTPData(ektpData)
                        }
                        .onFailure { error ->
                            viewModel.setError(error.message ?: "Failed to read e-KTP")
                        }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        nfcReader.close()
    }
}