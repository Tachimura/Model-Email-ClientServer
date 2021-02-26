package progetto_data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author gianluca
 */
public class XMLMessage implements Serializable {

    private int comando;
    private int result; //per contenere risposte veloci da server
    private Utente user;
    private Email email;
    private EmailPreview emailPreview;
    private ArrayList<EmailPreview> previewEmailsReceived, previewEmailsSent, previewEmailsTrash;

    public XMLMessage() {
        comando = -1;
        result = -1;
        user = null;
        email = null;
        emailPreview = null;
        previewEmailsReceived = null;
        previewEmailsSent = null;
        previewEmailsTrash = null;
    }

    public int getComando() {
        return comando;
    }

    public void setComando(int comando) {
        this.comando = comando;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public void setUser(Utente user) {
        this.user = user;
    }

    public Utente getUser() {
        return user;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmailPreview(EmailPreview emailPreview) {
        this.emailPreview = emailPreview;
    }

    public EmailPreview getEmailPreview() {
        return emailPreview;
    }

    public void setEmailReceivedPreviewArray(ArrayList<EmailPreview> previewEmailsReceived) {
        this.previewEmailsReceived = previewEmailsReceived;
    }

    public ArrayList<EmailPreview> getEmailReceivedPreviewArray() {
        return previewEmailsReceived;
    }

    public void setEmailSentPreviewArray(ArrayList<EmailPreview> previewEmailsSent) {
        this.previewEmailsSent = previewEmailsSent;
    }

    public ArrayList<EmailPreview> getEmailSentPreviewArray() {
        return previewEmailsSent;
    }

    public void setEmailTrashPreviewArray(ArrayList<EmailPreview> previewEmailsTrash) {
        this.previewEmailsTrash = previewEmailsTrash;
    }

    public ArrayList<EmailPreview> getEmailTrashPreviewArray() {
        return previewEmailsTrash;
    }
}