package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Budget;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.BudgetStatus;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.YearMonth;

/**
 * Dialog per la creazione e modifica di budget.
 * Permette di configurare budget mensili
 */
public class BudgetDialog extends Dialog<Budget> {

    public BudgetDialog() {
        setTitle("Nuovo Budget");
        setHeaderText("Configura un budget mensile");
        setResizable(true);

        // Setup buttons
        ButtonType saveButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Campo Nome
        TextField nameField = new TextField();
        nameField.setPromptText("Es: Budget Spese Casa, Budget Svago, etc.");

        // Periodo (combo con prossimi 6 mesi)
        ComboBox<String> periodCombo = new ComboBox<>();
        for (int i = 0; i < 6; i++) {
            periodCombo.getItems().add(YearMonth.now().plusMonths(i).toString());
        }
        periodCombo.setValue(YearMonth.now().toString());
        periodCombo.setPromptText("Seleziona periodo");

        // Importo
        TextField amountField = new TextField();
        amountField.setPromptText("Es: 500.00");

        // Valuta
        ComboBox<String> currencyCombo = new ComboBox<>();
        currencyCombo.getItems().addAll("EUR", "USD", "GBP", "CHF");
        currencyCombo.setValue("EUR");

        // Add controls to grid
        grid.add(new Label("Nome Budget:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Periodo:"), 0, 1);
        grid.add(periodCombo, 1, 1);
        grid.add(new Label("Importo:"), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Valuta:"), 0, 3);
        grid.add(currencyCombo, 1, 3);

        // Add info label
        Label infoLabel = new Label("Il budget sarà attivo automaticamente per il periodo selezionato.");
        infoLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        grid.add(infoLabel, 0, 4, 2, 1);

        getDialogPane().setContent(grid);

        // Validation
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Abilita il bottone quando nome e importo sono validi
        Runnable updateSaveButton = () -> {
            boolean nameValid = !nameField.getText().trim().isEmpty();
            boolean amountValid = isValidAmount(amountField.getText());
            saveButton.setDisable(!nameValid || !amountValid);
        };

        nameField.textProperty().addListener((observable, oldValue, newValue) -> updateSaveButton.run());
        amountField.textProperty().addListener((observable, oldValue, newValue) -> updateSaveButton.run());

        // Set focus on name field
        nameField.requestFocus();

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new Budget(
                            java.util.UUID.randomUUID().toString(),
                            nameField.getText().trim(),
                            YearMonth.parse(periodCombo.getValue()),
                            Double.parseDouble(amountField.getText()),
                            currencyCombo.getValue(),
                            BudgetStatus.ACTIVE
                    );
                } catch (Exception e) {
                    System.err.println("❌ Errore creazione budget: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
    }

    /**
     * Valida che l'importo inserito sia un numero positivo.
     */
    private boolean isValidAmount(String text) {
        try {
            double value = Double.parseDouble(text);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}