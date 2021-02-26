package progetto_data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author gianluca
 */
public class ConnectionManager {

    private final XMLManager manager;
    private final String ip;
    private final int port;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    /**
     *
     * @param ip
     * @param port
     */
    public ConnectionManager(String ip, int port) {
        manager = new XMLManager();
        this.socket = null;
        this.port = port;
        this.ip = ip;
        output = null;
        input = null;
    }

    public ConnectionManager(Socket socket) throws IOException {
        manager = new XMLManager();
        this.socket = socket;
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        port = -1;
        ip = null;
    }

    /**
     *
     */
    public void startConnection() {
        if (socket == null || (socket != null && socket.isClosed())) {
            connect();
        }
    }

    /**
     *
     */
    public void closeConnection() {
        if (socket != null && !socket.isClosed()) {
            disconnect();
        }
    }

    /**
     *
     */
    private void connect() {
        try {
            socket = new Socket(ip, port);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
        } catch (IOException ex) {
            System.out.println("Errore creazione connessione con il server");
        }
    }

    /**
     *
     */
    private void disconnect() {
        if (socket != null && !socket.isClosed()) {
            try {
                sendMessage(manager.createRequestCloseConnection());
                socket.close();
                input.close();
                output.close();
            } catch (IOException ex) {
                System.out.println("Errore chiusura connessione con il server");
            }
            if (socket.isClosed()) {
                System.out.println("L'host si è chiuso correttamente");
            }
        }
    }

    /**
     * ritorna true se il messaggio è stato invitato ritorna false se ci sono
     * stati errori
     *
     * @param message
     * @return boolean
     * @throws java.io.IOException
     */
    public boolean sendMessage(XMLMessage message) throws IOException {
        return manager.sendMessage(output, message);
    }

    /**
     * Metodo che legge un XMLMessage da input
     *
     * @return XMLMessage
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public XMLMessage getMessage() throws IOException, ClassNotFoundException {
        return manager.getMessage(input);
    }

    /**
     *
     * @return XMLManager
     */
    public XMLManager getManager() {
        return manager;
    }

    /**
     *
     * @return Socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     *
     * @return ObjectOutputStream
     */
    public ObjectOutputStream getOutputChannel() {
        return output;
    }

    /**
     *
     * @return ObjectInputStream
     */
    public ObjectInputStream getInputChannel() {
        return input;
    }

    public void resetSocket() {
        try {
            socket.close();
            input.close();
            output.close();
        } catch (IOException ex) {
            System.out.println("Errore resetting socket");
        }
    }
}