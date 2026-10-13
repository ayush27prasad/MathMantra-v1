package com.zendalona.mathmantra.ui.numberline

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zendalona.mathmantra.domain.NumberLine
import com.zendalona.mathmantra.platform.LocalAppServices
import com.zendalona.mathmantra.resources.Res
import com.zendalona.mathmantra.resources.activity_number_line
import com.zendalona.mathmantra.resources.ic_chevron_left
import com.zendalona.mathmantra.resources.ic_chevron_right
import com.zendalona.mathmantra.resources.mole_pointing_up
import com.zendalona.mathmantra.resources.number_line_left
import com.zendalona.mathmantra.resources.number_line_position
import com.zendalona.mathmantra.resources.number_line_right
import com.zendalona.mathmantra.resources.number_line_steps_left
import com.zendalona.mathmantra.resources.number_line_steps_right
import com.zendalona.mathmantra.resources.question_prompt
import com.zendalona.mathmantra.ui.components.FeedbackDialog
import com.zendalona.mathmantra.ui.components.GameScaffold
import com.zendalona.mathmantra.ui.components.HearAgainButton
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val EdgePadding = 24.dp
private val WalkerHeight = 88.dp

@Composable
fun NumberLineScreen(onBack: () -> Unit) {
    val speaker = LocalAppServices.current.speaker
    val viewModel = viewModel { NumberLineViewModel(speaker) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val question = state.question

    GameScaffold(title = stringResource(Res.string.activity_number_line), onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${stringResource(Res.string.question_prompt)} ${question.text}?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.semantics {
                                contentDescription = question.spoken
                                liveRegion = LiveRegionMode.Polite
                            },
                        )
                        Text(
                            text = stringResource(
                                if (question.movesRight) Res.string.number_line_steps_right else Res.string.number_line_steps_left,
                                question.steps,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                    HearAgainButton(onClick = viewModel::speakQuestion)
                }
            }

            NumberLineView(state.line)

            Text(
                text = stringResource(Res.string.number_line_position, state.line.position),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                MoveButton(Res.string.number_line_left, leading = true, onClick = viewModel::stepLeft, modifier = Modifier.weight(1f))
                MoveButton(Res.string.number_line_right, leading = false, onClick = viewModel::stepRight, modifier = Modifier.weight(1f))
            }
        }
    }

    state.feedback?.let { FeedbackDialog(it, onDismiss = viewModel::dismissFeedback) }
}

@Composable
private fun MoveButton(
    label: StringResource,
    leading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Button(onClick = onClick, shape = MaterialTheme.shapes.large, modifier = modifier.height(96.dp)) {
        if (leading) Icon(painterResource(Res.drawable.ic_chevron_left), contentDescription = null, modifier = Modifier.size(32.dp))
        Text(stringResource(label), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        if (!leading) Icon(painterResource(Res.drawable.ic_chevron_right), contentDescription = null, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun NumberLineView(line: NumberLine) {
    val colors = MaterialTheme.colorScheme
    val labelStyle = MaterialTheme.typography.titleMedium
    val measurer = rememberTextMeasurer()
    val span = (line.end - line.start).toFloat()

    // A new window (after walking off an edge) starts a fresh animation instead of sliding across the whole line.
    val walker = remember(line.start) { Animatable((line.position - line.start) / span) }
    LaunchedEffect(line.position) {
        walker.animateTo((line.position - line.start) / span, spring(dampingRatio = Spring.DampingRatioLowBouncy))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(WalkerHeight + 72.dp)
            .clearAndSetSemantics {},
    ) {
        val trackWidth = maxWidth - EdgePadding * 2
        Canvas(Modifier.fillMaxSize()) {
            val y = WalkerHeight.toPx() + 8.dp.toPx()
            val left = EdgePadding.toPx()
            val right = size.width - EdgePadding.toPx()
            drawLine(colors.primary, Offset(0f, y), Offset(size.width, y), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
            for (number in line.numbers) {
                val x = left + (number - line.start) / span * (right - left)
                val current = number == line.position
                drawLine(colors.primary, Offset(x, y - 10.dp.toPx()), Offset(x, y + 10.dp.toPx()), strokeWidth = 3.dp.toPx())
                if (current) drawCircle(colors.tertiary, radius = 10.dp.toPx(), center = Offset(x, y))
                drawLabel(measurer, "$number", labelStyle.copy(color = if (current) colors.tertiary else colors.onSurface), Offset(x, y + 18.dp.toPx()))
            }
        }
        Image(
            painter = painterResource(Res.drawable.mole_pointing_up),
            contentDescription = null,
            modifier = Modifier
                .height(WalkerHeight)
                .offset { IntOffset((EdgePadding + trackWidth * walker.value).roundToPx() - (WalkerHeight * 0.3f).roundToPx(), 0) },
        )
    }
}

private fun DrawScope.drawLabel(measurer: TextMeasurer, text: String, style: TextStyle, topCenter: Offset) {
    val layout = measurer.measure(text, style)
    drawText(layout, topLeft = Offset(topCenter.x - layout.size.width / 2f, topCenter.y))
}
