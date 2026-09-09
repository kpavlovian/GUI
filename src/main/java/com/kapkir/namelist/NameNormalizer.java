package com.kapkir.namelist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилитный класс для нормализации и валидации вводимых имён.
 *
 * Реализует требования задания:
 *  - удаление пробелов перед, после и внутри слова;
 *  - преобразование первой буквы к заглавной, остальных — к строчным
 *    (в т.ч. корректная обработка ввода при включённом CapsLock,
 *     например "ИнноКЕнТиЙ" -> "Иннокентий");
 *  - поиск недопустимых символов (цифры, знаки препинания, скобки,
 *    арифметические знаки и т.п.) — буквы (кириллица/латиница) и дефис допустимы.
 */
public final class NameNormalizer {

    // Разрешены только буквы (любого алфавита) и дефис (для двойных имён типа "Анна-Мария")
    private static final Pattern INVALID_CHARS = Pattern.compile("[^\\p{L}-]");

    private NameNormalizer() {
    }

    /**
     * Убирает пробелы в начале, конце и внутри строки полностью (в т.ч. между словами),
     * так как по условию задания вводится одно имя, а не имя-фамилия.
     */
    public static String removeAllSpaces(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("\\s+", "");
    }

    /**
     * Приводит имя к формату: первая буква — заглавная, остальные — строчные.
     * Корректно работает независимо от регистра исходного ввода
     * (полезно, если у пользователя случайно включён CapsLock).
     */
    public static String toCapitalized(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String lower = input.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    /**
     * Полная нормализация: удаление пробелов + приведение регистра.
     */
    public static String normalize(String input) {
        String noSpaces = removeAllSpaces(input);
        return toCapitalized(noSpaces);
    }

    /**
     * Проверяет строку на наличие недопустимых символов
     * (цифры, знаки препинания, скобки, арифметические знаки и т.д.).
     *
     * @return true, если найден хотя бы один недопустимый символ
     */
    public static boolean containsInvalidCharacters(String input) {
        if (input == null) {
            return false;
        }
        Matcher matcher = INVALID_CHARS.matcher(input);
        return matcher.find();
    }

    /**
     * Возвращает строку из всех найденных недопустимых символов (без повторов),
     * чтобы показать пользователю, что именно нужно убрать.
     */
    public static String findInvalidCharacters(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Matcher matcher = INVALID_CHARS.matcher(input);
        while (matcher.find()) {
            char c = matcher.group().charAt(0);
            if (sb.indexOf(String.valueOf(c)) < 0) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
