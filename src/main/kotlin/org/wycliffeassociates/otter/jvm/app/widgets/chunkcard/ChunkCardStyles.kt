package org.wycliffeassociates.otter.jvm.app.widgets.chunkcard

import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class ChunkCardStyles : Stylesheet() {
    companion object {
        val defaultChunkCard by cssclass()
        val titleLabel by cssclass()
        val selectedTakeLabel by cssclass()
    }

    init {
        defaultChunkCard {
            alignment = Pos.CENTER
            spacing = 10.px
            backgroundRadius += box(10.px)
            borderRadius += box(10.px)
            padding = box(10.px)

            label {
                and(titleLabel) {
                    fontSize = 20.px
                }
                and(selectedTakeLabel) {
                    fontSize = 15.px
                }
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
        }
    }
}