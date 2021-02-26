package progettoserver;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

/**
 *
 * @author gianluca
 */
public class ServerController implements Initializable {

    private ServerModel model;
    private Server server;

    @FXML
    private ListView lsvActions;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        model = new ServerModel();
        server = new Server(6789);
        server.setDataModel(model);
        lsvActions.setItems(model.getActions());
        server.start();
    }

    /**
     *
     */
    void closeServer() {
        server.stopServer();
    }
}