package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.controllers;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Budget;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Category;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Movement;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.*;
import it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs.BudgetDialog;
import it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs.BudgetManagementDialog;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller  per la dashboard Budget.
 */
public class BudgetController extends BaseController {

    // FXML Controls - Header
    @FXML private ComboBox<String> budgetPeriodCombo;
    @FXML private Button refreshBudgetBtn;
    @FXML private Button createBudgetBtn;
    @FXML private Button manageBudgetsBtn;

    // FXML Controls - Financial Overview
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label currentBalanceLabel;
    @FXML private Label savingsRateLabel;
    @FXML private ProgressBar incomeProgressBar;
    @FXML private ProgressBar expensesProgressBar;

    // FXML Controls - Budget Status
    @FXML private VBox noBudgetContainer;
    @FXML private VBox budgetDetailsContainer;
    @FXML private Label budgetNameLabel;
    @FXML private Label budgetAmountLabel;
    @FXML private Label budgetSpentLabel;
    @FXML private Label budgetRemainingLabel;
    @FXML private Label budgetUsageLabel;
    @FXML private ProgressBar budgetProgressBar;
    @FXML private HBox budgetWarningContainer;
    @FXML private Label budgetWarningLabel;

    // FXML Controls - Category Analysis
    @FXML private GridPane categoryCardsGrid;
    @FXML private VBox noCategoryDataContainer;

    // Services
    private final BudgetService budgetService;
    private final MovementService movementService;
    private final BudgetCalculationService budgetCalculationService;

    public BudgetController(BudgetService budgetService,
                            MovementService movementService,
                            BudgetCalculationService budgetCalculationService) {
        this.budgetService = budgetService;
        this.movementService = movementService;
        this.budgetCalculationService = budgetCalculationService;
    }

    @Override
    public void initialize() {
        setupPeriodCombo();
        setupEventHandlers();
        refreshData();

        // Registra per ricevere notifiche di aggiornamento
        DataRefreshManager.getInstance().addRefreshListener(this::refreshData);
    }

    private void setupPeriodCombo() {
        budgetPeriodCombo.setItems(FXCollections.observableArrayList(
                YearMonth.now().toString(),
                YearMonth.now().minusMonths(1).toString(),
                YearMonth.now().minusMonths(2).toString(),
                YearMonth.now().minusMonths(3).toString(),
                YearMonth.now().minusMonths(4).toString(),
                YearMonth.now().minusMonths(5).toString()
        ));
        budgetPeriodCombo.setValue(YearMonth.now().toString());
    }

    private void setupEventHandlers() {
        budgetPeriodCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                refreshData();
            }
        });
    }

    @FXML
    private void onRefreshBudget() {
        refreshData();
        showAlert("Aggiornato", "Dashboard budget aggiornata con successo!");
    }

    @FXML
    private void onCreateBudget() {
        System.out.println("🔄 Apertura dialog nuovo budget...");
        try {
            BudgetDialog dialog = new BudgetDialog();
            dialog.showAndWait().ifPresent(budget -> {
                try {
                    budgetService.createOrUpdateBudget(budget);
                    refreshData();
                    DataRefreshManager.getInstance().notifyDataChanged("BudgetController");
                    showAlert("Successo", "Budget creato con successo!");
                } catch (Exception e) {
                    System.err.println("❌ Errore salvataggio budget: " + e.getMessage());
                    showError("Errore", "Impossibile salvare il budget: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Errore creazione budget: " + e.getMessage());
            showError("Errore", "Impossibile creare il budget: " + e.getMessage());
        }
    }

    @FXML
    private void onManageBudgets() {
        System.out.println("🔄 Apertura gestione budget...");
        try {
            BudgetManagementDialog dialog = new BudgetManagementDialog(budgetService);
            dialog.showAndWait();
            refreshData();
            DataRefreshManager.getInstance().notifyDataChanged("BudgetController");
        } catch (Exception e) {
            System.err.println("❌ Errore gestione budget: " + e.getMessage());
            showError("Errore", "Impossibile aprire la gestione budget: " + e.getMessage());
        }
    }

    /**
     * Aggiorna tutti i dati della dashboard.
     */
    public void refreshData() {
        String currentPeriod = budgetPeriodCombo.getValue();
        if (currentPeriod == null) {
            currentPeriod = YearMonth.now().toString();
            budgetPeriodCombo.setValue(currentPeriod);
        }

        try {
            YearMonth period = YearMonth.parse(currentPeriod);

            // Aggiorna panoramica finanziaria
            updateFinancialOverview(period);

            // Aggiorna stato budget
            updateBudgetStatus(period);

            // Aggiorna analisi per categoria
            updateCategoryAnalysis(period);

            System.out.println("📊 Dashboard budget aggiornata per periodo: " + currentPeriod);

        } catch (Exception e) {
            System.err.println("❌ Errore refresh budget dashboard: " + e.getMessage());
            resetToDefault();
        }
    }

    private void updateFinancialOverview(YearMonth period) {
        try {
            var movements = movementService.getMovementsInPeriod(
                    period.atDay(1),
                    period.atEndOfMonth()
            );

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
            totalIncomeLabel.setText(String.format("€ %.2f", totalIncome));
            totalExpensesLabel.setText(String.format("€ %.2f", totalExpenses));
            currentBalanceLabel.setText(String.format("€ %.2f", balance));
            savingsRateLabel.setText(String.format("%.1f%%", savingsRate));

            // Aggiorna progress bars
            updateFinancialProgressBars(totalIncome, totalExpenses, period);

            // Applica stili basati sui valori
            applyFinancialStyles(balance, savingsRate);

        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento panoramica finanziaria: " + e.getMessage());
        }
    }

    private void updateFinancialProgressBars(double income, double expenses, YearMonth period) {
        try {
            // Calcola i valori dell'anno precedente per confronto
            YearMonth previousYear = period.minusYears(1);
            var previousMovements = movementService.getMovementsInPeriod(
                    previousYear.atDay(1),
                    previousYear.atEndOfMonth()
            );

            double previousIncome = previousMovements.stream()
                    .filter(Movement::isIncome)
                    .mapToDouble(Movement::amount)
                    .sum();

            double previousExpenses = previousMovements.stream()
                    .filter(Movement::isExpense)
                    .mapToDouble(m -> Math.abs(m.amount()))
                    .sum();

            // Calcola progresso relativo (max 100%)
            double incomeProgress = previousIncome > 0 ? Math.min(income / previousIncome, 2.0) / 2.0 : 0.5;
            double expenseProgress = previousExpenses > 0 ? Math.min(expenses / previousExpenses, 2.0) / 2.0 : 0.5;

            incomeProgressBar.setProgress(incomeProgress);
            expensesProgressBar.setProgress(expenseProgress);

        } catch (Exception e) {
            incomeProgressBar.setProgress(0.5);
            expensesProgressBar.setProgress(0.5);
        }
    }

    private void applyFinancialStyles(double balance, double savingsRate) {
        // Stili per il bilancio
        if (balance >= 0) {
            currentBalanceLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        } else {
            currentBalanceLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        }

        // Stili per il tasso di risparmio
        if (savingsRate >= 20) {
            savingsRateLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        } else if (savingsRate >= 10) {
            savingsRateLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
        } else {
            savingsRateLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        }
    }

    private void updateBudgetStatus(YearMonth period) {
        try {
            var budgets = budgetService.getBudgetsForPeriod(period);

            if (budgets.isEmpty()) {
                showNoBudgetState();
            } else {
                showBudgetDetails(budgets.get(0), period);
            }

        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento stato budget: " + e.getMessage());
            showNoBudgetState();
        }
    }

    private void showNoBudgetState() {
        noBudgetContainer.setVisible(true);
        budgetDetailsContainer.setVisible(false);
    }

    private void showBudgetDetails(Budget budget, YearMonth period) {
        noBudgetContainer.setVisible(false);
        budgetDetailsContainer.setVisible(true);

        // Calcola spese totali del periodo
        double totalExpenses = movementService.getMovementsInPeriod(
                        period.atDay(1),
                        period.atEndOfMonth()
                ).stream()
                .filter(Movement::isExpense)
                .mapToDouble(m -> Math.abs(m.amount()))
                .sum();

        double remaining = budget.amount() - totalExpenses;
        double usagePercentage = budget.amount() > 0 ? (totalExpenses / budget.amount()) * 100 : 0;

        // Aggiorna informazioni budget
        budgetNameLabel.setText(budget.name());
        budgetAmountLabel.setText(String.format("€ %.2f", budget.amount()));
        budgetSpentLabel.setText(String.format("€ %.2f", totalExpenses));
        budgetRemainingLabel.setText(String.format("€ %.2f", remaining));
        budgetUsageLabel.setText(String.format("%.1f%%", usagePercentage));

        // Aggiorna progress bar
        budgetProgressBar.setProgress(Math.min(usagePercentage / 100.0, 1.0));

        // Applica stili e warning
        applyBudgetStyles(usagePercentage, remaining);
        updateBudgetWarning(usagePercentage, remaining);
    }

    private void applyBudgetStyles(double usagePercentage, double remaining) {
        if (usagePercentage > 100) {
            budgetRemainingLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            budgetUsageLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            budgetProgressBar.setStyle("-fx-accent: #EF4444;");
        } else if (usagePercentage > 80) {
            budgetRemainingLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
            budgetUsageLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
            budgetProgressBar.setStyle("-fx-accent: #F59E0B;");
        } else {
            budgetRemainingLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
            budgetUsageLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
            budgetProgressBar.setStyle("-fx-accent: #10B981;");
        }
    }

    private void updateBudgetWarning(double usagePercentage, double remaining) {
        if (usagePercentage > 100) {
            budgetWarningContainer.setVisible(true);
            budgetWarningLabel.setText("Budget superato di € " + String.format("%.2f", Math.abs(remaining)) +
                    ". Considera di rivedere le spese o aumentare il budget.");
        } else if (usagePercentage > 90) {
            budgetWarningContainer.setVisible(true);
            budgetWarningLabel.setText("Attenzione: hai utilizzato oltre il 90% del budget. " +
                    "Rimangono solo € " + String.format("%.2f", remaining) + ".");
        } else if (usagePercentage > 80) {
            budgetWarningContainer.setVisible(true);
            budgetWarningLabel.setText("Stai utilizzando più dell'80% del budget. " +
                    "Monitoraggio consigliato per le prossime spese.");
        } else {
            budgetWarningContainer.setVisible(false);
        }
    }

    private void updateCategoryAnalysis(YearMonth period) {
        try {
            var movements = movementService.getMovementsInPeriod(
                    period.atDay(1),
                    period.atEndOfMonth()
            );

            Map<Category, Double> expensesByCategory = movements.stream()
                    .filter(Movement::isExpense)
                    .collect(Collectors.groupingBy(
                            Movement::category,
                            Collectors.summingDouble(m -> Math.abs(m.amount()))
                    ));

            if (expensesByCategory.isEmpty()) {
                showNoCategoryData();
            } else {
                buildCategoryCards(expensesByCategory, movements);
            }

        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento analisi categoria: " + e.getMessage());
            showNoCategoryData();
        }
    }

    private void showNoCategoryData() {
        categoryCardsGrid.getChildren().clear();
        noCategoryDataContainer.setVisible(true);
    }

    private void buildCategoryCards(Map<Category, Double> expensesByCategory, List<Movement> movements) {
        categoryCardsGrid.getChildren().clear();
        noCategoryDataContainer.setVisible(false);

        int col = 0;
        int row = 0;

        for (Map.Entry<Category, Double> entry : expensesByCategory.entrySet()) {
            Category category = entry.getKey();
            Double amount = entry.getValue();

            if (amount > 0) {
                VBox categoryCard = createCategoryCard(category, amount, movements);
                categoryCardsGrid.add(categoryCard, col, row);

                col++;
                if (col >= 3) { // 3 colonne
                    col = 0;
                    row++;
                }
            }
        }
    }

    private VBox createCategoryCard(Category category, double amount, List<Movement> movements) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("stats-card", "category-card");
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);

        // Header con icona e nome
        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label icon = new Label(category.getEmoji());
        icon.setStyle("-fx-font-size: 18px;");

        Label name = new Label(category.name());
        name.getStyleClass().add("card-title");

        header.getChildren().addAll(icon, name);

        // Importo
        Label amountLabel = new Label(String.format("€ %.2f", amount));
        amountLabel.getStyleClass().addAll("amount-display", "amount-negative");

        // Numero transazioni
        long transactionCount = movements.stream()
                .filter(Movement::isExpense)
                .filter(m -> m.category() == category)
                .count();

        Label countLabel = new Label(transactionCount + " transazioni");
        countLabel.getStyleClass().add("card-subtitle");

        // Media per transazione
        double average = transactionCount > 0 ? amount / transactionCount : 0;
        Label avgLabel = new Label("Media: € " + String.format("%.2f", average));
        avgLabel.getStyleClass().add("card-subtitle");

        // Progress bar (relativa alla categoria con più spese)
        double maxAmount = movements.stream()
                .filter(Movement::isExpense)
                .collect(Collectors.groupingBy(
                        Movement::category,
                        Collectors.summingDouble(m -> Math.abs(m.amount()))
                )).values().stream()
                .max(Double::compareTo)
                .orElse(1.0);

        ProgressBar progressBar = new ProgressBar(amount / maxAmount);
        progressBar.setPrefWidth(180);
        progressBar.getStyleClass().add("expense-progress");

        card.getChildren().addAll(header, amountLabel, countLabel, avgLabel, progressBar);

        return card;
    }

    private void resetToDefault() {
        // Reset financial overview
        totalIncomeLabel.setText("€ 0,00");
        totalExpensesLabel.setText("€ 0,00");
        currentBalanceLabel.setText("€ 0,00");
        savingsRateLabel.setText("0%");
        incomeProgressBar.setProgress(0);
        expensesProgressBar.setProgress(0);

        // Reset budget status
        showNoBudgetState();

        // Reset category analysis
        showNoCategoryData();
    }

    /**
     * Imposta un periodo specifico.
     */
    public void setPeriod(String period) {
        budgetPeriodCombo.setValue(period);
        refreshData();
    }


}