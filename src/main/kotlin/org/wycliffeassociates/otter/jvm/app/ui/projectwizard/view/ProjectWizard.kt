package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view

import javafx.geometry.Pos
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.fragments.SelectLanguage
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.progressstepper.ProgressStepper
import tornadofx.*

class ProjectWizard : View() {
    override val root = borderpane {}
    private val wizardViewModel: ProjectWizardViewModel by inject()
    val wizardWorkspace = Workspace()

    init {
        importStylesheet<ProjectWizardStyles>()
        root.addClass(AppStyles.appBackground)

        root.top {
            vbox (16.0){
                alignment = Pos.CENTER
                label("Language", AppStyles.chapterIcon("40px")) {
                    style {
                        textFill = Color.TOMATO
                        fontSize = 32.px
                    }
                }
                add(ProgressStepper())
            }
        }
        root.center {
            add(wizardWorkspace)
            wizardWorkspace.header.removeFromParent()
            wizardWorkspace.dock(SelectLanguage())
        }
        root.bottom {
            buttonbar {
                paddingAll = 40.0
                button(messages["cancel"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    action {
                        wizardViewModel.closeWizard()
                    }
                }
                button(messages["back"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(wizardViewModel.canGoBack and !wizardViewModel.showOverlayProperty)
                    action {
                        wizardViewModel.goBack()
                    }
                }
                button(messages["next"]) {
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(wizardViewModel.languagesValid())
                    visibleWhen(!wizardViewModel.languageConfirmed)
                    action {
                        wizardViewModel.goNext()
                    }
                }
            }
        }
        wizardViewModel.creationCompletedProperty.onChange {
            if (it) {
                runLater {
                    wizardViewModel.closeWizard()
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        wizardViewModel.reset()
    }

    override fun onUndock() {
        super.onUndock()
        wizardWorkspace.navigateBack()
        wizardViewModel.reset()
    }
}