package app.ui.welcomeScreen

import app.UIColorsObject.Colors
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.layout.Priority
import tornadofx.*
import app.widgets.usersList.UsersList
import app.ui.userCreation.*
import app.widgets.profileIcon.ProfileIcon
import app.widgets.WidgetsStyles
import app.ui.welcomeScreen.View.WelcomeScreenStyles


class WelcomeScreen : View() {

    //grab the usernames from outside
    //in the real thing, we'll grab icons and sound clips instead
    //so replace this injection with whatever injection you do
    val gridWidth = 400.0
    //the left half of the screen, which displays:
    //the last user to log in, a welcome message, and a button to go to that user's home
    private val rad = 100.0
    //the grid of users
    //hooked up to the list we pulled in up top from DataService
    //right now it has just 9 elems but the real one will have who-knows-how-many
    val pad = 60.0
    private val welcomeScreen = hbox {
        importStylesheet(WelcomeScreenStyles::class)
        vbox {
            addClass(WelcomeScreenStyles.welcomeBack)
            add(ProfileIcon("12345678901", 150.0, true))
            label(FX.messages["welcome"]).addClass(WelcomeScreenStyles.welcomeLabel)
            val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
            button("", homeIcon) {
                importStylesheet(WidgetsStyles::class)
                addClass(WidgetsStyles.alternateRectangleButton)
                style {
                    minWidth = 175.0.px
                    homeIcon.fill = c(Colors["base"])
                }
            }
        }
        vbox {
            add(UsersList())
            style {
                prefWidth = 1200.px
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS
            }
            vbox(8) {
                addClass(WelcomeScreenStyles.createVBox).style { vgrow = Priority.ALWAYS }
                //INSIDE a vbox to allow for alignment
                val addUserIcon = MaterialIconView(MaterialIcon.GROUP_ADD, "25px")
                addUserIcon.fill = c(Colors["primary"])
                button("", addUserIcon) {
                        importStylesheet(WidgetsStyles::class)
                        addClass(WidgetsStyles.roundButton)
                    action {
                        find(WelcomeScreen::class).replaceWith(UserCreation::class)
                    }
                }
                padding = insets(pad)
                label(messages["create"]).addClass(WelcomeScreenStyles.createLabel)
            }
        }
    }
    //set the root of the view to the welcomeScreen
    override val root = welcomeScreen

    //DON'T MOVE THIS TO THE TOP
    //current window will be null unless init goes under setting of root
    init {
        //set minimum size of window so they can always see the last user and the grid of other users
        val minWidth = 3 * pad + 2 * rad + gridWidth
        //add 100 for home button and Welcome message; probably in real thing these will be vars
        val minHeight = 2 * pad + 2 * rad + 100.0
        setWindowMinSize(minWidth, minHeight)
    }
}