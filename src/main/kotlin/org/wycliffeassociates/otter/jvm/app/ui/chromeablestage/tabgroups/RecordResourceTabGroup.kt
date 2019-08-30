package org.wycliffeassociates.otter.jvm.app.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view.RecordableTab
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.utils.getNotNull

class RecordResourceTabGroup : TabGroup() {
    private val viewModel: RecordResourceViewModel by inject()

    private val tabs: List<RecordableTab> = listOf(
        recordableTab(ContentType.TITLE),
        recordableTab(ContentType.BODY)
    )

    private fun recordableTab(contentType: ContentType): RecordableTab {
        return RecordableTab(
            viewModel = viewModel.contentTypeToViewModelMap.getNotNull(contentType),
            onTabSelect = viewModel::onTabSelect
        )
    }

    override fun activate() {
        tabs.forEach { tab ->
            if (tab.isActive()) {
                tabPane.tabs.add(tab)
            }
        }
    }
}