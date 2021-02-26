package progetto_data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author gianluca
 */
public class EmailPreview implements Serializable {

    private int ID;
    private final Utente mittente;
    private final String argomento;
    private boolean isNew;
    private Date data;

    public EmailPreview(int ID, Utente mittente, String argomento, Date data, boolean isNew) {
        this.ID = ID;
        this.mittente = mittente;
        this.argomento = argomento;
        this.data = data;
        this.isNew = isNew;
    }

    public int getID() {
        return ID;
    }

    public void updateID(int ID) {
        this.ID = ID;
    }

    public Utente getMittente() {
        return mittente;
    }

    public String getArgomento() {
        return argomento;
    }

    public Date getData() {
        return data;
    }

    public void updateData(Date data) {
        this.data = data;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isNew() {
        return isNew;
    }

    @Override
    public String toString() {
        if (isNew) {
            return "NEW\nData: " + data + "\nDa: " + mittente.getEmail() + "\nObject: " + argomento;
        } else {
            return "Data: " + data + "\nDa: " + mittente.getEmail() + "\nObject: " + argomento;
        }
    }

}