package com.kapkir.namelist;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Задание 1: "Отработка Ввода/Вывода" — реализация функций сложения/вычитания.
 *
 * Форма повторяет макет из методички:
 *  - заголовок "Отработка Ввода/Вывода";
 *  - подзаголовок "Задача: реализовать функции сложения/вычитания";
 *  - два поля "Аргумент 1" / "Аргумент 2" (в макете оба подписаны как
 *    "Аргумент 1" — опечатка оригинала; здесь подписи исправлены на
 *    "Аргумент 1" и "Аргумент 2" для ясности, остальной вид сохранён);
 *  - кнопка "Вычислить:";
 *  - панель "Результат:" с выводом суммы и произведения
 *    (в примере из макета: 5 + 6 = 11, 5 * 6 = 30).
 */
public class CalculatorApp extends Application {

    private final TextField argument1Field = new TextField();
    private final TextField argument2Field = new TextField();
    private final Label resultSum = new Label();
    private final Label resultProduct = new Label();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Отработка Ввода/Вывода");

        Label header = new Label("Отработка Ввода/Вывода");
        header.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #999; " +
                "-fx-padding: 8 20 8 20; -fx-font-weight: bold;");
        StackPane headerBox = new StackPane(header);

        Label taskLabel = new Label("Задача: реализовать функции сложения/вычитания");
        taskLabel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #999; -fx-padding: 6 12 6 12;");
        StackPane taskBox = new StackPane(taskLabel);
        taskBox.setMaxWidth(Double.MAX_VALUE);

        // ----- Аргумент 1 -----
        Label arg1Label = new Label("Аргумент 1:");
        arg1Label.setPrefWidth(90);
        arg1Label.setStyle(fieldBoxStyle());
        argument1Field.setPrefWidth(140);
        argument1Field.setText("5");
        HBox arg1Row = new HBox(0, arg1Label, argument1Field);

        // ----- Аргумент 2 -----
        Label arg2Label = new Label("Аргумент 2:");
        arg2Label.setPrefWidth(90);
        arg2Label.setStyle(fieldBoxStyle());
        argument2Field.setPrefWidth(140);
        argument2Field.setText("6");
        HBox arg2Row = new HBox(0, arg2Label, argument2Field);

        Button calcButton = new Button("Вычислить:");
        calcButton.setMaxWidth(Double.MAX_VALUE);
        calcButton.setDefaultButton(true);
        calcButton.setStyle("-fx-background-color: #eeeeee; -fx-border-color: #999; -fx-padding: 8;");
        calcButton.setOnAction(e -> calculate());

        VBox leftColumn = new VBox(10, arg1Row, arg2Row, calcButton);
        leftColumn.setPadding(new Insets(10));

        // ----- Панель результата -----
        Label resultTitle = new Label("Результат:");
        resultTitle.setStyle("-fx-font-weight: bold;");
        resultSum.setText("5 + 6 = 11");
        resultProduct.setText("5 * 6 = 30");

        VBox resultBox = new VBox(8, resultTitle, resultSum, resultProduct);
        resultBox.setPadding(new Insets(14));
        resultBox.setStyle("-fx-background-color: white; -fx-border-color: #6699cc; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        resultBox.setPrefWidth(180);

        HBox contentRow = new HBox(20, leftColumn, resultBox);
        contentRow.setAlignment(Pos.CENTER_LEFT);
        contentRow.setPadding(new Insets(10));

        VBox root = new VBox(10, headerBox, taskBox, contentRow);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: white; -fx-border-color: #999;");

        Scene scene = new Scene(root, 480, 320);
        stage.setScene(scene);
        stage.show();

        calculate(); // сразу показать пример из макета
    }

    private String fieldBoxStyle() {
        return "-fx-background-color: #f0f0f0; -fx-border-color: #999; -fx-padding: 4 8 4 8; " +
                "-fx-alignment: center-left;";
    }

    /**
     * Реализация функций сложения и вычитания (в макете показаны сложение и умножение
     * как пример; здесь дополнительно считается и разность, чтобы полностью покрыть
     * формулировку задания "сложение/вычитание").
     */
    private void calculate() {
        Double a = parseOrNull(argument1Field.getText());
        Double b = parseOrNull(argument2Field.getText());

        if (a == null || b == null) {
            resultSum.setText("Ошибка: введите числа");
            resultProduct.setText("");
            return;
        }

        double sum = a + b;
        double difference = a - b;

        resultSum.setText(formatNumber(a) + " + " + formatNumber(b) + " = " + formatNumber(sum));
        resultProduct.setText(formatNumber(a) + " - " + formatNumber(b) + " = " + formatNumber(difference));
    }

    private Double parseOrNull(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
