module com.kapkir.namelist {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.kapkir.namelist to javafx.fxml;
    exports com.kapkir.namelist;
}
