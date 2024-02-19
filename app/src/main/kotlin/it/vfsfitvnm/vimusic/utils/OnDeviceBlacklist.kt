package it.vfsfitvnm.vimusic.utils

import it.vfsfitvnm.vimusic.Database
import it.vfsfitvnm.vimusic.models.OnDeviceBlacklistPath


class OnDeviceBlacklist() {
    var paths: List<OnDeviceBlacklistPath> = emptyList()

    init {
        paths = Database.getOnDeviceBlacklistPaths()
    }

    fun contains(path: String): Boolean {
        return paths.any { it.test(path) }
    }
}