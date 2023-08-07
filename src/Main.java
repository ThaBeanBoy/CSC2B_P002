import Mail.GUI.MailGUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception{
        stage.setTitle("SMTP Client");
        stage.setScene(new Scene(new MailGUI(stage)));
        stage.show();
    }
}