package com.silverboxsoft

import kotlin.Throws
import java.lang.Exception
import javafx.stage.Stage
import java.io.IOException
import javafx.fxml.FXMLLoader
import javafx.scene.layout.VBox
import javafx.scene.Scene
import kotlin.jvm.JvmStatic
import javafx.application.Application
import javafx.scene.control.*

class App : Application() {
    @Throws(Exception::class)
    override fun start(stage: Stage) {
        prepareControl(stage)
        stage.show()
    }

    @Throws(IOException::class)
    private fun prepareControl(stage: Stage) {
        val fxmlLoader = FXMLLoader(
                javaClass.getResource("dynamodbtool/controller/javafx/DynamoDbToolController.fxml"))
        val newPane = fxmlLoader.load<Any>() as VBox
        val scene = Scene(newPane)
        stage.title = "DynamoDB Tool"
        stage.scene = scene
    }
}

fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
}