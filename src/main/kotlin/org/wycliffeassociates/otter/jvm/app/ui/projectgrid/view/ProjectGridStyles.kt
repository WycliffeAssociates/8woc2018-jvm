package org.wycliffeassociates.otter.jvm.app.ui.projectgrid.view

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class ProjectGridStyles : Stylesheet() {
    companion object {
        val projectsGrid by cssclass()
        val noProjectsLabel by cssclass()
        val tryCreatingLabel by cssclass()
        val addProjectButton by cssclass()
    }

    init {

        projectsGrid {
            vgap = 32.px
            hgap = 24.px
            horizontalCellSpacing = 24.0.px
            verticalCellSpacing = 32.0.px
            alignment = Pos.TOP_LEFT
            // Add larger padding on bottom to keep FAB from blocking last row cards
            padding = box(0.px, 0.px, 95.px, 0.px)
        }

        noProjectsLabel {
            fontSize = 30.px
            fontWeight = FontWeight.BOLD
            textFill = AppTheme.colors.defaultText
        }

        tryCreatingLabel {
            fontSize = 20.px
            textFill = AppTheme.colors.defaultText
        }

        addProjectButton {
            unsafe("-jfx-button-type", raw("RAISED"))
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            backgroundColor += AppTheme.colors.white
            minHeight = 50.px
            minWidth = 50.px
            maxHeight = 50.px
            maxWidth = 50.px
            cursor = Cursor.HAND
            child("*") {
                fill = AppTheme.colors.appRed
            }
        }
    }
}