package persistence

import data.model.User
import java.io.File
import java.nio.file.FileSystems

class DirectoryProvider(private val appName: String) : IDirectoryProvider {

    private val separator = FileSystems.getDefault().separator   //if mac '/' if windows '\\'

    // create a directory to store the user's application projects/documents
    override fun getUserDataDirectory(appendedPath: String): File {
        // create the directory if it does not exist
        val pathComponents = mutableListOf(System.getProperty("user.home"), appName)
        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)
        val pathString = pathComponents.joinToString(separator)
        val file = File(pathString)
        file.mkdirs()
        return file
    }

    // create a directory to store the application's private data
    override fun getAppDataDirectory(appendedPath: String): File {
        // convert to upper case
        val os: String = System.getProperty("os.name")

        val upperOS = os.toUpperCase()

        val pathComponents = mutableListOf(appName)

        if (appendedPath.isNotEmpty()) pathComponents.add(appendedPath)

        if (upperOS.contains("WIN")) {
            // on windows use app data
            pathComponents.add(0, System.getenv("APPDATA"))
        } else if (upperOS.contains("MAC")) {
            // use /Users/<user>/Library/Application Support/ for macOS
            pathComponents.add(0, "Application Support")
            pathComponents.add(0, "Library")
            pathComponents.add(0, System.getProperty("user.home"))
        } else if (upperOS.contains("LINUX")) {
            pathComponents.add(0, ".config")
            pathComponents.add(0, System.getProperty("user.home"))
        }
        // create the directory if it does not exist
        val pathString = pathComponents.joinToString(separator)
        val file = File(pathString)
        file.mkdirs()
        return file
    }

    // Creates a user subdirectory and stores their hash info
    fun getAudioDirectory(): File {
        return createInternalAppDirectory("Audio")
    }

    // Stores the image with name of the user who stored it, followed by the original name of the file
    fun storeImageDirectory(userMine: User, imageFile: File): File {
        return createInternalAppDirectory("Images")
    }

    // Returns path of new directory
    fun createInternalAppDirectory(directoryName: String): File {
        val pathDir = getAppDataDirectory(directoryName)
        return pathDir
    }

}