package org.wycliffeassociates.otter.jvm.app.ui.styles

import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import tornadofx.*

class AppStyles : Stylesheet() {

    companion object {
        val datagridStyle by cssclass()
        val addProjectButton by cssclass()
        val refreshButton by cssclass()
        val cardButton by cssclass()
    }

    init {
        datagridStyle {
            cell {
                backgroundColor += Color.TRANSPARENT
            }
            effect = DropShadow(8.0, 3.0, 3.0, c(UIColorsObject.Colors["dropShadow"]))
            backgroundRadius += box(10.0.px)
            borderRadius += box(10.0.px)
            cellHeight = 250.0.px
            cellWidth = 232.0.px
            horizontalCellSpacing = 10.0.px
        }

        addProjectButton {
            padding = box(15.0.px)
            backgroundRadius += box(100.0.px)
            borderRadius += box(100.0.px)
            backgroundColor += c(UIColorsObject.Colors["primary"])
            effect = DropShadow(8.0, c(UIColorsObject.Colors["dropShadow"]))
            prefHeight = 50.0.px
            prefWidth = 50.0.px
            cursor = Cursor.HAND
        }

        refreshButton {
            prefWidth = 232.0.px
            prefHeight = 40.0.px
            backgroundColor += c(UIColorsObject.Colors["base"])
            textFill = c(UIColorsObject.Colors["primary"])
            effect = DropShadow(2.0, c(UIColorsObject.Colors["dropShadow"]))
            backgroundRadius += box(5.0.px)
            borderRadius += box(5.0.px)
            cursor = Cursor.HAND
        }

        cardButton {
            prefWidth = 232.0.px
            prefHeight = 40.0.px
            backgroundColor += c(UIColorsObject.Colors["primary"])
            textFill = c(UIColorsObject.Colors["base"])
            cursor = Cursor.HAND
            fontSize = (16.0.px)
            fontWeight = FontWeight.BLACK
        }
    }

}
