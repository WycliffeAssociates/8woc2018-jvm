package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class ResourceTakeCardSkin(control: TakeCard) : TakeCardSkin(control) {

    private var container: VBox

    init {
        importStylesheet<TakeCardStyles>()

        container = VBox().apply {
            addClass(TakeCardStyles.resourceTakeCard)

            hbox(10.0) {
                addClass(TakeCardStyles.topHalf)
                alignment = Pos.CENTER_LEFT

                label("%02d.".format(control.take.number), TakeCardStyles.draggingIcon()) {
                    addClass(TakeCardStyles.takeNumberLabel)
                }
                add(control.simpleAudioPlayer.apply {
                    addClass(TakeCardStyles.takeProgressBar)
                    vgrow = Priority.ALWAYS
                    hgrow = Priority.ALWAYS
                })
            }

            hbox {
                add(JFXButton("", MaterialIconView(MaterialIcon.EDIT, "18px")))
                alignment = Pos.CENTER
                hbox {
                    hgrow = Priority.ALWAYS
                    alignment = Pos.CENTER
                    add(JFXButton("", MaterialIconView(MaterialIcon.SKIP_PREVIOUS, "18px")))
                    add(playButton)
                    add(JFXButton("", MaterialIconView(MaterialIcon.FAST_FORWARD, "18px")))
                }
                add(JFXButton("", MaterialIconView(MaterialIcon.DELETE, "18px")))
            }
        }

        children.setAll(container)
    }
}