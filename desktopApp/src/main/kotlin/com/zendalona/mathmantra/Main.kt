package com.zendalona.mathmantra

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.zendalona.mathmantra.platform.desktopAppServices

fun main() {
    val services = desktopAppServices()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MathMantra",
            state = rememberWindowState(width = 440.dp, height = 880.dp),
        ) {
            App(services)
        }
    }
}
