package app.widgets

import app.UIColorsObject
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*


class WidgetsStyles : Stylesheet() {
    companion object {
        val alternateRectangleButton by cssclass()
        val rectangleButtonDefault by cssclass()
        val ProfileIcon by cssclass()
        val UsersListGrid by cssclass()
        val roundButtonLarge by cssclass()
        val roundButtonMedium by cssclass()
        val roundButtonMini by cssclass()
        val roundButton by cssclass()
        val usersListCell by cssclass()
    }

    init {
        val rectangleButtonStyle = mixin {
            minWidth = 100.0.px
            cursor = Cursor.HAND
            effect = DropShadow(10.0, c(UIColorsObject.Colors["baseBackground"]))
            and(hover) {
                scaleX = 1.1
                scaleY = 1.1
            }
        }
        val roundButtonStyle = mixin {
            backgroundColor += Color.WHITE
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            cursor = Cursor.HAND
            effect = DropShadow(10.0, Color.GRAY)
            borderColor += box(Color.TRANSPARENT)
            and(hover) {
                opacity = 0.90
                scaleX = 1.1
                scaleY = 1.1
            }
        }
        rectangleButtonDefault {
            +rectangleButtonStyle
            fill = c("#CC4141")
            backgroundColor += c("#FFF")
        }
        alternateRectangleButton {
            +rectangleButtonStyle
            fill = c("#FFF")
            backgroundColor += c("#CC4141")
        }
        ProfileIcon {
            +roundButtonStyle
        }
        roundButtonLarge {
            +roundButtonStyle
            minWidth = 64.0.px
            minHeight = 64.0.px
            prefWidth = 150.0.px
            prefHeight = 150.0.px
        }
        roundButtonMedium {
            +roundButtonStyle
            minWidth = 64.0.px
            minHeight = 64.0.px
            prefWidth = 120.0.px
            prefHeight = 120.0.px
        }
        roundButtonMini {
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            effect = DropShadow(10.0, Color.GRAY)
            minWidth = 64.0.px
            minHeight = 64.0.px
            prefWidth = 64.0.px
            prefHeight = 64.0.px
            cursor = Cursor.HAND
            backgroundColor += Color.WHITE
            borderColor += box(Color.TRANSPARENT)
        }
        UsersListGrid {
            verticalCellSpacing = 25.0.px
            backgroundColor += c("#DFDEE3")
            prefHeight = 800.0.px
            verticalCellSpacing = 24.0.px
            maxCellsInRow = 3
            horizontalCellSpacing = 32.0.px
        }
        roundButton {
            +roundButtonStyle
            minWidth = 64.0.px
            minHeight = 64.0.px
            borderColor += box(c("#0000"))
        }
        usersListCell {
            backgroundColor += Color.valueOf("#DFDEE3")
            alignment = Pos.CENTER
        }
    }
}