package org.wycliffeassociates.otter.jvm.app

import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.common.domain.ImportLanguages
import org.wycliffeassociates.otter.common.domain.plugins.InitializePlugins
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.menu.MainMenu
import org.wycliffeassociates.otter.jvm.app.ui.menu.MainMenuStylesheet
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeView
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.view.ProjectPageStylesheet
import org.wycliffeassociates.otter.jvm.app.ui.styles.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view.ViewTakesStylesheet
import tornadofx.*
import java.io.File
import java.util.*

class MyApp : App(Workspace::class) {
    init {
        importStylesheet(ProjectPageStylesheet::class)
        importStylesheet(ViewTakesStylesheet::class)
        importStylesheet(MainMenuStylesheet::class)
        importStylesheet(AppStyles::class)
        workspace.header.removeFromParent()
        workspace.add(MainMenu())
        FX.locale = Locale.FRANCE
    }
    override fun onBeforeShow(view:UIComponent) {
        workspace.dock<ProjectHomeView>()
    }
}
//launch the org.wycliffeassociates.otter.jvm.app
fun main(args: Array<String>) {
    initApp()
    launch<MyApp>(args)
}

private fun initApp() {
    ImportLanguages(
            File(ClassLoader.getSystemResource("langnames.json").toURI()),
            Injector.languageRepo)
            .import()
            .onErrorComplete()
            .subscribe()

    ImportAudioPlugins(Injector.audioPluginRegistrar, Injector.directoryProvider)
            .importAll()
            .andThen(InitializePlugins(Injector.pluginRepository).initDefault())
            .subscribe()

    Injector.takeRepository
            .removeNonExistentTakes()
            .subscribe()
}
