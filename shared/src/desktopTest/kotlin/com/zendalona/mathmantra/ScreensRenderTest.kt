package com.zendalona.mathmantra

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.zendalona.mathmantra.domain.Feedback
import com.zendalona.mathmantra.domain.Lessons
import com.zendalona.mathmantra.platform.AppServices
import com.zendalona.mathmantra.platform.LocalAppServices
import com.zendalona.mathmantra.ui.components.FeedbackDialog
import com.zendalona.mathmantra.ui.counting.CountingGameScreen
import com.zendalona.mathmantra.ui.counting.Instrument
import com.zendalona.mathmantra.ui.home.HomeScreen
import com.zendalona.mathmantra.ui.lessons.LessonListScreen
import com.zendalona.mathmantra.ui.lessons.LessonScreen
import com.zendalona.mathmantra.ui.numberline.NumberLineScreen
import com.zendalona.mathmantra.ui.quiz.MultipleChoiceScreen
import com.zendalona.mathmantra.ui.quiz.TypedQuizScreen
import com.zendalona.mathmantra.ui.recite.ReciteScreen
import com.zendalona.mathmantra.ui.theme.MathMantraTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test

/**
 * Composes every screen in light and dark themes at phone size, failing on any crash.
 * The frames are written to `build/screens/` for visual review.
 */
class ScreensRenderTest {
    private val screens: Map<String, @Composable () -> Unit> = mapOf(
        "home" to { HomeScreen(onOpen = {}) },
        "bell" to { CountingGameScreen(Instrument.Bell, onBack = {}) },
        "drum" to { CountingGameScreen(Instrument.Drum, onBack = {}) },
        "recite" to { ReciteScreen(onBack = {}) },
        "lessons" to { LessonListScreen(onOpen = {}, onBack = {}) },
        "lesson" to { LessonScreen(Lessons.all.first(), onBack = {}) },
        "number_line" to { NumberLineScreen(onBack = {}) },
        "choices" to { MultipleChoiceScreen(onBack = {}) },
        "quiz" to { TypedQuizScreen(onBack = {}) },
        "feedback" to { FeedbackDialog(Feedback.correct("7 times 3 is 21."), onDismiss = {}) },
    )

    @Test
    fun everyScreenRenders() = runBlocking(Dispatchers.Main) {
        val output = File("build/screens").apply { mkdirs() }
        for (dark in listOf(false, true)) {
            for ((name, screen) in screens) {
                val png = render(dark, screen)
                File(output, "${name}_${if (dark) "dark" else "light"}.png").writeBytes(png)
            }
        }
    }

    private suspend fun render(dark: Boolean, screen: @Composable () -> Unit): ByteArray {
        val owner = TestOwner()
        return ImageComposeScene(width = 412 * 3, height = 892 * 3, density = Density(3f)) {
            CompositionLocalProvider(
                LocalAppServices provides AppServices.Silent,
                LocalViewModelStoreOwner provides owner,
                LocalLifecycleOwner provides owner,
            ) {
                MathMantraTheme(darkTheme = dark) { screen() }
            }
        }.use { scene ->
            var time = 0L
            repeat(30) {
                scene.render(time)
                time += 100_000_000
                withContext(Dispatchers.IO) { Thread.sleep(20) }
            }
            scene.render(time).encodeToData(EncodedImageFormat.PNG)!!.bytes
        }
    }

    private class TestOwner : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore = ViewModelStore()
        override val lifecycle = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
    }
}
