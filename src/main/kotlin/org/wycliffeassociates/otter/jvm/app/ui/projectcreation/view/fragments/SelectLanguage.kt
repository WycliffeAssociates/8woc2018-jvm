package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.util.StringConverter
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.ui.styles.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox.filterablecombobox
import tornadofx.*

class SelectLanguage : View() {
    private val viewModel: ProjectCreationViewModel by inject()

    override val complete = viewModel.valid(viewModel.sourceLanguage, viewModel.targetLanguage)
    override val root = hbox {
        alignment = Pos.CENTER
        style {
            padding = box(100.0.px)
            setPrefSize(1200.0, 800.0)
        }
        hbox(100.0) {
            anchorpaneConstraints {
                leftAnchor = 50.0
                topAnchor = 250.0
            }
            setPrefSize(600.0, 200.0)

            vbox {
                label(messages["sourceLanguage"], MaterialIconView(MaterialIcon.HEARING, "25px")) {
                    style {
                        backgroundColor += Color.TRANSPARENT
                    }
                }
                filterablecombobox(viewModel.sourceLanguage, viewModel.languagesList) {
                    converter = object: StringConverter<Language>() {
                        override fun fromString(string: String?): Language? {
                            return items.filter { string?.contains("(${it.slug})") ?: false }.firstOrNull()
                        }

                        override fun toString(language: Language?): String {
                            return "${language?.name} (${language?.slug})"
                        }
                    }

                    filterConverter = { language ->
                        listOf(language.name, language.anglicizedName, language.slug)
                    }

                    addClass(ProjectWizardStyles.filterableComboBox)
                    promptText = messages["comboBoxPrompt"]
                }.required()
            }

            vbox {
                label(messages["targetLanguage"], MaterialIconView(MaterialIcon.RECORD_VOICE_OVER, "25px")) {
                    style {
                        backgroundColor += Color.TRANSPARENT
                    }
                }
                filterablecombobox(viewModel.targetLanguage, viewModel.languagesList) {
                    converter = object: StringConverter<Language>() {
                        override fun fromString(string: String?): Language? {
                            return items.filter { string?.contains("(${it.slug})") ?: false }.firstOrNull()
                        }

                        override fun toString(language: Language?): String {
                            return "${language?.name} (${language?.slug})"
                        }
                    }

                    filterConverter = { language ->
                        listOf(language.name, language.anglicizedName, language.slug)
                    }

                    addClass(ProjectWizardStyles.filterableComboBox)
                    promptText = messages["comboBoxPrompt"]
                }.required()
            }
        }

    }
    init {
        importStylesheet<ProjectWizardStyles>()
    }

    override fun onSave() {
        viewModel.commit(viewModel.sourceLanguage, viewModel.targetLanguage)
        viewModel.getRootSources()
    }
}