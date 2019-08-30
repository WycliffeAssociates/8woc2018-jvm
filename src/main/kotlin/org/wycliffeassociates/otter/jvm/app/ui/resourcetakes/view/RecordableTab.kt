package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel.RecordableTabViewModel
import tornadofx.*

class RecordableTab(
    val viewModel: RecordableTabViewModel,
    private val onTabSelect: (Recordable) -> Unit
) : Tab() {

    init {
        textProperty().bind(viewModel.labelProperty)

        RecordResourceFragment(viewModel).apply {
            formattedTextProperty.bind(viewModel.getFormattedTextBinding())
            this@RecordableTab.content = this.root
        }

        selectedProperty().onChange { selected ->
            if (selected) {
                callOnTabSelect()
            }
        }
    }

    private fun callOnTabSelect() {
        viewModel.recordable?.let { onTabSelect(it) }
            ?: throw IllegalStateException("Selected tab's recordable is null")
    }
}