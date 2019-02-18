package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.viewmodel


import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.AccessTakes
import org.wycliffeassociates.otter.common.domain.content.EditTake
import org.wycliffeassociates.otter.common.domain.content.RecordTake
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.view.AddPluginView
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.TakeContext
import org.wycliffeassociates.otter.jvm.persistence.WaveFileCreator
import tornadofx.*

class ViewTakesViewModel : ViewModel() {
    private val injector: Injector by inject()
    private val directoryProvider = injector.directoryProvider
    private val collectionRepository = injector.collectionRepo
    private val contentRepository = injector.contentRepository
    private val takeRepository = injector.takeRepository
    private val pluginRepository = injector.pluginRepository

    var activeProperty: Collection by property()
    val activeProjectProperty = getProperty(ViewTakesViewModel::activeProperty)

    var activeCollection: Collection by property()
    var activeCollectionProperty = getProperty(ViewTakesViewModel::activeCollection)

    var activeContent: Content by property()
    val activeContentProperty = getProperty(ViewTakesViewModel::activeContent)

    val selectedTakeProperty = SimpleObjectProperty<Take>()
    var isSelectedTake = SimpleBooleanProperty(false)

    private var context: TakeContext by property(TakeContext.RECORD)
    val contextProperty = getProperty(ViewTakesViewModel::context)

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    var title: String by property()
    val titleProperty = getProperty(ViewTakesViewModel::title)

    // Whether the UI should show the plugin as active
    private var showPluginActive: Boolean by property(false)
    var showPluginActiveProperty = getProperty(ViewTakesViewModel::showPluginActive)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val launchPlugin = LaunchPlugin(pluginRepository)
    private val recordTake = RecordTake(
            collectionRepository,
            contentRepository,
            takeRepository,
            directoryProvider,
            WaveFileCreator(),
            LaunchPlugin(pluginRepository)
    )

    private val accessTakes = AccessTakes(
            contentRepository,
            takeRepository
    )

    private val editTake = EditTake(takeRepository, launchPlugin)

    init {
        activeContentProperty.toObservable().subscribe{
            title ="${FX.messages[activeContentProperty.value?.labelKey ?: "verse"]} ${activeContentProperty.value?.start ?: ""}"
            activeContent = it
            populateTakes(it)
        }
        reset()
        //listen for changes to the selected take property, if there is a change activate edit button
        selectedTakeProperty.onChange {
            if (it == null) {
                isSelectedTake.set(false)
            } else {
                isSelectedTake.set(true)
            }
        }
    }

    fun audioPlayer(): IAudioPlayer = injector.audioPlayer

    fun addPlugin(record: Boolean, edit: Boolean) {
        find<AddPluginViewModel>().apply {
            canRecord = record
            canEdit = edit
        }
        find<AddPluginView>().openModal()
    }

    private fun populateTakes(content: Content) {
        accessTakes
                .getByContent(content)
                .observeOnFx()
                .subscribe { retrievedTakes ->
                    alternateTakes.clear()
                    alternateTakes.addAll(retrievedTakes.filter { it != content.selectedTake })
                    selectedTakeProperty.value = content.selectedTake
                    // if we have a selected take, make the edit button active
                    if (selectedTakeProperty.value != null) {
                        isSelectedTake.set(true)
                    }
                }
    }

    fun acceptTake(take: Take) {
        val content = activeContentProperty.value
        // Move the old selected take back to the alternates (if not null)
        if (selectedTakeProperty.value != null) alternateTakes.add(selectedTakeProperty.value)
        // Do the database action
        accessTakes
                .setSelectedTake(content, take)
                .subscribe()
        // Set the new selected take value
        selectedTakeProperty.value = take
        // Remove the new selected take from the alternates
        alternateTakes.remove(take)
    }

    fun setTakePlayed(take: Take) {
        accessTakes
                .setTakePlayed(take, true)
                .subscribe()
    }

    fun recordContent() {
        contextProperty.set(TakeContext.RECORD)
        activeProjectProperty.value?.let { project ->
            showPluginActive = true
            recordTake
                    .record(project, activeCollectionProperty.value, activeContentProperty.value)
                    .observeOnFx()
                    .doOnSuccess { result ->
                        showPluginActive = false
                        when (result) {
                            RecordTake.Result.SUCCESS -> {
                                populateTakes(activeContentProperty.value)
                            }

                            RecordTake.Result.NO_RECORDER -> snackBarObservable.onNext(messages["noRecorder"])
                            RecordTake.Result.NO_AUDIO -> {
                            }
                        }
                    }
                    .toCompletable()
                    .onErrorResumeNext {
                        Completable.fromAction {
                            showPluginActive = false
                            snackBarObservable.onNext(messages["noRecorder"])
                        }
                    }
                    .subscribe()
        }
    }

    fun editContent() {
        contextProperty.set(TakeContext.EDIT_TAKES)
        selectedTakeProperty.value?.let { take ->
            showPluginActive = true
            editTake
                    .edit(take)
                    .observeOnFx()
                    .subscribe { result ->
                        showPluginActive = false
                        when (result) {
                            EditTake.Result.SUCCESS -> {
                            }
                            EditTake.Result.NO_EDITOR -> snackBarObservable.onNext(messages["noEditor"])
                        }
                    }
        }
    }

    fun delete(take: Take) {
        if (take == selectedTakeProperty.value) {
            // Delete the selected take
            accessTakes
                    .setSelectedTake(activeContentProperty.value, null)
                    .concatWith(accessTakes.delete(take))
                    .subscribe()
            selectedTakeProperty.value = null
        } else {
            alternateTakes.remove(take)
            accessTakes
                    .delete(take)
                    .subscribe()
        }
        if (take.path.exists()) take.path.delete()
    }

    fun reset() {
        alternateTakes.clear()
        selectedTakeProperty.value = null
        activeContentProperty.value?.let { populateTakes(it) }
        title = if (activeContentProperty.value?.labelKey == "chapter") {
            activeCollectionProperty.value?.titleKey ?: ""
        } else {
            "${FX.messages[activeContentProperty.value?.labelKey ?: "verse"]} ${activeContentProperty.value?.start ?: ""}"
        }
    }
}