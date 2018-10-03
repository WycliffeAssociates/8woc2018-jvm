package org.wycliffeassociates.otter.jvm.app.ui.inject



import org.wycliffeassociates.otter.jvm.persistence.injection.DaggerPersistenceComponent

object Injector {
    private val persistenceComponent = DaggerPersistenceComponent.builder().build()
    private val database = persistenceComponent.injectDatabase()

    val directoryProvider = persistenceComponent.injectDirectoryProvider()
    val resourceContainerDirectory = directoryProvider.resourceContainerDirectory

    val languageDao = database.getLanguageDao()
    val collectionDao = database.getCollectionDao()
    val contentDao = database.getChunkDao()
    val takeDao = database.getTakeDao()
}
