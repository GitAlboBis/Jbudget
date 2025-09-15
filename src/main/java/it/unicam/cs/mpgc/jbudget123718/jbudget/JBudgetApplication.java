package it.unicam.cs.mpgc.jbudget123718.jbudget;

import it.unicam.cs.mpgc.jbudget123718.jbudget.config.DataSourceFactory;
import it.unicam.cs.mpgc.jbudget123718.jbudget.config.DatabaseInitializer;
import it.unicam.cs.mpgc.jbudget123718.jbudget.config.ServiceFactory;
import it.unicam.cs.mpgc.jbudget123718.jbudget.ui.controllers.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe principale dell'applicazione JBudget.
 * Gestisce l'avvio, l'inizializzazione e la configurazione dell'applicazione.
 */
public class JBudgetApplication extends Application {

    private static final String APP_TITLE = "JBudget - Gestione Budget Familiare";
    private static final String APP_VERSION = "1.0.0";
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final int MIN_WIDTH = 1000;
    private static final int MIN_HEIGHT = 700;

    private ServiceFactory serviceFactory;
    private DataSource dataSource;
    private Stage primaryStage;

    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("🚀 Inizializzazione JBudget " + APP_VERSION + "...");

        try {
            // Setup del database
            System.out.println("🔧 Configurazione database...");
            dataSource = DataSourceFactory.create();

            // Test della connessione
            testDatabaseConnection();

            // Inizializzazione database
            DatabaseInitializer.init(dataSource);

            // Setup dei servizi con dependency injection
            System.out.println("⚙️ Inizializzazione servizi...");
            serviceFactory = new ServiceFactory(dataSource);

            // Stampa statistiche database
            DatabaseInitializer.printDatabaseStats(dataSource);

            System.out.println("✅ Inizializzazione completata con successo!");

        } catch (Exception e) {
            System.err.println("❌ Errore durante l'inizializzazione: " + e.getMessage());
            e.printStackTrace();

            // Mostra un alert di errore e chiudi l'applicazione
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore Inizializzazione");
                alert.setHeaderText("Impossibile avviare JBudget");
                alert.setContentText("Errore durante l'inizializzazione del database:\n" + e.getMessage() +
                        "\n\nDettagli:\n" + getErrorDetails(e));
                alert.getDialogPane().setPrefWidth(600);
                alert.showAndWait();
                Platform.exit();
            });
            throw e;
        }
    }

    /**
     * Testa la connessione al database
     */
    private void testDatabaseConnection() throws SQLException {
        System.out.println("🔍 Test connessione database...");

        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(5)) {
                throw new SQLException("Connessione database non valida");
            }

            System.out.println("✅ Connessione database OK");
            System.out.println("📁 Database URL: " + conn.getMetaData().getURL());
            System.out.println("🔧 Database Driver: " + conn.getMetaData().getDriverName());
            System.out.println("📊 Database Version: " + conn.getMetaData().getDatabaseProductVersion());

        } catch (SQLException e) {
            System.err.println("❌ Errore connessione database: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        try {
            System.out.println("🎨 Caricamento interfaccia utente...");

            // Carica il file FXML principale
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));

            // Imposta il controller factory per dependency injection
            loader.setControllerFactory(param -> {
                System.out.println("🏗️ Creazione controller: " + param.getSimpleName());

                if (param == MainController.class) {
                    return new MainController(
                            serviceFactory.getMovementService(),
                            serviceFactory.getBudgetService(),
                            serviceFactory.getScadenzaService(),
                            serviceFactory.getBudgetCalculationService(),
                            serviceFactory.getStatsService()
                    );
                }
                else if (param == MovementController.class) {
                    return new MovementController(
                            serviceFactory.getMovementService(),
                            serviceFactory.getLoanService()
                    );
                }
                else if (param == BudgetController.class) {
                    return new BudgetController(
                            serviceFactory.getBudgetService(),
                            serviceFactory.getMovementService(),
                            serviceFactory.getBudgetCalculationService()
                    );
                }
                else if (param == ScadenzeController.class) {
                    return new ScadenzeController(serviceFactory.getScadenzaService());
                }
                else if (param == StatisticsController.class) {
                    return new StatisticsController(
                            serviceFactory.getMovementService(),
                            serviceFactory.getBudgetService(),
                            serviceFactory.getBudgetCalculationService()
                    );
                }

                // Per controller generici
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Impossibile creare controller: " + param.getName(), e);
                }
            });

            // Carica la scena
            Scene scene = new Scene(loader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);

            // Carica il CSS
            loadStyles(scene);

            // Configura la finestra principale
            setupPrimaryStage(primaryStage, scene);

            // Aggiorna automaticamente le scadenze all'avvio
            System.out.println("🔄 Aggiornamento status scadenze...");
            serviceFactory.getScadenzaService().updateOverdueStatus();

            // Aggiorna automaticamente le rate prestiti scadute
            if (serviceFactory.getLoanService() != null) {
                System.out.println("🏦 Aggiornamento status rate prestiti...");
                serviceFactory.getLoanService().updateOverduePayments();
            }

            // Mostra la finestra
            primaryStage.show();

            System.out.println("🎉 JBudget avviato con successo!");
            System.out.println("📊 Applicazione pronta per l'uso");

            // Log informazioni di sistema
            logSystemInfo();

        } catch (IOException e) {
            System.err.println("❌ Errore caricamento interfaccia: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore Caricamento");
            alert.setHeaderText("Impossibile caricare l'interfaccia");
            alert.setContentText("Errore nel caricamento del file FXML:\n" + e.getMessage());
            alert.showAndWait();

            Platform.exit();
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("🔄 Chiusura JBudget...");

        try {
            // Chiudi connessioni database se necessario
            if (dataSource != null) {
                System.out.println("🔌 Chiusura connessioni database...");
                // H2 si chiude automaticamente
            }

            if (serviceFactory != null) {
                System.out.println("🧹 Cleanup servizi completato");
            }

            System.out.println("👋 JBudget chiuso correttamente");

        } catch (Exception e) {
            System.err.println("❌ Errore durante la chiusura: " + e.getMessage());
            e.printStackTrace();
        } finally {
            super.stop();
        }
    }

    /**
     * Configura la finestra principale dell'applicazione.
     */
    private void setupPrimaryStage(Stage stage, Scene scene) {
        stage.setTitle(APP_TITLE + " v" + APP_VERSION);
        stage.setScene(scene);

        // Dimensioni finestra
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        // Centra la finestra
        stage.centerOnScreen();

        // Imposta l'icona dell'applicazione se disponibile
        try {
            var iconUrl = getClass().getResourceAsStream("/images/jbudget-icon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new Image(iconUrl));
                System.out.println("🎨 Icona applicazione caricata");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Icona applicazione non trovata (opzionale)");
        }

        // Gestione chiusura finestra
        stage.setOnCloseRequest(event -> {
            System.out.println("👋 Richiesta chiusura applicazione...");
        });

        System.out.println("🪟 Finestra principale configurata");
    }

    /**
     * Carica i fogli di stile CSS per l'applicazione.
     */
    private void loadStyles(Scene scene) {
        try {
            // Carica il CSS principale
            var cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("🎨 Stili CSS caricati");
            } else {
                System.out.println("⚠️ File CSS non trovato, usando stili di default");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Errore caricamento CSS: " + e.getMessage());

        }
    }

    /**
     * Registra informazioni di sistema utili per debug.
     */
    private void logSystemInfo() {
        System.out.println("\n📋 INFORMAZIONI SISTEMA:");
        System.out.println("🔸 JavaFX Version: " + System.getProperty("javafx.version", "Unknown"));
        System.out.println("🔸 Java Version: " + System.getProperty("java.version"));
        System.out.println("🔸 OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("🔸 User Home: " + System.getProperty("user.home"));
        System.out.println("🔸 Working Directory: " + System.getProperty("user.dir"));

        // Informazioni memoria
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        System.out.println("🔸 Memoria Utilizzata: " + usedMemory + " MB");
        System.out.println("🔸 Memoria Totale: " + totalMemory + " MB");
        System.out.println();
    }

    /**
     * Ottiene i dettagli di un errore per il debug.
     */
    private String getErrorDetails(Exception e) {
        StringBuilder details = new StringBuilder();
        details.append("Tipo: ").append(e.getClass().getSimpleName()).append("\n");
        details.append("Messaggio: ").append(e.getMessage()).append("\n");

        if (e.getCause() != null) {
            details.append("Causa: ").append(e.getCause().getMessage()).append("\n");
        }

        return details.toString();
    }

    /**
     * Punto di ingresso principale dell'applicazione.
     */
    public static void main(String[] args) {
        System.out.println("💰 Avvio JBudget - Gestione Budget Familiare");
        System.out.println("📅 " + java.time.LocalDateTime.now());
        System.out.println("🔸 Versione: " + APP_VERSION);
        System.out.println("🔸 Working Directory: " + System.getProperty("user.dir"));
        System.out.println();

        try {
            // Imposta proprietà di sistema se necessarie
            setupSystemProperties();

            // Avvia l'applicazione JavaFX
            launch(args);

        } catch (Exception e) {
            System.err.println("❌ ERRORE FATALE: Impossibile avviare JBudget");
            System.err.println("Dettagli: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Configura le proprietà di sistema necessarie per l'applicazione.
     */
    private static void setupSystemProperties() {
        // Imposta il thread factory per JavaFX se necessario
        System.setProperty("javafx.animation.fullspeed", "true");

        // Abilita l'accelerazione hardware se disponibile
        System.setProperty("prism.dirtyopts", "false");

        System.out.println("⚙️ Proprietà sistema configurate");
    }

}