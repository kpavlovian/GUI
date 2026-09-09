package com.kapkir.namelist;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Стартовое окно: выбор между двумя заданиями.
 * Запускать вместо CalculatorApp/ListEditorApp напрямую, если нужно
 * выбирать задание из одного приложения.
 */
public class LauncherApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Выбор задания");

        Label title = new Label("Выберите задание для запуска");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button task1Button = new Button("Задание 1: Отработка Ввода/Вывода (калькулятор)");
        task1Button.setMaxWidth(Double.MAX_VALUE);
        task1Button.setOnAction(e -> new CalculatorApp().start(new Stage()));

        Button task2Button = new Button("Задание 2: Редактирование списка имён");
        task2Button.setMaxWidth(Double.MAX_VALUE);
        task2Button.setOnAction(e -> new ListEditorApp().start(new Stage()));

        VBox root = new VBox(16, title, task1Button, task2Button);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 420, 220);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
