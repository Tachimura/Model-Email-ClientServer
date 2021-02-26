package progetto_data;

import java.util.ArrayList;

/**
 *
 * @author gianluca
 */
public class CasellaPostale {

    private final Utente user;
    private final ArrayList<EmailPreview> messaggi;

    /**
     *
     * @param user
     */
    public CasellaPostale(Utente user) {
        this.user = user;
        messaggi = new ArrayList<>();
    }

    /**
     *
     * @return user
     */
    public Utente getUser() {
        return user;
    }

    /**
     *
     * @return messaggi
     */
    public ArrayList<EmailPreview> getMessaggi() {
        return messaggi;
    }

    /**
     *
     * @param ID
     * @return email
     */
    public EmailPreview getMessaggio(int ID) {
        EmailPreview email = null;
        for (int cont = 0; cont < messaggi.size(); cont++) {
            if (messaggi.get(cont).getID() == ID) {
                email = messaggi.get(cont);
                break;
            }
        }
        return email;
    }

    /**
     *
     * @param email
     */
    public void addMessaggio(EmailPreview email) {
        messaggi.add(email);
    }
}