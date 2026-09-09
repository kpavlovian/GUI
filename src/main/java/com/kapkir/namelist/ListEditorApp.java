package com.kapkir.namelist;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Задание 2: "Создание экранной формы с обработкой перебираемого типа (список)".
 *
 * Форма повторяет макет из методички:
 *  - заголовок "Редактирование списка";
 *  - поле "Введите имя:" + кнопка "Добавить в список";
 *  - панель "Список имён:" (ListView, элементы подсвечиваются при выборе);
 *  - кнопка "Проверить на символы";
 *  - модальное окно "Внимание: Есть не текстовые символы, очистить?"
 *    с кнопками "Очистить" (зелёная) и "Оставить" (красная).
 *
 * Дополнительно (сверх макета, по тексту задания):
 *  - нормализация имени при добавлении (пробелы, регистр);
 *  - запрет дубликатов с модальным окном "Уже есть в списке";
 *  - навигация по списку стрелками, удаление кнопкой, отмена вставки Ctrl+Z.
 */
public class ListEditorApp extends Application {

    private final ObservableListWrapper names = new ObservableListWrapper();
    private final ListView<String> listView = new ListView<>(names.getList());
    private final TextField inputField = new TextField();
    private final Deque<UndoAction> undoStack = new ArrayDeque<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Редактирование списка");

        Label header = new Label("Редактирование списка");
        header.setStyle("-fx-background-color: #f5b942; -fx-background-radius: 16; " +
                "-fx-padding: 8 20 8 20; -fx-font-weight: bold;");
        StackPane headerBox = new StackPane(header);
        headerBox.setAlignment(Pos.CENTER);

        // ----- Левая колонка: ввод имени -----
        Label nameLabel = new Label("Введите имя:");
        inputField.setPrefWidth(160);
        inputField.setOnAction(e -> addName());
        HBox nameRow = new HBox(8, nameLabel, inputField);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Button addButton = new Button("Добавить в список");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setStyle(buttonStyle("#dcecc9"));
        addButton.setOnAction(e -> addName());

        Button checkButton = new Button("Проверить на символы");
        checkButton.setMaxWidth(Double.MAX_VALUE);
        checkButton.setStyle(buttonStyle("#dcecc9"));
        checkButton.setOnAction(e -> checkInvalidCharacters());

        Button deleteButton = new Button("Удалить выбранное");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setStyle(buttonStyle("#f0dede"));
        deleteButton.setOnAction(e -> deleteSelected());

        VBox leftColumn = new VBox(12, nameRow, addButton, checkButton, deleteButton);
        leftColumn.setPadding(new Insets(10));
        leftColumn.setPrefWidth(220);

        // ----- Правая колонка: список имён -----
        Label listLabel = new Label("Список имён:");
        listView.setPrefSize(220, 180);
        listView.setOnKeyPressed(this::handleArrowKeys);

        VBox rightColumn = new VBox(6, listLabel, listView);
        rightColumn.setPadding(new Insets(10));

        HBox content = new HBox(16, leftColumn, rightColumn);
        content.setPadding(new Insets(16));
        content.setAlignment(Pos.TOP_CENTER);

        // Крестик-декорация в правом нижнем углу, как в макете
        StackPane closeMark = buildCloseMark();
        StackPane.setAlignment(closeMark, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(closeMark, new Insets(0, 10, 10, 0));

        StackPane cardInner = new StackPane(content, closeMark);
        VBox card = new VBox(headerBox, cardInner);
        card.setSpacing(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #eaf3e2; -fx-background-radius: 12; " +
                "-fx-border-color: #b9d1a4; -fx-border-radius: 12;");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setCenter(card);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 620, 420);

        KeyCombination undoCombo = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(undoCombo, this::undoLastInsert);

        stage.setScene(scene);
        stage.show();
        inputField.requestFocus();
    }

    private String buttonStyle(String bgColor) {
        return "-fx-background-color: " + bgColor + "; -fx-background-radius: 16; -fx-padding: 8 16 8 16;";
    }

    private StackPane buildCloseMark() {
        Circle circle = new Circle(14);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3b6fb5"));
        circle.setStrokeWidth(2);
        Line l1 = new Line(-8, -8, 8, 8);
        Line l2 = new Line(-8, 8, 8, -8);
        l1.setStroke(Color.web("#3b6fb5"));
        l2.setStroke(Color.web("#3b6fb5"));
        l1.setStrokeWidth(2);
        l2.setStrokeWidth(2);
        StackPane pane = new StackPane(circle, l1, l2);
        return pane;
    }

    private void handleArrowKeys(javafx.scene.input.KeyEvent event) {
        int currentIndex = listView.getSelectionModel().getSelectedIndex();
        if (event.getCode() == KeyCode.DOWN) {
            int next = Math.min(currentIndex + 1, names.size() - 1);
            if (next >= 0) {
                listView.getSelectionModel().select(next);
                listView.scrollTo(next);
            }
            event.consume();
        } else if (event.getCode() == KeyCode.UP) {
            int prev = Math.max(currentIndex - 1, 0);
            listView.getSelectionModel().select(prev);
            listView.scrollTo(prev);
            event.consume();
        }
    }

    /**
     * Добавление имени: сначала проверка на недопустимые символы (модалка
     * "Очистить/Оставить" из макета), затем нормализация и проверка дубликата.
     */
    private void addName() {
        String rawInput = inputField.getText();
        if (rawInput == null || rawInput.isBlank()) {
            return;
        }

        if (NameNormalizer.containsInvalidCharacters(rawInput)) {
            showCleanOrKeepDialog(rawInput);
            return;
        }

        insertNormalized(rawInput);
    }

    /** Кнопка "Проверить на символы" — ручная проверка текущего ввода. */
    private void checkInvalidCharacters() {
        String text = inputField.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        if (NameNormalizer.containsInvalidCharacters(text)) {
            showCleanOrKeepDialog(text);
        } else {
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Проверка символов");
            ok.setHeaderText(null);
            ok.setContentText("Недопустимых символов не найдено.");
            ok.showAndWait();
        }
    }

    /**
     * Модальное окно из макета:
     * "Внимание: Есть не текстовые символы, очистить?" [Очистить] [Оставить]
     */
    private void showCleanOrKeepDialog(String rawInput) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Внимание");

        Label title = new Label("Внимание:");
        title.setStyle("-fx-background-color: #f5b942; -fx-background-radius: 10; " +
                "-fx-padding: 6 16 6 16; -fx-font-weight: bold;");
        StackPane titleBox = new StackPane(title);

        Label message = new Label("Есть не текстовые символы, очистить?");
        message.setWrapText(true);
        message.setStyle("-fx-font-size: 13px;");

        Button cleanButton = new Button("Очистить");
        cleanButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 18 8 18;");
        Button keepButton = new Button("Оставить");
        keepButton.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 8 18 8 18;");

        cleanButton.setOnAction(e -> {
            // Убираем недопустимые символы, оставляя только буквы и дефис
            String cleaned = rawInput.replaceAll("[^\\p{L}-\\s]", "");
            dialog.close();
            insertNormalized(cleaned);
        });

        keepButton.setOnAction(e -> {
            // Оставить как есть — вставляем без удаления "плохих" символов
            // (нормализация пробелов/регистра всё равно применяется к тексту)
            dialog.close();
            insertRawWithoutCharacterFilter(rawInput);
        });

        HBox buttonRow = new HBox(12, cleanButton, keepButton);
        buttonRow.setAlignment(Pos.CENTER);

        VBox box = new VBox(14, titleBox, message, buttonRow);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #fbe4d0; -fx-background-radius: 12; " +
                "-fx-border-color: #d9a97a; -fx-border-radius: 12;");

        Scene scene = new Scene(box, 320, 180);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /** Вставка после нормализации с полной проверкой на дубликаты. */
    private void insertNormalized(String rawText) {
        String normalized = NameNormalizer.normalize(rawText);
        if (normalized.isEmpty()) {
            return;
        }
        tryInsert(normalized);
    }

    /** Вставка без фильтрации символов (пользователь выбрал "Оставить"), но с нормализацией регистра/пробелов. */
    private void insertRawWithoutCharacterFilter(String rawText) {
        String normalized = NameNormalizer.normalize(rawText);
        if (normalized.isEmpty()) {
            return;
        }
        tryInsert(normalized);
    }

    private void tryInsert(String normalized) {
        if (names.contains(normalized)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Дубликат");
            alert.setHeaderText(null);
            alert.setContentText("Уже есть в списке");
            alert.showAndWait();
            inputField.clear();
            return;
        }
        names.add(normalized);
        undoStack.push(new UndoAction(normalized));
        inputField.clear();
        listView.getSelectionModel().select(normalized);
        listView.scrollTo(normalized);
    }

    private void deleteSelected() {
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        names.remove(selected);
        undoStack.removeIf(action -> action.value.equals(selected));
    }

    private void undoLastInsert() {
        if (undoStack.isEmpty()) {
            return;
        }
        UndoAction last = undoStack.pop();
        names.remove(last.value);
    }

    private static final class UndoAction {
        final String value;

        UndoAction(String value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
