package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Scadenza;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.ScadenzaStatus;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

public class ScadenzaDialog extends Dialog<Scadenza> {

    public ScadenzaDialog() {
        setTitle("Nuova Scadenza");
        setHeaderText("Aggiungi una scadenza da ricordare");

        ButtonType saveButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Es: Bolletta elettricità");

        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(30));

        grid.add(new Label("Descrizione:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        grid.add(new Label("Data Scadenza:"), 0, 1);
        grid.add(dueDatePicker, 1, 1);

        getDialogPane().setContent(grid);

        // Validation
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Scadenza(
                        java.util.UUID.randomUUID().toString(), // Genera ID
                        dueDatePicker.getValue(),
                        descriptionField.getText().trim(),
                        ScadenzaStatus.PENDING
                );
            }
            return null;
        });
    }
}
