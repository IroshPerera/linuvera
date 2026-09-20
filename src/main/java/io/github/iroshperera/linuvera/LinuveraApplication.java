package io.github.iroshperera.linuvera;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class LinuveraApplication extends Application {

    @Override
    public void start(Stage primaryStage) {

        Label message = new Label(
                "Linuvera application started successfully."
        );

        StackPane root = new StackPane(message);

        Scene scene = new Scene(
                root,
                600,
                400
        );

        primaryStage.setTitle("Linuvera");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
