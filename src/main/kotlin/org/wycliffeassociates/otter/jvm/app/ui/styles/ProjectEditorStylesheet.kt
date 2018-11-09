package org.wycliffeassociates.otter.jvm.app.ui.styles

import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.*

class ProjectPageStylesheet : Stylesheet() {
    companion object {
        val chunkCard by cssclass()
        val disabledCard by cssclass()
        val cardTitleLabel by cssclass("title")
        val selectedTakeLabel by cssclass("selected-take")

        val recordContext by cssclass()
        val hasTakes by cssclass()
        val editContext by cssclass()
        val viewContext by cssclass()

        val recordMenuItem by cssclass()
        val editMenuItem by cssclass()
        val viewMenuItem by cssclass()

        val projectTitle by cssclass()
        val chunkGridContainer by cssclass()

        val active by csspseudoclass("active")

        val listmenu by cssclass("list-menu")

        val chapterList by cssclass()

        val chunkLoadingProgress by cssclass()
    }

    init {
        projectTitle {
            fontSize = 20.px
            padding = box(10.px)
            backgroundColor += Color.DARKGRAY
            textFill = Color.WHITE
            maxWidth = Double.MAX_VALUE.px
            alignment = Pos.BOTTOM_LEFT
            prefHeight = 100.px
        }
        chunkGridContainer {
            padding = box(0.px, 20.px)
        }
        datagrid {
            cellWidth = 200.px
            cellHeight = 200.px
            cell {
                backgroundColor += Color.TRANSPARENT
            }
        }
        chunkCard {
            backgroundColor += c(Colors["base"])
            effect = DropShadow(10.0, Color.LIGHTGRAY)
            backgroundRadius += box(10.px)
            borderRadius += box(10.px)
            padding = box(10.px)

            label {
                textFill = c(Colors["baseText"])
            }

            and(disabledCard) {
                backgroundColor += c(Colors["baseBackground"])
            }

            cardTitleLabel {
                fontSize = 20.px
            }

            selectedTakeLabel {
                fontSize = 15.px
            }

            button {
                unsafe("-jfx-button-type", raw("RAISED"))
                textFill = Color.WHITE
                fontSize = 16.px
                child("*") {
                    fill = Color.WHITE
                }
                maxWidth = Double.MAX_VALUE.px
                fillWidth = true
            }
            and(recordContext) {
                button {
                    backgroundColor += c(Colors["primary"])
                }
                and(hasTakes) {
                    button {
                        backgroundColor += Color.WHITE
                        borderRadius += box(3.px)
                        borderColor += box(c(Colors["primary"]))
                        textFill = c(Colors["primary"])
                        child("*") {
                            fill = c(Colors["primary"])
                        }
                    }
                }
            }
            and(viewContext) {
                button {
                    backgroundColor += c(Colors["secondary"])
                }
            }
            and(editContext) {
                button {
                    backgroundColor += c(Colors["tertiary"])
                }
            }
        }

        listmenu {
            effect = DropShadow(10.0, Color.LIGHTGRAY)
        }

        s(recordMenuItem, viewMenuItem, editMenuItem) {
            padding = box(20.px)
            backgroundColor += Color.WHITE
            and(hover, active) {
                child("*") {
                    fill = Color.WHITE
                }
            }
        }

        recordMenuItem {
            and(hover, active) {
                backgroundColor += c(Colors["primary"])
            }
            child("*") {
                fill = c(Colors["primary"])
            }
        }

        viewMenuItem {
            and(hover, active) {
                backgroundColor += c(Colors["secondary"])
            }
            child("*") {
                fill = c(Colors["secondary"])
            }
        }

        editMenuItem {
            and(hover, active) {
                backgroundColor += c(Colors["tertiary"])
            }
            child("*") {
                fill = c(Colors["tertiary"])
            }
        }

        chapterList {
            focusColor = Color.TRANSPARENT
            faintFocusColor = Color.TRANSPARENT
            borderWidth += box(0.px)
            padding = box(10.px, 0.px, 0.px, 10.px)
            listCell {
                padding = box(0.px, 0.px, 0.px, 20.px)
                backgroundColor += Color.WHITE
                backgroundRadius += box(10.px)
                fontSize = 14.px
                fontWeight = FontWeight.BOLD
                prefHeight = 40.px
                and(hover) {
                    backgroundColor += Color.WHITE.deriveColor(
                            1.0, 1.0,
                            0.95, 1.0
                    )
                }
                and(selected) {
                    backgroundColor += c(Colors["primary"])
                    textFill = Color.WHITE
                }
            }
        }

        chunkLoadingProgress {
            progressColor = c(Colors["primary"])
        }
    }
}