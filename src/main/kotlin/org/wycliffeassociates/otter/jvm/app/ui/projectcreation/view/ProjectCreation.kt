package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Node
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.*
import org.wycliffeassociates.otter.jvm.app.ui.styles.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeView
import org.wycliffeassociates.otter.jvm.app.ui.styles.AppStyles
import org.wycliffeassociates.otter.jvm.app.widgets.progressOverlay
import tornadofx.*

class ProjectCreationWizard : Wizard() {

    val creationViewModel: ProjectCreationViewModel by inject()

    val steps = FXCollections.observableArrayList<Node>(
            MaterialIconView(MaterialIcon.RECORD_VOICE_OVER, "16px"),
            MaterialIconView(MaterialIcon.COLLECTIONS_BOOKMARK, "16px")
    )
    override val canGoNext = currentPageComplete

    init {
        showStepsHeader = false
        showSteps = false
        showHeader = true
        enableStepLinks = true
        root.bottom {
            buttonbar {
                padding = Insets(10.0)

                button(messages["back"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(canGoBack.and(!creationViewModel.showOverlayProperty))
                    action {
                        creationViewModel.goBack(this@ProjectCreationWizard)
                    }
                }

                button(messages["next"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(canGoNext.and(hasNext))
                    action {
                        next()
                    }
                }

                button(messages["cancel"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(!creationViewModel.showOverlayProperty)
                    action {
                        onCancel()
                    }
                }
            }
        }

        add(SelectLanguage::class)
        add(SelectCollection::class)

        creationViewModel.creationCompletedProperty.onChange {
            if (it) {
                runLater {
                    creationViewModel.reset()
                    currentPage = pages[0]
                    close()
                }
            }
        }
    }

    override fun onCancel() {
        creationViewModel.reset()
        currentPage = pages[0]
        close()
    }

}