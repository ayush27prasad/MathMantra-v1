package com.zendalona.mathmantra.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zendalona.mathmantra.platform.LocalAppServices
import com.zendalona.mathmantra.resources.Res
import com.zendalona.mathmantra.resources.activity_bell
import com.zendalona.mathmantra.resources.activity_bell_hint
import com.zendalona.mathmantra.resources.activity_bell_hint_tap
import com.zendalona.mathmantra.resources.activity_choices
import com.zendalona.mathmantra.resources.activity_choices_hint
import com.zendalona.mathmantra.resources.activity_drum
import com.zendalona.mathmantra.resources.activity_drum_hint
import com.zendalona.mathmantra.resources.activity_lessons
import com.zendalona.mathmantra.resources.activity_lessons_hint
import com.zendalona.mathmantra.resources.activity_number_line
import com.zendalona.mathmantra.resources.activity_number_line_hint
import com.zendalona.mathmantra.resources.activity_quiz
import com.zendalona.mathmantra.resources.activity_quiz_hint
import com.zendalona.mathmantra.resources.activity_recite
import com.zendalona.mathmantra.resources.activity_recite_hint
import com.zendalona.mathmantra.resources.app_name
import com.zendalona.mathmantra.resources.bell
import com.zendalona.mathmantra.resources.books
import com.zendalona.mathmantra.resources.drum
import com.zendalona.mathmantra.resources.home_tagline
import com.zendalona.mathmantra.resources.mcq
import com.zendalona.mathmantra.resources.mic
import com.zendalona.mathmantra.resources.mole_pointing_up
import com.zendalona.mathmantra.resources.number_line
import com.zendalona.mathmantra.resources.section_learn
import com.zendalona.mathmantra.resources.section_play
import com.zendalona.mathmantra.resources.section_practice
import com.zendalona.mathmantra.resources.test
import com.zendalona.mathmantra.ui.theme.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

enum class Activity(val title: StringResource, val hint: StringResource, val image: DrawableResource) {
    Bell(Res.string.activity_bell, Res.string.activity_bell_hint, Res.drawable.bell),
    Drum(Res.string.activity_drum, Res.string.activity_drum_hint, Res.drawable.drum),
    Recite(Res.string.activity_recite, Res.string.activity_recite_hint, Res.drawable.mic),
    Lessons(Res.string.activity_lessons, Res.string.activity_lessons_hint, Res.drawable.books),
    NumberLine(Res.string.activity_number_line, Res.string.activity_number_line_hint, Res.drawable.number_line),
    Choices(Res.string.activity_choices, Res.string.activity_choices_hint, Res.drawable.mcq),
    Quiz(Res.string.activity_quiz, Res.string.activity_quiz_hint, Res.drawable.test),
}

private val sections = listOf(
    Res.string.section_play to listOf(Activity.Bell, Activity.Drum, Activity.Recite),
    Res.string.section_learn to listOf(Activity.Lessons, Activity.NumberLine),
    Res.string.section_practice to listOf(Activity.Choices, Activity.Quiz),
)

@Composable
fun HomeScreen(onOpen: (Activity) -> Unit) {
    val canShake = LocalAppServices.current.motion.isAvailable
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { insets ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            fullWidth { Header() }
            sections.forEach { (title, activities) ->
                fullWidth {
                    Text(
                        text = stringResource(title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 10.dp, start = 4.dp).semantics { heading() },
                    )
                }
                items(activities, key = { it.name }) { activity ->
                    val hint = if (activity == Activity.Bell && !canShake) Res.string.activity_bell_hint_tap else activity.hint
                    ActivityCard(activity, hint, onClick = { onOpen(activity) })
                }
            }
        }
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) =
    item(span = { GridItemSpan(maxLineSpan) }) { content() }

@Composable
private fun Header() {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.extraLarge) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(bottom = 12.dp)) {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.home_tagline),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
            Image(
                painter = painterResource(Res.drawable.mole_pointing_up),
                contentDescription = null,
                modifier = Modifier.height(132.dp).widthIn(max = 96.dp),
            )
        }
    }
}

@Composable
private fun ActivityCard(activity: Activity, hint: StringResource, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = AppTheme.extendedColors.accents[activity.ordinal % AppTheme.extendedColors.accents.size],
        modifier = Modifier.fillMaxWidth().heightIn(min = 156.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Image(painterResource(activity.image), contentDescription = null, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(14.dp))
            Text(stringResource(activity.title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
