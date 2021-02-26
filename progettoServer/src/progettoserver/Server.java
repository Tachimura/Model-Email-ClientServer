package progettoserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import progetto_data.CasellaPostale;
import progetto_data.ConnectionManager;
import progetto_data.Email;
import progetto_data.EmailPreview;
import progetto_data.Utente;
import progetto_data.XMLManager;
import progetto_data.XMLMessage;

/**
 *
 * @author gianluca
 */
public class Server extends Thread {

    private ServerModel model;
    private final int porta;
    private final AtomicInteger cont_id;
    private boolean server_attivo;
    private ServerSocket sSocket;
    private ArrayList<ClientConnection> connections;

    /**
     *
     * @param porta
     */
    public Server(int porta) {
        this.porta = porta;
        server_attivo = false;
        sSocket = null;
        model = null;
        cont_id = StorageController.initServerDocuments();
    }

    @Override
    public void run() {
        //creo i vari dati del server   
        try {
            sSocket = new ServerSocket(porta);
        } catch (IOException ex) {
            System.out.println("Error creating server socket");
        }
        //se la creazione è andata a buon fine
        if (sSocket != null) {
            model.addAction("Server partito");
            server_attivo = true;
            connections = new ArrayList<>();
            Socket clientSocket;
            ClientConnection newConnection;
            //ciclo per accettare le varie connessioni
            while (server_attivo) {
                try {
                    //mi metto in attesa di una richiesta di connessione
                    clientSocket = sSocket.accept();
                    //creo un nuovo thread con il socket della connessione
                    newConnection = new ClientConnection(clientSocket);
                    //aggiungo il thread alla lista dei thread attivi
                    connections.add(newConnection);
                    //faccio partire un thread per gestire la connessione appena ricevuta
                    newConnection.start();
                } catch (IOException ex) {
                    //se il server è attivo ho avuto un errore nella creazione della connessione client-server
                    if (server_attivo) {
                        System.out.println("Error creating server-client connection");
                    } //se il server non è attivo ho ricevuto un errore causato dalla chiusura del server socket
                    //da parte del metodo stopServer().
                    else {
                        System.out.println("Server chiuso da chiusura applicazione");
                    }
                }
            }
        }
    }

    /**
     *
     * @param model
     */
    void setDataModel(ServerModel model) {
        this.model = model;
    }

    /**
     *
     */
    void stopServer() {
        model.addAction("Server Terminato");
        //chiudo connessioni
        if (connections != null) {
            while (connections.size() > 0) {
                connections.get(0).closeConnection();
            }
        }
        //imposto il server attivo a false
        server_attivo = false;
        //chiudo il socket del server, questo fa si che il metodo run non rimanga ad aspettare
        //per una connessione nuova di un client ma che si fermi
        //il metodo socket.accept() è bloccante per cui l'unico modo per sbloccarlo è
        //creare una connessione falsa oppure chiudere il socket del server

        //@TODO: SE CREO UN ARRAY DI THREAD DI CONNESSIONE POSSO DIRGLI DI CHIUDERE CONNESSIONE PRIMA DI CHIUDERE IL SOCKET
        try {
            sSocket.close();
        } catch (IOException ex) {
            System.out.println("Errore chiusura socket server");
        }
        if (sSocket.isClosed()) {
            StorageController.saveServerState(cont_id);
            System.out.println("Server si è chiuso");
        }
    }

    /**
     *
     * @param name
     */
    private void rimuoviThread(String name) {
        for (int cont = 0; cont < connections.size(); cont++) {
            if (connections.get(cont).getName().equals(name)) {
                connections.remove(cont);
            }
        }
    }

    /**
     *
     * @param email
     * @param cartella
     */
    public void notifyNewMail(Email email, int cartella) {
        //per ogni connessione che ho
        if (cartella == XMLManager.SENT) {
            connections.stream().filter((utente) -> (utente.userBox.getUser().toString().equals(email.getMittente().toString()))).forEachOrdered((utente) -> {
                utente.notifiedNewEmail(email, cartella);
            });
        } else {
            connections.forEach((connection) -> {
                //se l'user gestito dalla connessione è uno dei destinatari allora gli dico che ha una nuova email
                email.getDestinatari().stream().filter((utente) -> (connection.userBox.getUser().getEmail().equals(utente.getEmail()))).forEachOrdered((_item) -> {
                    connection.notifiedNewEmail(email, cartella);
                });
            });
        }
    }

    /**
     *
     * @param email
     * @param cartella
     */

    /**
     *
     * @param utente
     * @param email
     * @param success
     * @param cartella
     */
    public void notifyDeleteMail(Utente utente, Email email, int success, int cartella) {
        //per ogni connessione che ho
        connections.forEach((connection) -> {
            //se l'user gestito dalla connessione è uno dei destinatari allora gli dico che ha una nuova email
            if (connection.userBox.getUser().getEmail().equals(utente.getEmail())) {
                connection.notifiedDeleteMail(email, success, cartella);
            }
        });
    }

    /*
        Thread che gestirà tutta la connessione con il server
     */
    private class ClientConnection extends Thread {

        private ConnectionManager connection;
        private StorageController storage;
        private CasellaPostale userBox;
        private boolean mContinue;

        /**
         *
         * @param cSocket
         */
        public ClientConnection(Socket cSocket) {
            mContinue = false;
            storage = new StorageController();
            try {
                connection = new ConnectionManager(cSocket);
            } catch (IOException ex) {
                try {
                    if (connection.getSocket() != null && !connection.getSocket().isClosed()) {
                        connection.getSocket().close();
                    }
                } catch (IOException e) {
                    System.out.println("Errore chiusura socket");
                }
                System.out.println("Errore creazione parametri del socket");
            }
        }

        @Override
        public void run() {
            mContinue = true;
            model.addAction(getName() + ": Client collegato");
            while (server_attivo && mContinue) {
                XMLMessage message = null;
                try {
                    message = connection.getMessage();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Errore ricezione messaggio dal client");
                }
                if (message == null) {
                    mContinue = false;
                } else {
                    switch (message.getComando()) {
                        //l'utente ha richiesto il login
                        case XMLManager.REQUEST_LOGIN:
                            model.addAction(getName() + ": " + message.getUser() + " LOGIN REQUEST");
                            //controllo che l'utente sia registrato, se lo è gli ritorno correct
                            String res = "INCORRECT";
                            int res_num = XMLManager.INCORRECT;
                            if (storage.searchUser(message.getUser().getEmail())) {
                                res = "CORRECT";
                                res_num = XMLManager.CORRECT;
                                userBox = new CasellaPostale(new Utente(message.getUser().getEmail()));
                            }
                            model.addAction(getName() + ": " + message.getUser() + " EMAIL " + res);
                            try {
                                connection.sendMessage(connection.getManager().createAnswerLogin(res_num));
                            } catch (IOException ex) {
                                System.out.println("Errore invio risposta login " + res);
                            }
                            break;
                        //richiede le email sul suo indirizzo
                        case XMLManager.REQUEST_EMAIL_PREVIEW:
                            model.addAction(getName() + ": " + userBox.getUser() + " requested his preview emails");
                            try {
                                connection.sendMessage(connection.getManager().createAnswerPreviewEmail(storage.getUserPreviewEmail(userBox.getUser(), XMLManager.RECEIVED), storage.getUserPreviewEmail(userBox.getUser(), XMLManager.SENT), storage.getUserPreviewEmail(userBox.getUser(), XMLManager.TRASH)));
                            } catch (IOException ex) {
                                System.out.println("Errore invio risposta lista preview email");
                            }
                            break;
                        //richiede i dati di una particolare email
                        case XMLManager.REQUEST_EMAIL_DATA:
                            model.addAction(getName() + ": " + userBox.getUser() + " has requested email details");
                            try {
                                connection.sendMessage(connection.getManager().createAnswerEmailData(storage.readEmailFromFile(userBox.getUser(), message.getEmailPreview(), message.getResult())));
                            } catch (IOException ex) {
                                System.out.println("Errore invio risposta email data");
                            }
                            break;
                        //richiede la creazione di una nuova email
                        case XMLManager.REQUEST_EMAIL_CREATE:
                            model.addAction(getName() + ": " + userBox.getUser() + " has created a mail");
                            message.getEmail().updateID(cont_id.incrementAndGet());
                            message.getEmail().updateData(new Date());
                            storage.saveEmailIntoFile(message.getEmail(), XMLManager.RECEIVED);
                            notifyNewMail(message.getEmail(), XMLManager.RECEIVED);
                            message.getEmail().setNew(false);
                            storage.saveEmailIntoFile(message.getEmail(), XMLManager.SENT);
                            notifyNewMail(message.getEmail(), XMLManager.SENT);
                            break;
                        //richiede l'eliminazione di una email
                        case XMLManager.REQUEST_EMAIL_DELETE:
                            int success = XMLManager.INCORRECT;
                            model.addAction(getName() + ": " + userBox.getUser() + " has deleted a mail");
                            if (storage.deleteEmail(userBox.getUser(), message.getEmail(), message.getResult())) {
                                success = XMLManager.CORRECT;
                                if (message.getResult() == XMLManager.TRASH) {
                                    model.addAction(getName() + ": success removing email");
                                } else {
                                    model.addAction(getName() + ": success email sent to trash");
                                    notifyNewMail(message.getEmail(), XMLManager.TRASH);
                                }
                            } else {
                                model.addAction(getName() + ": failed removing email");
                            }
                            notifyDeleteMail(userBox.getUser(), message.getEmail(), success, message.getResult());
                            break;
                        case XMLManager.REQUEST_LOGOUT:
                            model.addAction(getName() + ": " + userBox.getUser() + " disconnected");
                            userBox = null;
                            break;
                        //chiude la connessione
                        case XMLManager.REQUEST_CLOSE_CONNECTION:
                            mContinue = false;
                            break;
                        case XMLManager.REQUEST_RECONNECTION:
                            model.addAction(getName() + ": " + message.getUser() + " requested reconnection");
                            userBox = new CasellaPostale(new Utente(message.getUser().getEmail()));

                            try {
                                connection.sendMessage(connection.getManager().createAnswerReconnection(XMLManager.CORRECT));
                                model.addAction(getName() + ": " + userBox.getUser() + " reconnected");
                            } catch (IOException ex) {
                                System.out.println("Errore riconessione con l'utente");
                            }

                            break;
                    }
                }
            }
            closeConnection();
        }

        private void closeConnection() {
            mContinue = false;
            model.addAction(getName() + ": Client disconnected");
            try {
                if (connection.getSocket() != null) {
                    connection.getSocket().close();
                }
                if (connection.getInputChannel() != null) {
                    connection.getInputChannel().close();
                }
                if (connection.getOutputChannel() != null) {
                    connection.getOutputChannel().close();
                }
            } catch (IOException ex) {
                System.out.println("Errore chiusura connessione");
            }
            rimuoviThread(getName());
        }

        /**
         *
         * @param email
         */
        private void notifiedNewEmail(Email email, int cartella) {
            if (cartella == XMLManager.RECEIVED) {
                model.addAction(getName() + ": " + userBox.getUser() + " has received a new email");
            }
            try {
                connection.sendMessage(connection.getManager().createAnswerNewEmail(new EmailPreview(email.getID(), email.getMittente(), email.getArgomento(), email.getData(), false), cartella));
            } catch (IOException ex) {
                System.out.println("Errore invio nuova email");
            }
        }

        private void notifiedDeleteMail(Email email, int success, int result) {
            try {
                connection.sendMessage(connection.getManager().createAnswerDeleteEmail(email, success, result));
            } catch (IOException e) {
                System.out.println("Errore invio messaggio di elimina email all'utente");
            }
        }
    }
}