module hr.java {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.sql;
    requires spring.security.crypto;
    requires org.jetbrains.annotations;

    exports hr.java.production.model;
    exports hr.java.production.main;
    exports hr.java.production.log;
    opens hr.java.production.main to javafx.fxml;
//    exports hr.java.production.controller;
    opens hr.java.production.controller to javafx.fxml;
}