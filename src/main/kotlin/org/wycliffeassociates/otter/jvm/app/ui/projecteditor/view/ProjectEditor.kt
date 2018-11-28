package org.wycliffeassociates.otter.jvm.app.ui.projecteditor.view

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXToggleButton
import io.reactivex.Observable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.projecteditor.ChapterContext
import org.wycliffeassociates.otter.jvm.app.ui.projecteditor.viewmodel.ProjectEditorViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.chunkcard.ChunkCard
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import tornadofx.*

class ProjectEditor : View() {
    private val viewModel: ProjectEditorViewModel by inject()
    private var childrenList by singleAssign<ListView<Collection>>()

    init {
        importStylesheet<ProjectEditorStyles>()
    }

    override fun onDock() {
        super.onDock()
        // Make sure we refresh the chunks if need be
        // The chunk selected take could have changed since last docked
        viewModel.filteredChunks.forEach {
            // null the chunk and then reassign it to force
            // property on change to be called and update the bound card
            val tmp = it.first.value
            it.first.value = null
            it.first.value = tmp
        }
        viewModel.refreshActiveChunk()
    }

    override val root = stackpane {
        val snackBar = JFXSnackbar(this)
        viewModel.snackBarObservable.subscribe { message ->
            snackBar.enqueue(
                    JFXSnackbar.SnackbarEvent(message, messages["addPlugin"].toUpperCase(), 5000, false, EventHandler {
                        viewModel.addPlugin(message == messages["noRecorder"], message == messages["noEditor"])
                    })
            )
        }
        addClass(AppStyles.appBackground)
        hbox {
            vbox {
                label {
                    textProperty().bind(viewModel.projectTitleProperty)
                    addClass(ProjectEditorStyles.projectTitle)
                }
                childrenList = listview {
                    items = viewModel.children
                    vgrow = Priority.ALWAYS
                    addClass(ProjectEditorStyles.chapterList)
                    cellCache {
                        label(it.titleKey) {
                            graphic = AppStyles.chapterIcon("20px")
                        }
                    }
                    selectionModel.selectedIndexProperty().onChange {
                        // Tell the view model which child was selected
                        if (it >= 0) viewModel.selectChildCollection(viewModel.children[it])
                    }
                }
            }

            vbox {
                hgrow = Priority.ALWAYS
                hbox {
                    addClass(ProjectEditorStyles.backButtonContainer)
                    // Mode toggle
                    add(JFXToggleButton().apply {
                        text = messages["chapterMode"]
                        viewModel.chapterModeEnabledProperty.bind(selectedProperty())
                        addClass(ProjectEditorStyles.chapterModeToggleButton)
                    })
                    // Back button
                    add(JFXButton(messages["back"], AppStyles.backIcon()).apply {
                        action {
                            workspace.navigateBack()
                        }
                        addClass(AppStyles.backButton)
                    })
                }
                vbox {
                    vgrow = Priority.ALWAYS
                    progressindicator {
                        visibleProperty().bind(viewModel.loadingProperty)
                        managedProperty().bind(visibleProperty())
                        addClass(ProjectEditorStyles.chunksLoadingProgress)
                    }
                    datagrid(viewModel.filteredChunks) {
                        vgrow = Priority.ALWAYS
                        visibleProperty().bind(viewModel.loadingProperty.toBinding().not())
                        managedProperty().bind(visibleProperty())
                        cellCache { item ->
                            val chunkCard = ChunkCard()
                            chunkCard.chunkProperty().bind(item.first)
                            chunkCard.bindClass(cardContextCssRuleProperty())
                            chunkCard.bindClass(disabledCssRuleProperty(item.second))
                            chunkCard.bindClass(hasTakesCssRuleProperty(item.second))
                            if (item.first.value.labelKey == "chapter") {
                                // Special rendering
                                chunkCard.titleLabel.textProperty().unbind()
                                chunkCard.titleLabel.graphic = AppStyles.chapterIcon("30px")
                                chunkCard.titleLabel.text = viewModel.activeChildProperty.value.titleKey
                            }
                            viewModel.contextProperty.toObservable().subscribe { context ->
                                chunkCard.actionButton.visibleProperty().bind(
                                        item.second.or(viewModel.contextProperty.isEqualTo(ChapterContext.RECORD))
                                )
                                when (context ?: ChapterContext.RECORD) {
                                    ChapterContext.RECORD -> {
                                        chunkCard.actionButton.apply {
                                            graphic = AppStyles.recordIcon()
                                            text = messages["record"]
                                        }
                                    }
                                    ChapterContext.VIEW_TAKES -> {
                                        chunkCard.actionButton.apply {
                                            graphic = AppStyles.viewTakesIcon()
                                            text = messages["viewTakes"]
                                        }
                                    }
                                    ChapterContext.EDIT_TAKES -> {
                                        chunkCard.actionButton.apply {
                                            graphic = AppStyles.editIcon()
                                            text = messages["edit"]
                                        }
                                    }
                                }
                            }
                            chunkCard.actionButton.action { viewModel.doChunkContextualAction(chunkCard.chunk) }
                            // Add common classes
                            chunkCard.addClass(ProjectEditorStyles.chunkCard)
                            return@cellCache chunkCard
                        }
                    }
                    addClass(ProjectEditorStyles.chunkGridContainer)
                }
                listmenu {
                    orientation = Orientation.HORIZONTAL
                    useMaxWidth = true
                    addClass(ProjectEditorStyles.contextMenu)
                    item(graphic = AppStyles.recordIcon("25px")) {
                        activeItem = this
                        whenSelected { viewModel.changeContext(ChapterContext.RECORD) }
                        addClass(ProjectEditorStyles.recordMenuItem)
                        parent.layoutBoundsProperty().onChange { newBounds ->
                            newBounds?.let { prefWidth = it.width / items.size }
                        }
                    }
                    item(graphic = AppStyles.viewTakesIcon("25px")) {
                        whenSelected { viewModel.changeContext(ChapterContext.VIEW_TAKES) }
                        addClass(ProjectEditorStyles.viewMenuItem)
                        parent.layoutBoundsProperty().onChange { newBounds ->
                            newBounds?.let { prefWidth = it.width / items.size }
                        }
                    }
                    item(graphic = AppStyles.editIcon("25px")) {
                        whenSelected { viewModel.changeContext(ChapterContext.EDIT_TAKES) }
                        addClass(ProjectEditorStyles.editMenuItem)
                        parent.layoutBoundsProperty().onChange { newBounds ->
                            newBounds?.let { prefWidth = it.width / items.size }
                        }
                    }
                }
            }
        }

        val dialog = progressdialog {
            root.addClass(AppStyles.progressDialog)
            viewModel.contextProperty.toObservable().subscribe { newContext ->
                when (newContext) {
                    ChapterContext.RECORD -> graphic = AppStyles.recordIcon("60px")
                    ChapterContext.EDIT_TAKES -> graphic = AppStyles.editIcon("60px")
                    else -> { }
                }
            }
        }
        viewModel.showPluginActiveProperty.onChange { value ->
            Platform.runLater {
                if (value == true) {
                    dialog.open()
                } else {
                    dialog.close()
                }
            }
        }

    }

    private fun cardContextCssRuleProperty(): ObservableValue<CssRule> {
        val cssRuleProperty = SimpleObjectProperty<CssRule>()
        viewModel.contextProperty.toObservable().subscribe {
            cssRuleProperty.value = when (it ?: ChapterContext.RECORD) {
                ChapterContext.RECORD -> ProjectEditorStyles.recordContext
                ChapterContext.VIEW_TAKES -> ProjectEditorStyles.viewContext
                ChapterContext.EDIT_TAKES -> ProjectEditorStyles.editContext
            }
        }
        return cssRuleProperty
    }

    private fun hasTakesCssRuleProperty(
            hasTakesProperty: SimpleBooleanProperty
    ): ObservableValue<CssRule> {
        val cssRuleProperty = SimpleObjectProperty<CssRule>()
        hasTakesProperty.toObservable().subscribe {
            cssRuleProperty.value = if (it == true) ProjectEditorStyles.hasTakes else null
        }
        return cssRuleProperty
    }

    private fun disabledCssRuleProperty(
            hasTakesProperty: SimpleBooleanProperty
    ): ObservableValue<CssRule> {
        val cssRuleProperty = SimpleObjectProperty<CssRule>()
        // Merge the observable to detect changes on either property
        Observable.merge(viewModel.contextProperty.toObservable(), hasTakesProperty.toObservable()).subscribe {
            cssRuleProperty.value = when (viewModel.contextProperty.value ?: ChapterContext.RECORD) {
                ChapterContext.RECORD -> null
                ChapterContext.VIEW_TAKES, ChapterContext.EDIT_TAKES -> {
                    if (hasTakesProperty.value) null else ProjectEditorStyles.disabledCard
                }
            }
        }
        return cssRuleProperty
    }
}



