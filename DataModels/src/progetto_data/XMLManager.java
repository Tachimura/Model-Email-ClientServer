package progetto_data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.ArrayList;

/**
 *
 * @author gianluca
 */
public class XMLManager {
    
    public final static int NOTHING = 0; //USATO PER INDICARE CHE NON CE NULLA DI NUOVO O NULLA DA FARE

    //MESSAGGI DEL CLIENT
    public final static int REQUEST_LOGIN = 1;//RICHIESTA LOGIN
    public final static int REQUEST_EMAIL_PREVIEW = 2;//RICHIESTA EMAIL PREVIEW
    public final static int REQUEST_EMAIL_DATA = 3;//RICHIESTI DATI EMAIL
    public final static int REQUEST_EMAIL_CREATE = 4;//RICHIESTA CREAZIONE EMAIL
    public final static int REQUEST_EMAIL_DELETE = 5;//RICHIESTA ELIMINAZIONE EMAIL
    public final static int REQUEST_LOGOUT = 6;//RICHIESTA LOGOUT
    public final static int REQUEST_CLOSE_CONNECTION = 7;//RICHIESTA CHIUSURA SOCKET
    public final static int REQUEST_RECONNECTION = 8;//RICHIESTA RICONESSIONE
    //MESSAGGI DEL SERVER
    public final static int ANSWER_EMAIL_NEW = 9;//NOTIFY NUOVA EMAIL

    public final static int CORRECT = 10;
    public final static int INCORRECT = 11;
    public final static int RECEIVED = 12;
    public final static int SENT = 13;
    public final static int TRASH = 14;
    
    public static XMLManager getIstance() {
        return new XMLManager();
    }
    
    public XMLMessage createRequestCloseConnection() {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_CLOSE_CONNECTION);
        return msg;
    }
    
    public XMLMessage createRequestEmailCreate(Email newEmail) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_EMAIL_CREATE);
        msg.setEmail(newEmail);
        return msg;
    }
    
    public XMLMessage createRequestLogin(Utente utente) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_LOGIN);
        msg.setUser(utente);
        return msg;
    }
    
    public XMLMessage createRequestEmailData(EmailPreview email, int cartella) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_EMAIL_DATA);
        msg.setEmailPreview(email);
        msg.setResult(cartella);
        return msg;
    }
    
    public XMLMessage createRequestEmailPreview() {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_EMAIL_PREVIEW);
        return msg;
    }

    /**
     * ritorna true se il messaggio è stato invitato ritorna false se ci sono
     * stati errori
     *
     * @param output
     * @param message
     * @return boolean
     * @throws java.io.IOException
     */
    public boolean sendMessage(ObjectOutputStream output, XMLMessage message) throws IOException {
        boolean success = true;
        if (output != null) {
            try {
                output.writeObject(message);
            } catch (SocketException ex) {
                success = false;
                System.out.println("Problema di connessione con l'host di destinazione.");
            }
        } else {
            success = false;
        }
        return success;
    }

    /**
     * Metodo che legge un XMLMessage da input
     *
     * @param input
     * @return XMLMessage
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public XMLMessage getMessage(ObjectInputStream input) throws IOException, ClassNotFoundException {
        XMLMessage msg = null;
        if (input != null) {
            msg = (XMLMessage) input.readObject();
        }
        return msg;
    }
    
    public XMLMessage createAnswerLogin(int value) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_LOGIN);
        msg.setResult(value);
        return msg;
    }
    
    public XMLMessage createAnswerPreviewEmail(ArrayList<EmailPreview> received, ArrayList<EmailPreview> sent, ArrayList<EmailPreview> trash) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_EMAIL_PREVIEW);
        if (received == null && sent == null && trash == null) {
            msg.setResult(NOTHING);
        } else {
            if (received != null) {
                ArrayList<EmailPreview> array = received;
                msg.setEmailReceivedPreviewArray(array);
            }
            if (sent != null) {
                ArrayList<EmailPreview> array = sent;
                msg.setEmailSentPreviewArray(array);
            }
            if (trash != null) {
                ArrayList<EmailPreview> array = trash;
                msg.setEmailTrashPreviewArray(array);
            }
        }
        return msg;
    }
    
    public XMLMessage createAnswerEmailData(Email email) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_EMAIL_DATA);
        msg.setEmail(email);
        return msg;
    }
    
    public XMLMessage createAnswerNewEmail(EmailPreview emailPreview, int cartella) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(ANSWER_EMAIL_NEW);
        msg.setEmailPreview(emailPreview);
        msg.setResult(cartella);
        return msg;
    }
    
    public XMLMessage createRequestDeleteEmail(Email actualEmail, int cartella) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_EMAIL_DELETE);
        msg.setEmail(actualEmail);
        msg.setResult(cartella);
        return msg;
    }
    
    public XMLMessage createRequestLogout() {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_LOGOUT);
        return msg;
    }
    
    public XMLMessage createRequestReconnection(Utente utente) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_RECONNECTION);
        msg.setUser(utente);
        return msg;
    }
    
    public XMLMessage createAnswerReconnection(int result) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_RECONNECTION);
        msg.setResult(result);
        return msg;
    }
    
    public XMLMessage createAnswerDeleteEmail(Email email, int result, int cartella) {
        XMLMessage msg = new XMLMessage();
        msg.setComando(REQUEST_EMAIL_DELETE);
        msg.setEmail(email);
        msg.setResult(result);
        msg.setUser(new Utente(String.valueOf(cartella)));
        return msg;
    }
}