package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model

import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Resource
import org.commonmark.parser.Parser
import org.commonmark.renderer.text.TextContentRenderer

data class ResourceCardItem(val resource: Resource, val onSelect: () -> Unit) {
    val title: String = getTitleTextContent()
    private val disposables = CompositeDisposable()
    val titleProgressProperty: DoubleProperty = resource.titleAudio.progressProperty()
    val bodyProgressProperty: DoubleProperty? = resource.bodyAudio?.progressProperty()
    val hasBodyAudio: Boolean = resource.bodyAudio != null

    fun clearDisposables() {
        disposables.clear()
    }

    private fun AssociatedAudio.progressProperty(): DoubleProperty {
        val progressProperty = SimpleDoubleProperty(0.0)
        val sub = this.selected.subscribe {
            progressProperty.set( if (it.value != null) 1.0 else 0.0)
        }
        disposables.add(sub)
        return progressProperty
    }

    companion object {
        val parser: Parser = Parser.builder().build()
        val renderer: TextContentRenderer = TextContentRenderer.builder().build()
    }

    private fun getTitleTextContent(): String {
        val document = parser.parse(resource.title.text)
        return renderer.render(document)
    }
}