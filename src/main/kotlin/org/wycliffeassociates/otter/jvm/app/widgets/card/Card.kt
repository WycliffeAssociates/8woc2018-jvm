package org.wycliffeassociates.otter.jvm.app.widgets.card

import javafx.scene.Node
import javafx.scene.layout.VBox
import tornadofx.*

class Card: VBox() {

    var cardChildren = observableList<Node>()


    fun cardfront(init: CardFront.() -> Unit = {}): CardFront {
        val cf = CardFront()
        cf.init()
        cardChildren.add(cf)
        return cf
    }

    // todo    private val cardBack: CardBack by singleAssign()

    init {
        cardChildren.onChange {
            it.list.forEach {
                add(it)
            }
        }
    }
}

fun card(init: Card.() -> Unit= {}): Card {
    val cd = Card()
    cd.init()
    return cd
}