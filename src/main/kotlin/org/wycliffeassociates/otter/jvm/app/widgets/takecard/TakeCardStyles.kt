package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.images.ImageLoader
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class TakeCardStyles : Stylesheet() {

    companion object {
        val scriptureTakeCard by cssclass()
        val resourceTakeCard by cssclass()
        val badge by cssclass()
        val iconStyle by cssclass()
        val content by cssclass()
        val takeNumberLabel by cssclass()
        val timestampLabel by cssclass()
        val defaultButton by cssclass()
        val takeprogressBar by cssclass()
        val editButton by cssclass()
        val topHalf by cssclass()
        val defaultGreen : Color = c("#58BD2F")
        val grey = c("#C9C8C8")

        fun draggingIcon() = ImageLoader.load(
            ClassLoader.getSystemResourceAsStream("images/baseline-drag_indicator-24px.svg"),
            ImageLoader.Format.SVG
        )
    }

    init {
        takeNumberLabel {
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
        }

        takeprogressBar {
            track {
                backgroundColor += Color.LIGHTGRAY
                backgroundRadius += box(5.0.px)
            }
            bar {
                backgroundColor += c("#0094F0")
                backgroundRadius += box(5.0.px)
            }
        }

        // RESOURCE TAKE CARD specific styles
        resourceTakeCard {
            minHeight = 80.px
            maxWidth = 500.px
            borderColor += box(grey)
            borderRadius += box(5.px)
            topHalf {
                padding = box(4.px, 5.px, 5.px, 5.px)
                borderColor += box(Color.TRANSPARENT, Color.TRANSPARENT, c("C9C8C8"), Color.TRANSPARENT)
                borderWidth += box(1.px)
            }
            takeprogressBar {
                track {
                    minHeight = 30.px
                }
                bar {
                    minHeight = 30.px
                }
            }
        }

        // SCRIPTURE TAKE CARD specific styles
        scriptureTakeCard {
            borderRadius += box(5.px)
            borderColor += box(AppTheme.colors.imagePlaceholder)
            borderWidth += box(1.px)
            backgroundColor += AppTheme.colors.cardBackground
            label {
                textFill = AppTheme.colors.defaultText
            }
            minWidth = 348.px
            maxWidth = minWidth
            minHeight = 200.px
            maxHeight = minHeight
            backgroundRadius += box(5.px)
            badge {
                backgroundColor += AppTheme.colors.appRed
                backgroundRadius += box(0.px, 10.px, 0.px, 10.px)
                padding = box(8.px)
                iconStyle {
                    fill = Color.WHITE
                }
            }
            padding = box(5.px)
            content {
                padding = box(10.px)
            }
            takeNumberLabel {
                graphicTextGap = 7.5.px
            }
            timestampLabel {
                fontSize = 12.px
                fontWeight = FontWeight.LIGHT
                fontStyle = FontPosture.ITALIC
                textFill = Color.LIGHTGRAY
                padding = box(2.5.px)
            }
            button {
                backgroundColor += Color.TRANSPARENT
            }
            defaultButton {
                minHeight = 40.px
                minWidth = 150.px
                borderRadius += box(5.0.px)
                backgroundRadius += box(5.0.px)
                borderColor += box(Color.LIGHTGRAY)
                borderWidth += box(0.5.px)
                effect = DropShadow(1.0,2.0,2.0,Color.LIGHTGRAY)
                backgroundColor += Color.WHITE
            }

            editButton {
                textFill = TakeCardStyles.defaultGreen
            }

            takeprogressBar {
                track {
                    minHeight = 40.px
                }
                bar {
                    minHeight = 40.px
                }
            }
        }
    }
}