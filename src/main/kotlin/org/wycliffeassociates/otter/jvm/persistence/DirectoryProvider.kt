package org.wycliffeassociates.otter.jvm.persistence

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.nio.file.FileSystems

class DirectoryProvider(
    private val appName: String,
    pathSeparator: String? = null,
    userHome: String? = null,
    windowsAppData: String? = null,
    osName: String? = null
) : IDirectoryProvider {
    private val pathSeparator = pathSeparator ?: FileSystems.getDefault().separator
    private val userHome = userHome ?: System.getProperty("user.home")
    private val windowsAppData = windowsAppData ?: System.getenv("APPDATA")
    private val osName = (osName ?: System.getProperty("os.name")).toUpperCase()

    // create a directory to store the user's application projects/documents
    override fun getUserDataDirectory(appendedPath: String): File {
        // create the directory if it does not exist
        val pathComponents = mutableListOf(userHome, appName)
        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)
        val pathString = pathComponents.joinToString(pathSeparator)
        val file = File(pathString)
        file.mkdirs()
        return file
    }

    // create a directory to store the application's private data
    override fun getAppDataDirectory(appendedPath: String): File {
        val pathComponents = mutableListOf<String>()

        when {
            osName.contains("WIN") -> pathComponents.add(windowsAppData)
            osName.contains("MAC") -> {
                // use /Users/<user>/Library/Application Support/ for macOS
                pathComponents.add(userHome)
                pathComponents.add("Library")
                pathComponents.add("Application Support")
            }
            osName.contains("LINUX") -> {
                pathComponents.add(userHome)
                pathComponents.add(".config")
            }
        }

        pathComponents.add(appName)

        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)

        // create the directory if it does not exist
        val pathString = pathComponents.joinToString(pathSeparator)
        val file = File(pathString)
        file.mkdirs()
        return file
    }

    override fun getProjectAudioDirectory(
        sourceMetadata: ResourceMetadata,
        book: Collection
    ): File {
        val appendedPath = listOf(
            book.resourceContainer?.creator ?: ".",
            sourceMetadata.creator,
            "${sourceMetadata.language.slug}_${sourceMetadata.identifier}",
            "v${book.resourceContainer?.version ?: "-none"}",
            book.resourceContainer?.language?.slug ?: "no_language",
            book.slug
        ).joinToString(pathSeparator)
        val path = getUserDataDirectory(appendedPath)
        path.mkdirs()
        return path
    }

    override fun getSourceContainerDirectory(container: ResourceContainer): File {
        val dublinCore = container.manifest.dublinCore
        val appendedPath = listOf(
            "src",
            dublinCore.creator,
            "${dublinCore.language.identifier}_${dublinCore.identifier}",
            "v${dublinCore.version}"
        ).joinToString(pathSeparator)
        val path = resourceContainerDirectory.resolve(appendedPath)
        path.mkdirs()
        return path
    }

    override fun getDerivedContainerDirectory(metadata: ResourceMetadata, source: ResourceMetadata): File {
        val appendedPath = listOf(
            "der",
            metadata.creator,
            source.creator,
            "${source.language.slug}_${source.identifier}",
            "v${metadata.version}",
            metadata.language.slug
        ).joinToString(pathSeparator)
        val path = resourceContainerDirectory.resolve(appendedPath)
        path.mkdirs()
        return path
    }

    override val resourceContainerDirectory: File
        get() = getAppDataDirectory("rc")

    override val userProfileAudioDirectory: File
        get() = getAppDataDirectory("users${pathSeparator}audio")

    override val userProfileImageDirectory: File
        get() = getAppDataDirectory("users${pathSeparator}images")

    override val audioPluginDirectory: File
        get() = getAppDataDirectory("plugins")
}