package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.content.AccessTakes
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.viewmodel.ProjectPageViewModel
import org.wycliffeassociates.otter.jvm.persistence.WaveFileCreator
import tornadofx.*
import tornadofx.FX.Companion.messages

class ViewTakesModel {
    private val directoryProvider = Injector.directoryProvider
    private val collectionRepository = Injector.collectionRepo
    private val chunkRepository = Injector.chunkRepository
    private val takeRepository = Injector.takeRepository
    private val pluginRepository = Injector.pluginRepository

    val chunkProperty = find(ProjectPageViewModel::class).activeChunkProperty
    val projectProperty = find(ProjectHomeViewModel::class).selectedProjectProperty
    var activeChild = find(ProjectPageViewModel::class).activeChildProperty

    val selectedTakeProperty = SimpleObjectProperty<Take>()

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    var title: String by property("View Takes")
    val titleProperty = getProperty(ViewTakesModel::title)

    // Whether the UI should show the plugin as active
    var showPluginActive: Boolean by property(false)
    var showPluginActiveProperty = getProperty(ViewTakesModel::showPluginActive)

    val recordTake = RecordTake(
            collectionRepository,
            chunkRepository,
            takeRepository,
            directoryProvider,
            WaveFileCreator(),
            LaunchPlugin(pluginRepository)
    )

    private val accessTakes = AccessTakes(
            Injector.chunkRepository,
            Injector.takeRepository
    )

    init {
        reset()
    }

    private fun populateTakes(chunk: Chunk) {
        accessTakes
                .getByChunk(chunk)
                .observeOnFx()
                .subscribe { retrievedTakes ->
                    alternateTakes.clear()
                    alternateTakes.addAll(retrievedTakes.filter { it != chunk.selectedTake })
                    selectedTakeProperty.value = chunk.selectedTake
                }
    }

    fun acceptTake(take: Take) {
        val chunk = chunkProperty.value
        alternateTakes.add(selectedTakeProperty.value)
        accessTakes
                .setSelectedTake(chunk, take)
                .subscribe()
        selectedTakeProperty.value = take
        alternateTakes.remove(take)
        println(alternateTakes)
    }

    fun setTakePlayed(take: Take) {
        accessTakes
                .setTakePlayed(take, true)
                .subscribe()
    }

    fun reset() {
        alternateTakes.clear()
        selectedTakeProperty.value = null
        chunkProperty.value?.let { populateTakes(it) }
        title = "${messages[chunkProperty.value?.labelKey ?: "verse"]} ${chunkProperty.value?.start ?: ""}"
    }


    fun recordChunk() {
        projectProperty.value?.let { project ->
            showPluginActive = true
            recordTake
                    .record(project, activeChild.value, chunkProperty.value)
                    .observeOnFx()
                    .subscribe {
                        showPluginActive = false
                        populateTakes(chunkProperty.value)
                    }
        }
    }

    fun delete(take: Take) {
        println(alternateTakes)
        alternateTakes.remove(take)
        accessTakes
                .delete(take)
                .subscribe()
    }
}

