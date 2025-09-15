package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Budget;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.BudgetStatus;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.BudgetService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Dialog per gestire (modificare/eliminare) i budget esistenti.
 */
public class BudgetManagementDialog extends Dialog<Void> {

    private final BudgetService budgetService;
    private TableView<Budget> budgetTable;
    private Button editButton;
    private Button deleteButton;

    public BudgetManagementDialog(BudgetService budgetService) {
        this.budgetService = budgetService;

        initializeDialog();
        setupTable();
        setupButtons();
        loadBudgets();
    }

    private void initializeDialog() {
        setTitle("Gestisci Budget");
        setHeaderText("Modifica o elimina i tuoi budget esistenti");
        setResizable(true);

        // Buttons
        ButtonType closeButtonType = new ButtonType("Chiudi", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(closeButtonType);
    }

    private void setupTable() {
        budgetTable = new TableView<>();
        budgetTable.setPrefHeight(300);
        budgetTable.setPrefWidth(600);

        // Colonne
        TableColumn<Budget, String> nameCol = new TableColumn<>("Nome");
        nameCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() -> cell.getValue().name()));
        nameCol.setPrefWidth(200);

        TableColumn<Budget, String> periodCol = new TableColumn<>("Periodo");
        periodCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() -> cell.getValue().period().toString()));
        periodCol.setPrefWidth(100);

        TableColumn<Budget, Double> amountCol = new TableColumn<>("Importo");
        amountCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().amount()));
        amountCol.setPrefWidth(120);

        // Formatting per importi
        amountCol.setCellFactory(column -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("€ %.2f", amount));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Budget, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() -> cell.getValue().status().name()));
        statusCol.setPrefWidth(100);

        budgetTable.getColumns().addAll(nameCol, periodCol, amountCol, statusCol);
    }

    private void setupButtons() {
        editButton = new Button("✏️ Modifica");
        editButton.setDisable(true);
        editButton.setOnAction(e -> editSelectedBudget());

        deleteButton = new Button("🗑️ Elimina");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteSelectedBudget());

        // Abilita bottoni quando viene selezionato un elemento
        budgetTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean hasSelection = newSelection != null;
                    editButton.setDisable(!hasSelection);
                    deleteButton.setDisable(!hasSelection);
                }
        );

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(editButton, deleteButton);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Budget esistenti:"),
                budgetTable,
                buttonBox
        );

        getDialogPane().setContent(content);
    }

    private void loadBudgets() {
        try {
            // Carica tutti i budget di tutti i periodi
            List<Budget> allBudgets = budgetService.getBudgetsForPeriod(YearMonth.now());

            // Aggiungi anche budget dei mesi precedenti
            for (int i = 1; i <= 12; i++) {
                allBudgets.addAll(budgetService.getBudgetsForPeriod(YearMonth.now().minusMonths(i)));
            }

            // Rimuovi duplicati (se esistono)
            allBudgets = allBudgets.stream().distinct().toList();

            budgetTable.setItems(FXCollections.observableArrayList(allBudgets));

        } catch (Exception e) {
            System.err.println("❌ Errore caricamento budget: " + e.getMessage());
            showError("Errore", "Impossibile caricare i budget: " + e.getMessage());
        }
    }

    private void editSelectedBudget() {
        Budget selected = budgetTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Crea dialog di modifica
        Dialog<Budget> editDialog = new Dialog<>();
        editDialog.setTitle("Modifica Budget");
        editDialog.setHeaderText("Modifica " + selected.name());

        ButtonType saveButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        editDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Form di modifica
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.name());
        TextField amountField = new TextField(String.valueOf(selected.amount()));
        ComboBox<String> currencyCombo = new ComboBox<>();
        currencyCombo.getItems().addAll("EUR", "USD", "GBP", "CHF");
        currencyCombo.setValue(selected.currency());

        ComboBox<BudgetStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(BudgetStatus.values());
        statusCombo.setValue(selected.status());

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Importo:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Valuta:"), 0, 2);
        grid.add(currencyCombo, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);

        editDialog.getDialogPane().setContent(grid);

        // Validation
        Button saveButton = (Button) editDialog.getDialogPane().lookupButton(saveButtonType);

        Runnable updateSaveButton = () -> {
            boolean nameValid = !nameField.getText().trim().isEmpty();
            boolean amountValid = isValidAmount(amountField.getText());
            saveButton.setDisable(!nameValid || !amountValid);
        };

        nameField.textProperty().addListener((observable, oldValue, newValue) -> updateSaveButton.run());
        amountField.textProperty().addListener((observable, oldValue, newValue) -> updateSaveButton.run());

        // Result converter
        editDialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new Budget(
                            selected.id(), // Mantieni stesso ID
                            nameField.getText().trim(),
                            selected.period(), // Mantieni stesso periodo
                            Double.parseDouble(amountField.getText()),
                            currencyCombo.getValue(),
                            statusCombo.getValue()
                    );
                } catch (Exception e) {
                    showError("Errore", "Dati non validi: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Mostra dialog e salva risultato
        Optional<Budget> result = editDialog.showAndWait();
        result.ifPresent(updatedBudget -> {
            try {
                budgetService.createOrUpdateBudget(updatedBudget);
                loadBudgets(); // Ricarica la tabella
                showAlert("Successo", "Budget modificato con successo!");
            } catch (Exception e) {
                showError("Errore", "Impossibile salvare le modifiche: " + e.getMessage());
            }
        });
    }

    private void deleteSelectedBudget() {
        Budget selected = budgetTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Conferma eliminazione
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Conferma Eliminazione");
        confirmation.setHeaderText("Eliminazione Budget");
        confirmation.setContentText(String.format(
                "Sei sicuro di voler eliminare il budget '%s'?\n\n" +
                        "Periodo: %s\n" +
                        "Importo: €%.2f\n\n" +
                        "Questa azione non può essere annullata.",
                selected.name(),
                selected.period(),
                selected.amount()
        ));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    budgetService.deleteBudget(selected.id());
                    loadBudgets(); // Ricarica la tabella
                    showAlert("Successo", "Budget eliminato con successo!");
                } catch (Exception e) {
                    showError("Errore", "Impossibile eliminare il budget: " + e.getMessage());
                }
            }
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Si è verificato un errore");
        alert.setContentText(message);
        alert.showAndWait();
    }
}