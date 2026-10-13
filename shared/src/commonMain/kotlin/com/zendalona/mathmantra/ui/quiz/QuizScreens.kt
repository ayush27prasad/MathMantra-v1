package com.zendalona.mathmantra.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zendalona.mathmantra.domain.Question
import com.zendalona.mathmantra.platform.LocalAppServices
import com.zendalona.mathmantra.resources.Res
import com.zendalona.mathmantra.resources.activity_choices
import com.zendalona.mathmantra.resources.activity_quiz
import com.zendalona.mathmantra.resources.choice_answer
import com.zendalona.mathmantra.resources.ic_star
import com.zendalona.mathmantra.resources.question_prompt
import com.zendalona.mathmantra.resources.quiz_hint
import com.zendalona.mathmantra.resources.quiz_skip
import com.zendalona.mathmantra.resources.quiz_submit
import com.zendalona.mathmantra.resources.score
import com.zendalona.mathmantra.ui.components.FeedbackDialog
import com.zendalona.mathmantra.ui.components.GameScaffold
import com.zendalona.mathmantra.ui.components.HearAgainButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MultipleChoiceScreen(onBack: () -> Unit) {
    val speaker = LocalAppServices.current.speaker
    val viewModel = viewModel(key = QuizMode.Choices.name) { QuizViewModel(QuizMode.Choices, speaker) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    GameScaffold(
        title = stringResource(Res.string.activity_choices),
        onBack = onBack,
        actions = { ScoreChip(state.score) },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            QuestionCard(state.question, onHearAgain = viewModel::speakQuestion)
            state.choices.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { value ->
                        val rejected = value in state.rejected
                        val description = stringResource(Res.string.choice_answer, value)
                        Button(
                            onClick = { viewModel.choose(value) },
                            enabled = !rejected,
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                disabledContentColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                            ),
                            modifier = Modifier.weight(1f).height(112.dp).semantics { contentDescription = description },
                        ) {
                            Text("$value", style = MaterialTheme.typography.displaySmall)
                        }
                    }
                }
            }
        }
    }

    state.feedback?.let { FeedbackDialog(it, onDismiss = viewModel::dismissFeedback) }
}

@Composable
fun TypedQuizScreen(onBack: () -> Unit) {
    val speaker = LocalAppServices.current.speaker
    val viewModel = viewModel(key = QuizMode.Typed.name) { QuizViewModel(QuizMode.Typed, speaker) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focus = remember { FocusRequester() }

    LaunchedEffect(state.question, state.feedback) {
        if (state.feedback == null) focus.requestFocus()
    }

    GameScaffold(
        title = stringResource(Res.string.activity_quiz),
        onBack = onBack,
        actions = { ScoreChip(state.score) },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            QuestionCard(state.question, onHearAgain = viewModel::speakQuestion)
            OutlinedTextField(
                value = state.input,
                onValueChange = viewModel::onInputChange,
                label = { Text(stringResource(Res.string.quiz_hint)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.submit() }),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
            Button(
                onClick = viewModel::submit,
                enabled = state.input.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            ) {
                Text(stringResource(Res.string.quiz_submit), style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = viewModel::skip, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(Res.string.quiz_skip))
            }
        }
    }

    state.feedback?.let { FeedbackDialog(it, onDismiss = viewModel::dismissFeedback) }
}

@Composable
private fun QuestionCard(question: Question, onHearAgain: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.question_prompt),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                HearAgainButton(onClick = onHearAgain)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${question.text} = ?",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .semantics {
                        contentDescription = question.spoken
                        liveRegion = LiveRegionMode.Polite
                    },
            )
        }
    }
}

@Composable
private fun ScoreChip(score: Int) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.padding(end = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(painterResource(Res.drawable.ic_star), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(stringResource(Res.string.score, score), style = MaterialTheme.typography.labelLarge)
        }
    }
}
