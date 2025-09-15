package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Loan;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.LoanPayment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dialog per il pagamento di una rata del prestito.
 * Permette di registrare il pagamento di una rata specifica.
 */
public class LoanPaymentDialog extends Dialog<LocalDate> {

    public LoanPaymentDialog(Loan loan, LoanPayment payment) {
        setTitle("Paga Rata Prestito");
        setHeaderText("Registra il pagamento della rata " + payment.paymentNumber());
        setResizable(true);

        ButtonType payButtonType = new ButtonType("💰 Paga Rata", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        //  SEZIONE INFORMAZIONI PRESTITO
        Label loanInfoTitle = new Label("📋 Informazioni Prestito");
        loanInfoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        Label loanInfo = new Label(String.format("%s %s", loan.type().getEmoji(), loan.name()));
        loanInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #34495e;");

        //  SEZIONE DETTAGLI RATA
        Label rateInfoTitle = new Label("💳 Dettagli Rata");
        rateInfoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        GridPane rateInfo = new GridPane();
        rateInfo.setHgap(20);
        rateInfo.setVgap(12);
        rateInfo.setPadding(new Insets(15));
        rateInfo.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-border-color: #dee2e6; -fx-border-width: 1;");

        // Riga 1: Numero rata e data scadenza
        Label numRateLabel = new Label("Numero Rata:");
        numRateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label numRateValue = new Label(String.valueOf(payment.paymentNumber()));
        numRateValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #212529;");

        Label dataScadenzaLabel = new Label("Data Scadenza:");
        dataScadenzaLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label dataScadenzaValue = new Label(payment.dueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dataScadenzaValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #212529;");

        rateInfo.add(numRateLabel, 0, 0);
        rateInfo.add(numRateValue, 1, 0);
        rateInfo.add(dataScadenzaLabel, 2, 0);
        rateInfo.add(dataScadenzaValue, 3, 0);

        // Riga 2: Importo totale
        Label importoTotaleLabel = new Label("Importo Totale:");
        importoTotaleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label importoTotaleValue = new Label(String.format("€ %.2f", payment.totalAmount()));
        importoTotaleValue.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");

        rateInfo.add(importoTotaleLabel, 0, 1);
        rateInfo.add(importoTotaleValue, 1, 1, 3, 1);

        // Riga 3: Quota capitale e interessi
        Label quotaCapitaleLabel = new Label("Quota Capitale:");
        quotaCapitaleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label quotaCapitaleValue = new Label(String.format("€ %.2f", payment.principalAmount()));
        quotaCapitaleValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");

        Label quotaInteressiLabel = new Label("Quota Interessi:");
        quotaInteressiLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label quotaInteressiValue = new Label(String.format("€ %.2f", payment.interestAmount()));
        quotaInteressiValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc3545;");

        rateInfo.add(quotaCapitaleLabel, 0, 2);
        rateInfo.add(quotaCapitaleValue, 1, 2);
        rateInfo.add(quotaInteressiLabel, 2, 2);
        rateInfo.add(quotaInteressiValue, 3, 2);

        // Riga 4: Debito residuo
        Label debitoResiduoLabel = new Label("Debito Residuo:");
        debitoResiduoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        Label debitoResiduoValue = new Label(String.format("€ %.2f", payment.remainingBalance()));
        debitoResiduoValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");

        rateInfo.add(debitoResiduoLabel, 0, 3);
        rateInfo.add(debitoResiduoValue, 1, 3, 3, 1);

        //  SEZIONE DATA PAGAMENTO
        Label paymentDateTitle = new Label("📅 Data Pagamento");
        paymentDateTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        VBox paymentDateSection = new VBox(10);
        paymentDateSection.setPadding(new Insets(15));
        paymentDateSection.setStyle("-fx-background-color: #e3f2fd; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label paymentDateLabel = new Label("Seleziona la data di pagamento:");
        paymentDateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1565c0;");

        DatePicker paymentDatePicker = new DatePicker(LocalDate.now());
        paymentDatePicker.setPrefWidth(200);

        // Controllo per pagamenti in anticipo/ritardo
        Label dateWarning = new Label();
        dateWarning.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        updateDateWarning(dateWarning, LocalDate.now(), payment.dueDate());

        paymentDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                updateDateWarning(dateWarning, newDate, payment.dueDate());
            }
        });

        paymentDateSection.getChildren().addAll(paymentDateLabel, paymentDatePicker, dateWarning);

        //  SEZIONE NOTA INFORMATIVA
        Label noteTitle = new Label("ℹ️ Nota Importante");
        noteTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #856404;");

        Label noteLabel = new Label(
                "Il pagamento di questa rata creerà automaticamente due movimenti:\n\n" +
                        "• 📉 Uscita per quota capitale (€" + String.format("%.2f", payment.principalAmount()) + ")\n" +
                        "• 📉 Uscita per quota interessi (€" + String.format("%.2f", payment.interestAmount()) + ")\n\n" +
                        "Entrambi i movimenti saranno categorizzati come: " +
                        loan.category().getEmoji() + " " + loan.category().name()
        );
        noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #856404; -fx-background-color: #fff3cd; " +
                "-fx-padding: 12; -fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-border-color: #ffeaa7; -fx-border-width: 1;");
        noteLabel.setWrapText(true);
        content.getChildren().addAll(
                loanInfoTitle,
                loanInfo,
                new Separator(),
                rateInfoTitle,
                rateInfo,
                new Separator(),
                paymentDateTitle,
                paymentDateSection,
                new Separator(),
                noteTitle,
                noteLabel
        );

        getDialogPane().setContent(content);

        //CONFIGURAZIONE RISULTATO
        setResultConverter(dialogButton -> {
            if (dialogButton == payButtonType) {
                return paymentDatePicker.getValue();
            }
            return null;
        });
        paymentDatePicker.requestFocus();
    }

    /**
     * Aggiorna il messaggio di warning per la data di pagamento.
     */
    private void updateDateWarning(Label warningLabel, LocalDate paymentDate, LocalDate dueDate) {
        if (paymentDate == null) {
            warningLabel.setText("");
            return;
        }

        if (paymentDate.isBefore(dueDate)) {
            long daysEarly = java.time.temporal.ChronoUnit.DAYS.between(paymentDate, dueDate);
            warningLabel.setText("⚡ Pagamento in anticipo di " + daysEarly + " giorni");
            warningLabel.setStyle(warningLabel.getStyle() + "-fx-text-fill: #28a745;");
        } else if (paymentDate.isAfter(dueDate)) {
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, paymentDate);
            warningLabel.setText("⚠️ Pagamento in ritardo di " + daysLate + " giorni");
            warningLabel.setStyle(warningLabel.getStyle() + "-fx-text-fill: #dc3545;");
        } else {
            warningLabel.setText("✅ Pagamento puntuale");
            warningLabel.setStyle(warningLabel.getStyle() + "-fx-text-fill: #28a745;");
        }
    }
}