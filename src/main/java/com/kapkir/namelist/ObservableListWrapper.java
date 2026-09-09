package com.kapkir.namelist;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Небольшая обёртка над ObservableList<String>, чтобы отделить
 * работу со списком имён от кода экранной формы (MainApp).
 */
public class ObservableListWrapper {

    private final ObservableList<String> list = FXCollections.observableArrayList();

    public ObservableList<String> getList() {
        return list;
    }

    public boolean contains(String value) {
        return list.contains(value);
    }

    public void add(String value) {
        list.add(value);
    }

    public void remove(String value) {
        list.remove(value);
    }

    public int size() {
        return list.size();
    }
}
