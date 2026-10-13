package com.zendalona.mathmantra.ui.counting

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zendalona.mathmantra.domain.shakes
import com.zendalona.mathmantra.platform.LocalAppServices
import com.zendalona.mathmantra.resources.Res
import com.zendalona.mathmantra.resources.activity_bell
import com.zendalona.mathmantra.resources.activity_drum
import com.zendalona.mathmantra.resources.bell
import com.zendalona.mathmantra.resources.bell_button
import com.zendalona.mathmantra.resources.bell_shake_hint
import com.zendalona.mathmantra.resources.bell_tap_hint
import com.zendalona.mathmantra.resources.counting_progress
import com.zendalona.mathmantra.resources.drum
import com.zendalona.mathmantra.resources.drum_button
import com.zendalona.mathmantra.resources.drum_tap_hint
import com.zendalona.mathmantra.ui.components.FeedbackDialog
import com.zendalona.mathmantra.ui.components.GameScaffold
import com.zendalona.mathmantra.ui.components.HearAgainButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CountingGameScreen(instrument: Instrument, onBack: () -> Unit) {
    val services = LocalAppServices.current
    val viewModel = viewModel(key = instrument.name) {
        CountingGameViewModel(instrument, services.speaker, services.sounds)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shakeToPlay = instrument == Instrument.Bell && services.motion.isAvailable

    if (shakeToPlay) {
        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                services.motion.accelerometer().shakes().collect { viewModel.hit() }
            }
        }
    }

    val title = if (instrument == Instrument.Bell) Res.string.activity_bell else Res.string.activity_drum
    val hint = when {
        instrument == Instrument.Drum -> Res.string.drum_tap_hint
        shakeToPlay -> Res.string.bell_shake_hint
        else -> Res.string.bell_tap_hint
    }

    GameScaffold(title = stringResource(title), onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.prompt,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f, fill = false).semantics { liveRegion = LiveRegionMode.Polite },
                )
                Spacer(Modifier.size(8.dp))
                HearAgainButton(onClick = viewModel::speakPrompt)
            }

            Counter(count = state.count, target = state.target)

            InstrumentPad(
                instrument = instrument,
                hits = state.count,
                tappable = !shakeToPlay,
                onTap = viewModel::hit,
            )

            Text(
                text = stringResource(hint),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }

    state.feedback?.let { FeedbackDialog(it, onDismiss = viewModel::nextRound) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Counter(count: Int, target: Int) {
    val progress = stringResource(Res.string.counting_progress, count, target)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = progress
            liveRegion = LiveRegionMode.Polite
        },
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(target) { index ->
                val filled = index < count
                val color by animateColorAsState(
                    if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                )
                Box(Modifier.size(16.dp).clip(CircleShape).background(color))
            }
        }
    }
}

@Composable
private fun InstrumentPad(instrument: Instrument, hits: Int, tappable: Boolean, onTap: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(hits) {
        if (hits == 0) return@LaunchedEffect
        when (instrument) {
            Instrument.Bell -> rotation.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 600
                    18f at 80
                    -14f at 200
                    9f at 320
                    -5f at 440
                },
            )
            Instrument.Drum -> {
                scale.snapTo(0.86f)
                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
            }
        }
    }

    val label = stringResource(if (instrument == Instrument.Bell) Res.string.bell_button else Res.string.drum_button)
    val image = if (instrument == Instrument.Bell) Res.drawable.bell else Res.drawable.drum

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(240.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = if (tappable) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClickLabel = label,
                    onClick = onTap,
                ).semantics { contentDescription = label }
            } else {
                Modifier
            },
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier
                    .padding(36.dp)
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = rotation.value
                        scaleX = scale.value
                        scaleY = scale.value
                        // The bell swings from its handle; the drum squashes about its centre.
                        transformOrigin = if (instrument == Instrument.Bell) TransformOrigin(0.5f, 0.1f) else TransformOrigin.Center
                    },
            )
        }
    }
}
