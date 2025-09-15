package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.controllers;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Movement;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.MovementType;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.MovementService;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.LoanService;
import it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs.MovementDialog;
import it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs.LoanDialog;
import it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs.LoanSummaryDialog;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Controller per la gestione dei movimenti finanziari prestiti.
 */
public class MovementController extends BaseController {

    @FXML private TableView<Movement> movementsTable;
    @FXML private TableColumn<Movement, String> movNameCol;
    @FXML private TableColumn<Movement, LocalDate> movDateCol;
    @FXML private TableColumn<Movement, Double> movAmountCol;
    @FXML private TableColumn<Movement, String> movTypeCol;
    @FXML private TableColumn<Movement, String> movCategoryCol;
    @FXML private Button addMovementBtn;
    @FXML private Button deleteMovementBtn;

    @FXML private Button createLoanBtn;
    @FXML private Button viewLoansBtn;

    // Labels per statistiche
    @FXML private Label totalMovementsLabel;
    @FXML private Label netBalanceLabel;
    @FXML private Label activeLoansLabel;
    @FXML private Label overduePaymentsLabel;
    @FXML private Label tableInfoLabel;

    private final MovementService movementService;
    private final LoanService loanService;

    public MovementController(MovementService movementService, LoanService loanService) {
        this.movementService = movementService;
        this.loanService = loanService;
    }

    @Override
    public void initialize() {
        setupTable();
        setupEventHandlers();
        setupLoanButtons();
        refreshData();

        // Registra per ricevere notifiche di aggiornamento da altri controller
        DataRefreshManager.getInstance().addRefreshListener(this::refreshData);
    }

    private void setupTable() {
        movNameCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() -> cell.getValue().name()));

        movDateCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().date()));

        movAmountCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().amount()));

        movTypeCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cell.getValue().type().name()));

        movCategoryCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cell.getValue().category().getEmoji() + " " + cell.getValue().category().name()));

        // Formatting per gli importi
        movAmountCol.setCellFactory(column -> new TableCell<Movement, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("€ %.2f", Math.abs(amount)));
                    if (amount >= 0) {
                        setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Formatting per il tipo con emoji
        movTypeCol.setCellFactory(column -> new TableCell<Movement, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    switch (type) {
                        case "INCOME" -> {
                            setText("📈 Entrata");
                            setStyle("-fx-text-fill: #10B981;");
                        }
                        case "EXPENSE" -> {
                            setText("📉 Uscita");
                            setStyle("-fx-text-fill: #EF4444;");
                        }
                        case "LOAN" -> {
                            setText("🏦 Prestito");
                            setStyle("-fx-text-fill: #3498db;");
                        }
                        default -> setText(type);
                    }
                }
            }
        });
    }

    private void setupEventHandlers() {
        // Gestione selezione per abilitare/disabilitare bottoni
        movementsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    deleteMovementBtn.setDisable(newSelection == null);
                }
        );

        // Inizializza stati bottoni
        deleteMovementBtn.setDisable(true);
    }

    private void setupLoanButtons() {
        // Abilita/disabilita bottoni prestiti in base alla disponibilità del servizio
        if (createLoanBtn != null) {
            createLoanBtn.setDisable(loanService == null);
        }
        if (viewLoansBtn != null) {
            viewLoansBtn.setDisable(loanService == null);
        }
    }

    @FXML
    private void onAddMovement() {
        System.out.println("🔄 Apertura dialog nuovo movimento...");
        try {
            MovementDialog dialog = new MovementDialog(loanService);
            dialog.showAndWait().ifPresent(movement -> {
                System.out.println("💾 Tentativo salvataggio movimento:");
                System.out.println("   ID: " + movement.id());
                System.out.println("   Nome: " + movement.name());
                System.out.println("   Data: " + movement.date());
                System.out.println("   Importo: " + movement.amount());
                System.out.println("   Tipo: " + movement.type());
                System.out.println("   Categoria: " + movement.category());

                try {
                    Movement savedMovement = movementService.addMovement(movement);
                    refreshData();

                    // Notifica altri controller dell'aggiornamento
                    DataRefreshManager.getInstance().notifyDataChanged("MovementController");

                    showAlert("Successo", "Movimento aggiunto con successo!");
                } catch (Exception e) {
                    System.err.println("❌ Errore salvataggio movimento: " + e.getMessage());
                    e.printStackTrace();
                    showError("Errore", "Impossibile salvare il movimento: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Errore aggiunta movimento: " + e.getMessage());
            e.printStackTrace();
            showError("Errore", "Impossibile aggiungere il movimento: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteMovement() {
        Movement selected = movementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Nessuna Selezione", "Seleziona un movimento da eliminare.");
            return;
        }

        String message = String.format(
                "Sei sicuro di voler eliminare questo movimento?\n\n" +
                        "Nome: %s\n" +
                        "Data: %s\n" +
                        "Importo: €%.2f\n" +
                        "Categoria: %s",
                selected.name(),
                selected.date(),
                Math.abs(selected.amount()),
                selected.category().name()
        );

        if (showConfirmation("Conferma Eliminazione", message)) {
            try {
                movementService.removeMovement(selected.id());
                refreshData();

                // Notifica altri controller dell'aggiornamento
                DataRefreshManager.getInstance().notifyDataChanged("MovementController");

                showAlert("Successo", "Movimento eliminato con successo!");
            } catch (Exception e) {
                System.err.println("❌ Errore eliminazione movimento: " + e.getMessage());
                showError("Errore", "Impossibile eliminare il movimento: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onCreateLoan() {
        if (loanService == null) {
            showError("Servizio Non Disponibile", "Il servizio prestiti non è disponibile.");
            return;
        }

        System.out.println("🏦 Apertura dialog nuovo prestito...");
        try {
            LoanDialog dialog = new LoanDialog();
            dialog.showAndWait().ifPresent(loan -> {
                try {
                    // Crea il prestito con piano di ammortamento
                    var savedLoan = loanService.createLoan(loan);

                    // Crea automaticamente un movimento di entrata per l'importo ricevuto
                    Movement loanMovement = new Movement(
                            UUID.randomUUID().toString(),
                            "Prestito ricevuto - " + loan.name(),
                            loan.startDate(),
                            loan.totalAmount(), // Positivo perché è denaro ricevuto
                            MovementType.INCOME,
                            loan.category()
                    );

                    movementService.addMovement(loanMovement);
                    refreshData();

                    // Notifica altri controller dell'aggiornamento
                    DataRefreshManager.getInstance().notifyDataChanged("MovementController");

                    showAlert("Prestito Creato",
                            String.format("Prestito '%s' creato con successo!\n\n" +
                                            "✅ Piano di ammortamento generato (%d rate)\n" +
                                            "✅ Scadenze create automaticamente\n" +
                                            "✅ Movimento di entrata registrato (€%.2f)",
                                    loan.name(), loan.totalPayments(), loan.totalAmount()));

                    // Chiedi se vuole vedere il piano di ammortamento
                    if (showConfirmation("Visualizza Piano",
                            "Vuoi visualizzare il piano di ammortamento appena creato?")) {
                        showLoanSummary(savedLoan);
                    }

                } catch (Exception e) {
                    System.err.println("❌ Errore creazione prestito: " + e.getMessage());
                    e.printStackTrace();
                    showError("Errore", "Impossibile creare il prestito: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Errore apertura dialog prestito: " + e.getMessage());
            showError("Errore", "Impossibile aprire il dialog del prestito: " + e.getMessage());
        }
    }

    @FXML
    private void onViewLoans() {
        if (loanService == null) {
            showError("Servizio Non Disponibile", "Il servizio prestiti non è disponibile.");
            return;
        }

        try {
            var activeLoans = loanService.getActiveLoans();

            if (activeLoans.isEmpty()) {
                showAlert("Nessun Prestito", "Non ci sono prestiti attivi da visualizzare.\n\nPuoi crearne uno nuovo usando il pulsante 'Nuovo Prestito'.");
                return;
            }

            // Se c'è un solo prestito, mostra direttamente il piano
            if (activeLoans.size() == 1) {
                showLoanSummary(activeLoans.get(0));
            } else {
                // Mostra dialog di selezione prestito
                showLoanSelectionDialog(activeLoans);
            }

        } catch (Exception e) {
            System.err.println("❌ Errore visualizzazione prestiti: " + e.getMessage());
            showError("Errore", "Impossibile visualizzare i prestiti: " + e.getMessage());
        }
    }

    /**
     * Mostra il piano di ammortamento di un prestito specifico.
     */
    private void showLoanSummary(it.unicam.cs.mpgc.jbudget123718.jbudget.model.Loan loan) {
        try {
            var payments = loanService.getLoanPayments(loan.id());
            LoanSummaryDialog summaryDialog = new LoanSummaryDialog(loan, payments);
            summaryDialog.showAndWait();
        } catch (Exception e) {
            System.err.println("❌ Errore visualizzazione piano ammortamento: " + e.getMessage());
            showError("Errore", "Impossibile visualizzare il piano di ammortamento: " + e.getMessage());
        }
    }

    /**
     * Mostra un dialog per selezionare quale prestito visualizzare.
     */
    private void showLoanSelectionDialog(List<it.unicam.cs.mpgc.jbudget123718.jbudget.model.Loan> loans) {
        // Crea una lista di stringhe formattate per la selezione
        String[] loanOptions = loans.stream()
                .map(loan -> String.format("%s %s - €%.2f (%d rate)",
                        loan.type().getEmoji(), loan.name(),
                        loan.totalAmount(), loan.totalPayments()))
                .toArray(String[]::new);

        ChoiceDialog<String> dialog = new ChoiceDialog<>(loanOptions[0], loanOptions);
        dialog.setTitle("Seleziona Prestito");
        dialog.setHeaderText("Seleziona il prestito da visualizzare");
        dialog.setContentText("Prestito:");

        dialog.showAndWait().ifPresent(selectedString -> {
            // Trova l'indice della stringa selezionata
            for (int i = 0; i < loanOptions.length; i++) {
                if (loanOptions[i].equals(selectedString)) {
                    showLoanSummary(loans.get(i));
                    break;
                }
            }
        });
    }

    /**
     * Aggiorna i dati della tabella e le statistiche.
     */
    public void refreshData() {
        try {
            // Carica movimenti ultimi 6 mesi
            List<Movement> movements = movementService.getMovementsInPeriod(
                    LocalDate.now().minusMonths(6),
                    LocalDate.now()
            );
            movementsTable.setItems(FXCollections.observableArrayList(movements));

            // Aggiorna statistiche
            updateStatistics(movements);

            System.out.println("📄 Caricati " + movements.size() + " movimenti");
        } catch (Exception e) {
            System.err.println("❌ Errore refresh movimenti: " + e.getMessage());
            movementsTable.setItems(FXCollections.observableArrayList());
            resetStatistics();
        }
    }

    /**
     * Aggiorna le statistiche nei label.
     */
    private void updateStatistics(List<Movement> movements) {
        try {
            // Calcola statistiche movimenti
            int totalMovements = movements.size();

            double totalIncome = movements.stream()
                    .filter(m -> m.type() == MovementType.INCOME)
                    .mapToDouble(Movement::amount)
                    .sum();

            double totalExpenses = movements.stream()
                    .filter(m -> m.type() == MovementType.EXPENSE)
                    .mapToDouble(m -> Math.abs(m.amount()))
                    .sum();

            double netBalance = totalIncome - totalExpenses;

            // Aggiorna label movimenti
            if (totalMovementsLabel != null) {
                totalMovementsLabel.setText(String.valueOf(totalMovements));
            }

            if (netBalanceLabel != null) {
                netBalanceLabel.setText(String.format("€ %.2f", netBalance));

                // Applica colore basato sul valore
                if (netBalance >= 0) {
                    netBalanceLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                } else {
                    netBalanceLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                }
            }

            if (tableInfoLabel != null) {
                tableInfoLabel.setText("Mostra " + totalMovements + " movimenti degli ultimi 6 mesi");
            }

            // Aggiorna statistiche prestiti se disponibili
            updateLoanStatistics();

        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento statistiche: " + e.getMessage());
        }
    }

    /**
     * Aggiorna le statistiche sui prestiti.
     */
    private void updateLoanStatistics() {
        if (loanService == null) {
            if (activeLoansLabel != null) {
                activeLoansLabel.setText("N/A");
            }
            if (overduePaymentsLabel != null) {
                overduePaymentsLabel.setText("N/A");
            }
            return;
        }

        try {
            var activeLoans = loanService.getActiveLoans();
            var overduePayments = loanService.getOverduePayments();
            double monthlyTotal = loanService.getMonthlyPaymentTotal();

            if (activeLoansLabel != null) {
                activeLoansLabel.setText(String.valueOf(activeLoans.size()));
            }

            if (overduePaymentsLabel != null) {
                overduePaymentsLabel.setText(String.valueOf(overduePayments.size()));

                // Applica stile warning se ci sono rate scadute
                if (overduePayments.size() > 0) {
                    overduePaymentsLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                } else {
                    overduePaymentsLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento statistiche prestiti: " + e.getMessage());
        }
    }

    /**
     * Resetta le statistiche ai valori predefiniti.
     */
    private void resetStatistics() {
        if (totalMovementsLabel != null) {
            totalMovementsLabel.setText("0");
        }
        if (netBalanceLabel != null) {
            netBalanceLabel.setText("€ 0,00");
            netBalanceLabel.setStyle("-fx-text-fill: #6B7280;");
        }
        if (activeLoansLabel != null) {
            activeLoansLabel.setText("0");
        }
        if (overduePaymentsLabel != null) {
            overduePaymentsLabel.setText("0");
        }
        if (tableInfoLabel != null) {
            tableInfoLabel.setText("Nessun movimento caricato");
        }
    }
}