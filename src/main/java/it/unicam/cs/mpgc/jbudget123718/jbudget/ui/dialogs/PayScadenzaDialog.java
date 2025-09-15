package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Category;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Scadenza;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

public class PayScadenzaDialog extends Dialog<PaymentInfo> {

    public PayScadenzaDialog(Scadenza scadenza) {
        setTitle("Paga Scadenza");
        setHeaderText("Registra il pagamento per: " + scadenza.description());

        ButtonType payButtonType = new ButtonType("Paga", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("Importo pagato");

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(Category.values());
        categoryCombo.setValue(Category.UTILITIES);

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

        DatePicker paymentDatePicker = new DatePicker(LocalDate.now());

        grid.add(new Label("Importo:"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Categoria:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(new Label("Data Pagamento:"), 0, 2);
        grid.add(paymentDatePicker, 1, 2);

        getDialogPane().setContent(grid);

        // Validation
        Button payButton = (Button) getDialogPane().lookupButton(payButtonType);
        payButton.setDisable(true);

        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            payButton.setDisable(newValue.trim().isEmpty() || !isValidAmount(newValue));
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == payButtonType) {
                try {
                    return new PaymentInfo(
                            Double.parseDouble(amountField.getText()),
                            categoryCombo.getValue(),
                            paymentDatePicker.getValue()
                    );
                } catch (NumberFormatException e) {
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
}