package org.wycliffeassociates.otter.jvm.persistence.injection

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.jvm.persistence.AppPreferences
import org.wycliffeassociates.otter.jvm.persistence.DirectoryProvider
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.repositories.AudioPluginRepository
import java.io.File
import javax.inject.Singleton

@Module
class PersistenceModule {
    @Provides
    @Singleton
    fun providesAppDatabase(directoryProvider: IDirectoryProvider): AppDatabase =
        AppDatabase(directoryProvider.getAppDataDirectory().resolve(File("content.sqlite")))

    @Provides
    @Singleton
    fun providesAppPreferences(database: AppDatabase): IAppPreferences = AppPreferences(database)

    @Provides
    fun providesDirectoryProvider(): IDirectoryProvider = DirectoryProvider("TranslationRecorder")

    @Provides
    fun providesAudioPluginRepository(database: AppDatabase, preferences: IAppPreferences): IAudioPluginRepository =
        AudioPluginRepository(database, preferences)
}