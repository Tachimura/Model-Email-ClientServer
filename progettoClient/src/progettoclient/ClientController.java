package progettoclient;

import java.io.EOFException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
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
public class ClientController implements Initializable {

    private ClientModel model;
    private ExecutorService clientService;

    //OGGETTI DEL PANE DI LOGIN
    @FXML
    private Label lblLoginError;
    @FXML
    private ComboBox cmbLoginEmail;

    //OGGETTI DEL PANE DEL BOX UTENTE
    @FXML
    private Label lblUserEmail;
    @FXML
    private ListView lsvEmailReceived, lsvEmailSent, lsvEmailTrash;

    //OGGETTI DEL PANE CREA MAIL
    @FXML
    private ComboBox cmbCreateMailDestinatari, cmbCreateMailLista;
    @FXML
    private TextField txtCreateMailObject;
    @FXML
    private TextArea txtCreateMailText;
    @FXML
    private Label lblCreateMailMissingObject;

    //OGGETTI DEL PANE READ MAIL
    @FXML
    private Label lblReadEmailMittente, lblReadEmailArgomento, lblReadEmailData;
    @FXML
    private TextArea txtReadEmailTesto;
    @FXML
    private ComboBox cmbReadEmailDestinatari;

    //VARI PANE USATI NEL PROGETTO
    @FXML
    private AnchorPane paneLogin;
    private final int PANE_LOGIN = 0;

    @FXML
    private AnchorPane paneUserBox;
    private final int PANE_CASELLA_POSTALE = 1;

    @FXML
    private AnchorPane paneStatus;

    @FXML
    private Label lblServerStatus, lblNotifiche;
    @FXML
    private Button btnRiconnettiti;

    @FXML
    private AnchorPane paneScrivi;
    private final int PANE_CREATE_EMAIL = 2;

    @FXML
    private AnchorPane paneLeggi;
    private final int PANE_READ_EMAIL = 3;

    @FXML
    private void reconnectToServerHandler(ActionEvent event) {
        model.getConnection().startConnection();
        clientService.submit(new ClientBackgroundService());
        try {
            model.getConnection().sendMessage(model.getConnection().getManager().createRequestReconnection(model.getUtente()));
        } catch (IOException ex) {
            System.out.println("Errore nella riconnessione, riprovare più tardi");
        }
    }

    @FXML
    private void logoutHandler(ActionEvent event) {
        model.resetConnection();
        try {
            model.getConnection().sendMessage(model.getConnection().getManager().createRequestLogout());
        } catch (IOException ex) {
            System.out.println("Errore richiesta logout");
        }
        updateGUI(PANE_LOGIN);
    }

    @FXML
    private void scriviEmailHandler(ActionEvent event) {
        pulisciScriviPane();
        updateGUI(PANE_CREATE_EMAIL);
    }

    @FXML
    private void replyEmailHandler(ActionEvent event) {
        pulisciScriviPane();
        txtCreateMailObject.setText(model.getActualEmail().getArgomento());
        txtCreateMailText.setText(model.getActualEmail().getTesto() + "\n");
        cmbCreateMailDestinatari.getItems().add(model.getActualEmail().getMittente());
        updateGUI(PANE_CREATE_EMAIL);
    }

    @FXML
    private void replyAllEmailHandler(ActionEvent event) {
        pulisciScriviPane();
        txtCreateMailObject.setText(model.getActualEmail().getArgomento());
        txtCreateMailText.setText(model.getActualEmail().getTesto() + "\n");
        cmbCreateMailDestinatari.getItems().add(model.getActualEmail().getMittente());
        model.getActualEmail().getDestinatari().forEach((utente) -> {
            if (!utente.getEmail().equals(model.getUtente().getEmail()) && !utente.getEmail().equals(model.getActualEmail().getMittente().getEmail())) {
                cmbCreateMailDestinatari.getItems().add(utente);
            }
        });
        updateGUI(PANE_CREATE_EMAIL);
    }

    @FXML
    private void backToUserBoxButtonHandler(ActionEvent event) {
        updateGUI(PANE_CASELLA_POSTALE);
    }

    @FXML
    private void addToDestinatari(ActionEvent event) {
        if (cmbCreateMailLista.getSelectionModel().getSelectedIndex() != -1) {
            boolean nExists = true;
            for (int cont = 0; cont < cmbCreateMailDestinatari.getItems().size(); cont++) {
                if (cmbCreateMailDestinatari.getItems().get(cont).equals(cmbCreateMailLista.getSelectionModel().getSelectedItem())) {
                    nExists = false;
                }
            }
            if (nExists) {
                cmbCreateMailDestinatari.getItems().add(cmbCreateMailLista.getSelectionModel().getSelectedItem());
            }
        }
    }

    @FXML
    private void removeFromDestinatari(ActionEvent event) {
        if (cmbCreateMailDestinatari.getSelectionModel().getSelectedIndex() != -1) {
            cmbCreateMailDestinatari.getItems().remove(cmbCreateMailDestinatari.getItems().get(cmbCreateMailDestinatari.getSelectionModel().getSelectedIndex()));
        }
    }

    @FXML
    private void forwardEmailHandler(ActionEvent event) {
        pulisciScriviPane();
        txtCreateMailObject.setText(model.getActualEmail().getArgomento());
        String testo = model.getActualEmail().getTesto().substring(0, model.getActualEmail().getTesto().length() - 21);
        txtCreateMailText.setText("<FORWARDED>\n" + testo + "</FORWARDED>\n____________________\n");
        updateGUI(PANE_CREATE_EMAIL);
    }

    @FXML
    private void deleteEmailHandler(ActionEvent event) {
        //QUESTO ORA È FATTO COME RISPOSTA DAL SERVER
        XMLMessage msg = model.getConnection().getManager().createRequestDeleteEmail(model.getActualEmail(), model.getCartella());
        try {
            model.getConnection().sendMessage(msg);
        } catch (IOException e) {
            System.out.println("Errore invio richiesta eliminazione email");
        }
        updateGUI(PANE_CASELLA_POSTALE);
    }

    @FXML
    private void sendEmailButtonHandler(ActionEvent event) {
        //controllo che il campo di object non sia vuoto
        if (txtCreateMailObject.getText().isEmpty()) {
            lblCreateMailMissingObject.setText("Missing Object!");
            //controllo di aver selezionato almeno un destinatario
        } else if (cmbCreateMailDestinatari.getItems().isEmpty()) {
            lblCreateMailMissingObject.setText("Missing destinatari!");
        } else {
            lblCreateMailMissingObject.setText("");
            ArrayList<Utente> dest = new ArrayList<>();
            for (int cont = 0; cont < cmbCreateMailDestinatari.getItems().size(); cont++) {
                String nUtente = cmbCreateMailDestinatari.getItems().get(cont).toString();
                dest.add(new Utente(nUtente));
            }
            //inserisco la mia email come header della mia risposta
            String emailText = txtCreateMailText.getText();
            String[] campi = emailText.split("____________________");
            campi[campi.length - 1] = model.getUtente().getEmail() + " ha scritto:\n" + campi[campi.length - 1].trim();
            emailText = "";
            for (String campo : campi) {
                emailText = emailText + campo.trim() + "\n____________________\n";
            }
            Email newEmail = new Email(0, model.getUtente(), dest, txtCreateMailObject.getText(), emailText, new Date(), true);
            XMLMessage msg = model.getConnection().getManager().createRequestEmailCreate(newEmail);
            try {
                model.getConnection().sendMessage(msg);

            } catch (IOException e) {
                System.out.println("Errore invio richiesta creazione nuova email");
            }
            //pulisco il corpo della mail
            txtCreateMailObject.setText("");
            txtCreateMailText.setText("");
            while (cmbCreateMailDestinatari.getItems().size() > 0) {
                cmbCreateMailDestinatari.getItems().remove(0);
            }
            updateGUI(PANE_CASELLA_POSTALE);
        }
    }

    @FXML
    private void loginButtonHandler(ActionEvent event) {
        System.out.println("Login button pressed");
        lblLoginError.setText("");
        if (cmbLoginEmail.getSelectionModel().getSelectedIndex() == -1) {
            lblLoginError.setText("Email not selected");
        } else {
            //provo a creare la connessione se già non è creata
            model.getConnection().startConnection();

            clientService.submit(new ClientBackgroundService());

            String text = cmbLoginEmail.getSelectionModel().getSelectedItem().toString();
            model.setUtente(text);
            try {
                model.getConnection().sendMessage(model.getConnection().getManager().createRequestLogin(new Utente(text)));
            } catch (IOException e) {
                System.out.println("Errore invio richiesta login");
            }
        }
    }

    /*
        Metodo per aggiornare la GUI
     */
    private void updateGUI(int paneNewStatus) {
        //Per aggiungere nuovi layout all'applicazione basta creare un nuovo valore di pane
        //e aggiungere un caso in entrambi gli switch.
        //se il nuovo pane è diverso da quello attuale aggiorno la gui, se no non c'è ne bisogno
        if (paneNewStatus != model.getPaneStatus()) {
            //nascondo il pane attuale e la disabilito
            switch (model.getPaneStatus()) {
                case PANE_LOGIN:
                    paneLogin.setVisible(false);
                    paneLogin.setDisable(true);
                    break;
                case PANE_READ_EMAIL:
                    paneLeggi.setVisible(false);
                    paneLeggi.setDisable(true);
                    break;
                case PANE_CREATE_EMAIL:
                    paneScrivi.setVisible(false);
                    paneScrivi.setDisable(true);
                    break;
            }
            //mostro il nuovo pane e lo abilito
            switch (paneNewStatus) {
                case PANE_LOGIN:
                    paneLogin.setVisible(true);
                    paneLogin.setDisable(false);
                    paneUserBox.setVisible(false);
                    paneUserBox.setDisable(true);
                    paneStatus.setVisible(false);
                    paneStatus.setDisable(true);
                    break;
                case PANE_CASELLA_POSTALE:
                    paneUserBox.setVisible(true);
                    paneUserBox.setDisable(false);
                    paneStatus.setVisible(true);
                    paneStatus.setDisable(false);
                    break;
                case PANE_READ_EMAIL:
                    paneLeggi.setVisible(true);
                    paneLeggi.setDisable(false);
                    break;
                case PANE_CREATE_EMAIL:
                    paneScrivi.setVisible(true);
                    paneScrivi.setDisable(false);
                    break;
            }
            //imposto il pane attuale al suo nuovo valore
            model.setPaneStatus(paneNewStatus);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        model = new ClientModel();
        lsvEmailReceived.setItems(model.getEmailReceivedPreview());
        lsvEmailReceived.setCellFactory(column -> {
            return new ListCell<EmailPreview>() {
                @Override
                protected void updateItem(EmailPreview email, boolean empty) {
                    super.updateItem(email, empty);
                    if (email == null || empty) {
                        setGraphic(null);
                        return;
                    }
                    Label label = new Label(email.toString());
                    //INSERISCI QUI MODIFICHE GRAFICHE
                    if (email.isNew()) {
                        label.setStyle("-fx-background-color: lightblue;");
                    }

                    setGraphic(label);
                }

            };
        });
        lsvEmailReceived.setOnMouseClicked((MouseEvent event) -> {
            int pos = lsvEmailReceived.getSelectionModel().getSelectedIndex();
            if (pos >= 0) {
                model.setCartella(XMLManager.RECEIVED);
                if (model.getEmailReceivedPreview().get(pos).isNew()) {
                    model.getEmailReceivedPreview().get(pos).setNew(false);
                    lsvEmailReceived.refresh();
                }
                updateGUI(PANE_READ_EMAIL);
                setReadEmailData(pos);
            }
        });
        lsvEmailSent.setItems(model.getEmailSentPreview());
        lsvEmailSent.setOnMouseClicked((MouseEvent event) -> {
            int pos = lsvEmailSent.getSelectionModel().getSelectedIndex();
            if (pos >= 0) {
                model.setCartella(XMLManager.SENT);
                updateGUI(PANE_READ_EMAIL);
                setReadEmailData(pos);
            }
        });
        lsvEmailTrash.setItems(model.getEmailTrashPreview());
        lsvEmailTrash.setOnMouseClicked((MouseEvent event) -> {
            int pos = lsvEmailTrash.getSelectionModel().getSelectedIndex();
            if (pos >= 0) {
                model.setCartella(XMLManager.TRASH);
                updateGUI(PANE_READ_EMAIL);
                setReadEmailData(pos);
            }
        });
        cmbLoginEmail.setItems(Utente.getEmailPossibiliObservable());
        cmbCreateMailLista.setItems(Utente.getEmailRegistrateObservable());
        model.setConnection(new ConnectionManager("localhost", 6789));
        /* PROVA
        model.getConnection().startConnection();
        //faccio partire il mio listener
         *///faccio partire il mio listener se non è ancora attivo
        if (clientService == null) {
            clientService = Executors.newSingleThreadExecutor();
        }
        /*if (clientService == null) {
            clientService = Executors.newSingleThreadExecutor();
            clientService.submit(new ClientBackgroundService());
        }*/
    }

    private void setReadEmailData(int position) {
        EmailPreview email;
        switch (model.getCartella()) {
            case XMLManager.RECEIVED:
                email = (EmailPreview) model.getEmailReceivedPreview().get(position);
                break;
            case XMLManager.SENT:
                email = (EmailPreview) model.getEmailSentPreview().get(position);
                break;
            default:
                email = (EmailPreview) model.getEmailTrashPreview().get(position);
                break;
        }
        try {
            model.getConnection().sendMessage(model.getConnection().getManager().createRequestEmailData(email, model.getCartella()));
        } catch (IOException e) {
            System.out.println("Errore richiesta dati email");
            updateGUI(PANE_CASELLA_POSTALE);
        }
    }

    void closeConnection() {
        model.setContinue(false);
        model.getConnection().closeConnection();
        clientService.shutdownNow();
    }

    private void initUser() {
        //LANCIATO QUANDO L'UTENTE FA IL LOGIN
        //RICHIEDO AL SERVER TUTTA LA LISTA DELLE MIE EMAIL
        //E CREO UN THREAD CHE RICEVE LE NUOVE EMAIL(se non esiste gia)
        //imposto il mio username nella label
        lblUserEmail.setText(model.getUtente().toString());
        //richiedo le email dell'utente
        try {
            model.getConnection().sendMessage(model.getConnection().getManager().createRequestEmailPreview());
        } catch (IOException e) {
            System.out.println("Errore richiesta email preview");
        }
    }

    private void pulisciScriviPane() {
        if (cmbCreateMailDestinatari.getItems().size() > 0) {
            while (cmbCreateMailDestinatari.getItems().size() > 0) {
                cmbCreateMailDestinatari.getItems().remove(0);
            }
        }
        txtCreateMailText.setText("");
        txtCreateMailObject.setText("");
    }

    private void createNotification(String string) {
        Platform.runLater(() -> {
            lblNotifiche.setText(string);
            FadeTransition blinktransition = new FadeTransition(Duration.seconds(0.25), lblNotifiche);
            blinktransition.setFromValue(1.0);
            blinktransition.setToValue(0.0);
            blinktransition.setCycleCount(10);
            blinktransition.play();
        });
    }

    private class ClientBackgroundService implements Runnable {

        public ClientBackgroundService() {
            model.setContinue(true);
        }

        @Override
        public void run() {
            System.out.println("service lavoro iniziato");
            //creo la notifica che verrà mostrata quando serve
            //ora attendo che il server mi mandi nuove email
            while (model.getConnection().getSocket() != null && model.getContinue()) {
                XMLMessage message = null;
                try {
                    message = model.getConnection().getMessage();
                } catch (EOFException ex) {
                    model.setContinue(false);
                    System.out.println("Server disconnesso");
                    createNotification("Connessione al server persa");
                } catch (IOException | ClassNotFoundException ex) {
                    if (!model.getContinue()) {
                        System.out.println("Client chiuso");
                    } else {
                        System.out.println("Server disconnesso");
                        createNotification("Connessione al server persa");
                        model.setContinue(false);
                    }
                }
                if (message != null) {
                    switch (message.getComando()) {
                        case XMLManager.REQUEST_LOGIN:
                            if (message.getResult() == XMLManager.CORRECT) {
                                //se il login è corretto allora vado nella mia casella postale
                                Platform.runLater(() -> {
                                    lblServerStatus.setText("Online");
                                    lblServerStatus.setTextFill(Color.GREEN);
                                    lblLoginError.setText("");
                                    updateGUI(PANE_CASELLA_POSTALE);
                                    initUser();
                                });
                            } else {
                                Platform.runLater(() -> {
                                    lblLoginError.setText("Email Incorrect");
                                });
                            }
                            break;
                        //SE MI ARRIVANO LE EMAIL PREVIEW VARIE FACCIO LA INIT
                        case XMLManager.REQUEST_EMAIL_PREVIEW:
                            initListView(message);
                            break;
                        //SE HO RICEVUTO UNA NUOVA EMAIL LA AGGIUNGO ALLA LISTA
                        case XMLManager.ANSWER_EMAIL_NEW:
                            if (message.getEmailPreview() != null) {
                                switch (message.getResult()) {
                                    case XMLManager.RECEIVED:
                                        createNotification("Nuova email ricevuta");
                                        message.getEmailPreview().setNew(true);
                                        model.addEmailReceivedPreview(message.getEmailPreview());
                                        break;
                                    case XMLManager.SENT:
                                        createNotification("Email inviata");
                                        model.addEmailSentPreview(message.getEmailPreview());
                                        break;
                                    default:
                                        createNotification("Email spostata nel cestino");
                                        model.addEmailTrashPreview(message.getEmailPreview());
                                        break;
                                }
                            }
                            break;
                        //SE HO RICEVUTO I DATI DI UNA EMAIL IMPOSTO IL LAYOUT
                        case XMLManager.REQUEST_EMAIL_DATA:
                            Email email = message.getEmail();
                            model.setActualEmail(email);
                            Platform.runLater(() -> {
                                lblReadEmailArgomento.setText(email.getArgomento());
                                lblReadEmailMittente.setText(email.getMittente().toString());
                                lblReadEmailData.setText(email.getData().toString());
                                txtReadEmailTesto.setText(email.getTesto());
                                cmbReadEmailDestinatari.setItems(FXCollections.observableArrayList(email.getDestinatari()));
                            });
                            break;
                        case XMLManager.REQUEST_EMAIL_DELETE:
                            if (message.getResult() == XMLManager.CORRECT) {
                                switch (Integer.valueOf(message.getUser().getEmail())) {
                                    case XMLManager.RECEIVED:
                                        createNotification("Email cestinata");
                                        for (int cont = 0; cont < model.getEmailReceivedPreview().size(); cont++) {
                                            if (model.getEmailReceivedPreview().get(cont).getID() == message.getEmail().getID()) {
                                                model.removeEmailReceivedPreviewPosition(cont);
                                                break;
                                            }
                                        }
                                        break;
                                    case XMLManager.SENT:
                                        createNotification("Email cestinata");
                                        for (int cont = 0; cont < model.getEmailSentPreview().size(); cont++) {
                                            if (model.getEmailSentPreview().get(cont).getID() == message.getEmail().getID()) {
                                                model.removeEmailSentPreviewPosition(cont);
                                                break;
                                            }
                                        }
                                        break;
                                    default:
                                        createNotification("Email cancellata");
                                        for (int cont = model.getEmailTrashPreview().size() - 1; cont >= 0; cont--) {
                                            if (model.getEmailTrashPreview().get(cont).getID() == message.getEmail().getID()) {
                                                model.removeEmailTrashPreviewPosition(cont);
                                            }
                                        }
                                        break;
                                }
                            } else {
                                createNotification("Errore eliminazione email");
                            }
                            break;
                        case XMLManager.REQUEST_RECONNECTION:
                            if (message.getResult() == XMLManager.CORRECT) {
                                createNotification("Connessione al server ricreata");
                                System.out.println("Server riconnesso");
                                Platform.runLater(() -> {
                                    lblServerStatus.setText("Online");
                                    lblServerStatus.setTextFill(Color.GREEN);
                                    btnRiconnettiti.setDisable(true);
                                });
                            }
                            break;
                    }
                }
            }
            System.out.println("service lavoro concluso");
            model.getConnection().resetSocket();
            Platform.runLater(() -> {
                lblServerStatus.setText("Offline");
                lblServerStatus.setTextFill(Color.RED);
                btnRiconnettiti.setDisable(false);
            });
        }

        private void initListView(XMLMessage message) {
            if (message.getResult() != XMLManager.NOTHING) {
                if (message.getEmailReceivedPreviewArray() != null) {
                    message.getEmailReceivedPreviewArray().forEach((preview) -> {
                        model.addEmailReceivedPreview(preview);
                    });
                }
                if (message.getEmailSentPreviewArray() != null) {
                    message.getEmailSentPreviewArray().forEach((preview) -> {
                        model.addEmailSentPreview(preview);
                    });
                }
                if (message.getEmailTrashPreviewArray() != null) {
                    message.getEmailTrashPreviewArray().forEach((preview) -> {
                        model.addEmailTrashPreview(preview);
                    });
                }
            }
        }
    }
}