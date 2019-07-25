package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import org.wycliffeassociates.controls.ChromeableTabPane
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel.RecordResourceViewModel
import tornadofx.*
import org.wycliffeassociates.otter.jvm.utils.getNotNull

class RecordResourceFragment : Fragment() {
    private val viewModel: RecordResourceViewModel by inject()
    private val tabPane = ChromeableTabPane()

    override val root = tabPane

    // The tabs will add or remove themselves from the tabPane when their view model's 'recordable' property changes
    @Suppress("unused")
    private val tabs: List<RecordableTab> = listOf(
        recordableTab(ContentType.TITLE, 0),
        recordableTab(ContentType.BODY, 1)
    )

    private fun recordableTab(contentType: ContentType, sort: Int) = RecordableTab(
        viewModel.contentTypeToViewModelMap.getNotNull(contentType),
        tabPane,
        sort,
        viewModel::onTabSelect
    )

    init {
        importStylesheet<RecordResourceStyles>()
    }
}