// === LoanDialog.java ===
package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dialog per la creazione di un nuovo prestito.
 */
public class LoanDialog extends Dialog<Loan> {

    public LoanDialog() {
        setTitle("Nuovo Piano di Ammortamento");
        setHeaderText("Inserisci i dettagli del prestito");
        setResizable(true);

        ButtonType saveButtonType = new ButtonType("Calcola Piano", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Nome prestito
        TextField nameField = new TextField();
        nameField.setPromptText("Es: Mutuo casa, Prestito auto...");

        // Importo totale
        TextField amountField = new TextField();
        amountField.setPromptText("Es: 100000.00");

        // Tasso di interesse annuo
        TextField interestField = new TextField();
        interestField.setPromptText("Es: 3.5 (per 3.5%)");

        // Numero di rate
        TextField paymentsField = new TextField();
        paymentsField.setPromptText("Es: 240 (20 anni x 12 mesi)");

        // Data inizio
        DatePicker startDatePicker = new DatePicker(LocalDate.now());

        // Tipo prestito
        ComboBox<LoanType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(LoanType.values());
        typeCombo.setValue(LoanType.PERSONAL_LOAN);

        // Formato tipo prestito
        typeCombo.setCellFactory(lv -> new ListCell<LoanType>() {
            @Override
            protected void updateItem(LoanType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getEmoji() + " " + item.name().replace("_", " "));
                }
            }
        });
        typeCombo.setButtonCell(new ListCell<LoanType>() {
            @Override
            protected void updateItem(LoanType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getEmoji() + " " + item.name().replace("_", " "));
                }
            }
        });

        // Categoria
        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(Category.values());
        categoryCombo.setValue(Category.OTHER);

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

        // Descrizione
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Descrizione aggiuntiva (opzionale)");
        descriptionArea.setPrefRowCount(3);

        // Label di anteprima
        Label previewLabel = new Label();
        previewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Aggiorna anteprima in tempo reale
        Runnable updatePreview = () -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                double rate = Double.parseDouble(interestField.getText());
                int payments = Integer.parseInt(paymentsField.getText());

                // Calcola rata mensile
                double monthlyRate = rate / 100 / 12;
                double monthlyPayment;
                if (rate == 0) {
                    monthlyPayment = amount / payments;
                } else {
                    double factor = Math.pow(1 + monthlyRate, payments);
                    monthlyPayment = amount * (monthlyRate * factor) / (factor - 1);
                }

                double totalInterest = (monthlyPayment * payments) - amount;
                double totalAmount = amount + totalInterest;

                previewLabel.setText(String.format(
                        "Anteprima: Rata mensile €%.2f | Interessi totali €%.2f | Totale da pagare €%.2f",
                        monthlyPayment, totalInterest, totalAmount));

            } catch (NumberFormatException e) {
                previewLabel.setText("Inserisci valori numerici validi per vedere l'anteprima");
            }
        };

        amountField.textProperty().addListener((obs, old, newVal) -> updatePreview.run());
        interestField.textProperty().addListener((obs, old, newVal) -> updatePreview.run());
        paymentsField.textProperty().addListener((obs, old, newVal) -> updatePreview.run());

        // Layout
        grid.add(new Label("Nome Prestito:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Importo Totale (€):"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Tasso Interesse (%):"), 0, 2);
        grid.add(interestField, 1, 2);
        grid.add(new Label("Numero Rate:"), 0, 3);
        grid.add(paymentsField, 1, 3);
        grid.add(new Label("Data Inizio:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("Tipo Prestito:"), 0, 5);
        grid.add(typeCombo, 1, 5);
        grid.add(new Label("Categoria:"), 0, 6);
        grid.add(categoryCombo, 1, 6);
        grid.add(new Label("Descrizione:"), 0, 7);
        grid.add(descriptionArea, 1, 7);
        grid.add(previewLabel, 0, 8, 2, 1);

        getDialogPane().setContent(grid);

        // Validation
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        Runnable updateSaveButton = () -> {
            boolean nameValid = !nameField.getText().trim().isEmpty();
            boolean amountValid = isValidAmount(amountField.getText());
            boolean rateValid = isValidRate(interestField.getText());
            boolean paymentsValid = isValidPayments(paymentsField.getText());
            saveButton.setDisable(!nameValid || !amountValid || !rateValid || !paymentsValid);
        };

        nameField.textProperty().addListener((obs, old, newVal) -> updateSaveButton.run());
        amountField.textProperty().addListener((obs, old, newVal) -> updateSaveButton.run());
        interestField.textProperty().addListener((obs, old, newVal) -> updateSaveButton.run());
        paymentsField.textProperty().addListener((obs, old, newVal) -> updateSaveButton.run());

        nameField.requestFocus();

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new Loan(
                            java.util.UUID.randomUUID().toString(),
                            nameField.getText().trim(),
                            Double.parseDouble(amountField.getText()),
                            Double.parseDouble(interestField.getText()),
                            Integer.parseInt(paymentsField.getText()),
                            startDatePicker.getValue(),
                            typeCombo.getValue(),
                            categoryCombo.getValue(),
                            LoanStatus.ACTIVE,
                            descriptionArea.getText().trim()
                    );
                } catch (Exception e) {
                    System.err.println("❌ Errore creazione prestito: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
    }

    private boolean isValidAmount(String text) {
        try {
            double value = Double.parseDouble(text);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidRate(String text) {
        try {
            double value = Double.parseDouble(text);
            return value >= 0 && value <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidPayments(String text) {
        try {
            int value = Integer.parseInt(text);
            return value > 0 && value <= 600; // Max 50 anni
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

