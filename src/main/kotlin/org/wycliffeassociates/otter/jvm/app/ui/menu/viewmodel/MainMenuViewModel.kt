package org.wycliffeassociates.otter.jvm.app.ui.menu.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.ViewModel
import java.io.File

class MainMenuViewModel : ViewModel() {
    private val languageRepo = Injector.languageRepo
    private val metadataRepo = Injector.metadataRepo
    private val collectionRepo = Injector.collectionRepo
    private val contentRepo = Injector.contentRepo
    private val directoryProvider = Injector.directoryProvider
    private val pluginRepository = Injector.pluginRepository

    val editorPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()
    val recorderPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()
    val selectedEditorProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedRecorderProperty = SimpleObjectProperty<AudioPluginData>()

    val showImportDialogProperty = SimpleBooleanProperty(false)
    val showImportErrorDialogProperty = SimpleBooleanProperty(false)

    init {
        refreshPlugins()
    }

    fun importContainerDirectory(dir: File) {
        val importer = ImportResourceContainer(
                collectionRepo,
                directoryProvider
        )
        showImportDialogProperty.value = true
        importer.import(dir)
                .observeOnFx()
                .doOnError {
                    showImportDialogProperty.value = false
                    showImportErrorDialogProperty.value = true
                }
                .onErrorComplete()
                .subscribe {
                    showImportDialogProperty.value = false
                }
    }

    fun refreshPlugins() {
        pluginRepository
                .getAll()
                .observeOnFx()
                .doOnSuccess { pluginData ->
                    editorPlugins.setAll(pluginData.filter { it.canEdit })
                    recorderPlugins.setAll(pluginData.filter { it.canRecord })
                }
                .observeOn(Schedulers.io())
                .flatMapMaybe {
                    pluginRepository.getRecorderData()
                }
                .observeOnFx()
                .doOnSuccess {
                    selectedRecorderProperty.set(it)
                }
                .observeOn(Schedulers.io())
                .flatMap {
                    pluginRepository.getEditorData()
                }
                .observeOnFx()
                .doOnSuccess {
                    selectedEditorProperty.set(it)
                }
                .subscribe()
    }

    fun selectEditor(editorData: AudioPluginData) {
        pluginRepository.setEditorData(editorData).subscribe()
        selectedEditorProperty.set(editorData)
    }

    fun selectRecorder(recorderData: AudioPluginData) {
        pluginRepository.setRecorderData(recorderData).subscribe()
        selectedRecorderProperty.set(recorderData)
    }
}