package progetto_data;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author gianluca
 */
public class Email extends EmailPreview {

    private final ArrayList<Utente> destinatari;
    private final String testo;

    public Email(int ID, Utente mittente, ArrayList<Utente> destinatari, String argomento, String testo, Date data, boolean isNew) {
        super(ID, mittente, argomento, data, isNew);
        this.destinatari = destinatari;
        this.testo = testo;
    }

    public ArrayList<Utente> getDestinatari() {
        return destinatari;
    }

    public String getTesto() {
        return testo;
    }
}