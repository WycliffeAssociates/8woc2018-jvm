package org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view

import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class MainScreenStyles : Stylesheet() {
    companion object {
        val main by cssclass()
        val listMenu by cssclass()
        val listItem by cssclass()
        val belowMenuBar by cssclass()
        val navBoxInnercard by cssclass()
        val navbutton by cssclass()
        val menuBarHeight = 55.px
        val workingArea by cssclass()
        val scripture by cssclass()
        val translationNotes by cssclass()
    }

    init {

        main {
            prefWidth = Screen.getPrimary().visualBounds.width.px - 20.0
            prefHeight = Screen.getPrimary().visualBounds.height.px - 20.0
        }

        // this gets compiled down to list-menu
        listMenu {
            backgroundColor += AppTheme.colors.defaultBackground
            prefHeight = menuBarHeight
        }

        //this gets compiled down to list-item
        listItem {
            backgroundColor += AppTheme.colors.defaultBackground
            padding = box(24.px)
        }

        belowMenuBar {
            padding = box(menuBarHeight, 0.px, 0.px, 0.px)
        }

        workingArea {
            backgroundColor += AppTheme.colors.workingAreaBackground
        }
    }
}