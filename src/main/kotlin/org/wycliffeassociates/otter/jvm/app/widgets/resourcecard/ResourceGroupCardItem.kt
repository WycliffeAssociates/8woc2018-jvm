package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard

import io.reactivex.Observable

data class ResourceGroupCardItem(
    val title: String,
    val resources: Observable<ResourceCardItem>
)