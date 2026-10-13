package com.zendalona.mathmantra

import androidx.compose.ui.window.ComposeUIViewController
import com.zendalona.mathmantra.platform.iosAppServices
import platform.UIKit.UIViewController

private val services by lazy { iosAppServices() }

@Suppress("FunctionName", "unused") // Called from Swift.
fun MainViewController(): UIViewController = ComposeUIViewController { App(services) }
