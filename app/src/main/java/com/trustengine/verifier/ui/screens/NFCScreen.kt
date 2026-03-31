package com.trustengine.verifier.ui.screens

import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trustengine.verifier.R
import com.trustengine.verifier.domain.model.EKTPData
import com.trustengine.verifier.nfc.NFCEKTPReader
import com.trustengine.verifier.ui.theme.SuccessGreen
import com.trustengine.verifier.ui.theme.TrustEngineAccent
import com.trustengine.verifier.ui.theme.TrustEngineDarkBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCScreen(
    nfcReader: NFCEKTPReader,
    onEKTPRead: (EKTPData) -> Unit,
    onBack: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isReading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var nfcEnabled by remember { mutableStateOf(true) }
    
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    
    // Check NFC status
    LaunchedEffect(Unit) {
        if (!nfcReader.isNFCSupported(nfcAdapter)) {
            onError(context.getString(R.string.nfc_not_supported))
            return@LaunchedEffect
        }
        nfcEnabled = nfcReader.isNFCEnabled(nfcAdapter)
        if (!nfcEnabled) {
            onError(context.getString(R.string.nfc_disabled))
        }
    }
    
    // Pulsing animation for NFC icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nfc_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // NFC Icon with animation
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(if (isReading) scale else 1f)
                        .background(
                            color = if (isSuccess) {
                                SuccessGreen.copy(alpha = 0.1f)
                            } else {
                                TrustEngineAccent.copy(alpha = 0.1f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "NFC",
                        modifier = Modifier.size(100.dp),
                        tint = if (isSuccess) SuccessGreen else TrustEngineDarkBlue
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Status Text
                Text(
                    text = when {
                        isSuccess -> stringResource(R.string.nfc_success)
                        isReading -> stringResource(R.string.nfc_reading)
                        else -> stringResource(R.string.nfc_instruction)
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isSuccess -> SuccessGreen
                            isReading -> TrustEngineAccent
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subtitle
                if (!isSuccess) {
                    Text(
                        text = "Hold your e-KTP card against the back of your phone",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Manual read button for testing
                if (!isReading && !isSuccess) {
                    Button(
                        onClick = {
                            scope.launch {
                                isReading = true
                                // Simulate reading for demo
                                kotlinx.coroutines.delay(2000)
                                
                                // Create mock data for testing
                                val mockData = EKTPData(
                                    nik = "3175091209870001",
                                    name = "JOHN DOE",
                                    placeOfBirth = "JAKARTA",
                                    dateOfBirth = "12-09-1987",
                                    gender = "LAKI-LAKI",
                                    address = "JL. SUDIRMAN NO. 123",
                                    village = "KEBON MELATI",
                                    district = "TANAH ABANG"
                                )
                                
                                isReading = false
                                isSuccess = true
                                kotlinx.coroutines.delay(500)
                                onEKTPRead(mockData)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TrustEngineDarkBlue
                        )
                    ) {
                        Text(
                            text = "Simulate NFC Read (Demo)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                // Progress indicator
                if (isReading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        color = TrustEngineAccent
                    )
                }
            }
        }
    }
}