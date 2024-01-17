package it.vfsfitvnm.vimusic.equalizer.audio

class AudioPlayer {



    private val audioComputer = VisualizerComputer()
/*
    fun play(assets: AssetManager, fileName: String, visualizerData: MutableState<VisualizerData>) {
        val afd = assets.openFd(fileName)
        player = MediaPlayer().apply {
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            setVolume(0.01f, 0.01f)
            prepare()
            start()
        }
        audioComputer.start(audioSessionId = player!!.audioSessionId, onData = { data ->
            visualizerData.value = data
        })
    }

    fun stop() {
        audioComputer.stop()
        player?.stop()
        player?.release()
        player = null
    }

 */
}