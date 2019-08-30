package org.wycliffeassociates.otter.jvm.app.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.view.CardGridFragment
import org.wycliffeassociates.otter.jvm.app.ui.resources.view.ResourceListFragment
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class SelectRecordableTabGroup : TabGroup() {
    private val workbookViewModel: WorkbookViewModel by inject()

    override fun activate() {
        workbookViewModel.activeChunkProperty.set(null)

        when (workbookViewModel.activeResourceInfo.type) {
            "book" -> createChunkTab()
            "help" -> createResourceTab()
        }
    }

    private fun createChunkTab() {
        tabPane.apply {
            tab<CardGridFragment>()
        }
    }

    private fun createResourceTab() {
        tabPane.apply {
            tab<ResourceListFragment>()
        }
    }
}
