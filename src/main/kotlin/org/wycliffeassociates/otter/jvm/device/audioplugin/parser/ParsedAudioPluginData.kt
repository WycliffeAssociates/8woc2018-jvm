package org.wycliffeassociates.otter.jvm.device.audioplugin.parser

data class ParsedAudioPluginData(
        var name: String,
        var version: String,
        var canEdit: Boolean,
        var canRecord: Boolean,
        var executable: ParsedExecutable,
        var args: List<String>
)