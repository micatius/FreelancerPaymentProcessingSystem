package hr.java.production.main;

//223. Freelancer Payment Processing System
//•	Problem: Process payments for freelancers efficiently.
//        •	Features: Payment tracking, invoicing, reporting.
//        •	Database: Freelancer records, payment history, invoice logs.
//        •	Threads: No
//        Notify finance teams of pending payments, monitor payment status.


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

import java.io.IOException;

public class PaymentApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PaymentApplication.class.getResource("login-screen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();



    }

    public static void main(String[] args) {
        launch(args);
    }
}