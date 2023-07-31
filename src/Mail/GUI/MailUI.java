package Mail.GUI;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

/**
 * A GUI for sending mail
 * @author TG Chipoyera
 * @version P04
 */
public class MailUI extends VBox {
    public MailUI(){
        // Container set up
        this.setPadding(new Insets(8));
        this.setSpacing(12);
        this.setMinWidth(500);
        this.setMinHeight(500);

        // Server details
        TitledPane Pane = new TitledPane("Server Details", new TextField());
        this.getChildren().add(Pane);
    }
}
