package com.zendalona.mathmantra.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zendalona.mathmantra.domain.Feedback
import com.zendalona.mathmantra.resources.Res
import com.zendalona.mathmantra.resources.continue_label
import com.zendalona.mathmantra.resources.ic_check
import com.zendalona.mathmantra.resources.ic_close
import com.zendalona.mathmantra.resources.try_again
import com.zendalona.mathmantra.ui.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedbackDialog(feedback: Feedback, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val extended = AppTheme.extendedColors
    val badgeColor = if (feedback.correct) extended.successContainer else colors.errorContainer
    val iconColor = if (feedback.correct) extended.success else colors.error

    val scale = remember(feedback) { Animatable(0.3f) }
    LaunchedEffect(feedback) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = colors.surfaceContainerLowest, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                        .background(badgeColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(if (feedback.correct) Res.drawable.ic_check else Res.drawable.ic_close),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(64.dp),
                    )
                }
                Text(
                    text = feedback.title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                feedback.detail?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    colors = if (feedback.correct) {
                        ButtonDefaults.buttonColors(containerColor = extended.success, contentColor = extended.onSuccess)
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                ) {
                    Text(
                        text = stringResource(if (feedback.correct) Res.string.continue_label else Res.string.try_again),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
