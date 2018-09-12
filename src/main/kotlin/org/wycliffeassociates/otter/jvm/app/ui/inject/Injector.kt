package org.wycliffeassociates.otter.jvm.app.ui.inject

import org.wycliffeassociates.otter.jvm.device.audio.injection.DaggerAudioComponent
import org.wycliffeassociates.otter.jvm.persistence.injection.DaggerPersistenceComponent


object Injector {
//    val projectDao = DaggerPersistenceComponent
//            .builder()
//            .build()
//            .injectDatabase()
//            .getProjectDao()
//
//    val chapterDao = DaggerPersistenceComponent
//            .builder()
//            .build()
//            .injectDatabase()
//            .getChapterDao()
//
//    val bookDao = DaggerPersistenceComponent
//            .builder()
//            .build()
//            .injectDatabase()
//            .getBookDao()
//
//    val chunkDao = DaggerPersistenceComponent
//            .builder()
//            .build()
//            .injectDatabase()
//            .getChunkDao()
//
//    val takesDao = DaggerPersistenceComponent
//            .builder()
//            .build()
//            .injectDatabase()
//            .getTakesDao()

    // Audio Injection
    private val audioComponent =  DaggerAudioComponent
            .builder()
            .build()

    val audioPlayer
        get() = audioComponent.injectPlayer()
}