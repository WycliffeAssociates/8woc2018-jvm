package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.jvm.app.ui.languageselectorfragment.LanguageSelectionItem
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox.ComboBoxSelectionItem
import org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox.FilterableComboBox
import tornadofx.*

class SelectLanguage : View() {
    val model = SelectLanguageModel()
    val viewModel: ProjectCreationViewModel by inject()
    val selectionData: List<ComboBoxSelectionItem>
        get() = viewModel.languageList.map { LanguageSelectionItem(it) }

    override val complete = viewModel.valid(viewModel.sourceLanguage, viewModel.targetLanguage)

    init {

    }

    override val root = hbox {
        alignment = Pos.CENTER
        style {
            padding = box(100.0.px)
        }
        hbox(100.0) {
            anchorpaneConstraints {
                leftAnchor = 50.0
                topAnchor = 250.0
            }
            setPrefSize(600.0, 200.0)

            vbox {
                button("Target Language", MaterialIconView(MaterialIcon.RECORD_VOICE_OVER, "25px")) {
                    style {
                        backgroundColor += Color.TRANSPARENT
                    }
                }
                combobox(viewModel.targetLanguage, viewModel.languageList)
//                {
//                    makeAutocompletable(true) {
//                        language -> viewModel.languageList.filtered { it.name.contains(language) }
//                    }
//
//                    isEditable = true
//                    bind(viewModel.targetLanguage)
////                    text("Target Language")
//                }
                        .required()
//                this +=FilterableComboBox(selectionData,"Type here...", viewModel::setTarget)

            }

            vbox {

                button("Source Language", MaterialIconView(MaterialIcon.HEARING, "25px")) {
                    style {
                        backgroundColor += Color.TRANSPARENT
                    }
                }

                combobox(viewModel.sourceLanguage, viewModel.languageList)
//                {
//                    makeAutocompletable(true)
//                    isEditable = true
////                    text("Target Language")
//                }
                        .required()
//                this +=FilterableComboBox(selectionData,"Type here...", viewModel::setSource)
            }
        }
    }
}

class SelectLanguageModel {
    val languageVals = listOf<Language>()

}