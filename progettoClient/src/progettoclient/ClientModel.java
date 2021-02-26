package progettoclient;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import progetto_data.ConnectionManager;
import progetto_data.Email;
import progetto_data.EmailPreview;
import progetto_data.Utente;
import progetto_data.XMLManager;

/**
 *
 * @author gianluca
 */
public class ClientModel {

    private final ObservableList<EmailPreview> emailReceived;
    private final ObservableList<EmailPreview> emailSent;
    private final ObservableList<EmailPreview> emailTrash;
    private ConnectionManager connection;
    private Utente user;
    private Email actualEmail;
    private int paneStatus, cartella;
    private boolean mContinue;

    /**
     *
     */
    public ClientModel() {
        emailReceived = FXCollections.observableArrayList();
        emailSent = FXCollections.observableArrayList();
        emailTrash = FXCollections.observableArrayList();
        user = null;
        actualEmail = null;
        paneStatus = 0;
        cartella = XMLManager.RECEIVED;
        mContinue = false;
    }

    /**
     *
     * @param email
     */
    public void setUtente(String email) {
        user = new Utente(email);
    }

    /**
     *
     * @return Utente
     */
    public Utente getUtente() {
        return user;
    }

    /**
     *
     * @param newEmail
     */
    public void addEmailReceivedPreview(EmailPreview newEmail) {
        Platform.runLater(() -> {
            emailReceived.add(newEmail);
            sortEmailPreview(emailReceived);
        });
    }

    /**
     *
     * @param newEmail
     */
    public void addEmailSentPreview(EmailPreview newEmail) {
        Platform.runLater(() -> {
            emailSent.add(newEmail);
            sortEmailPreview(emailSent);
        });
    }

    /**
     *
     * @param newEmail
     */
    public void addEmailTrashPreview(EmailPreview newEmail) {
        Platform.runLater(() -> {
            emailTrash.add(newEmail);
            sortEmailPreview(emailTrash);
        });
    }

    /**
     *
     * @param actualEmail
     */
    public void setActualEmail(Email actualEmail) {
        this.actualEmail = actualEmail;
    }

    /**
     *
     * @return Email
     */
    public Email getActualEmail() {
        return actualEmail;
    }

    /**
     *
     */
    private void sortEmailPreview(ObservableList list) {
        list.sort((Object o1, Object o2) -> {
            return ((EmailPreview) o1).getData().before(((EmailPreview) o2).getData()) ? 1 : -1;
        });
    }

    /**
     *
     * @return paneStatus
     */
    public int getPaneStatus() {
        return paneStatus;
    }

    /**
     *
     * @param paneNewStatus
     */
    public void setPaneStatus(int paneNewStatus) {
        paneStatus = paneNewStatus;
    }

    /**
     *
     * @return cartella
     */
    public int getCartella() {
        return cartella;
    }

    /**
     *
     * @param cartella
     */
    public void setCartella(int cartella) {
        this.cartella = cartella;
    }

    /**
     *
     * @return boolean
     */
    public boolean getContinue() {
        return mContinue;
    }

    /**
     *
     * @param mContinue
     */
    public void setContinue(boolean mContinue) {
        this.mContinue = mContinue;
    }

    /**
     *
     * @return ConnectionManager
     */
    public ConnectionManager getConnection() {
        return connection;
    }

    /**
     *
     * @param connection
     */
    public void setConnection(ConnectionManager connection) {
        this.connection = connection;
    }

    /**
     *
     * @return ObservableList
     */
    public ObservableList<EmailPreview> getEmailReceivedPreview() {
        return emailReceived;
    }

    /**
     *
     * @return ObservableList
     */
    public ObservableList<EmailPreview> getEmailSentPreview() {
        return emailSent;
    }

    /**
     *
     * @return ObservableList
     */
    public ObservableList<EmailPreview> getEmailTrashPreview() {
        return emailTrash;
    }

    /**
     *
     */
    public void resetConnection() {
        emailReceived.clear();
        emailSent.clear();
        emailTrash.clear();
    }

    /**
     *
     * @param position
     */
    public void removeEmailReceivedPreviewPosition(int position) {
        Platform.runLater(() -> {
            emailReceived.remove(position);
        });
    }

    /**
     *
     * @param position
     */
    public void removeEmailSentPreviewPosition(int position) {
        Platform.runLater(() -> {
            emailSent.remove(position);
        });
    }

    /**
     *
     * @param position
     */
    public void removeEmailTrashPreviewPosition(int position) {
        Platform.runLater(() -> {
            emailTrash.remove(position);
        });
    }
}