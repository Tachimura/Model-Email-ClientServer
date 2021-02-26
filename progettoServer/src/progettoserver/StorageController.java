/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package progettoserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import progetto_data.Email;
import progetto_data.EmailPreview;
import progetto_data.Utente;
import progetto_data.XMLManager;

/**
 *
 * @author gianluca
 */
public class StorageController {

    public static final String PATH = "./storage/";
    private static final String RECEIVED = "received/";
    private static final String SENT = "sent/";
    private static final String TRASH = "trash/";

    /**
     *
     * Metodo che controlla se l'utente è registrato
     *
     * @param email
     * @return
     */
    public boolean searchUser(String email) {
        boolean result = false;
        //se la mail ricevuta fa parte delle email registrate eseguo il login
        for (String registrata : Utente.EMAILREGISTRATE) {
            if (registrata.equals(email)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Metodo che data un'EmailPreview cerca il file dell'Email a cui fanno
     * riferimento il mittente e l'ID.
     *
     * @param user
     * @param prev
     * @param cartella
     * @return email
     */
    public Email readEmailFromFile(Utente user, EmailPreview prev, int cartella) {
        Email email = null;
        File f;
        FileChannel fileChannel = null;
        FileLock lock = null;
        FileInputStream fi = null;
        ObjectInputStream oi = null;
        String path = PATH + user + "/";
        switch (cartella) {
            case XMLManager.RECEIVED:
                path = path + RECEIVED;
                break;
            case XMLManager.SENT:
                path = path + SENT;
                break;
            default:
                path = path + TRASH;
                break;
        }
        path = path + prev.getID() + ".txt";
        try {
            f = new File(path);
            fileChannel = FileChannel.open(f.toPath(), StandardOpenOption.READ);
            lock = fileChannel.lock(0, Long.MAX_VALUE, true);
            fi = new FileInputStream(f);
            oi = new ObjectInputStream(fi);
            try {
                email = (Email) oi.readObject();
            } catch (ClassNotFoundException ex) {
                System.out.println("Errore lettura email da file -> salvataggio corrotto");
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } finally {
            try {
                if (oi != null) {
                    oi.close();
                }
                if (fi != null) {
                    fi.close();
                }
                if (lock != null) {
                    lock.close();
                }
                if (fileChannel != null) {
                    fileChannel.close();
                }
            } catch (IOException ex) {
                System.out.println("Errore chiusura stream in readEmailFromFile");
            }
        }
        //se la email non è ancora mai stata letta la imposto come letta sovrascrivendola
        if (email.isNew()) {
            email.setNew(false);
            saveEmailIntoFile(email, cartella);
        }
        return email;
    }

    /**
     * Metodo che data una Mail si appresta a salvarla su file. Salva la mail
     * usando l'id come nome del file e la salve nella directory con il nome del
     * mittente. Il metodo controlla e crea anche eventuali directory se
     * mancanti
     *
     * @param email
     * @param cartella
     */
    public void saveEmailIntoFile(Email email, int cartella) {
        if (cartella == XMLManager.SENT) {
            writeIntoFile(email.getMittente(), email, cartella);
        } else {
            email.getDestinatari().forEach((destinatario) -> {
                writeIntoFile(destinatario, email, cartella);
            });
        }
    }

    private void writeIntoFile(Utente utente, Email email, int cartella) {
        File f;
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        String path = PATH + utente.getEmail() + "/";
        switch (cartella) {
            case XMLManager.RECEIVED:
                path = path + RECEIVED;
                break;
            case XMLManager.SENT:
                path = path + SENT;
                break;
            default:
                path = path + TRASH;
                break;
        }
        path = path + email.getID() + ".txt";
        try {
            f = new File(path);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            f.createNewFile();
            fos = new FileOutputStream(f, false); //riscrivi la mail
            oos = new ObjectOutputStream(fos);
            oos.writeObject(email); //scrive su file
            //chiudi tutti i file pointer
            oos.close();
            fos.close();
        } catch (IOException ex) {
            System.out.println("Errore salvataggio Email nel file in saveEmailIntoFile()");
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                System.out.println("Errore chiusura stream nel salvataggio Email nel file");
            }
        }
    }

    /**
     *
     * @param user
     * @param email
     * @param cartella
     * @return deleted
     */
    public boolean deleteEmail(Utente user, Email email, int cartella) {
        String path = PATH + user.getEmail() + "/";
        boolean delete = false;
        switch (cartella) {
            case XMLManager.RECEIVED:
                path = path + RECEIVED;
                break;
            case XMLManager.SENT:
                path = path + SENT;
                break;
            default:
                path = path + TRASH;
                delete = true;
                break;
        }
        path = path + email.getID() + ".txt";
        File f = new File(path);
        FileChannel fileChannel = null;
        FileLock lock = null;
        if (!delete) {
            path = PATH + user.getEmail() + "/" + TRASH + email.getID() + ".txt";
            File f2 = new File(path);
            if (!f2.getParentFile().exists()) {
                f2.getParentFile().mkdirs();
            }
            InputStream inStream = null;
            OutputStream outStream = null;
            try {
                fileChannel = FileChannel.open(f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.READ);
                lock = fileChannel.lock(0, Long.MAX_VALUE, true);
                inStream = new FileInputStream(f);
                outStream = new FileOutputStream(f2);
                byte[] buffer = new byte[1024];
                int length;
                //copy the file content in bytes 
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }

                inStream.close();
                outStream.close();
            } catch (IOException e) {
                System.out.println("Errore spostamento nel cestino metodo -> deleteEmail()");
            } finally {
                try {
                    if (inStream != null) {
                        inStream.close();
                    }
                    if (outStream != null) {
                        outStream.close();
                    }
                    if (lock != null) {
                        lock.close();
                    }
                    if (fileChannel != null) {
                        fileChannel.close();
                    }
                } catch (IOException e2) {
                    System.out.println("Errore chiusura buffers -> deleteEmail()");
                }
            }
        }
        return f.delete();
    }

    /**
     *
     * @param user
     * @param cartella
     * @return ArrayList
     */
    public ArrayList<EmailPreview> getUserPreviewEmail(Utente user, int cartella) {
        ArrayList<EmailPreview> preview = null;
        Email email;
        EmailPreview emailPreview;
        File f;
        FileChannel fileChannel;
        FileLock lock;
        FileInputStream fi = null;
        ObjectInputStream oi = null;
        String path;
        path = PATH + user.getEmail() + "/";
        switch (cartella) {
            case XMLManager.RECEIVED:
                path = path + RECEIVED;
                break;
            case XMLManager.SENT:
                path = path + SENT;
                break;
            default:
                path = path + TRASH;
                break;
        }
        try {
            f = new File(path);
            File[] lista = f.listFiles();
            if (lista != null) {
                preview = new ArrayList<>();
                for (File rF : lista) {
                    fileChannel = FileChannel.open(rF.toPath(), StandardOpenOption.READ);
                    lock = fileChannel.lock(0, Long.MAX_VALUE, true);
                    fi = new FileInputStream(rF);
                    oi = new ObjectInputStream(fi);
                    email = (Email) oi.readObject();
                    emailPreview = new EmailPreview(email.getID(), email.getMittente(), email.getArgomento(), email.getData(), email.isNew());
                    preview.add(emailPreview);
                    if (oi != null) {
                        oi.close();
                    }
                    if (fi != null) {
                        fi.close();
                    }
                    lock.release();
                    fileChannel.close();
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Errore lettura email da file in getUserPreviewEmail() -> File corrotto");
        } finally {
            try {
                if (oi != null) {
                    oi.close();
                }
                if (fi != null) {
                    fi.close();
                }
            } catch (IOException ex) {
                System.out.println("Errore chiusura stream in getUserEmailPreview()");
            }
        }

        return preview;
    }

    /**
     *
     * @return cont_id
     */
    public static AtomicInteger initServerDocuments() {
        //QUA CREO SE ANCORA NON C'È LA CARTELLA DEL SERVER
        //DENTRO CREO LE CARTELLE DEI VARI TIZI
        //UN DOCUMENTO DI TESTO X TENER SALVATI ALCUNI VALORI DEL SERVER
        //TIPO L'ID ATTUALE DELLE EMAIL, CHE VERRA SALVATO IN UN ATOMICINTEGER
        //E QUESTO È USATO X DARE L'ID NELLE EMAIL, ED ESSENDO ATOMIC FA ISTRUZIONI SENZA VENIRE BLOCCATO
        AtomicInteger cont_id = new AtomicInteger(1);
        File f;
        FileInputStream fi = null;
        ObjectInputStream oi = null;
        String path = StorageController.PATH + "/server_preferences.txt";
        try {
            f = new File(path);
            //se esiste già il file, carico il valore di cont_id
            if (f.exists()) {
                fi = new FileInputStream(f);
                oi = new ObjectInputStream(fi);
                cont_id = new AtomicInteger(oi.readInt());
                oi.close();
                fi.close();
            } else {
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                if (!f.exists()) {
                    f.createNewFile();
                }
            }
        } catch (IOException e) {
            System.out.println("Errore lettura dati da server_preferences.txt");
        } finally {
            try {
                if (oi != null) {
                    oi.close();
                }
                if (fi != null) {
                    fi.close();
                }
            } catch (IOException ex) {
                System.out.println("Errore chiusura input stream initServerDocuments()");
            }
        }
        return cont_id;
    }

    /**
     *
     * @param cont_id
     */
    public static void saveServerState(AtomicInteger cont_id) {
        //COME SOPRA MA DEVO SALVARE I VARI VALORI DEL SERVER IN UN FILE DI TESTO PRIMA CHE ESSO VENGA SPENTO
        File f;
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        String path = StorageController.PATH + "/server_preferences.txt";
        try {
            f = new File(path);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            if (!f.exists()) {
                f.createNewFile();
            }
            fos = new FileOutputStream(f, false); //riscrivi la mail
            oos = new ObjectOutputStream(fos);
            oos.writeInt(cont_id.get()); //scrive su file
            //chiudi tutti i file pointer
            oos.close();
            fos.close();
        } catch (IOException ex) {
            System.err.println("Errore salvataggio dati nel file server in saveServerState()");
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                System.out.println("Errore chiusura file output in saveServerState()");
            }
        }
    }
}