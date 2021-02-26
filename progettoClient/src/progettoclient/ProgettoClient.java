package progettoclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author gianluca
 */
public class ProgettoClient extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientForm.fxml"));
        Parent root = loader.load();
        ClientController controller = loader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        //imposto il css
        scene.getStylesheets().add(ProgettoClient.class.getResource("stile_client.css").toExternalForm());
        stage.setTitle("Client");
        stage.setOnHidden(ignored -> {
            controller.closeConnection();
        });
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}