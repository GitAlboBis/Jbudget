package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.controllers;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Scadenza;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.ScadenzaStatus;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.ScadenzaService;
import it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs.PayScadenzaDialog;
import it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs.ScadenzaDialog;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller dedicato alla gestione delle scadenze.
 */
public class ScadenzeController extends BaseController {

    @FXML private TableView<Scadenza> scadenzeTable;
    @FXML private TableColumn<Scadenza, String> scadDescCol;
    @FXML private TableColumn<Scadenza, LocalDate> scadDateCol;
    @FXML private TableColumn<Scadenza, String> scadStatusCol;
    @FXML private Button addScadenzaBtn;
    @FXML private Button payScadenzaBtn;
    @FXML private Button deleteScadenzaBtn;
    @FXML private Button updateStatusBtn;

    private final ScadenzaService scadenzaService;
    private Runnable onDataChangedCallback;

    public ScadenzeController(ScadenzaService scadenzaService) {
        this.scadenzaService = scadenzaService;
    }

    @Override
    public void initialize() {
        setupTable();
        setupEventHandlers();
        refreshData();

        // Registra per ricevere notifiche di aggiornamento
        DataRefreshManager.getInstance().addRefreshListener(this::refreshData);
    }

    private void setupTable() {
        scadDescCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() -> cell.getValue().description()));

        scadDateCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().dueDate()));

        scadStatusCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> {
                            ScadenzaStatus status = cell.getValue().status();
                            return switch (status) {
                                case PENDING -> "⏳ In Attesa";
                                case COMPLETED -> "✅ Completata";
                                case OVERDUE -> "🚨 Scaduta";
                            };
                        }));

        // Formatting per date scadenze
        scadDateCol.setCellFactory(column -> new TableCell<Scadenza, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(date.toString());
                    LocalDate today = LocalDate.now();
                    if (date.isBefore(today)) {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    } else if (date.isBefore(today.plusDays(7))) {
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #10B981;");
                    }
                }
            }
        });
    }

    private void setupEventHandlers() {
        scadenzeTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean nothingSelected = newSelection == null;
                    payScadenzaBtn.setDisable(nothingSelected);
                    deleteScadenzaBtn.setDisable(nothingSelected);
                }
        );

        // Inizializza stati bottoni
        payScadenzaBtn.setDisable(true);
        deleteScadenzaBtn.setDisable(true);
    }

    @FXML
    private void onAddScadenza() {
        System.out.println("🔄 Apertura dialog nuova scadenza...");
        try {
            ScadenzaDialog dialog = new ScadenzaDialog();
            dialog.showAndWait().ifPresent(scadenza -> {
                try {
                    scadenzaService.addScadenza(scadenza);
                    refreshData();
                    notifyDataChanged();
                    showAlert("Successo", "Scadenza aggiunta con successo!");
                } catch (Exception e) {
                    System.err.println("❌ Errore salvataggio scadenza: " + e.getMessage());
                    showError("Errore", "Impossibile salvare la scadenza: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Errore aggiunta scadenza: " + e.getMessage());
            showError("Errore", "Impossibile aggiungere la scadenza: " + e.getMessage());
        }
    }

    @FXML
    private void onPayScadenza() {
        Scadenza selected = scadenzeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Nessuna Selezione", "Seleziona una scadenza da pagare.");
            return;
        }

        if (selected.status() == ScadenzaStatus.COMPLETED) {
            showAlert("Attenzione", "Questa scadenza è già stata pagata!");
            return;
        }

        try {
            System.out.println("💰 Apertura dialog pagamento scadenza...");
            PayScadenzaDialog dialog = new PayScadenzaDialog(selected);
            dialog.showAndWait().ifPresent(paymentInfo -> {
                try {
                    scadenzaService.payScadenza(
                            selected.id(),
                            paymentInfo.amount(),
                            paymentInfo.category()
                    );
                    refreshData();

                    // Notifica altri controller dell'aggiornamento (importante per statistiche e budget!)
                    DataRefreshManager.getInstance().notifyDataChanged("ScadenzeController");

                    showAlert("Successo",
                            "Scadenza pagata e movimento di uscita registrato!\n\n" +
                                    "Importo: €" + String.format("%.2f", paymentInfo.amount()) + "\n" +
                                    "Categoria: " + paymentInfo.category().name());
                } catch (Exception e) {
                    System.err.println("❌ Errore pagamento scadenza: " + e.getMessage());
                    showError("Errore", "Impossibile processare il pagamento: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Errore pagamento scadenza: " + e.getMessage());
            showError("Errore", "Impossibile processare il pagamento: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteScadenza() {
        Scadenza selected = scadenzeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Nessuna Selezione", "Seleziona una scadenza da eliminare.");
            return;
        }

        String message = String.format(
                "Sei sicuro di voler eliminare questa scadenza?\n\n" +
                        "Descrizione: %s\n" +
                        "Data Scadenza: %s\n" +
                        "Status: %s",
                selected.description(),
                selected.dueDate(),
                selected.status()
        );

        if (showConfirmation("Conferma Eliminazione", message)) {
            try {
                scadenzaService.deleteScadenza(selected.id());
                refreshData();

                // Notifica altri controller dell'aggiornamento
                DataRefreshManager.getInstance().notifyDataChanged("ScadenzeController");

                showAlert("Successo", "Scadenza eliminata con successo!");
            } catch (Exception e) {
                System.err.println("❌ Errore eliminazione scadenza: " + e.getMessage());
                showError("Errore", "Impossibile eliminare la scadenza: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onUpdateStatus() {
        try {
            System.out.println("🔄 Aggiornamento status scadenze...");
            scadenzaService.updateOverdueStatus();
            refreshData();
            notifyDataChanged();
            showAlert("Aggiornato", "Status delle scadenze aggiornato! Le scadenze passate sono ora marcate come scadute.");
        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento status: " + e.getMessage());
            showError("Errore", "Impossibile aggiornare lo status: " + e.getMessage());
        }
    }

    /**
     * Aggiorna i dati della tabella.
     */
    public void refreshData() {
        try {
            List<Scadenza> scadenze = scadenzaService.getAllScadenze();
            scadenzeTable.setItems(FXCollections.observableArrayList(scadenze));
            System.out.println("📅 Caricate " + scadenze.size() + " scadenze");
        } catch (Exception e) {
            System.err.println("❌ Errore refresh scadenze: " + e.getMessage());
            scadenzeTable.setItems(FXCollections.observableArrayList());
        }
    }

    /**
     * Notifica altri controller dei cambiamenti.
     */
    private void notifyDataChanged() {
        if (onDataChangedCallback != null) {
            onDataChangedCallback.run();
        }
    }

}