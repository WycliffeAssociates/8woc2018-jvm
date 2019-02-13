package org.wycliffeassociates.otter.jvm.recorder.app.viewmodel

import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.app.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.app.RecordingTimer
import org.wycliffeassociates.otter.common.app.WavFileWriter
import org.wycliffeassociates.otter.common.io.wav.WavFile
import org.wycliffeassociates.otter.jvm.recorder.app.view.BaseWaveLine
import org.wycliffeassociates.otter.jvm.recorder.app.view.CanvasFragment
import org.wycliffeassociates.otter.jvm.recorder.app.view.FramerateView
import org.wycliffeassociates.otter.jvm.recorder.app.view.layers.WaveformLayer
import org.wycliffeassociates.otter.jvm.recorder.device.AudioRecorder
import org.wycliffeassociates.otter.jvm.recorder.renderer.VolumeBar
import tornadofx.ViewModel
import tornadofx.add
import tornadofx.getValue
import tornadofx.setValue
import java.io.File


class RecorderViewModel : ViewModel() {

    val wav = WavFile(File(app.parameters.named["wav"]))
    val recorder = AudioRecorder()
    var volumeTest: AudioRecorder? = AudioRecorder()

    val writer = WavFileWriter(wav, recorder.getAudioStream())
    val renderer = ActiveRecordingRenderer(recorder.getAudioStream())

    val waveformView = CanvasFragment("#000000")
    val volumeBarView = CanvasFragment("#000000")

    val waveformLayer = WaveformLayer(renderer)
    //val fps = FramerateView()

    val volumeBar = VolumeBar(recorder.getAudioStream().mergeWith(volumeTest!!.getAudioStream()))

    val timer = RecordingTimer()
    val timerTextProperty = SimpleStringProperty("00:00:00")
    var timerText by timerTextProperty


    val at = object : AnimationTimer() {
        override fun handle(now: Long) {
            waveformView.draw()
            volumeBarView.draw()
            val t = timer.timeElapsed
//            timerText = String.format(
//                "%02d:%02d:%02d",
//                t / 3600000,
//                (t / 60000) % 60,
//                (t / 1000) % 60
//            )
        }
    }

    init {
        volumeBarView.addDrawable(volumeBar)
        waveformView.addDrawable(waveformLayer)
        waveformView.addDrawable(BaseWaveLine())
        //waveformView.add(fps)

        at.start()
        volumeTest!!.start()
    }

    @Volatile
    var recordingProperty = SimpleBooleanProperty(false)
    var isRecording by recordingProperty

    var hasWrittenProperty = SimpleBooleanProperty(false)
    var hasWritten by hasWrittenProperty

    var canSaveProperty: BooleanBinding = (recordingProperty.not()).and(hasWrittenProperty)


    fun toggle() {
        volumeTest?.let {
            it.stop()
            volumeTest = null
        }
        if (isRecording) {
            hasWritten = true
            recorder.pause()
            at.stop()
            timer.pause()
        } else {
            recorder.start()
            at.start()
            timer.start()
        }
        isRecording = !isRecording
    }

    fun save() {
        recorder.stop()
        Platform.exit()
    }
}