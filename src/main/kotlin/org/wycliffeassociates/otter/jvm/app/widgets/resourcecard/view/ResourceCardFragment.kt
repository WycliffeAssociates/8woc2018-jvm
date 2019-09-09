package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.app.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.app.widgets.highlightablebutton.highlightablebutton
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.ResourceCardItem
import org.wycliffeassociates.otter.jvm.statusindicator.control.StatusIndicator
import org.wycliffeassociates.otter.jvm.statusindicator.control.statusindicator
import tornadofx.*

class ResourceCardFragment(private val item: ResourceCardItem) : Fragment() {
    private val navigator: ChromeableStage by inject()
    override val root = HBox()
    val isCurrentResourceProperty = SimpleBooleanProperty(false)
    var primaryColorProperty = SimpleObjectProperty<Color>(Color.ORANGE)
    var primaryColor: Color by primaryColorProperty

    init {
        root.apply {
            isFillHeight = false
            alignment = Pos.CENTER_LEFT
            maxHeight = 50.0

            vbox {
                spacing = 3.0
                hbox {
                    spacing = 3.0
                    add(
                        statusindicator {
                            initForResourceCard()
                            progressProperty.bind(item.titleProgressProperty)
                        }
                    )
                    add(
                        statusindicator {
                            initForResourceCard()
                            item.bodyProgressProperty?.let { progressProperty.bind(it) }
                            isVisible = item.hasBodyAudio
                        }
                    )
                }
                text(item.title)
                maxWidth = 150.0
            }

            region {
                hgrow = Priority.ALWAYS
            }

            add(
                highlightablebutton {
                    highlightColorProperty.bind(primaryColorProperty)
                    secondaryColor = Color.WHITE
                    isHighlightedProperty.bind(isCurrentResourceProperty)
                    graphic = MaterialIconView(MaterialIcon.APPS, "25px")
                    maxWidth = 500.0
                    text = messages["viewRecordings"]
                    action {
                        item.onSelect()
                        navigator.navigateTo(TabGroupType.RECORD_RESOURCE)
                    }
                }
            )
        }
    }

    private fun StatusIndicator.initForResourceCard() {
        prefWidth = 75.0
        primaryFillProperty.bind(primaryColorProperty)
        accentFill = Color.LIGHTGRAY
        trackFill = Color.LIGHTGRAY
        indicatorRadius = 3.0
    }
}

fun resourcecardfragment(
    resource: ResourceCardItem,
    init: ResourceCardFragment.() -> Unit = {}
): ResourceCardFragment {
    val rc = ResourceCardFragment(resource)
    rc.init()
    return rc
}
