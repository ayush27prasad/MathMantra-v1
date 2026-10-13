package com.zendalona.mathmantra.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zendalona.mathmantra.platform.LocalAppServices
import com.zendalona.mathmantra.resources.Res
import com.zendalona.mathmantra.resources.back
import com.zendalona.mathmantra.resources.hear_again
import com.zendalona.mathmantra.resources.ic_arrow_back
import com.zendalona.mathmantra.resources.ic_volume_up
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Top bar with a back button, and content centred in a column no wider than a large phone. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScaffold(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    StopSpeakingOnExit()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                content = content,
            )
        }
    }
}

@Composable
fun HearAgainButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier) {
        Icon(painterResource(Res.drawable.ic_volume_up), contentDescription = stringResource(Res.string.hear_again))
    }
}

/** Cuts off any speech still playing when the player leaves a screen. */
@Composable
fun StopSpeakingOnExit() {
    val speaker = LocalAppServices.current.speaker
    DisposableEffect(speaker) { onDispose { speaker.stop() } }
}
