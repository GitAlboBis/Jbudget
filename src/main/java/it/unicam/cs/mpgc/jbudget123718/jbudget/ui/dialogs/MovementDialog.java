package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Category;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Movement;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.MovementType;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Loan;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.LoanService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * Dialog per la creazione di movimenti con supporto per pagamento rate prestiti.
 */
public class MovementDialog extends Dialog<Movement> {

    private final LoanService loanService;

    public MovementDialog() {
        this(null);
    }

    public MovementDialog(LoanService loanService) {
        this.loanService = loanService;

        setTitle("Nuovo Movimento");
        setHeaderText("Inserisci i dettagli del movimento finanziario");
        setResizable(true);

        // Imposta dimensioni più grandi per evitare problemi di visualizzazione
        getDialogPane().setPrefWidth(650);
        getDialogPane().setPrefHeight(550);

        // Setup buttons
        ButtonType saveButtonType = new ButtonType("💾 Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 10));

        int currentRow = 0;

        //TIPO MOVIMENTO
        Label typeLabel = new Label("Tipo Movimento:");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("📈 Entrata", "📉 Uscita", "🏦 Paga Rata Prestito");
        typeCombo.setValue("📉 Uscita");
        typeCombo.setPrefWidth(250);

        grid.add(typeLabel, 0, currentRow);
        grid.add(typeCombo, 1, currentRow);
        currentRow++;

        //SEZIONE PRESTITI (INIZIALMENTE NASCOSTA)
        VBox loanSection = new VBox(10);
        loanSection.setVisible(false);
        loanSection.setManaged(false);

        Label loanSectionTitle = new Label("🏦 Pagamento Rate");
        loanSectionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        // ComboBox per selezione prestito esistente
        ComboBox<Loan> existingLoanCombo = new ComboBox<>();
        existingLoanCombo.setPrefWidth(400);

        // ComboBox per selezione rata specifica
        ComboBox<String> paymentCombo = new ComboBox<>();
        paymentCombo.setPrefWidth(400);

        // Formato per visualizzazione prestiti
        existingLoanCombo.setCellFactory(lv -> new ListCell<Loan>() {
            @Override
            protected void updateItem(Loan item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s %s - €%.2f (%d rate)",
                            item.type().getEmoji(), item.name(),
                            item.totalAmount(), item.totalPayments()));
                }
            }
        });
        existingLoanCombo.setButtonCell(new ListCell<Loan>() {
            @Override
            protected void updateItem(Loan item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s %s", item.type().getEmoji(), item.name()));
                }
            }
        });

        loanSection.getChildren().addAll(
                loanSectionTitle,
                new Label("Prestito:"),
                existingLoanCombo,
                new Label("Rata da pagare:"),
                paymentCombo
        );

        grid.add(loanSection, 0, currentRow, 2, 1);
        currentRow++;

        //  CAMPI MOVIMENTO STANDARD
        TextField nameField = new TextField();
        nameField.setPromptText("Es: Spesa supermercato, Stipendio, Rata prestito...");
        nameField.setPrefWidth(350);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(200);

        TextField amountField = new TextField();
        amountField.setPromptText("Es: 50.00");
        amountField.setPrefWidth(200);

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(Category.values());
        categoryCombo.setValue(Category.OTHER);
        categoryCombo.setPrefWidth(200);

        // Format category display
        categoryCombo.setCellFactory(lv -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getEmoji() + " " + item.name());
                }
            }
        });
        categoryCombo.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getEmoji() + " " + item.name());
                }
            }
        });

        // Aggiungi campi al grid
        grid.add(new Label("Nome:"), 0, currentRow);
        grid.add(nameField, 1, currentRow);
        currentRow++;

        grid.add(new Label("Data:"), 0, currentRow);
        grid.add(datePicker, 1, currentRow);
        currentRow++;

        grid.add(new Label("Importo:"), 0, currentRow);
        grid.add(amountField, 1, currentRow);
        currentRow++;

        grid.add(new Label("Categoria:"), 0, currentRow);
        grid.add(categoryCombo, 1, currentRow);
        currentRow++;

        //  PANNELLO INFORMATIVO
        Label infoPanel = new Label();
        infoPanel.setWrapText(true);
        infoPanel.setPrefWidth(500);
        infoPanel.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 10; -fx-border-radius: 5; " +
                "-fx-background-radius: 5; -fx-font-size: 12px;");

        grid.add(infoPanel, 0, currentRow, 2, 1);

        getDialogPane().setContent(grid);

        //  EVENT HANDLERS

        // Gestione cambio tipo movimento
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("🏦 Paga Rata Prestito".equals(newVal)) {
                showLoanSection(loanSection, infoPanel, amountField);
                loadExistingLoans(existingLoanCombo);
            } else {
                hideLoanSection(loanSection, infoPanel, newVal, amountField);
            }
        });

        // Gestione selezione prestito esistente
        existingLoanCombo.valueProperty().addListener((obs, oldLoan, newLoan) -> {
            if (newLoan != null && loanService != null) {
                loadLoanPayments(newLoan, paymentCombo, nameField, amountField, categoryCombo);
            }
        });

        // Gestione selezione rata
        paymentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && existingLoanCombo.getValue() != null) {
                updatePaymentDetails(existingLoanCombo.getValue(), newVal, nameField, amountField);
            }
        });

        //  VALIDATION
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        Runnable updateSaveButton = () -> {
            boolean nameValid = !nameField.getText().trim().isEmpty();
            boolean amountValid = !amountField.isDisabled() ? isValidAmount(amountField.getText()) : true;
            boolean typeValid = typeCombo.getValue() != null;

            boolean loanValid = true;
            if ("🏦 Paga Rata Prestito".equals(typeCombo.getValue())) {
                loanValid = existingLoanCombo.getValue() != null && paymentCombo.getValue() != null;
            }

            saveButton.setDisable(!nameValid || !amountValid || !typeValid || !loanValid);
        };

        nameField.textProperty().addListener((obs, old, newVal) -> updateSaveButton.run());
        amountField.textProperty().addListener((obs, old, newVal) -> updateSaveButton.run());
        typeCombo.valueProperty().addListener((obs, old, newVal) -> updateSaveButton.run());
        existingLoanCombo.valueProperty().addListener((obs, old, newVal) -> updateSaveButton.run());
        paymentCombo.valueProperty().addListener((obs, old, newVal) -> updateSaveButton.run());

        // Focus iniziale
        nameField.requestFocus();

        //  RESULT CONVERTER
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String selectedType = typeCombo.getValue();
                    MovementType movementType;
                    double amount;

                    if ("🏦 Paga Rata Prestito".equals(selectedType)) {
                        // Pagamento rata = movimento di uscita
                        movementType = MovementType.EXPENSE;
                        amount = -Math.abs(parseAmount(amountField.getText()));
                    } else {
                        // Movimento standard
                        movementType = "📈 Entrata".equals(selectedType) ? MovementType.INCOME : MovementType.EXPENSE;
                        amount = parseAmount(amountField.getText());
                        if (movementType == MovementType.EXPENSE && amount > 0) {
                            amount = -amount;
                        }
                    }

                    return new Movement(
                            java.util.UUID.randomUUID().toString(),
                            nameField.getText().trim(),
                            datePicker.getValue(),
                            amount,
                            movementType,
                            categoryCombo.getValue()
                    );
                } catch (NumberFormatException e) {
                    System.err.println("❌ Errore parsing numero: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Imposta stato iniziale
        hideLoanSection(loanSection, infoPanel, "📉 Uscita", amountField);
    }

    private void showLoanSection(VBox loanSection, Label infoPanel, TextField amountField) {
        loanSection.setVisible(true);
        loanSection.setManaged(true);

        // Disabilita campi importo (sarà calcolato automaticamente)
        amountField.setDisable(true);
        amountField.setPromptText("Importo calcolato automaticamente dalla rata");

        infoPanel.setText("💡 Seleziona il prestito e la rata da pagare. L'importo sarà calcolato automaticamente " +
                "e verranno creati due movimenti separati per capitale e interessi.");
    }

    private void hideLoanSection(VBox loanSection, Label infoPanel, String selectedType, TextField amountField) {
        loanSection.setVisible(false);
        loanSection.setManaged(false);

        amountField.setDisable(false);
        amountField.setPromptText("Es: 50.00");

        if ("📈 Entrata".equals(selectedType)) {
            infoPanel.setText("💡 Registra denaro ricevuto: stipendio, vendite, rimborsi, interessi attivi, etc.");
        } else {
            infoPanel.setText("💡 Registra denaro speso: acquisti, bollette, affitti, spese varie, etc.");
        }
    }

    private void loadExistingLoans(ComboBox<Loan> comboBox) {
        if (loanService != null) {
            try {
                List<Loan> activeLoans = loanService.getActiveLoans();
                comboBox.getItems().clear();
                comboBox.getItems().addAll(activeLoans);

                if (activeLoans.isEmpty()) {
                    comboBox.setPromptText("Nessun prestito attivo trovato");
                } else {
                    comboBox.setPromptText("Seleziona prestito...");
                }
            } catch (Exception e) {
                System.err.println("❌ Errore caricamento prestiti: " + e.getMessage());
                comboBox.setPromptText("Errore caricamento prestiti");
            }
        } else {
            comboBox.setPromptText("Servizio prestiti non disponibile");
        }
    }

    private void loadLoanPayments(Loan loan, ComboBox<String> paymentCombo,
                                  TextField nameField, TextField amountField, ComboBox<Category> categoryCombo) {
        if (loanService != null) {
            try {
                var payments = loanService.getLoanPayments(loan.id());
                var pendingPayments = payments.stream()
                        .filter(p -> p.status() == it.unicam.cs.mpgc.jbudget123718.jbudget.model.LoanPaymentStatus.PENDING ||
                                p.status() == it.unicam.cs.mpgc.jbudget123718.jbudget.model.LoanPaymentStatus.OVERDUE)
                        .toList();

                paymentCombo.getItems().clear();

                for (var payment : pendingPayments) {
                    String displayText = String.format("Rata %d/%d - €%.2f (Scadenza: %s)",
                            payment.paymentNumber(), loan.totalPayments(),
                            payment.totalAmount(), payment.dueDate().toString());
                    paymentCombo.getItems().add(displayText);
                }

                if (pendingPayments.isEmpty()) {
                    paymentCombo.setPromptText("Nessuna rata da pagare");
                } else {
                    paymentCombo.setPromptText("Seleziona rata da pagare...");
                    // Pre-seleziona la prima rata in scadenza
                    paymentCombo.setValue(paymentCombo.getItems().get(0));
                }

                // Imposta categoria dal prestito
                categoryCombo.setValue(loan.category());

            } catch (Exception e) {
                System.err.println("❌ Errore caricamento rate: " + e.getMessage());
                paymentCombo.setPromptText("Errore caricamento rate");
            }
        }
    }

    private void updatePaymentDetails(Loan loan, String selectedPayment,
                                      TextField nameField, TextField amountField) {
        if (loanService != null && selectedPayment != null) {
            try {
                // Estrai numero rata dal testo selezionato
                String[] parts = selectedPayment.split(" ");
                if (parts.length > 1) {
                    String ratePart = parts[1].split("/")[0];
                    int paymentNumber = Integer.parseInt(ratePart);

                    var payments = loanService.getLoanPayments(loan.id());
                    var selectedPaymentObj = payments.stream()
                            .filter(p -> p.paymentNumber() == paymentNumber)
                            .findFirst();

                    if (selectedPaymentObj.isPresent()) {
                        var payment = selectedPaymentObj.get();
                        nameField.setText(String.format("Pagamento rata %d - %s",
                                payment.paymentNumber(), loan.name()));
                        amountField.setText(String.format("%.2f", payment.totalAmount()));
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Errore aggiornamento dettagli pagamento: " + e.getMessage());
            }
        }
    }

    private boolean isValidAmount(String text) {
        try {
            // Sostituisce virgola con punto per parsing corretto
            String normalizedText = text.replace(",", ".");
            double value = Double.parseDouble(normalizedText);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Converte stringa con virgola o punto in double.
     */
    private double parseAmount(String text) throws NumberFormatException {
        // Sostituisce virgola con punto per parsing corretto
        String normalizedText = text.replace(",", ".");
        return Double.parseDouble(normalizedText);
    }
}