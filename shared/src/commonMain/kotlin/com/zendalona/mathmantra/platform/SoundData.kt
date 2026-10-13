package com.zendalona.mathmantra.platform

import com.zendalona.mathmantra.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** Raw WAV bytes for [sound], for the platform audio players. */
@OptIn(ExperimentalResourceApi::class)
internal suspend fun Sound.readBytes(): ByteArray = Res.readBytes(path)
