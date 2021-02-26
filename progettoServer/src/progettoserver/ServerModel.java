package progettoserver;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import progetto_data.JDAction;

/**
 *
 * @author gianluca
 */
public class ServerModel {

    private final ObservableList<JDAction> actions;

    /**
     *
     */
    public ServerModel() {
        actions = FXCollections.observableArrayList();
    }

    /**
     *
     * @param action
     */
    public void addAction(String action) {
        Platform.runLater(() -> {
            actions.add(new JDAction(action));
        });
    }

    /**
     *
     * @return ObservableList
     */
    public ObservableList<JDAction> getActions() {
        return actions;
    }
}