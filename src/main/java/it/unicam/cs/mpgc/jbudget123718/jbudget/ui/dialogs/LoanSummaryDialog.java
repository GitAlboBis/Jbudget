package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Loan;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.LoanPayment;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.LoanPaymentStatus;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog per visualizzare il piano di ammortamento completo di un prestito.
 * Mostra tutte le rate con dettagli su capitale, interessi e stato pagamenti.
 */
public class LoanSummaryDialog extends Dialog<Void> {

    public LoanSummaryDialog(Loan loan, List<LoanPayment> payments) {
        setTitle("Piano di Ammortamento - " + loan.name());
        setHeaderText("Dettaglio completo del piano di rimborso");
        setResizable(true);

        ButtonType closeButtonType = new ButtonType("Chiudi", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType exportButtonType = new ButtonType("Esporta", ButtonBar.ButtonData.OTHER);
        getDialogPane().getButtonTypes().addAll(exportButtonType, closeButtonType);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(900);
        content.setPrefHeight(900);

        //  SEZIONE INFORMAZIONI PRESTITO
        VBox loanInfoSection = createLoanInfoSection(loan);

        // SEZIONE STATISTICHE
        VBox statsSection = createStatsSection(payments);

        // SEZIONE TABELLA PIANO AMMORTAMENTO
        VBox tableSection = createTableSection(payments);

        content.getChildren().addAll(loanInfoSection, new Separator(), statsSection, new Separator(), tableSection);
        getDialogPane().setContent(content);

        // Gestione bottone export
        Button exportButton = (Button) getDialogPane().lookupButton(exportButtonType);
        exportButton.setOnAction(e -> exportPlan(loan, payments));
    }

    /**
     * Crea la sezione con le informazioni generali del prestito.
     */
    private VBox createLoanInfoSection(Loan loan) {
        VBox section = new VBox(10);

        Label titleLabel = new Label("📋 Informazioni Prestito");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Grid con informazioni prestito
        javafx.scene.layout.GridPane infoGrid = new javafx.scene.layout.GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(8);
        infoGrid.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 15; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Riga 1
        infoGrid.add(createInfoLabel("💰 Importo Totale:"), 0, 0);
        infoGrid.add(createValueLabel(String.format("€ %.2f", loan.totalAmount())), 1, 0);

        infoGrid.add(createInfoLabel("📊 Tasso Interesse:"), 2, 0);
        infoGrid.add(createValueLabel(String.format("%.2f%% annuo", loan.interestRate())), 3, 0);

        // Riga 2
        infoGrid.add(createInfoLabel("📅 Numero Rate:"), 0, 1);
        infoGrid.add(createValueLabel(String.valueOf(loan.totalPayments())), 1, 1);

        infoGrid.add(createInfoLabel("🗓️ Data Inizio:"), 2, 1);
        infoGrid.add(createValueLabel(loan.startDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 3, 1);

        // Riga 3
        infoGrid.add(createInfoLabel("🏷️ Tipo:"), 0, 2);
        infoGrid.add(createValueLabel(loan.type().getEmoji() + " " + loan.type().name().replace("_", " ")), 1, 2);

        infoGrid.add(createInfoLabel("📂 Categoria:"), 2, 2);
        infoGrid.add(createValueLabel(loan.category().getEmoji() + " " + loan.category().name()), 3, 2);

        // Calcolo rata mensile
        double monthlyPayment = loan.calculateMonthlyPayment();
        Label monthlyPaymentLabel = new Label(String.format("💳 Rata Mensile: € %.2f", monthlyPayment));
        monthlyPaymentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        section.getChildren().addAll(titleLabel, infoGrid, monthlyPaymentLabel);
        return section;
    }

    /**
     * Crea la sezione con le statistiche del piano.
     */
    private VBox createStatsSection(List<LoanPayment> payments) {
        VBox section = new VBox(10);

        Label titleLabel = new Label("📈 Statistiche Piano");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Calcolo statistiche
        long paidCount = payments.stream().filter(p -> p.status() == LoanPaymentStatus.PAID).count();
        long pendingCount = payments.stream().filter(p -> p.status() == LoanPaymentStatus.PENDING).count();
        long overdueCount = payments.stream().filter(p -> p.status() == LoanPaymentStatus.OVERDUE).count();

        double totalPaid = payments.stream()
                .filter(p -> p.status() == LoanPaymentStatus.PAID)
                .mapToDouble(LoanPayment::totalAmount)
                .sum();

        double totalRemaining = payments.stream()
                .filter(p -> p.status() != LoanPaymentStatus.PAID)
                .mapToDouble(LoanPayment::totalAmount)
                .sum();

        double totalInterestPaid = payments.stream()
                .filter(p -> p.status() == LoanPaymentStatus.PAID)
                .mapToDouble(LoanPayment::interestAmount)
                .sum();

        double totalInterestRemaining = payments.stream()
                .filter(p -> p.status() != LoanPaymentStatus.PAID)
                .mapToDouble(LoanPayment::interestAmount)
                .sum();

        double progressPercentage = payments.isEmpty() ? 0 : (double) paidCount / payments.size() * 100;

        // Grid statistiche
        javafx.scene.layout.GridPane statsGrid = new javafx.scene.layout.GridPane();
        statsGrid.setHgap(30);
        statsGrid.setVgap(10);
        statsGrid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Prima riga - Contatori rate
        statsGrid.add(createStatsCard("✅ Rate Pagate", String.valueOf(paidCount), "#27ae60"), 0, 0);
        statsGrid.add(createStatsCard("⏳ Rate Rimanenti", String.valueOf(pendingCount), "#3498db"), 1, 0);
        statsGrid.add(createStatsCard("🚨 Rate Scadute", String.valueOf(overdueCount), "#e74c3c"), 2, 0);

        // Seconda riga - Importi
        statsGrid.add(createStatsCard("💸 Totale Pagato", String.format("€ %.2f", totalPaid), "#27ae60"), 0, 1);
        statsGrid.add(createStatsCard("💰 Totale Rimanente", String.format("€ %.2f", totalRemaining), "#3498db"), 1, 1);
        statsGrid.add(createStatsCard("📊 Progresso", String.format("%.1f%%", progressPercentage), "#9b59b6"), 2, 1);

        // Terza riga - Interessi
        statsGrid.add(createStatsCard("💸 Interessi Pagati", String.format("€ %.2f", totalInterestPaid), "#e67e22"), 0, 2);
        statsGrid.add(createStatsCard("💰 Interessi Rimanenti", String.format("€ %.2f", totalInterestRemaining), "#f39c12"), 1, 2);

        // Progress bar visuale
        ProgressBar progressBar = new ProgressBar(progressPercentage / 100.0);
        progressBar.setPrefWidth(400);
        progressBar.setStyle("-fx-accent: #3498db;");

        Label progressLabel = new Label(String.format("Completamento: %.1f%% (%d di %d rate)",
                progressPercentage, paidCount, payments.size()));
        progressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        VBox progressBox = new VBox(5, progressLabel, progressBar);
        progressBox.setStyle("-fx-alignment: center;");

        section.getChildren().addAll(titleLabel, statsGrid, progressBox);
        return section;
    }

    /**
     * Crea la sezione con la tabella del piano di ammortamento.
     */
    private VBox createTableSection(List<LoanPayment> payments) {
        VBox section = new VBox(10);
        VBox.setVgrow(section, Priority.ALWAYS);

        Label titleLabel = new Label("📋 Piano di Ammortamento");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Creazione tabella
        TableView<LoanPayment> table = new TableView<>();
        table.setPrefHeight(350);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Colonne
        TableColumn<LoanPayment, Integer> numCol = new TableColumn<>("N°");
        numCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().paymentNumber()));
        numCol.setPrefWidth(50);
        numCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LoanPayment, LocalDate> dateCol = new TableColumn<>("Scadenza");
        dateCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().dueDate()));
        dateCol.setPrefWidth(100);

        TableColumn<LoanPayment, Double> totalCol = new TableColumn<>("Rata Totale");
        totalCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().totalAmount()));
        totalCol.setPrefWidth(110);

        TableColumn<LoanPayment, Double> principalCol = new TableColumn<>("Quota Capitale");
        principalCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().principalAmount()));
        principalCol.setPrefWidth(120);

        TableColumn<LoanPayment, Double> interestCol = new TableColumn<>("Quota Interessi");
        interestCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().interestAmount()));
        interestCol.setPrefWidth(120);

        TableColumn<LoanPayment, Double> balanceCol = new TableColumn<>("Debito Residuo");
        balanceCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> cell.getValue().remainingBalance()));
        balanceCol.setPrefWidth(120);

        TableColumn<LoanPayment, String> statusCol = new TableColumn<>("Stato");
        statusCol.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() -> cell.getValue().status().name()));
        statusCol.setPrefWidth(100);

        // Formattazione celle
        setupTableFormatting(dateCol, totalCol, principalCol, interestCol, balanceCol, statusCol);

        table.getColumns().addAll(numCol, dateCol, totalCol, principalCol, interestCol, balanceCol, statusCol);
        table.setItems(FXCollections.observableArrayList(payments));

        // Evidenzia righe per status
        table.setRowFactory(tv -> new TableRow<LoanPayment>() {
            @Override
            protected void updateItem(LoanPayment payment, boolean empty) {
                super.updateItem(payment, empty);
                if (empty || payment == null) {
                    setStyle("");
                } else {
                    switch (payment.status()) {
                        case PAID -> setStyle("-fx-background-color: #d5f4e6;");
                        case OVERDUE -> setStyle("-fx-background-color: #fdeaea;");
                        case PENDING -> {
                            if (payment.dueDate().isBefore(LocalDate.now().plusDays(30))) {
                                setStyle("-fx-background-color: #fff3cd;"); // In scadenza presto
                            } else {
                                setStyle("");
                            }
                        }
                    }
                }
            }
        });

        section.getChildren().addAll(titleLabel, table);
        return section;
    }

    /**
     * Configura la formattazione delle celle della tabella.
     */
    private void setupTableFormatting(TableColumn<LoanPayment, LocalDate> dateCol,
                                      TableColumn<LoanPayment, Double> totalCol,
                                      TableColumn<LoanPayment, Double> principalCol,
                                      TableColumn<LoanPayment, Double> interestCol,
                                      TableColumn<LoanPayment, Double> balanceCol,
                                      TableColumn<LoanPayment, String> statusCol) {

        // Formato data
        dateCol.setCellFactory(column -> new TableCell<LoanPayment, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yy")));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Formato importi
        totalCol.setCellFactory(column -> new TableCell<LoanPayment, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("€ %.2f", amount));
                    setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");
                }
            }
        });

        principalCol.setCellFactory(column -> new TableCell<LoanPayment, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("€ %.2f", amount));
                    setStyle("-fx-alignment: CENTER_RIGHT; -fx-text-fill: #27ae60;");
                }
            }
        });

        interestCol.setCellFactory(column -> new TableCell<LoanPayment, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("€ %.2f", amount));
                    setStyle("-fx-alignment: CENTER_RIGHT; -fx-text-fill: #e74c3c;");
                }
            }
        });

        balanceCol.setCellFactory(column -> new TableCell<LoanPayment, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("€ %.2f", amount));
                    setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold; -fx-text-fill: #3498db;");
                }
            }
        });

        statusCol.setCellFactory(column -> new TableCell<LoanPayment, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
                    switch (status) {
                        case "PAID" -> {
                            setText("✅ Pagata");
                            setStyle(getStyle() + "-fx-text-fill: #27ae60;");
                        }
                        case "PENDING" -> {
                            setText("⏳ In attesa");
                            setStyle(getStyle() + "-fx-text-fill: #3498db;");
                        }
                        case "OVERDUE" -> {
                            setText("🚨 Scaduta");
                            setStyle(getStyle() + "-fx-text-fill: #e74c3c;");
                        }
                        default -> setText(status);
                    }
                }
            }
        });
    }

    /**
     * Crea un label per le informazioni.
     */
    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        return label;
    }

    /**
     * Crea un label per i valori.
     */
    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        return label;
    }

    /**
     * Crea una card per le statistiche.
     */
    private VBox createStatsCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setStyle(String.format("-fx-background-color: white; -fx-padding: 10; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-border-color: %s; -fx-border-width: 2; -fx-alignment: center;", color));
        card.setPrefWidth(120);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        titleLabel.setWrapText(true);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: %s;", color));

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    /**
     * Esporta il piano di ammortamento (placeholder per funzionalità futura).
     */
    private void exportPlan(Loan loan, List<LoanPayment> payments) {
        try {
            StringBuilder export = new StringBuilder();
            export.append("=== PIANO DI AMMORTAMENTO ===\n");
            export.append("Prestito: ").append(loan.name()).append("\n");
            export.append("Importo: €").append(String.format("%.2f", loan.totalAmount())).append("\n");
            export.append("Tasso: ").append(String.format("%.2f%%", loan.interestRate())).append("\n");
            export.append("Rate: ").append(loan.totalPayments()).append("\n");
            export.append("Data inizio: ").append(loan.startDate()).append("\n\n");

            export.append("N°\tData\t\tRata\t\tCapitale\tInteressi\tResiduo\t\tStato\n");
            export.append("=".repeat(90)).append("\n");

            for (LoanPayment payment : payments) {
                export.append(String.format("%d\t%s\t€%.2f\t\t€%.2f\t\t€%.2f\t\t€%.2f\t\t%s\n",
                        payment.paymentNumber(),
                        payment.dueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        payment.totalAmount(),
                        payment.principalAmount(),
                        payment.interestAmount(),
                        payment.remainingBalance(),
                        payment.status().name()
                ));
            }

            System.out.println("📋 Piano di ammortamento esportato:\n" + export.toString());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Completato");
            alert.setHeaderText("Piano di ammortamento esportato");
            alert.setContentText("Il piano è stato generato nei log dell'applicazione.");
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore Export");
            alert.setHeaderText("Impossibile esportare il piano");
            alert.setContentText("Errore: " + e.getMessage());
            alert.showAndWait();
        }
    }
}