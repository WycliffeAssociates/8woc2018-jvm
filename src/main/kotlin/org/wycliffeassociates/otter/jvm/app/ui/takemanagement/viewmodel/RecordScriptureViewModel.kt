package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.*
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class RecordScriptureViewModel : ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val takeManagementViewModel: TakeManagementViewModel by inject()

    // This will be bidirectionally bound to workbookViewModel's activeChunkProperty
    private val activeChunkProperty = SimpleObjectProperty<Chunk?>()
    private val activeChunk: Chunk
        get() = activeChunkProperty.value ?: throw IllegalStateException("Chunk is null")

    val selectedTakeProperty = SimpleObjectProperty<Take?>()
    private val selectedTake by selectedTakeProperty

    private var context: TakeContext by property(TakeContext.RECORD)
    val contextProperty = getProperty(RecordScriptureViewModel::context)

    val alternateTakes: ObservableList<Take> = FXCollections.observableList(mutableListOf())

    var title: String by property()
    val titleProperty = getProperty(RecordScriptureViewModel::title)

    // Whether the UI should show the plugin as active
    private var showPluginActive: Boolean by property(false)
    var showPluginActiveProperty = getProperty(RecordScriptureViewModel::showPluginActive)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val chunkList: ObservableList<Chunk> = observableList()
    val hasNext = SimpleBooleanProperty(false)
    val hasPrevious = SimpleBooleanProperty(false)

    private var selectedTakeSubscription: Disposable? = null

    init {
        activeChunkProperty.bindBidirectional(workbookViewModel.activeChunkProperty)

        workbookViewModel.activeChapterProperty.onChangeAndDoNow {
            it?.let { chapter -> getChunkList(chapter.chunks) }
        }

        activeChunkProperty.onChangeAndDoNow {
            it?.let { chunk ->
                setTitle(chunk)
                loadTakes(chunk.audio)
                setHasNextAndPrevious()
                subscribeToSelectedTake(chunk.audio)
            }
        }
    }

    private fun subscribeToSelectedTake(audio: AssociatedAudio) {
        selectedTakeSubscription?.dispose()

        selectedTakeSubscription = audio.selected.subscribe {
            selectedTakeProperty.set(it.value)
        }
    }

    private fun setHasNextAndPrevious() {
        if (chunkList.isNotEmpty()) {
            doSetHasNextAndPrevious()
        } else {
            chunkList.isNotEmpty().toProperty().onChangeOnce {
                doSetHasNextAndPrevious()
            }
        }
    }

    private fun doSetHasNextAndPrevious() {
        if (chunkList.isNotEmpty()) {
            hasNext.set(activeChunk.start < chunkList.last().start)
            hasPrevious.set(activeChunk.start > chunkList.first().start)
        } else throw IllegalStateException("Chunk list is empty")
    }

    private fun setTitle(chunk: Chunk) {
        val label = ContentLabel.VERSE.value
        val start = chunk.start
        title = "$label $start"
    }

    private fun getChunkList(chunks: Observable<Chunk>) {
        chunks.toList()
            .observeOnFx()
            .subscribe { list ->
                chunkList.setAll(list.sortedBy { chunk -> chunk.start })
            }
    }

    private fun loadTakes(audio: AssociatedAudio) {
        audio.takes
            .subscribe {
                if ( it != selectedTake ) {
                    Platform.runLater {
                        alternateTakes.add(it)
                    }
                }
            }
    }

    fun selectTake(take: Take?) {
        take?.let {
            alternateTakes.remove(it)

            selectedTake?.let { oldSelectedTake ->
                alternateTakes.add(oldSelectedTake)
            }
        }

        // Set the new selected take value
        activeChunk.audio.selectTake(take)
    }

    fun editContent(take: Take) {
        contextProperty.set(TakeContext.EDIT_TAKES)
        showPluginActive = true
        takeManagementViewModel.edit(take)
            .observeOnFx()
            .subscribe { result ->
                showPluginActive = false
                when (result) {
                    EditTake.Result.NO_EDITOR -> snackBarObservable.onNext(messages["noEditor"])
                    else -> {}
                }
            }
    }

    private enum class StepDirection {
        FORWARD,
        BACKWARD
    }

    private fun stepToChunk(direction: StepDirection) {
        val amount = when (direction) {
            StepDirection.FORWARD -> 1
            StepDirection.BACKWARD -> -1
        }
        chunkList
            .find { it.start == activeChunk.start + amount }
            ?.let { newChunk -> activeChunkProperty.set(newChunk) }
    }

    fun nextChunk() {
        stepToChunk(StepDirection.FORWARD)
    }

    fun previousChunk() {
        stepToChunk(StepDirection.BACKWARD)
    }

    fun delete(take: Take) {
        take.deletedTimestamp.accept(DateHolder.now())
        if (take == selectedTake) {
            selectTake(null)
        } else {
            alternateTakes.remove(take)
        }
    }

    fun recordContent(recordable: Recordable) {
        contextProperty.set(TakeContext.RECORD)
        showPluginActive = true

        takeManagementViewModel
            .record(recordable)
            .observeOnFx()
            .doOnSuccess { result ->
                showPluginActive = false
                when (result) {
                    RecordTake.Result.NO_RECORDER -> snackBarObservable.onNext(messages["noRecorder"])
                    else -> {}
                }
            }
            .subscribe()
    }
}