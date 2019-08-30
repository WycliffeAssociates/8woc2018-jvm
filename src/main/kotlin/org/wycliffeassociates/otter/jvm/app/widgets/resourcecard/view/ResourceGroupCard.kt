package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.view

import javafx.application.Platform
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.ResourceGroupCardItem
import tornadofx.*

class ResourceGroupCard(group: ResourceGroupCardItem) : VBox() {
    companion object {
        const val RENDER_BATCH_SIZE = 10
    }
    init {
        importStylesheet<ResourceGroupCardStyles>()

        addClass(ResourceGroupCardStyles.resourceGroupCard)
        label(group.title)

        group.resources.buffer(RENDER_BATCH_SIZE).subscribe { items ->
            Platform.runLater {
                items.forEach {
                    add(
                        resourcecard(it).root
                    )
                }
            }
        }
    }
}

fun resourcegroupcard(group: ResourceGroupCardItem, init: ResourceGroupCard.() -> Unit = {}): ResourceGroupCard {
    val rgc = ResourceGroupCard(group)
    rgc.init()
    return rgc
}