package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import java.util.EnumMap
import javafx.collections.ListChangeListener
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class RecordResourceViewModel : ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    private var activeRecordable: Recordable? = null

    internal val recordableList: ObservableList<Recordable> = FXCollections.observableArrayList()

    class ContentTypeToViewModelMap(map: Map<ContentType, TabRecordableViewModel>):
        EnumMap<ContentType, TabRecordableViewModel>(map)
    val contentTypeToViewModelMap = ContentTypeToViewModelMap(
        hashMapOf(
            ContentType.TITLE to TabRecordableViewModel(SimpleStringProperty()),
            ContentType.BODY to TabRecordableViewModel(SimpleStringProperty())
        )
    )

    init {
        initTabs()

        recordableList.onChange {
            updateRecordables(it)
        }

        workbookViewModel.activeResourceSlugProperty.onChangeAndDoNow {
            setTabLabels(it)
        }
    }

    fun onTabSelect(recordable: Recordable) {
        activeRecordable = recordable
    }

    fun setRecordableListItems(items: List<Recordable>) {
        if (!recordableList.containsAll(items))
            recordableList.setAll(items)
    }

    fun recordNewTake() {
        activeRecordable?.let {
            audioPluginViewModel
                .record(it)
                .observeOnFx()
                // Subscribing on an I/O thread is not completely necessary but it is is safer
                .subscribeOn(Schedulers.io())
                .subscribe()
        } ?: throw IllegalStateException("Active recordable is null")
    }

    fun editTake(take: Take) {
        audioPluginViewModel
            .edit(take)
            .observeOnFx()
            // Subscribing on an I/O thread is not completely necessary but it is is safer
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    private fun initTabs() {
        recordableList.forEach {
            addRecordableToTabViewModel(it)
        }
    }

    private fun setTabLabels(resourceSlug: String?) {
        when(resourceSlug) {
            "tn" -> {
                setLabelProperty(ContentType.TITLE, messages["snippet"])
                setLabelProperty(ContentType.BODY, messages["note"])
            }
        }
    }

    private fun setLabelProperty(contentType: ContentType, label: String) {
        contentTypeToViewModelMap.getNotNull(contentType).labelProperty.set(label)
    }

    private fun updateRecordables(change: ListChangeListener.Change<out Recordable>) {
        while (change.next()) {
            change.removed.forEach { recordable ->
                removeRecordableFromTabViewModel(recordable)
            }
            change.addedSubList.forEach { recordable ->
                addRecordableToTabViewModel(recordable)
            }
        }
    }

    private fun addRecordableToTabViewModel(item: Recordable) {
        contentTypeToViewModelMap.getNotNull(item.contentType).recordable = item
    }

    private fun removeRecordableFromTabViewModel(item: Recordable) {
        contentTypeToViewModelMap.getNotNull(item.contentType).recordable = null
    }
}