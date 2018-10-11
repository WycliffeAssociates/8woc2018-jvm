package org.wycliffeassociates.otter.jvm.app

//import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.ProjectCreationWizard
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeView
import org.wycliffeassociates.otter.common.domain.ImportLanguages
import org.wycliffeassociates.otter.common.domain.PluginActions
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.menu.MainMenu
import org.wycliffeassociates.otter.jvm.app.ui.menu.MainMenuStylesheet
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.view.ProjectPage
import org.wycliffeassociates.otter.jvm.app.ui.projectpage.view.ProjectPageStylesheet
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view.ViewTakesStylesheet
import org.wycliffeassociates.otter.jvm.persistence.DefaultPluginPreference
import sun.plugin2.main.server.Plugin
import tornadofx.*
import java.io.File

class MyApp : App(Workspace::class) {
    init {
        importStylesheet(ProjectPageStylesheet::class)
        importStylesheet(ViewTakesStylesheet::class)
        importStylesheet(MainMenuStylesheet::class)
        workspace.header.removeFromParent()
        workspace.add(MainMenu())
    }
    override fun onBeforeShow(view:UIComponent) {
<<<<<<< HEAD
        workspace.dock<ProjectHomeView>()
=======
        workspace.dock<ProjectPage>()
>>>>>>> dev
    }
}
//launch the org.wycliffeassociates.otter.jvm.app
fun main(args: Array<String>) {
    ImportLanguages(
            File(ClassLoader.getSystemResource("langnames.json").toURI()),
            Injector.languageRepo
<<<<<<< HEAD
    ).import().onErrorComplete().subscribe()
=======
    )
            .import()
            .onErrorComplete()
            .subscribe()

    PluginActions(Injector.pluginRepository)
            .initializeDefault()
            .subscribe()

>>>>>>> dev
    launch<MyApp>(args)
}
