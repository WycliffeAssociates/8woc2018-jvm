package org.wycliffeassociates.otter.jvm.app.ui.collectionsgrid.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.Property
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.ui.collectionsgrid.viewmodel.CollectionsGridViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.app.widgets.card.card
import tornadofx.*

class CollectionsGrid : Fragment() {
    private val viewModel: CollectionsGridViewModel by inject()

    val activeCollection: Property<Collection> = viewModel.activeCollectionProperty
    val activeProject: Property<Collection> = viewModel.activeProjectProperty

    init {
        importStylesheet<CollectionGridStyles>()
        importStylesheet<DefaultStyles>()
    }

    override val root =  hbox {
            addClass(AppStyles.appBackground)
            addClass(CollectionGridStyles.panelStyle)
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            vbox {
                hgrow= Priority.ALWAYS
                progressindicator {
                    visibleProperty().bind(viewModel.loadingProperty)
                    managedProperty().bind(visibleProperty())
                    addClass(CollectionGridStyles.contentLoadingProgress)
                }
                scrollpane {
                    isFitToHeight = true
                    isFitToWidth = true
                    flowpane {
                        addClass(AppStyles.appBackground)
                        addClass(CollectionGridStyles.collectionsContainer)
                        bindChildren(viewModel.children) {
                            vbox {
                                add(card {
                                    addClass(DefaultStyles.defaultCard)
                                    cardfront {
                                        innercard(AppStyles.chapterGraphic()) {
                                            title = it.labelKey.toUpperCase()
                                            bodyText = it.titleKey
                                        }
                                        cardbutton {
                                            addClass(DefaultStyles.defaultCardButton)
                                            text = messages["openProject"]
                                            graphic = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px")
                                                    .apply { fill = AppTheme.colors.appRed }
                                            action {
                                                viewModel.selectCollection(it)
                                            }
                                        }
                                    }

                                })
                            }
                        }
                    }
                }
            }
        }
    }