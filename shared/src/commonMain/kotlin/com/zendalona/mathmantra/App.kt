package com.zendalona.mathmantra

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.zendalona.mathmantra.domain.Lessons
import com.zendalona.mathmantra.platform.AppServices
import com.zendalona.mathmantra.platform.LocalAppServices
import com.zendalona.mathmantra.ui.counting.CountingGameScreen
import com.zendalona.mathmantra.ui.counting.Instrument
import com.zendalona.mathmantra.ui.home.Activity
import com.zendalona.mathmantra.ui.home.HomeScreen
import com.zendalona.mathmantra.ui.lessons.LessonListScreen
import com.zendalona.mathmantra.ui.lessons.LessonScreen
import com.zendalona.mathmantra.ui.numberline.NumberLineScreen
import com.zendalona.mathmantra.ui.quiz.MultipleChoiceScreen
import com.zendalona.mathmantra.ui.quiz.TypedQuizScreen
import com.zendalona.mathmantra.ui.recite.ReciteScreen
import com.zendalona.mathmantra.ui.theme.MathMantraTheme
import kotlinx.serialization.Serializable

private sealed interface Route {
    @Serializable data object Home : Route
    @Serializable data object Bell : Route
    @Serializable data object Drum : Route
    @Serializable data object Recite : Route
    @Serializable data object LessonList : Route
    @Serializable data class Lesson(val id: String) : Route
    @Serializable data object NumberLine : Route
    @Serializable data object Choices : Route
    @Serializable data object Quiz : Route
}

private fun Activity.route(): Route = when (this) {
    Activity.Bell -> Route.Bell
    Activity.Drum -> Route.Drum
    Activity.Recite -> Route.Recite
    Activity.Lessons -> Route.LessonList
    Activity.NumberLine -> Route.NumberLine
    Activity.Choices -> Route.Choices
    Activity.Quiz -> Route.Quiz
}

@Composable
fun App(services: AppServices) {
    CompositionLocalProvider(LocalAppServices provides services) {
        MathMantraTheme {
            val nav = rememberNavController()
            val back: () -> Unit = { nav.navigateUp() }

            NavHost(navController = nav, startDestination = Route.Home) {
                composable<Route.Home> { HomeScreen(onOpen = { nav.navigate(it.route()) }) }
                composable<Route.Bell> { CountingGameScreen(Instrument.Bell, onBack = back) }
                composable<Route.Drum> { CountingGameScreen(Instrument.Drum, onBack = back) }
                composable<Route.Recite> { ReciteScreen(onBack = back) }
                composable<Route.LessonList> {
                    LessonListScreen(onOpen = { nav.navigate(Route.Lesson(it.id)) }, onBack = back)
                }
                composable<Route.Lesson> { entry ->
                    LessonScreen(Lessons.byId(entry.toRoute<Route.Lesson>().id), onBack = back)
                }
                composable<Route.NumberLine> { NumberLineScreen(onBack = back) }
                composable<Route.Choices> { MultipleChoiceScreen(onBack = back) }
                composable<Route.Quiz> { TypedQuizScreen(onBack = back) }
            }
        }
    }
}
