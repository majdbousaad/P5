module org.oos {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens org.oos to javafx.fxml;
    exports org.oos;

    opens org.oos.controllers to javafx.fxml;
    exports org.oos.controllers;

    opens org.oos.bank to javafx.fxml;
    exports org.oos.bank;
}