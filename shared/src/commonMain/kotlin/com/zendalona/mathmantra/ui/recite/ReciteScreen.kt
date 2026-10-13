package com.zendalona.mathmantra.ui.recite

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zendalona.mathmantra.platform.LocalAppServices
import com.zendalona.mathmantra.resources.Res
import com.zendalona.mathmantra.resources.activity_recite
import com.zendalona.mathmantra.resources.ic_mic
import com.zendalona.mathmantra.resources.recite_allow
import com.zendalona.mathmantra.resources.recite_listening
import com.zendalona.mathmantra.resources.recite_permission_denied
import com.zendalona.mathmantra.resources.recite_start
import com.zendalona.mathmantra.resources.recite_unavailable
import com.zendalona.mathmantra.resources.recite_you_said
import com.zendalona.mathmantra.ui.components.FeedbackDialog
import com.zendalona.mathmantra.ui.components.GameScaffold
import com.zendalona.mathmantra.ui.components.HearAgainButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReciteScreen(onBack: () -> Unit) {
    val services = LocalAppServices.current
    val viewModel = viewModel { ReciteViewModel(services.speaker, services.speech) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    GameScaffold(title = stringResource(Res.string.activity_recite), onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = state.prompt,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f, fill = false).semantics { liveRegion = LiveRegionMode.Polite },
                )
                Spacer(Modifier.size(8.dp))
                HearAgainButton(onClick = viewModel::speakPrompt)
            }

            when (state.status) {
                MicStatus.Unavailable -> Notice(stringResource(Res.string.recite_unavailable))
                MicStatus.PermissionDenied -> Notice(stringResource(Res.string.recite_permission_denied)) {
                    Button(onClick = viewModel::listen) { Text(stringResource(Res.string.recite_allow)) }
                }
                MicStatus.Ready, MicStatus.Listening -> MicButton(
                    listening = state.status == MicStatus.Listening,
                    onClick = viewModel::listen,
                )
            }

            state.heard?.let {
                Text(
                    text = stringResource(Res.string.recite_you_said, it),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    state.feedback?.let { FeedbackDialog(it, onDismiss = viewModel::dismissFeedback) }
}

@Composable
private fun MicButton(listening: Boolean, onClick: () -> Unit) {
    val pulse = rememberInfiniteTransition()
    val halo by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
    )
    val label = stringResource(if (listening) Res.string.recite_listening else Res.string.recite_start)

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            if (listening) {
                Box(
                    Modifier
                        .size(140.dp)
                        .graphicsLayer { scaleX = halo; scaleY = halo; alpha = 1.6f - halo }
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                )
            }
            LargeFloatingActionButton(
                onClick = onClick,
                shape = CircleShape,
                containerColor = if (listening) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primary,
                contentColor = if (listening) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(140.dp),
            ) {
                Icon(painterResource(Res.drawable.ic_mic), contentDescription = label, modifier = Modifier.size(64.dp))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

@Composable
private fun Notice(message: String, action: @Composable () -> Unit = {}) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            action()
        }
    }
}
