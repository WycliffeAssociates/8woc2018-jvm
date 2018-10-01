package org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox

/**
 * This interface is used to hold the data that is selected from within the widget.
 *
 * @author Caleb Benedick
 */
interface ComboBoxSelectionItem {
    val labelText : String
    val filterText : List<String>
}