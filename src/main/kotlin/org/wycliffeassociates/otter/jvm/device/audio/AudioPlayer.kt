package org.wycliffeassociates.otter.jvm.device.audio

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import javax.sound.sampled.LineEvent

class AudioPlayer: IAudioPlayer {

    private var clip: Clip = AudioSystem.getClip()

    override fun load(file: File): Completable {
        pause()
        if (clip.isOpen) clip.close()
        clip = AudioSystem.getClip()

        val audioInputStream = AudioSystem.getAudioInputStream(file)
        return Completable.fromAction {
            clip.open(audioInputStream)
        }.subscribeOn(Schedulers.io())
    }

    override fun play(onFinish: (() -> Unit)) {
        // Setup listener
        clip.addLineListener {
            if (it.type == LineEvent.Type.STOP) {
                onFinish()
            }
        }
        if (!clip.isRunning) clip.start()
    }

    override fun pause() {
        if (clip.isRunning) clip.stop()
    }

    override fun stop() {
        pause()
        clip.framePosition = 0
    }

    override fun getAbsoluteDurationInFrames(): Int {
        return clip.frameLength
    }

    override fun getAbsoluteDurationMs(): Int {
        return (getAbsoluteDurationInFrames() / 44.1).toInt()
    }

    override fun getAbsoluteLocationInFrames(): Int {
        return clip.framePosition
    }

    override fun getAbsoluteLocationMs(): Int {
        return (getAbsoluteLocationInFrames() / 44.1).toInt()
    }
}