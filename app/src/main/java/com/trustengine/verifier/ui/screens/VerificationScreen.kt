package com.trustengine.verifier.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trustengine.verifier.R
import com.trustengine.verifier.domain.model.CaptureData
import com.trustengine.verifier.domain.model.EKTPData
import com.trustengine.verifier.domain.model.VerificationResult
import com.trustengine.verifier.ui.theme.SuccessGreen
import com.trustengine.verifier.ui.theme.ErrorRed
import com.trustengine.verifier.ui.theme.TrustEngineAccent
import com.trustengine.verifier.ui.theme.TrustEngineDarkBlue
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun VerificationScreen(
    ektpData: EKTPData,
    captureData: CaptureData,
    onVerificationComplete: (VerificationResult) -> Unit,
    onRetry: () -> Unit
) {
    var verificationState by remember { mutableStateOf(VerificationState.VERIFYING) }
    var progress by remember { mutableFloatStateOf(0f) }
    
    // Simulate verification process
    LaunchedEffect(Unit) {
        // Stage 1: Document verification (0-33%)
        while (progress < 0.33f) {
            delay(50)
            progress += 0.01f
        }
        
        // Stage 2: Face comparison (33-66%)
        while (progress < 0.66f) {
            delay(50)
            progress += 0.01f
        }
        
        // Stage 3: Liveness check (66-100%)
        while (progress < 1f) {
            delay(50)
            progress += 0.01f
        }
        
        delay(500)
        
        // Mock successful verification
        val result = VerificationResult(
            isVerified = true,
            confidenceScore = 0.94f,
            message = "Identity verified successfully",
            verificationId = UUID.randomUUID().toString()
        )
        
        verificationState = VerificationState.SUCCESS
        delay(500)
        onVerificationComplete(result)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (verificationState) {
            VerificationState.VERIFYING -> {
                VerifyingContent(progress = progress)
            }
            VerificationState.SUCCESS -> {
                SuccessContent()
            }
            VerificationState.FAILED -> {
                FailedContent(onRetry = onRetry)
            }
        }
    }
}

@Composable
private fun VerifyingContent(progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated progress circle
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 8.dp,
                color = TrustEngineAccent,
                trackColor = TrustEngineAccent.copy(alpha = 0.2f)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TrustEngineDarkBlue
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = stringResource(R.string.verification_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val statusText = when {
            progress < 0.33f -> "Verifying document authenticity..."
            progress < 0.66f -> "Comparing facial features..."
            else -> "Performing liveness check..."
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Verification steps
        VerificationSteps(progress = progress)
    }
}

@Composable
private fun VerificationSteps(progress: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VerificationStepItem(
            title = "Document Verification",
            isComplete = progress >= 0.33f,
            isActive = progress < 0.33f
        )
        
        VerificationStepItem(
            title = "Face Comparison",
            isComplete = progress >= 0.66f,
            isActive = progress >= 0.33f && progress < 0.66f
        )
        
        VerificationStepItem(
            title = "Liveness Detection",
            isComplete = progress >= 1f,
            isActive = progress >= 0.66f
        )
    }
}

@Composable
private fun VerificationStepItem(
    title: String,
    isComplete: Boolean,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isComplete -> SuccessGreen
                        isActive -> TrustEngineAccent
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = when {
                    isComplete -> SuccessGreen
                    isActive -> TrustEngineDarkBlue
                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                },
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
            )
        )
    }
}

@Composable
private fun SuccessContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(SuccessGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = SuccessGreen
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.verification_success),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Generating your certificate...",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = SuccessGreen
        )
    }
}

@Composable
private fun FailedContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(ErrorRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = ErrorRed
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.verification_failed),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ErrorRed
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "We couldn't verify your identity. Please try again.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = TrustEngineDarkBlue
            )
        ) {
            Text(
                text = stringResource(R.string.retry),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

enum class VerificationState {
    VERIFYING,
    SUCCESS,
    FAILED
}