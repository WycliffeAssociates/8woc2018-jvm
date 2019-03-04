package org.wycliffeassociates.otter.jvm.app.ui.menu.view

import com.github.thomasnield.rxkotlinfx.toObservable
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.MenuBar
import javafx.scene.control.ToggleGroup
import javafx.stage.FileChooser
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.view.AddPluginView
import org.wycliffeassociates.otter.jvm.app.ui.menu.viewmodel.MainMenuViewModel
import org.wycliffeassociates.otter.jvm.app.ui.removeplugins.view.RemovePluginsView
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import tornadofx.*
import tornadofx.FX.Companion.messages

class MainMenu : MenuBar() {

    private val viewModel: MainMenuViewModel = find()

    init {
        importStylesheet<MainMenuStyles>()
        with(this) {
            menu(messages["file"]) {
                item(messages["importResource"]) {
                    graphic = MainMenuStyles.importIcon("20px")
                    val dialog = progressdialog {
                        text = messages["importResource"]
                        graphic = MainMenuStyles.importIcon( "60px")
                        root.addClass(AppStyles.progressDialog)
                    }
                    viewModel.showImportDialogProperty.onChange {
                        Platform.runLater { if (it) dialog.open() else dialog.close() }
                    }
                    action {
//                        val file = chooseDirectory(messages["importResourceTip"])
//                        file?.let {
//                            viewModel.importContainerDirectory(file)
//                        }
                        val file = chooseFile(
                                messages["importResourceTip"],
                                arrayOf(FileChooser.ExtensionFilter("Zip files (*.zip)", "*.zip")),
                                FileChooserMode.Single).get(0)
                        file?.let {
                            viewModel.importContainerDirectory(file)
                        }
                    }
                }
            }
            menu(messages["audioPlugins"]) {
                onShowing = EventHandler {
                    viewModel.refreshPlugins()
                }
                item(messages["new"]) {
                    graphic = MainMenuStyles.addPluginIcon("20px")
                    action {
                        find<AddPluginView>().apply {
                            openModal()
                        }
                    }
                }
                item(messages["remove"]) {
                    graphic = MainMenuStyles.removePluginIcon("20px")
                    action {
                        find<RemovePluginsView>().apply {
                            openModal()
                        }
                    }
                }
                separator()
                menu(messages["audioRecorder"]) {
                    graphic = MainMenuStyles.recorderIcon("20px")
                    val pluginToggleGroup = ToggleGroup()
                    viewModel.recorderPlugins.onChange { _ ->
                        items.clear()
                        items.setAll(viewModel.recorderPlugins.map { pluginData ->
                            radiomenuitem(pluginData.name, pluginToggleGroup) {
                                userData = pluginData
                                action { if (isSelected) viewModel.selectRecorder(pluginData) }
                                viewModel.selectedRecorderProperty.toObservable().subscribe {
                                    isSelected = (it == pluginData)
                                }
                            }
                        })
                    }

                }
                menu(messages["audioEditor"]) {
                    graphic = MainMenuStyles.editorIcon("20px")
                    val pluginToggleGroup = ToggleGroup()
                    viewModel.editorPlugins.onChange { _ ->
                        items.clear()
                        items.setAll(viewModel.editorPlugins.map { pluginData ->
                            radiomenuitem(pluginData.name, pluginToggleGroup) {
                                userData = pluginData
                                action { if (isSelected) viewModel.selectEditor(pluginData) }
                                viewModel.selectedEditorProperty.toObservable().subscribe {
                                    isSelected = (it == pluginData)
                                }
                            }
                        })
                    }
                }
            }

        }
    }
}