package org.wycliffeassociates.otter.jvm.app.widgets

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jfoenix.controls.JFXProgressBar
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import tornadofx.*
import java.io.File
import java.util.concurrent.TimeUnit

// Named "Simple" since just displays a progress bar and play/pause button
// No waveform view
class SimpleAudioPlayer(private val audioFile: File, private val player: IAudioPlayer) : HBox() {
    val playPauseButton = Button()
    val progressBar = JFXProgressBar()
    var playGraphic: Node? = null
        set (value) {
            field = value
            if (!isPlaying) {
                playPauseButton.graphic = field
            }
        }
    var pauseGraphic: Node? = null
        set (value) {
            field = value
            if (isPlaying) {
                playPauseButton.graphic = field
            }
        }

    @Volatile private var isPlaying = false

    init {
        style {
            alignment = Pos.CENTER_LEFT
        }
        add(playPauseButton)
        add(progressBar)

        // Set up indefinite loading bar
        progressBar.progress = -1.0
        progressBar.hgrow = Priority.ALWAYS
        progressBar.maxWidth = Double.MAX_VALUE

        player.load(audioFile).subscribe()

        // progress update observable
        var disposable: Disposable? = null

        player.addEventListener { audioEvent ->
            when (audioEvent) {
                AudioPlayerEvent.LOAD -> {
                    Platform.runLater { progressBar.progress = 0.0 }
                }
                AudioPlayerEvent.PLAY -> {
                    isPlaying = true
                    disposable = startProgressUpdate()
                    Platform.runLater {  playPauseButton.graphic = pauseGraphic }
                }
                AudioPlayerEvent.PAUSE, AudioPlayerEvent.STOP -> {
                    disposable?.dispose()
                    isPlaying = false
                    Platform.runLater { playPauseButton.graphic = playGraphic }
                }
                AudioPlayerEvent.COMPLETE -> {
                    disposable?.dispose()
                    isPlaying = false
                    // Make sure we update on the main thread
                    // Only needed here since rest of events are triggered from FX thread
                    Platform.runLater {
                        progressBar.progress = 0.0
                        playPauseButton.graphic = playGraphic
                    }
                }
            }
        }

        playPauseButton.action {
            if (!isPlaying) player.play() else player.pause()
        }
    }

    private fun startProgressUpdate(): Disposable {
        return Observable
                .interval(16, TimeUnit.MILLISECONDS)
                .observeOnFx()
                .subscribe {
                    val location = player
                            .getAbsoluteLocationInFrames()
                            .toDouble()
                    progressBar.progress = location / player.getAbsoluteDurationInFrames()
                }
    }

}

fun simpleaudioplayer(
        audioFile: File,
        audioPlayer: IAudioPlayer,
        init: SimpleAudioPlayer.() -> Unit
): SimpleAudioPlayer {
    val audioPlayer = SimpleAudioPlayer(audioFile, audioPlayer)
    audioPlayer.init()
    return audioPlayer
}