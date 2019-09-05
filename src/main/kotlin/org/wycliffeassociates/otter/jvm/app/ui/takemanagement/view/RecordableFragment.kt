package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXSnackbar
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordableViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.*
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

abstract class RecordableFragment(
    protected val recordableViewModel: RecordableViewModel,
    dragTargetBuilder: DragTargetBuilder
) : Fragment() {

    abstract fun createTakeCard(take: Take): TakeCard

    protected val audioPluginViewModel: AudioPluginViewModel by inject()

    /** Add custom components to this container, rather than root*/
    protected val mainContainer = VBox()

    protected val lastPlayOrPauseEvent: SimpleObjectProperty<PlayOrPauseEvent> = SimpleObjectProperty()

    private val draggingNodeProperty = SimpleObjectProperty<Node>()

    val dragTarget =
        dragTargetBuilder
            .build(draggingNodeProperty.booleanBinding { it != null })
            .apply {
                recordableViewModel.selectedTakeProperty.onChangeAndDoNow { take ->
                    /* We can't just add the node being dragged, since the selected take might have just been
                        loaded from the database */
                    this.selectedNodeProperty.value = take?.let { createTakeCard(take) }
                }
            }

    private val dragContainer = VBox().apply {
        draggingNodeProperty.onChange { draggingNode ->
            clear()
            draggingNode?.let { add(draggingNode) }
        }
    }

    init {
        importStylesheet<AppStyles>()
        createAudioPluginProgressDialog()
    }

    final override val root: Parent = anchorpane {
        addDragTakeEventHandlers()
        addButtonEventHandlers()

        createSnackBar(this)

        add(mainContainer
            .apply {
                anchorpaneConstraints {
                    leftAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                    topAnchor = 0.0
                }
            }
        )
        add(dragContainer)
    }

    private fun Parent.addDragTakeEventHandlers() {
        addEventHandler(StartDragEvent.START_DRAG, ::startDrag)
        addEventHandler(AnimateDragEvent.ANIMATE_DRAG, ::animateDrag)
        addEventHandler(CompleteDragEvent.COMPLETE_DRAG, ::completeDrag)
    }

    private fun Parent.addButtonEventHandlers() {
        addEventHandler(PlayOrPauseEvent.PLAY) {
            lastPlayOrPauseEvent.set(it)
        }
        addEventHandler(DeleteTakeEvent.DELETE_TAKE) {
            recordableViewModel.deleteTake(it.take)
        }
        addEventHandler(EditTakeEvent.EDIT_TAKE) {
            recordableViewModel.editTake(it)
        }
    }

    private fun createSnackBar(pane: Pane) {
        // TODO: This doesn't actually handle anything correctly. Need to know whether the user
        // TODO... hasn't selected an editor or recorder and respond appropriately.
        val snackBar = JFXSnackbar(pane)
        recordableViewModel.snackBarObservable.subscribe {
//            snackBar.enqueue(
//                JFXSnackbar.SnackbarEvent(
//                    messages["noRecorder"],
//                    messages["addPlugin"].toUpperCase(),
//                    5000,
//                    false,
//                    EventHandler {
//                        audioPluginViewModel.addPlugin(true, false)
//                    }
//                )
//            )
        }
    }

    private fun createAudioPluginProgressDialog() {
        // Plugin active cover
        val dialog = progressdialog {
            root.addClass(AppStyles.progressDialog)
            recordableViewModel.contextProperty.toObservable().subscribe { newContext ->
                graphic = when (newContext) {
                    TakeContext.RECORD -> AppStyles.recordIcon("60px")
                    TakeContext.EDIT_TAKES -> AppStyles.editIcon("60px")
                    null -> null
                }
            }
        }
        recordableViewModel.showPluginActiveProperty.onChange {
            Platform.runLater {
                if (it) dialog.open() else dialog.close()
            }
        }
    }

    private fun getPointInRoot(node: Node, pointInNode: Point2D): Point2D {
        return when (node) {
            root -> pointInNode
            else -> getPointInRoot(node.parent, node.localToParent(pointInNode))
        }
    }

    private fun getBoundsInRoot(node: Node, bounds: Bounds): Bounds {
        return when (node) {
            root -> bounds
            else -> getBoundsInRoot(node.parent, node.localToParent(bounds))
        }
    }

    private var dragStartDelta = Point2D(0.0, 0.0)

    private fun relocateDragContainer(pointInRoot: Point2D) {
        val newX = pointInRoot.x - dragStartDelta.x
        val newY = pointInRoot.y - dragStartDelta.y
        dragContainer.relocate(newX, newY)
    }

    private fun startDrag(event: StartDragEvent) {
        if (event.take != recordableViewModel.selectedTakeProperty.value) {
            val draggingNode = event.draggingNode
            val mouseEvent = event.mouseEvent
            dragStartDelta = Point2D(mouseEvent.x, mouseEvent.y)
            val pointInRoot = getPointInRoot(draggingNode, Point2D(mouseEvent.x, mouseEvent.y))

            draggingNodeProperty.set(draggingNode)
            dragContainer.toFront()
            relocateDragContainer(pointInRoot)
        }
    }

    private fun animateDrag(event: AnimateDragEvent) {
        draggingNodeProperty.value?.let { draggingNode ->
            val pointInRoot = getPointInRoot(draggingNode, Point2D(event.mouseEvent.x, event.mouseEvent.y))
            relocateDragContainer(pointInRoot)
        }
    }

    private fun isDraggedToTarget(): Boolean {
        val draggingNodeBounds =
            getBoundsInRoot(draggingNodeProperty.value.parent, draggingNodeProperty.value.boundsInParent)
        val dragTargetBounds = getBoundsInRoot(dragTarget.parent, dragTarget.boundsInParent)

        // Reduce the bounds of the drag target slightly so that we are sure there is enough overlap
        // to trigger select take
        val dragTargetReducedBounds = dragTargetBounds.let {
            BoundingBox(
                it.minX + it.width*.05,
                it.minY + it.height*.05,
                it.width*.9,
                it.height*.9
            )
        }
        return draggingNodeBounds.intersects(dragTargetReducedBounds)
    }

    private fun completeDrag(event: CompleteDragEvent) {
        if (isDraggedToTarget()) {
            recordableViewModel.selectTake(event.take)
        } else {
            event.onCancel()
        }
        draggingNodeProperty.set(null)
    }
}