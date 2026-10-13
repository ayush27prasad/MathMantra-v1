package com.zendalona.mathmantra.ui.lessons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zendalona.mathmantra.domain.Lesson
import com.zendalona.mathmantra.domain.Lessons
import com.zendalona.mathmantra.platform.LocalAppServices
import com.zendalona.mathmantra.resources.Res
import com.zendalona.mathmantra.resources.activity_lessons
import com.zendalona.mathmantra.resources.finish
import com.zendalona.mathmantra.resources.ic_chevron_left
import com.zendalona.mathmantra.resources.ic_chevron_right
import com.zendalona.mathmantra.resources.ic_volume_up
import com.zendalona.mathmantra.resources.lesson_pages
import com.zendalona.mathmantra.resources.lesson_progress
import com.zendalona.mathmantra.resources.mole_reading
import com.zendalona.mathmantra.resources.next
import com.zendalona.mathmantra.resources.previous
import com.zendalona.mathmantra.resources.repeat
import com.zendalona.mathmantra.ui.components.GameScaffold
import com.zendalona.mathmantra.ui.theme.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val NARRATION_RATE = 0.9f

@Composable
fun LessonListScreen(onOpen: (Lesson) -> Unit, onBack: () -> Unit) {
    GameScaffold(title = stringResource(Res.string.activity_lessons), onBack = onBack) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            itemsIndexed(Lessons.all, key = { _, lesson -> lesson.id }) { index, lesson ->
                val accents = AppTheme.extendedColors.accents
                Surface(
                    onClick = { onOpen(lesson) },
                    shape = MaterialTheme.shapes.large,
                    color = accents[index % accents.size],
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(56.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(lesson.symbol, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                        Column {
                            Text(lesson.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = stringResource(Res.string.lesson_pages, lesson.pages.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonScreen(lesson: Lesson, onBack: () -> Unit) {
    val speaker = LocalAppServices.current.speaker
    val pager = rememberPagerState { lesson.pages.size }
    val scope = rememberCoroutineScope()
    val page = pager.currentPage
    val isLast = page == lesson.pages.lastIndex

    LaunchedEffect(pager) {
        snapshotFlow { pager.settledPage }.collect { speaker.speak(lesson.pages[it], NARRATION_RATE) }
    }

    GameScaffold(title = lesson.title, onBack = onBack) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(Res.drawable.mole_reading), contentDescription = null, modifier = Modifier.size(120.dp))

            Text(
                text = stringResource(Res.string.lesson_progress, page + 1, lesson.pages.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
            )
            LinearProgressIndicator(
                progress = { (page + 1f) / lesson.pages.size },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalPager(
                state = pager,
                pageSpacing = 16.dp,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 16.dp),
            ) { index ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(Modifier.verticalScroll(rememberScrollState()).padding(24.dp)) {
                        Text(
                            text = lesson.pages[index],
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Medium,
                                lineHeight = 36.sp,
                            ),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                OutlinedIconButton(
                    onClick = { scope.launch { pager.animateScrollToPage(page - 1) } },
                    enabled = page > 0,
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(painterResource(Res.drawable.ic_chevron_left), contentDescription = stringResource(Res.string.previous))
                }
                FilledTonalIconButton(
                    onClick = { speaker.speak(lesson.pages[page], NARRATION_RATE) },
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(painterResource(Res.drawable.ic_volume_up), contentDescription = stringResource(Res.string.repeat))
                }
                Button(
                    onClick = { if (isLast) onBack() else scope.launch { pager.animateScrollToPage(page + 1) } },
                    modifier = Modifier.weight(1f).heightIn(min = 64.dp),
                ) {
                    Text(stringResource(if (isLast) Res.string.finish else Res.string.next), style = MaterialTheme.typography.titleMedium)
                    if (!isLast) Icon(painterResource(Res.drawable.ic_chevron_right), contentDescription = null)
                }
            }
        }
    }
}
