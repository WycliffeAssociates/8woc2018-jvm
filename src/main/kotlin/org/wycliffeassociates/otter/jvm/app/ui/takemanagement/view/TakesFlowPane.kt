package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.PlayOrPauseEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.scripturetakecard
import tornadofx.*
import tornadofx.FX.Companion.messages

class TakesFlowPane(
    alternateTakes: ObservableList<Take>,
    private val audioPlayer: () -> IAudioPlayer,
    private val lastPlayOrPauseEvent: SimpleObjectProperty<PlayOrPauseEvent?>,
    private val recordNewTake: () -> Unit
): FlowPane() {
    init {
        importStylesheet<RecordScriptureStyles>()
        importStylesheet<AppStyles>()

        vgrow = Priority.ALWAYS
        addClass(RecordScriptureStyles.takeGrid)

        alternateTakes.onChange {
            updateTakeCards(it.list)
        }
    }

    private fun updateTakeCards(list: List<Take>) {
        clear()

        add(createRecordCard())
        list
            .sortedBy { take -> take.number }
            .map { take -> createTakeCard(take) }
            .forEach { add(it) }

        add(createBlankCard())
        add(createBlankCard())
    }

    private fun createTakeCard(take: Take): TakeCard {
        return scripturetakecard(take, audioPlayer(), lastPlayOrPauseEvent.toObservable()) {
//            addEventHandler(MouseEvent.MOUSE_PRESSED, ::startDrag)
        }
    }

    private fun createRecordCard(): VBox {
        return vbox(10.0) {
            alignment = Pos.CENTER
            addClass(RecordScriptureStyles.newTakeCard)
            label(messages["newTake"])
            button(messages["record"], AppStyles.recordIcon("25px")) {
                action {
                    recordNewTake()
                }
            }
        }
    }

    private fun createBlankCard(): VBox {
        return vbox(10.0) {
            alignment = Pos.CENTER
            addClass(RecordScriptureStyles.blankTakeCard)
            label("blank")
        }
    }
}