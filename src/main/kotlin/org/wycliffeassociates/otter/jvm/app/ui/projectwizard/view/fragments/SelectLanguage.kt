package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.fragments

import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.ProjectWizardStyles
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.searchablelist.searchablelist
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
            addClass(ProjectWizardStyles.languageSearchContainer)
            label(messages["sourceLanguage"], ProjectWizardStyles.sourceLanguageIcon()) {
                addClass(ProjectWizardStyles.languageBoxLabel)
            }
            searchablelist(viewModel.languages, viewModel.sourceLanguageProperty) {
                addClass(ProjectWizardStyles.searchableList)
                listView.cellCache { language ->
                    label("${language.name} (${language.slug})")
                }
                searchField.promptText = messages["comboBoxPrompt"]
                autoSelect = true
                filter(viewModel::filterLanguages)
                viewModel.clearLanguages.subscribe {
                    searchField.clear()
                    listView.selectionModel.clearSelection()
                }
                viewModel.sourceLanguageProperty.addValidator(searchField) {
                    if (it == null) error("Source language is required") else null
                }
            }
        }

        vbox {
            addClass(ProjectWizardStyles.languageSearchContainer)
            label(messages["targetLanguage"], ProjectWizardStyles.targetLanguageIcon()) {
                addClass(ProjectWizardStyles.languageBoxLabel)
            }
            searchablelist(viewModel.languages, viewModel.targetLanguageProperty) {
                addClass(ProjectWizardStyles.searchableList)
                listView.cellCache { language ->
                    label("${language.name} (${language.slug})")
                }
                searchField.promptText = messages["comboBoxPrompt"]
                autoSelect = true
                filter(viewModel::filterLanguages)
                viewModel.clearLanguages.subscribe {
                    searchField.clear()
                    listView.selectionModel.clearSelection()
                }
                viewModel.targetLanguageProperty.addValidator(searchField) {
                    if (it == null) error("Target language is required") else null
                }
            }
        }
    }

    override fun onSave() {
        viewModel.getRootSources()
    }
}