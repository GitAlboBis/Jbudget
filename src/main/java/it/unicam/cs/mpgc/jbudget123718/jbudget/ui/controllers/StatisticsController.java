package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.controllers;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Category;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Movement;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.BudgetCalculationService;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.BudgetService;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.MovementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller dedicato alla gestione delle statistiche con grafici.
 */
public class StatisticsController extends BaseController {

    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label balanceLabel;
    @FXML private Label savingsRateLabel;
    @FXML private ComboBox<String> periodCombo;
    @FXML private Button refreshStatsBtn;

    // Grafici
    @FXML private PieChart expensesByCategoryChart;
    @FXML private BarChart<String, Number> monthlyTrendChart;
    @FXML private CategoryAxis monthAxis;
    @FXML private NumberAxis amountAxis;
    @FXML private GridPane categoryDetailsGrid;

    private final MovementService movementService;
    private final BudgetService budgetService;
    private final BudgetCalculationService budgetCalculationService;

    public StatisticsController(MovementService movementService,
                                BudgetService budgetService,
                                BudgetCalculationService budgetCalculationService) {
        this.movementService = movementService;
        this.budgetService = budgetService;
        this.budgetCalculationService = budgetCalculationService;
    }

    @Override
    public void initialize() {
        setupPeriodCombo();
        setupCharts();
        setupEventHandlers();
        refreshStatistics();

        // Registra per ricevere notifiche di aggiornamento
        DataRefreshManager.getInstance().addRefreshListener(this::refreshStatistics);
    }

    private void setupPeriodCombo() {
        periodCombo.setItems(FXCollections.observableArrayList(
                YearMonth.now().toString(),
                YearMonth.now().minusMonths(1).toString(),
                YearMonth.now().minusMonths(2).toString(),
                YearMonth.now().minusMonths(3).toString(),
                YearMonth.now().minusMonths(4).toString(),
                YearMonth.now().minusMonths(5).toString()
        ));
        periodCombo.setValue(YearMonth.now().toString());
    }

    private void setupCharts() {
        // Configurazione Pie Chart
        expensesByCategoryChart.setTitle("Distribuzione Spese per Categoria");
        expensesByCategoryChart.setLegendVisible(true);
        expensesByCategoryChart.setLabelsVisible(true);

        // Configurazione Bar Chart
        monthlyTrendChart.setTitle("Entrate vs Uscite - Ultimi 6 Mesi");
        monthAxis.setLabel("Mese");
        amountAxis.setLabel("Importo (€)");
        monthlyTrendChart.setLegendVisible(true);
    }

    private void setupEventHandlers() {
        // Gestione cambio periodo per aggiornamento automatico statistiche
        periodCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                refreshStatistics();
            }
        });
    }

    @FXML
    private void onRefreshStats() {
        try {
            System.out.println("📊 Aggiornamento statistiche...");
            refreshStatistics();
            showAlert("Aggiornato", "Statistiche aggiornate con successo!");
        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento statistiche: " + e.getMessage());
            showError("Errore", "Impossibile aggiornare le statistiche: " + e.getMessage());
        }
    }

    /**
     * Aggiorna tutte le statistiche e i grafici.
     */
    public void refreshStatistics() {
        String periodValue = periodCombo.getValue();
        if (periodValue == null || periodValue.trim().isEmpty()) {
            periodValue = YearMonth.now().toString();
            periodCombo.setValue(periodValue);
        }

        final String currentPeriod = periodValue;

        try {
            System.out.println("📊 Calcolo statistiche per periodo: " + currentPeriod);
            YearMonth yearMonth = YearMonth.parse(currentPeriod);
            var movements = movementService.getMovementsInPeriod(
                    yearMonth.atDay(1),
                    yearMonth.atEndOfMonth()
            );

            // Calcola statistiche base
            double totalIncome = movements.stream()
                    .filter(Movement::isIncome)
                    .mapToDouble(Movement::amount)
                    .sum();

            double totalExpenses = movements.stream()
                    .filter(Movement::isExpense)
                    .mapToDouble(m -> Math.abs(m.amount()))
                    .sum();

            double balance = totalIncome - totalExpenses;
            double savingsRate = totalIncome > 0 ? (balance / totalIncome) * 100 : 0;

            // Aggiorna labels
            updateLabels(totalIncome, totalExpenses, balance, savingsRate);

            // Aggiorna grafici
            updateExpensesCategoryChart(movements);
            updateMonthlyTrendChart();
            updateCategoryDetails(movements);

            System.out.println("📊 Statistiche aggiornate - Entrate: €" + String.format("%.2f", totalIncome) +
                    ", Uscite: €" + String.format("%.2f", totalExpenses) +
                    ", Bilancio: €" + String.format("%.2f", balance));

        } catch (Exception e) {
            System.err.println("❌ Errore refresh statistiche: " + e.getMessage());
            resetLabelsToDefault();
        }
    }

    private void updateLabels(double totalIncome, double totalExpenses, double balance, double savingsRate) {
        totalIncomeLabel.setText(String.format("€ %.2f", totalIncome));
        totalIncomeLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");

        totalExpensesLabel.setText(String.format("€ %.2f", totalExpenses));
        totalExpensesLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");

        balanceLabel.setText(String.format("€ %.2f", balance));
        if (balance >= 0) {
            balanceLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 18px;");
        } else {
            balanceLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 18px;");
        }

        savingsRateLabel.setText(String.format("%.1f%%", savingsRate));
        if (savingsRate >= 20) {
            savingsRateLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        } else if (savingsRate >= 10) {
            savingsRateLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
        } else {
            savingsRateLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        }
    }

    private void updateExpensesCategoryChart(java.util.List<Movement> movements) {
        // Calcola spese per categoria
        Map<Category, Double> expensesByCategory = movements.stream()
                .filter(Movement::isExpense)
                .collect(Collectors.groupingBy(
                        Movement::category,
                        Collectors.summingDouble(m -> Math.abs(m.amount()))
                ));

        // Pulisci e aggiorna il grafico
        expensesByCategoryChart.getData().clear();

        if (expensesByCategory.isEmpty()) {
            expensesByCategoryChart.setTitle("Nessuna spesa per il periodo selezionato");
            return;
        }

        expensesByCategory.forEach((category, amount) -> {
            if (amount > 0) {
                PieChart.Data slice = new PieChart.Data(
                        category.getEmoji() + " " + category.name() + " (€" + String.format("%.0f", amount) + ")",
                        amount
                );
                expensesByCategoryChart.getData().add(slice);
            }
        });

        expensesByCategoryChart.setTitle("Spese per Categoria - " + periodCombo.getValue());
    }

    private void updateMonthlyTrendChart() {
        // Pulisci il grafico
        monthlyTrendChart.getData().clear();

        // Serie per entrate e uscite
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Entrate");

        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Uscite");

        // Calcola dati per ultimi 6 mesi
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            String monthName = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN) +
                    " " + month.getYear();

            var monthMovements = movementService.getMovementsInPeriod(
                    month.atDay(1),
                    month.atEndOfMonth()
            );

            double monthIncome = monthMovements.stream()
                    .filter(Movement::isIncome)
                    .mapToDouble(Movement::amount)
                    .sum();

            double monthExpenses = monthMovements.stream()
                    .filter(Movement::isExpense)
                    .mapToDouble(m -> Math.abs(m.amount()))
                    .sum();

            incomeSeries.getData().add(new XYChart.Data<>(monthName, monthIncome));
            expensesSeries.getData().add(new XYChart.Data<>(monthName, monthExpenses));
        }

        monthlyTrendChart.getData().addAll(incomeSeries, expensesSeries);
    }

    private void updateCategoryDetails(java.util.List<Movement> movements) {
        // Pulisci il grid
        categoryDetailsGrid.getChildren().clear();

        Map<Category, Double> expensesByCategory = movements.stream()
                .filter(Movement::isExpense)
                .collect(Collectors.groupingBy(
                        Movement::category,
                        Collectors.summingDouble(m -> Math.abs(m.amount()))
                ));

        if (expensesByCategory.isEmpty()) {
            Label noData = new Label("Nessuna spesa per il periodo selezionato");
            noData.setStyle("-fx-text-fill: #6B7280; -fx-font-style: italic;");
            categoryDetailsGrid.add(noData, 0, 0, 4, 1);
            return;
        }

        // Headers
        Label headerCategory = new Label("Categoria");
        headerCategory.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        categoryDetailsGrid.add(headerCategory, 0, 0);

        Label headerAmount = new Label("Importo");
        headerAmount.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        categoryDetailsGrid.add(headerAmount, 1, 0);

        Label headerTransactions = new Label("Transazioni");
        headerTransactions.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        categoryDetailsGrid.add(headerTransactions, 2, 0);

        Label headerAverage = new Label("Media");
        headerAverage.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        categoryDetailsGrid.add(headerAverage, 3, 0);

        // Dati per categoria
        int row = 1;
        for (Map.Entry<Category, Double> entry : expensesByCategory.entrySet()) {
            Category category = entry.getKey();
            Double totalAmount = entry.getValue();

            long transactionCount = movements.stream()
                    .filter(Movement::isExpense)
                    .filter(m -> m.category() == category)
                    .count();

            double averageAmount = transactionCount > 0 ? totalAmount / transactionCount : 0;

            // Categoria
            Label categoryLabel = new Label(category.getEmoji() + " " + category.name());
            categoryLabel.setStyle("-fx-font-size: 13px;");
            categoryDetailsGrid.add(categoryLabel, 0, row);

            // Importo
            Label amountLabel = new Label(String.format("€ %.2f", totalAmount));
            amountLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            categoryDetailsGrid.add(amountLabel, 1, row);

            // Numero transazioni
            Label transactionsLabel = new Label(String.valueOf(transactionCount));
            transactionsLabel.setStyle("-fx-text-fill: #6B7280;");
            categoryDetailsGrid.add(transactionsLabel, 2, row);

            // Media
            Label averageLabel = new Label(String.format("€ %.2f", averageAmount));
            averageLabel.setStyle("-fx-text-fill: #6B7280;");
            categoryDetailsGrid.add(averageLabel, 3, row);

            row++;
        }
    }
    private void resetLabelsToDefault() {
        totalIncomeLabel.setText("€ 0,00");
        totalExpensesLabel.setText("€ 0,00");
        balanceLabel.setText("€ 0,00");
        savingsRateLabel.setText("0%");

        // Reset stili
        totalIncomeLabel.setStyle("-fx-text-fill: #6B7280;");
        totalExpensesLabel.setStyle("-fx-text-fill: #6B7280;");
        balanceLabel.setStyle("-fx-text-fill: #6B7280;");
        savingsRateLabel.setStyle("-fx-text-fill: #6B7280;");

        // Pulisci grafici
        expensesByCategoryChart.getData().clear();
        monthlyTrendChart.getData().clear();
        categoryDetailsGrid.getChildren().clear();
    }
    /**
     * Imposta il periodo per le statistiche.
     */
    public void setPeriod(String period) {
        periodCombo.setValue(period);
        refreshStatistics();
    }


}