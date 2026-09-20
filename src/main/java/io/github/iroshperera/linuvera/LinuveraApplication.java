package io.github.iroshperera.linuvera;

import io.github.iroshperera.linuvera.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class LinuveraApplication extends Application {

    private static final String APPLICATION_TITLE = "Linuvera";

    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {

        MainView mainView = new MainView();

        Scene scene = new Scene(
                mainView,
                WINDOW_WIDTH,
                WINDOW_HEIGHT
        );

        var stylesheet = getClass()
                .getResource("/application.css");

        if (stylesheet != null) {
            scene.getStylesheets()
                    .add(stylesheet.toExternalForm());
        }

        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setMinWidth(1050);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
