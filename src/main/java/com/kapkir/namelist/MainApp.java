package com.kapkir.namelist;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Учебный проект: "Создание экранной формы с обработкой перебираемого типа (список)".
 *
 * Функциональность:
 *  - добавление элемента в список (кнопкой, не прямым редактированием списка);
 *  - удаление элемента из списка (кнопкой "Удалить");
 *  - подсветка выбранного элемента при клике;
 *  - навигация по списку клавишами вверх/вниз;
 *  - отмена последней вставки по Ctrl+Z;
 *  - нормализация вводимого имени (пробелы, регистр);
 *  - проверка на недопустимые символы с выводом модального окна;
 *  - исключение дубликатов (с выводом модального окна и запретом вставки).
 */
public class MainApp extends Application {

    private final ObservableListWrapper names = new ObservableListWrapper();
    private final ListView<String> listView = new ListView<>(names.getList());
    private final TextField inputField = new TextField();
    private final Deque<UndoAction> undoStack = new ArrayDeque<>();
    private final Label statusLabel = new Label("Список пуст");

    @Override
    public void start(Stage stage) {
        stage.setTitle("Список имён — нормализация и валидация данных");

        // ----- Верхняя панель: поле ввода + кнопки -----
        inputField.setPromptText("Введите имя...");
        inputField.setPrefWidth(220);
        inputField.setOnAction(e -> addName()); // Enter тоже добавляет

        Button addButton = new Button("Добавить");
        addButton.setDefaultButton(true);
        addButton.setOnAction(e -> addName());

        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteSelected());
        deleteButton.setTooltip(new Tooltip("Удалить выбранный элемент из списка"));

        Button checkButton = new Button("Проверить на «лишние» символы");
        checkButton.setOnAction(e -> checkInvalidCharactersManually());

        HBox topBar = new HBox(8, inputField, addButton, deleteButton, checkButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        // ----- Список -----
        listView.setPrefHeight(320);
        // Подсветка выбранного элемента обеспечивается штатным выделением ListView.
        // Навигация по списку стрелками работает "из коробки" (встроенное поведение
        // JavaFX ListView), дополнительно дублируем её явным обработчиком клавиш,
        // чтобы поведение было предсказуемым и в фокусе оставался список.
        listView.setOnKeyPressed(event -> {
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
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateStatus(newV));

        statusLabel.setPadding(new Insets(6, 10, 10, 10));

        VBox center = new VBox(listView, statusLabel);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(center);

        Scene scene = new Scene(root, 480, 460);

        // ----- Горячая клавиша Ctrl+Z — отмена последней вставки -----
        KeyCombination undoCombo = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(undoCombo, this::undoLastInsert);

        stage.setScene(scene);
        stage.show();

        inputField.requestFocus();
    }

    /**
     * Обработка добавления имени: нормализация -> проверка на недопустимые символы
     * -> проверка на дубликат -> вставка в список.
     */
    private void addName() {
        String rawInput = inputField.getText();

        if (rawInput == null || rawInput.isBlank()) {
            return; // пустой ввод игнорируем
        }

        // Сначала проверяем недопустимые символы на "сыром" вводе (до удаления пробелов),
        // чтобы явно показать пользователю, что именно он ввёл лишнего.
        if (NameNormalizer.containsInvalidCharacters(rawInput)) {
            String bad = NameNormalizer.findInvalidCharacters(rawInput);
            showModal(Alert.AlertType.WARNING,
                    "Недопустимые символы",
                    "В введённом тексте есть недопустимые символы: \"" + bad + "\"\n" +
                            "Разрешены только буквы и дефис.");
            return;
        }

        String normalized = NameNormalizer.normalize(rawInput);

        if (normalized.isEmpty()) {
            return;
        }

        if (names.contains(normalized)) {
            showModal(Alert.AlertType.INFORMATION,
                    "Дубликат",
                    "Уже есть в списке");
            // Вставка не осуществляется — очищаем поле, но список не трогаем
            inputField.clear();
            return;
        }

        names.add(normalized);
        undoStack.push(new UndoAction(normalized));
        inputField.clear();
        listView.getSelectionModel().select(normalized);
        listView.scrollTo(normalized);
        updateStatus(normalized);
    }

    /** Удаление выбранного элемента списка по кнопке. */
    private void deleteSelected() {
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModal(Alert.AlertType.WARNING, "Удаление", "Сначала выберите элемент в списке.");
            return;
        }
        names.remove(selected);
        // Убираем из истории отмены все записи о нём, чтобы Ctrl+Z не "воскрешал" удалённое
        undoStack.removeIf(action -> action.value.equals(selected));
        updateStatus(listView.getSelectionModel().getSelectedItem());
    }

    /** Отмена последней вставки по Ctrl+Z. */
    private void undoLastInsert() {
        if (undoStack.isEmpty()) {
            return;
        }
        UndoAction last = undoStack.pop();
        names.remove(last.value);
        updateStatus(listView.getSelectionModel().getSelectedItem());
    }

    /** Кнопка ручной проверки текущего текста в поле ввода на лишние символы. */
    private void checkInvalidCharactersManually() {
        String text = inputField.getText();
        if (text == null || text.isBlank()) {
            showModal(Alert.AlertType.INFORMATION, "Проверка символов", "Поле ввода пустое.");
            return;
        }
        if (NameNormalizer.containsInvalidCharacters(text)) {
            String bad = NameNormalizer.findInvalidCharacters(text);
            showModal(Alert.AlertType.WARNING,
                    "Недопустимые символы",
                    "Найдены недопустимые символы: \"" + bad + "\"");
        } else {
            showModal(Alert.AlertType.INFORMATION,
                    "Проверка символов",
                    "Недопустимых символов не найдено.");
        }
    }

    private void updateStatus(String selected) {
        if (selected == null) {
            statusLabel.setText("Элементов в списке: " + names.size());
        } else {
            statusLabel.setText("Выбрано: " + selected + "  |  Всего элементов: " + names.size());
        }
    }

    private void showModal(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
