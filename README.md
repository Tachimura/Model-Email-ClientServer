# Model - Email Service - ClientServer
Progetto scritto in Java, con alcuni file css per gestire la grafica.<br/>
Progetto suddiviso in 3 packages:
- Package per il Server
- Package per il Client
- Package per la gestione dei Dati

Il server permette la memorizzazione delle email inviate fra utenti, permettendo la creazione di conversazioni.<br/>
Il funzionamento dei client non è compromesso se il server viene spento. (utilizzo di cache)<br/>
Il funzionamento dei client viene totalmente ripreso (possibilità di inviare/ricevere nuove email) una volta che il server viene fatto ripartire.<br/>
L'invio e la gestione delle email è fatta tramite l'invio di classi serializzabili definite nel package della gestione dei Dati.<br/>
Non vengono usate connessioni, ma vengono creati socket a runtime per ridurre il consumo di memoria pur mantenendo buone performance grazie a dei pool di sockets.
