package org.wycliffeassociates.otter.jvm.app.ui.projecthome

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.style.ProjectHomeStyles
import org.wycliffeassociates.otter.jvm.app.widgets.projectcard
import tornadofx.*

class ProjectHomeView : View() {

    init {
        importStylesheet<ProjectHomeStyles>()
    }

    val viewModel: ProjectHomeViewModel by inject()
    override val root = borderpane {
        top = hbox {
            alignment = Pos.CENTER_RIGHT
            button("Refresh") {
              addClass(ProjectHomeStyles.refreshButton)
                action {
                    viewModel.getAllProjects()
                }
                style {
                    backgroundColor += c(Colors["base"])
                    textFill = c(Colors["primary"])
                }

            }
            style{
                padding = box(15.0.px)
            }
        }
        style {
            setPrefSize(1200.0, 800.0)
        }
        center = datagrid(viewModel.allProjects) {
            addClass(ProjectHomeStyles.datagridStyle)
            cellCache = {
                projectcard(it) {
                    style {
                        backgroundColor += c(Colors["base"])
                        backgroundRadius += box(10.0.px)
                        borderRadius += box(10.0.px)
                    }
                    buttonText = "Load Project"
                    loadButton.apply{
                        addClass(ProjectHomeStyles.cardButton)
                        style{
                            effect = DropShadow(0.0, Color.TRANSPARENT)
                            backgroundColor += c(Colors["primary"])
                            textFill = c(Colors["base"])
                        }
                    }
                }
            }
        }

        bottom = hbox {
            style {
                padding = box(25.0.px)
            }
            alignment = Pos.BOTTOM_RIGHT
            val icon = MaterialIconView(MaterialIcon.ADD)
            icon.fill = c(Colors["base"])
            button("", icon) {
                addClass(ProjectHomeStyles.addProjectButton)
                action {
                    viewModel.createProject()
                }
            }
        }
    }

    override fun onDock() {
        viewModel.getAllProjects()
    }
}