//import Mail.GUI.MailGUI;
import Mail.Model.FailedConnectionException;
import Mail.Model.InvalidEmailException;
import Mail.Model.MailNotSentException;
import Mail.Model.MailSender;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class Main extends Application {
    private static void write(PrintWriter out, String message){
        out.println(message);
        out.flush();
    }

    private static int getSMTPResponseCode(String responseMessage) throws NumberFormatException{
        String codeString = responseMessage.split(" ")[0];
        return Integer.parseInt(codeString);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception{
        VBox container = new VBox();
        container.setPadding(new Insets(8));
        container.setSpacing(12);
        container.setMinWidth(500);
        container.setAlignment(Pos.TOP_RIGHT);

        // Server Details
        GridPane ServerDetails = MailGrid();

        TextField ServerTextField = addInputToGrid(ServerDetails, "SMTP Server", 0, "127.0.0.1");
        TextField PortTextField = addInputToGrid(ServerDetails, "Port Number", 1, "25");

        container.getChildren().add(MailTitledPane("Server Details", ServerDetails));

        // Message
        GridPane MessageGrid = MailGrid();

        TextField SenderTextField = addInputToGrid(MessageGrid, "From", 0, "sendername@csc2b.uj.ac.za");
        TextField RecipientTextField = addInputToGrid(MessageGrid, "To", 1, "recipient@csc2b.uj.ac.za");
        TextField CCTextField = addInputToGrid(MessageGrid, "CC", 2);
//        TextField SubjectTextField = addInputToGrid(MessageGrid, "Subject", 3);

        MessageGrid.add(new Label("Subject"), 0, 3);
        TextField SubjectTextField = new TextField();
        GridPane.setColumnSpan(SubjectTextField, 2);
        MessageGrid.add(SubjectTextField, 0, 4);

        TextArea MessageBox = new TextArea();
        GridPane.setColumnSpan(MessageBox,2);
        MessageGrid.add(new Label("Message"), 0, 5);
        MessageGrid.add(MessageBox, 0, 6);

        container.getChildren().add(MailTitledPane("Message", MessageGrid));

        // Send Email Button
        Button SendEmailBtn = new Button("Send Email");
        SendEmailBtn.setOnAction(e->{
            String[] CCs = Arrays.stream(
                    CCTextField.getText().split(",") //Splitting CC emails by comma
                    )
                    .filter(emailAddress -> !emailAddress.isEmpty()) // Filtering out empty strings
                    .map(String::trim) // trimming the cc email
                    .toArray(String[]::new); // making the Object[] String[]

            System.out.println(Arrays.toString(CCs));

            try{
                MailSender.SendEmail(
                        ServerTextField.getText(),
                        Integer.parseInt(PortTextField.getText()),
                        SenderTextField.getText(),
                        RecipientTextField.getText(),
                        SubjectTextField.getText(),
                        MessageBox.getText(),
                        CCs,
                        new File[]{}
                );
            }
            catch (IOException exc){
                exc.printStackTrace();
            }
            catch(Exception exc){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unknown Host");

                if(exc instanceof FailedConnectionException){
                    alert.setContentText("Could not connect to server, consider fixing the hostname or make sure the server is running");
                    alert.show();
                }else if(exc instanceof InvalidEmailException){
                    alert.setContentText(String.format("The email '%s' is invalid", ((InvalidEmailException) exc).getInvalidEmail()));
                    alert.show();
                }else if(exc instanceof MailNotSentException){
                    alert.setContentText("Was unable to send email, you can retry");
                    alert.show();
                }else if(exc instanceof NumberFormatException){
                    alert.setContentText("Port number needs to be a integer");
                    alert.show();
                }else{
                    exc.printStackTrace();
                }
            }
        });
        container.getChildren().add(SendEmailBtn);

        stage.setTitle("SMTP Client");
        stage.setScene(new Scene(container));
        stage.show();
    }

    /**
     * This is a helper method that places a Label and a InputField next to each other in a Grid. This alleviates the repetitive task of manually placing Labels & TextFields in a Grid.
     * @param grid the grid where the label and input should be placed
     * @param label the label of the input
     * @param row the row to place the label and TextField
     * @param defaultValue the default value of the TextField
     * @return a TextField, this TextField can be used by client programmer if they want to make use of it
     * @see TextField,GridPane
     */
    private static TextField addInputToGrid(GridPane grid, String label, int row, String defaultValue){
        TextField input = new TextField(defaultValue);

        grid.add(new Label(label), 0, row);
        grid.add(input, 1, row);

        return input;
    }

    /**
     * This is a helper method that places a Label and a InputField next to each other in a Grid. This alleviates the repetitive task of manually placing Labels & TextFields in a Grid.
     * @param grid the grid where the label and input should be placed
     * @param label the label of the input
     * @param row the row to place the label and TextField
     * @return a TextField, this TextField can be used by client programmer if they want to make use of it
     * @see TextField,GridPane
     */
    private static TextField addInputToGrid(GridPane grid, String label, int row){
        return addInputToGrid(grid, label, row, "");
    }

    /**
     * A custom Grid for the Mail user interface
     * @return a GridPane
     * @see GridPane
     */
    private static GridPane MailGrid(){
        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(75);
        grid.getColumnConstraints().add(col1);

        return grid;
    }

    /**
     * A custom TitledPane for the Mail user interface
     * @param label the label of the TitlePane
     * @param node the node of the TitlePane
     * @return a TitledPane
     * @see TitledPane
     */
    private static TitledPane MailTitledPane(String label, Node node){
        TitledPane pane = new TitledPane(label, node);
        pane.setCollapsible(false);
        return pane;
    }
}