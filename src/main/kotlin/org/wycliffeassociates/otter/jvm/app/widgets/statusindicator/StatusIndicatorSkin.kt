package org.wycliffeassociates.otter.jvm.statusindicator.control

import javafx.beans.property.*
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.layout.*
import javafx.scene.paint.*
import tornadofx.*


class StatusIndicatorSkin(control: StatusIndicator) : SkinBase<StatusIndicator>(control) {

    private var bar: StackPane
    private var track: StackPane
    private var barWidth: Double = 0.0


    private var invalidBar = true
    private val localProgressProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    private val skinPrimaryFill: ObjectProperty<Color> = SimpleObjectProperty<Color>(Color.RED)
    private val skinAccentFill: ObjectProperty<Color> = SimpleObjectProperty<Color>(Color.RED)
    private val skinTrackColor: ObjectProperty<Color> = SimpleObjectProperty<Color>(Color.WHITE)
    private val barHeight: DoubleProperty = SimpleDoubleProperty(0.0)
    private val trackHeight: DoubleProperty = SimpleDoubleProperty(0.0)
    private val indicatorRadius: DoubleProperty = SimpleDoubleProperty(0.0)
    private val skinShowText: BooleanProperty = SimpleBooleanProperty(false)
    private val barBorderStyle: ObjectProperty<BorderStrokeStyle> =
        SimpleObjectProperty<BorderStrokeStyle>(BorderStrokeStyle.NONE)
    private val barBorderRadius: DoubleProperty = SimpleDoubleProperty(0.0)
    private val barBorderWidth: DoubleProperty = SimpleDoubleProperty(0.0)
    private val textFill: ObjectProperty<Paint> = SimpleObjectProperty<Paint>(Color.BLACK)
    private val barBorderColor: ObjectProperty<Paint> = SimpleObjectProperty<Paint>(Color.BLACK)
    private val trackBorderColor: ObjectProperty<Paint> = SimpleObjectProperty<Paint>(Color.BLACK)
    private val trackBorderStyle: ObjectProperty<BorderStrokeStyle> =
        SimpleObjectProperty<BorderStrokeStyle>(BorderStrokeStyle.NONE)
    private val trackBorderRadius: DoubleProperty = SimpleDoubleProperty(0.0)
    private val trackBorderWidth: DoubleProperty = SimpleDoubleProperty(0.0)

    init {
        //need to define super(statusindicator)? not sure if this is required atm
        bar = StackPane()
        bar.styleClass.setAll("indicator-bar")

        track = StackPane()
        track.styleClass.setAll("indicator-track")

        children.setAll(track, bar)

        control.widthProperty().onChange { invalidBar = true }
        control.heightProperty().onChange { invalidBar = true }
        control.primaryFillProperty.onChange { updateBarFill(control.primaryFill, control.accentFill) }
        control.accentFillProperty.onChange { updateBarFill(control.primaryFill, control.accentFill) }
        control.progressProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.barBorderStyleProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.barBorderRadiusProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.barBorderWidthProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.barBorderColorProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.trackHeightProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.textFillProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.trackBorderColorProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.trackBorderStyleProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.trackBorderRadiusProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.trackBorderWidthProperty.onChange { updateStatusIndicator(control.width, control.height) }
        control.trackHeightProperty.onChange { updateStatusIndicator(control.width, control.height) }

        skinnable.requestLayout()

        localProgressProperty.bind(control.progressProperty)
        skinPrimaryFill.bind(control.primaryFillProperty)
        skinAccentFill.bind(control.accentFillProperty)
        barHeight.bind(control.barHeightProperty)
        trackHeight.bind(control.trackHeightProperty)
        indicatorRadius.bind(control.indicatorRadiusProperty)
        skinTrackColor.bind(control.trackFillProperty)
        skinShowText.bind(control.showTextProperty)
        barBorderStyle.bind(control.barBorderStyleProperty)
        barBorderRadius.bind(control.barBorderRadiusProperty)
        barBorderWidth.bind(control.barBorderWidthProperty)
        textFill.bind(control.textFillProperty)
        barBorderColor.bind(control.barBorderColorProperty)
        trackBorderColor.bind(control.trackBorderColorProperty)
        trackBorderStyle.bind(control.trackBorderStyleProperty)
        trackBorderRadius.bind(control.trackBorderRadiusProperty)
        trackBorderWidth.bind(control.trackBorderWidthProperty)
    }

    fun updateBarFill(primaryFill: Color, accentFill: Color) {
        val stops = listOf(Stop(0.0, primaryFill), Stop(1.0, accentFill))
        if (bar != null && track != null) {
            bar.background = Background(
                BackgroundFill(
                    LinearGradient(
                        0.0,
                        0.0,
                        0.05,
                        0.7,
                        true,
                        CycleMethod.REPEAT,
                        stops
                    ),
                    CornerRadii(0.0),
                    Insets(0.0)
                )
            )
            skinnable.requestLayout()
        }
    }

    fun updateStatusIndicator(width: Double, height: Double) {
        val stops = listOf(Stop(0.0, skinPrimaryFill.value), Stop(1.0, skinAccentFill.value))

        if (bar != null && track != null) {
            children.remove(bar)
            children.remove(track)
        }

        bar = StackPane()
        bar.border = Border(
            BorderStroke(
                barBorderColor.value,
                barBorderStyle.value,
                CornerRadii(barBorderRadius.value),
                BorderWidths(barBorderWidth.value)
            )
        )

        val textLabel = Label("${((localProgressProperty.value * 100).toInt())}%")
        textLabel.textFill = textFill.value
        if (skinShowText.value) bar.add(textLabel)

        track = StackPane()
        track.border = Border(
            BorderStroke(
                trackBorderColor.value,
                trackBorderStyle.value,
                CornerRadii(trackBorderRadius.value),
                BorderWidths(trackBorderWidth.value)
            )
        )

        if (localProgressProperty.value <= 1.0000001) {
            barWidth =
                ((localProgressProperty.value * width) - snappedLeftInset() - snappedRightInset()).toInt().toDouble()
        }

        track.background = Background(
            BackgroundFill(
                skinTrackColor.value,
                CornerRadii(indicatorRadius.value),
                Insets(1.0)
            )
        )
        bar.background = Background(
            BackgroundFill(
                LinearGradient(
                    0.0,
                    0.0,
                    0.1,
                    0.5,
                    true,
                    CycleMethod.REFLECT,
                    stops
                ),
                CornerRadii(indicatorRadius.value),
                Insets(1.0)
            )
        )

        bar.styleClass.setAll("indicator-bar")
        track.styleClass.setAll("indicator-track")
        children.setAll(track, bar)

    }

    override fun layoutChildren(contentX: Double, contentY: Double, contentWidth: Double, contentHeight: Double) {
        if (invalidBar) {
            updateStatusIndicator(contentWidth, contentHeight)
        }
        if (trackHeight.value == 0.0){
            track.resizeRelocate(contentX, contentY, contentWidth, contentHeight)
            layoutInArea(track,
                contentX,
                contentY ,
                contentWidth,
                contentHeight,
                -1.0,
                HPos.CENTER,
                VPos.CENTER
            )
        }
        else {
            track.resizeRelocate(contentX, contentY, contentWidth, trackHeight.value)
            layoutInArea(
                track,
                contentX,
                contentY ,
                contentWidth,
                trackHeight.value,
                -1.0,
                HPos.CENTER,
                VPos.CENTER
            )
        }

        if (barHeight.value == 0.0) {
            bar.resizeRelocate(contentX, contentY, barWidth, contentHeight)
            layoutInArea(
                bar,
                contentX,
                contentY - barHeight.value / 4.0,
                barWidth,
                contentHeight,
                -1.0,
                HPos.CENTER,
                VPos.CENTER
            )
        }
        else {
            bar.resizeRelocate(contentX, contentY, barWidth, barHeight.value)
            layoutInArea(
                bar,
                contentX,
                contentY - barHeight.value / 4.0,
                barWidth,
                barHeight.value,
                -1.0,
                HPos.CENTER,
                VPos.CENTER
            )
        }

        track.isVisible = true
        bar.isVisible = true
    }


    override fun computePrefHeight(
        width: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return topInset + bottomInset + 10
    }

    override fun computePrefWidth(
        height: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return rightInset + leftInset + 2000
    }

    override fun computeMinHeight(
        width: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return 7.5 + topInset + bottomInset
    }

    override fun computeMinWidth(
        height: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return 7.5 + rightInset + leftInset
    }

    override fun computeMaxHeight(
        width: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset)
    }

    override fun computeMaxWidth(
        height: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset)
    }

    fun updateProgress(progress: Double) {
        barWidth = (progress - snappedLeftInset() - snappedRightInset()).toInt().toDouble()
    }

}