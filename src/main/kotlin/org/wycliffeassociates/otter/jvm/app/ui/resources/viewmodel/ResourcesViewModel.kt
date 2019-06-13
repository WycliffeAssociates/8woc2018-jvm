package org.wycliffeassociates.otter.jvm.app.ui.resources.viewmodel

import javafx.application.Platform
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.utils.mapNotNull
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel.TakesViewModel
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.ResourceGroupCardItemList
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.resourceGroupCardItem
import org.wycliffeassociates.otter.jvm.resourcestestapp.view.ResourcesView
import tornadofx.*

class ResourcesViewModel : ViewModel() {

    private val resourcesView: ResourcesView by inject()

    private val takesViewModel: TakesViewModel by inject()
    private val workbookViewModel: WorkbookViewModel by inject()

    val resourceGroups: ResourceGroupCardItemList = ResourceGroupCardItemList(mutableListOf())

    fun loadResourceGroups() {
        workbookViewModel.chapter.let { chapter ->
            chapter
                .children
                .startWith(chapter)
                .mapNotNull {
                    resourceGroupCardItem(it, workbookViewModel.resourceSlug, onSelect = this::navigateToTakesPage)
                }
                .buffer(2) // Buffering by 2 prevents the list UI from jumping while groups are loading
                .subscribe {
                    Platform.runLater {
                        resourceGroups.addAll(it)
                    }
                }
        }
    }

    private fun navigateToTakesPage(bookElement: BookElement, resource: Resource) {
        // TODO use navigator
        resourcesView.dockTakesView()
        takesViewModel.setRecordableListItems(buildRecordables(bookElement, resource))
    }

    private fun buildRecordables(bookElement: BookElement, resource: Resource): List<Recordable> {
        val titleRecordable = Recordable.build(bookElement, resource.title)
        val bodyRecordable = resource.body?.let {
            Recordable.build(bookElement, it)
        }

//        val titleRecordable = Recordable.build(ResourceTakesApp.createTestChunk(), resource.title)
//        val bodyRecordable = resource.body?.let {
//            Recordable.build(ResourceTakesApp.createTestChunk(), it)
//        }

//        val titleRecordable = Recordable.build(ResourceTakesApp.createTestChunk(), resource.title, ResourceTakesApp.createRandomizedAssociatedAudio())
//        val bodyRecordable = resource.body?.let {
//            Recordable.build(ResourceTakesApp.createTestChunk(), it, ResourceTakesApp.createRandomizedAssociatedAudio())
//        }

//        val titleRecordable = ResourceTakesApp.titleRecordable
//        val bodyRecordable = ResourceTakesApp.bodyRecordable

        return listOfNotNull(titleRecordable, bodyRecordable)
    }
}