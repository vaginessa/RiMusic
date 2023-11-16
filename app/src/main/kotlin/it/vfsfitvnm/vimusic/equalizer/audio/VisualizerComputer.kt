package it.vfsfitvnm.vimusic.equalizer.audio


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class VisualizerComputer {

    companion object {
        fun setupPermissions(activity: Activity) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.RECORD_AUDIO), 42
                )
            }
        }

        val CAPTURE_SIZE = Visualizer.getCaptureSizeRange()[1]

        const val SAMPLING_INTERVAL = 100
    }

    private var visualizer: Visualizer? = null

    // Callbacks are called on main thread
    private fun visualizerCallback(onData: (VisualizerData) -> Unit) =
        object : Visualizer.OnDataCaptureListener {
            override fun onFftDataCapture(
                visualizer: Visualizer,
                fft: ByteArray,
                samplingRate: Int
            ) {
                //Timber.e("FFT - samplingRate=$samplingRate, waveform=${fft.joinToString()} thread=" + Thread.currentThread())
                //onData(VisualizerData(bytes = process(fft), resolution = resolution))
            }

            var captureCounter = 0
            var start: Long? = null
            var lastDataTimestamp: Long? = null

            override fun onWaveFormDataCapture(
                visualizer: Visualizer,
                waveform: ByteArray,
                samplingRate: Int
            ) {
                val now = System.currentTimeMillis()
                /*
                if (start == null) start = now
                captureCounter++
                if (captureCounter % 100 == 0) Log.e(
                    "COUNTER",
                    "Captured $captureCounter (${captureCounter / ((now - start!!) / 1000.0)} capture/sec)"
                )
                */
                //Timber.e("Wave - samplingRate=$samplingRate, waveform=${waveform.joinToString()} thread=" + Thread.currentThread())
                val durationSinceLastData = lastDataTimestamp?.let { now - it } ?: 0
                if (lastDataTimestamp == null || durationSinceLastData > SAMPLING_INTERVAL) {
                    onData(
                        VisualizerData(
                            rawWaveform = waveform.clone(),
                            captureSize = CAPTURE_SIZE,
                            samplingRate = samplingRate,
                            durationSinceLastData = 0//if (durationSinceLastData < 200) durationSinceLastData else 0
                        )
                    )
                    lastDataTimestamp = now
                }
            }
        }

    fun start(audioSessionId: Int = 0, onData: (VisualizerData) -> Unit) {
        stop()
        visualizer = Visualizer(audioSessionId).apply {
            enabled = false // All configuration have to be done in a disabled state
            captureSize = CAPTURE_SIZE
            //scalingMode = Visualizer.SCALING_MODE_NORMALIZED
            //measurementMode = Visualizer.MEASUREMENT_MODE_NONE
            setDataCaptureListener(
                visualizerCallback(onData),
                Visualizer.getMaxCaptureRate(),
                true,
                true
            )
            enabled = true // Configuration is done, can enable now...
        }
    }

    fun stop() {
        visualizer?.release()
    }
}