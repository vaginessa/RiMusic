package it.vfsfitvnm.vimusic.service

import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi

@UnstableApi
class PlayableFormatNotFoundException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class UnplayableException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class LoginRequiredException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class VideoIdMismatchException : PlaybackException(null, null, ERROR_CODE_REMOTE_ERROR)
@UnstableApi
class PlayableFormatNonSupported : PlaybackException(null, null, ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED)