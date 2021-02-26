package progetto_data;

import java.io.Serializable;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author gianluca
 */
public class Utente implements Serializable {

    private final String email;
    public transient static final String[] EMAILREGISTRATE = {"ugo@jd.com", "gianluca@jd.com", "christian@jd.com"};
    public transient static final String[] EMAILPOSSIBILI = {"ugo@jd.com", "gianluca@jd.com", "christian@jd.com", "alzy@jd.com"};

    public static ObservableList getEmailPossibiliObservable() {
        ObservableList<String> emails = FXCollections.observableArrayList();
        emails.addAll(Arrays.asList(EMAILPOSSIBILI));
        return emails;
    }

    public static ObservableList getEmailRegistrateObservable() {
        ObservableList<String> emails = FXCollections.observableArrayList();
        emails.addAll(Arrays.asList(EMAILREGISTRATE));
        return emails;
    }

    public Utente(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return email;
    }
}