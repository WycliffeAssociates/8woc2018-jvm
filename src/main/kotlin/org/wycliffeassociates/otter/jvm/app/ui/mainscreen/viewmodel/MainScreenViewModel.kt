package org.wycliffeassociates.otter.jvm.app.ui.mainscreen.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenView
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.view.CardGridFragment
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view.RecordScriptureFragment
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class MainScreenViewModel : ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()

    val selectedProjectName = SimpleStringProperty()
    val selectedProjectLanguage = SimpleStringProperty()

    val selectedChapterTitle = SimpleStringProperty()
    val selectedChapterBody = SimpleStringProperty()

    val selectedChunkTitle = SimpleStringProperty()
    val selectedChunkBody = SimpleStringProperty()

    private val takesPageDocked = SimpleBooleanProperty(false)

    init {
        workbookViewModel.activeWorkbookProperty.onChange {
            it?.let { wb -> projectSelected(wb) }
        }

        workbookViewModel.activeChapterProperty.onChange {
            it?.let { chapter -> chapterSelected(chapter) }
        }

        workbookViewModel.activeChunkProperty.onChange {
            it?.let { chunk -> chunkSelected(chunk) }
                ?: takesPageDocked.set(false)
        }
    }

    private fun projectSelected(selectedWorkbook: Workbook) {
        setActiveProjectText(selectedWorkbook)

        find<MainScreenView>().activeFragment.dock<CardGridFragment>()
    }

    private fun chapterSelected(chapter: Chapter) {
        setActiveChapterText(chapter)
    }

    private fun chunkSelected(chunk: Chunk) {
        setActiveChunkText(chunk)

        if(takesPageDocked.value == false) {
            find<MainScreenView>().activeFragment.dock<RecordScriptureFragment>()
        }
        takesPageDocked.set(true)
    }

    private fun setActiveChunkText(chunk: Chunk) {
        selectedChunkTitle.set(ContentLabel.of(chunk.contentType).value.toUpperCase())
        selectedChunkBody.set(chunk.start.toString())
    }

    private fun setActiveChapterText(chapter: Chapter) {
        selectedChapterTitle.set(ContentLabel.CHAPTER.value.toUpperCase())
        selectedChapterBody.set(chapter.title)
    }

    private fun setActiveProjectText(activeWorkbook: Workbook) {
        selectedProjectName.set(activeWorkbook.target.title)
        selectedProjectLanguage.set(activeWorkbook.target.language.name)
    }
}