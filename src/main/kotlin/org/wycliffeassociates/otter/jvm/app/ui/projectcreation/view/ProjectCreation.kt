package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import org.wycliffeassociates.otter.jvm.app.ui.imageLoader
import org.wycliffeassociates.otter.jvm.app.ui.styles.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectBook
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectLanguage
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectResource
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments.SelectAnthology
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeView
import org.wycliffeassociates.otter.jvm.app.widgets.progressstepper.ProgressStepper
import tornadofx.*
import java.io.File

class ProjectCreationWizard : Wizard() {

    val creationViewModel: ProjectCreationViewModel by inject()

    val steps = listOf(
            MaterialIconView(MaterialIcon.RECORD_VOICE_OVER, "16px"),
            MaterialIconView(MaterialIcon.COLLECTIONS_BOOKMARK, "16px"),
            imageLoader(File(ClassLoader.getSystemResource("assets/Cross.svg").toURI())),
            MaterialIconView(MaterialIcon.BOOK, "16px")
    )
    override val canGoNext = currentPageComplete
    override val canFinish = creationViewModel.allPagesComplete

    init {
        showStepsHeader = false
        showSteps = false
        showHeader = true
        enableStepLinks = true
        root.top =
                ProgressStepper(steps).apply {
                    currentPageProperty.onChange {
                        activeIndex = pages.indexOf(currentPage)
                    }
                    addEventHandler(ActionEvent.ACTION) {
                        currentPage = pages[activeIndex]
                    }
                    addClass(ProjectWizardStyles.stepper)
                }
        root.bottom  {
            buttonbar {
                padding = Insets(10.0)

                button(messages["back"]){
                    addClass(ProjectWizardStyles.wizardButton)
                    enableWhen(canGoBack)
                    action {
                        back()
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
                    action {
                        onCancel()
                    }
                }
            }
        }

        add(SelectLanguage::class)
        add(SelectResource::class)
        add(SelectAnthology::class)
        add(SelectBook::class)
        val buttonBar = root.bottom as ButtonBar
        backButtonTextProperty.set(messages["back"])
        nextButtonTextProperty.set(messages["next"])
        cancelButtonTextProperty.set(messages["cancel"])
        finishButtonTextProperty.set(messages["finish"])
        buttonBar.buttonOrder = listOf(
                ButtonBar.ButtonData.BACK_PREVIOUS.typeCode,
                ButtonBar.ButtonData.BIG_GAP.typeCode,
                ButtonBar.ButtonData.CANCEL_CLOSE.typeCode,
                ButtonBar.ButtonData.NEXT_FORWARD.typeCode,
                ButtonBar.ButtonData.FINISH.typeCode
        ).joinToString("")
        buttonBar.buttons.forEach {
            val button = it as Button
            val data = ButtonBar.getButtonData(button)
            when (data) {
                ButtonBar.ButtonData.NEXT_FORWARD -> {
                    button.addClass(ProjectWizardStyles.nextButton)
                }
                else -> {}
            }
        }
    }

    override fun onCancel() {
        workspace.dock<ProjectHomeView>()
        //this.close()
    }

}