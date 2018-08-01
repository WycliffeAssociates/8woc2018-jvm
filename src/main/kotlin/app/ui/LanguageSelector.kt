package app.ui

import data.model.Language
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import widgets.*
import java.util.ResourceBundle

/**
 * This class creates a Fragment containing a FilterableComboBox that allows the user to select an item from a dropdown
 * list and see a chip created in a flowpane underneath.
 *
 * Language Selector takes in a list of languages to be selected, and it communicates with the ViewModel to update the
 * list of chips and the current selected/preferred chip. It controls the color change of the chips, which
 * change color depending on whether or not they are selected. When new data is added, it redraws the flowpane with
 * chips in the correct order and color.
 *
 * @author Caleb Benedick and Kimberly Horton
 *
 * @param languages List of languages the user can choose from.
 * @param label Text to appear above the comboBox
 * @param hint Text to appear inside the textfield before the user starts typing input
 * @param colorAccent The color of the comboBox border and background of a selected chip
 * @param updateLanguages A PublishSubject that updates the Fragment and higher views whenever a new item is selected
 * from the comboBox or removed from the selections
 * @param preferredLanguage A PublishSubject that updates the Fragment and higher views whenever the selected/preferred
 * chip changes
 */
class LanguageSelector(
        languages: List<Language>,
        label: String,
        labelIcon: MaterialIconView,
        hint: String,
        colorResourcesFile: String,
        private val colorAccent: Color,
        private val updateLanguages: PublishSubject<Language>,
        private val preferredLanguage: PublishSubject<Language>
) : Fragment() {

    private val selectionData : List<ComboBoxSelectionItem>
    private val compositeDisposable : CompositeDisposable
    private val chips : MutableList<Chip>
    private val viewModel : LanguageSelectorViewModel

    override val root = VBox()

    init {
        messages = ResourceBundle.getBundle(colorResourcesFile)

        compositeDisposable = CompositeDisposable()
        selectionData = languages.map { LanguageSelectionItem(it) }
        chips = mutableListOf()
        viewModel = LanguageSelectorViewModel(updateLanguages, preferredLanguage, languages)

        with(root) {

            alignment = Pos.CENTER

            hbox {
                setId("labelIconHBox")

                labelIcon.fill = colorAccent
                add(labelIcon)

                label(" " + label) {
                    setId("comboBoxLabel")
                    style {
                        textFill = colorAccent
                    }
                }
            }

            this += FilterableComboBox(selectionData, hint, viewModel::addNewValue).apply {
                style {
                    focusColor = colorAccent
                    borderColor = multi(box(colorAccent))
                }
            }
            separator()

            flowpane {

                /** Redraw the flowpane with any new data */
                compositeDisposable.add(
                        updateLanguages.subscribe {
                            val language = it
                            val check = chips.map { it.slugLabel.text == language.slug }

                            if (check.contains(true)) {
                                chips.removeAt(check.indexOf(true))
                            } else {
                                chips.add(0,
                                        Chip(
                                                language.slug,
                                                language.name,
                                                viewModel::removeLanguage,
                                                viewModel::newPreferredLanguage
                                        ).apply {
                                            setOnMouseEntered {
                                                effect = DropShadow(5.0, colorAccent)
                                            }
                                            setOnMouseExited {
                                                effect = null
                                            }
                                        }
                                )
                            }

                            this.requestFocus()
                            children.clear()
                            children.addAll(chips)
                        }
                )

                /** Change the chip colors based on which one is selected */
                compositeDisposable.add(
                        preferredLanguage.subscribe {
                            newSelected(it.slug)
                        }
                )

                vgrow = Priority.ALWAYS
                hgap = 6.0
                vgap = 6.0
            }

            padding = Insets(40.0)
            spacing = 10.0
        }
    }

    /** Change the highlighted chip to the one most recently clicked */
    private fun newSelected(language: String) {
        for (chip in chips) {
            if (chip.slug == language) {
                chip.slugLabel.textFill = c(messages["UI_NEUTRAL"])
                chip.button.fill = colorAccent
            } else {
                chip.slugLabel.textFill = c(messages["UI_NEUTRAL_TEXT"])
                chip.button.fill = c(messages["UI_NEUTRAL"])
            }
        }
    }

    /** Dispose of disposables */
    override fun onUndock() {
        super.onUndock()
        compositeDisposable.clear()
    }
    override fun onDelete() {
        super.onDelete()
        compositeDisposable.clear()
    }

}