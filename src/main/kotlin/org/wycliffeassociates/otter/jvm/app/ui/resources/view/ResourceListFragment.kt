package org.wycliffeassociates.otter.jvm.app.ui.resources.view

import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenStyles
import org.wycliffeassociates.otter.jvm.app.widgets.workbookheader.workbookheader
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.styles.ResourceListStyles
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.view.ResourceListView
import org.wycliffeassociates.otter.jvm.app.ui.resources.viewmodel.ResourcesViewModel
import tornadofx.*

class ResourceListFragment : Fragment() {
    val viewModel: ResourcesViewModel by inject()

    init {
        importStylesheet<MainScreenStyles>()
        importStylesheet<ResourceListStyles>()
    }
    override val root = vbox {

        addClass(MainScreenStyles.main)

        add(
            workbookheader {
                labelText = viewModel.chapter.title + " Resources"
                filterText = "Hide Completed"
            }
        )
        add(
            ResourceListView(viewModel.resourceGroups)
        )
    }
}