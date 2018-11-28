package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.fragments

import javafx.collections.FXCollections
import javafx.util.StringConverter
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox.filterablecombobox
import tornadofx.*

class SelectLanguage : Fragment() {
    private val viewModel: ProjectWizardViewModel by inject()

    override val complete = viewModel.languagesValid()

    init {
        importStylesheet<ProjectWizardStyles>()
    }

    override val root = hbox {
        addClass(ProjectWizardStyles.selectLanguageRoot)
        vbox {
            label(messages["sourceLanguage"], ProjectWizardStyles.sourceLanguageIcon()) {
                addClass(ProjectWizardStyles.languageBoxLabel)
            }
            filterablecombobox(viewModel.sourceLanguageProperty, viewModel.languages) {
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
            label(messages["targetLanguage"], ProjectWizardStyles.targetLanguageIcon()) {
                addClass(ProjectWizardStyles.languageBoxLabel)
            }
            filterablecombobox(viewModel.targetLanguageProperty, viewModel.languages) {
                // Don't display the source language in the list
                viewModel.sourceLanguageProperty.onChange {
                    items = if (it == null) viewModel.languages
                    else {
                        FXCollections.observableArrayList(viewModel.languages.filter {
                            it != viewModel.sourceLanguageProperty.value
                        })
                    }
                }
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

    override fun onSave() {
        viewModel.getRootSources()
    }
}