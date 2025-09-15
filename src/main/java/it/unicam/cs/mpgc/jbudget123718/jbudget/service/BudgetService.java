package it.unicam.cs.mpgc.jbudget123718.jbudget.service;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Budget;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.BudgetStatus;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.BudgetRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servizio  per la gestione dei budget.
 */
public class BudgetService {
    private final BudgetRepository budgetRepo;

    public BudgetService(BudgetRepository budgetRepo) {
        this.budgetRepo = budgetRepo;
    }

    /**
     * Crea o aggiorna un budget.
     * Se esiste già un budget con lo stesso nome e periodo, lo aggiorna.
     */
    public Budget createOrUpdateBudget(Budget budget) {
        try {
            // Verifica se esiste già un budget con lo stesso nome e periodo
            var existingBudgets = getBudgetsForPeriod(budget.period());
            var existingBudget = existingBudgets.stream()
                    .filter(b -> b.name().equals(budget.name()))
                    .findFirst();

            if (existingBudget.isPresent()) {
                System.out.println("📝 Aggiornamento budget esistente: " + budget.name());
                // Aggiorna il budget esistente mantenendo l'ID originale
                Budget updatedBudget = new Budget(
                        existingBudget.get().id(), // Mantieni l'ID esistente
                        budget.name(),
                        budget.period(),
                        budget.amount(),
                        budget.currency(),
                        budget.status()
                );
                return budgetRepo.save(updatedBudget);
            } else {
                System.out.println("➕ Creazione nuovo budget: " + budget.name());
                return budgetRepo.save(budget);
            }

        } catch (Exception e) {
            System.err.println("❌ Errore creazione/aggiornamento budget: " + e.getMessage());
            throw new RuntimeException("Impossibile salvare il budget", e);
        }
    }

    /**
     * Ottiene tutti i budget per un periodo specifico.
     */
    public List<Budget> getBudgetsForPeriod(YearMonth period) {
        return budgetRepo.findByPeriod(period);
    }

    /**
     * Calcola il budget totale per un periodo
     */
    public double getTotalBudgetForPeriod(YearMonth period) {
        return getBudgetsForPeriod(period).stream()
                .filter(b -> b.status() == BudgetStatus.ACTIVE)
                .mapToDouble(Budget::amount)
                .sum();
    }

    /**
     * Ottiene un riepilogo consolidato dei budget per periodo.
     */
    public BudgetSummary getBudgetSummary(YearMonth period) {
        var budgets = getBudgetsForPeriod(period);

        if (budgets.isEmpty()) {
            return new BudgetSummary(
                    period,
                    List.of(),
                    0.0,
                    0,
                    BudgetSummaryStatus.NO_BUDGET
            );
        }

        var activeBudgets = budgets.stream()
                .filter(b -> b.status() == BudgetStatus.ACTIVE)
                .collect(Collectors.toList());

        double totalAmount = activeBudgets.stream()
                .mapToDouble(Budget::amount)
                .sum();

        BudgetSummaryStatus status;
        if (activeBudgets.isEmpty()) {
            status = BudgetSummaryStatus.INACTIVE;
        } else if (activeBudgets.size() == 1) {
            status = BudgetSummaryStatus.SINGLE;
        } else {
            status = BudgetSummaryStatus.MULTIPLE;
        }

        return new BudgetSummary(
                period,
                activeBudgets,
                totalAmount,
                budgets.size(),
                status
        );
    }

    /**
     * Ottiene la distribuzione dei budget per categoria/nome.
     */
    public Map<String, Double> getBudgetDistribution(YearMonth period) {
        return getBudgetsForPeriod(period).stream()
                .filter(b -> b.status() == BudgetStatus.ACTIVE)
                .collect(Collectors.toMap(
                        Budget::name,
                        Budget::amount,
                        Double::sum // Somma i budget con lo stesso nome
                ));
    }

    /**
     * Verifica se un periodo ha budget attivi.
     */
    public boolean hasBudgetForPeriod(YearMonth period) {
        return getBudgetsForPeriod(period).stream()
                .anyMatch(b -> b.status() == BudgetStatus.ACTIVE);
    }

    /**
     * Ottiene tutti i budget attivi per tutti i periodi.
     */
    public List<Budget> getAllActiveBudgets() {
        return budgetRepo.findAll().stream()
                .filter(b -> b.status() == BudgetStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Calcola statistiche sui budget.
     */
    public BudgetStatistics getBudgetStatistics() {
        var allBudgets = budgetRepo.findAll();
        var activeBudgets = allBudgets.stream()
                .filter(b -> b.status() == BudgetStatus.ACTIVE)
                .collect(Collectors.toList());

        int totalBudgets = allBudgets.size();
        int activeBudgetsCount = activeBudgets.size();

        double totalAmount = activeBudgets.stream()
                .mapToDouble(Budget::amount)
                .sum();

        double averageAmount = activeBudgetsCount > 0 ? totalAmount / activeBudgetsCount : 0;

        // Raggruppa per periodo
        Map<YearMonth, List<Budget>> budgetsByPeriod = activeBudgets.stream()
                .collect(Collectors.groupingBy(Budget::period));

        int periodsWithBudget = budgetsByPeriod.size();

        // Trova il periodo con il budget più alto
        var topPeriod = budgetsByPeriod.entrySet().stream()
                .max(Map.Entry.comparingByValue(
                        (list1, list2) -> Double.compare(
                                list1.stream().mapToDouble(Budget::amount).sum(),
                                list2.stream().mapToDouble(Budget::amount).sum()
                        )
                ))
                .map(Map.Entry::getKey)
                .orElse(null);

        return new BudgetStatistics(
                totalBudgets,
                activeBudgetsCount,
                totalAmount,
                averageAmount,
                periodsWithBudget,
                topPeriod
        );
    }
    /**
     * Elimina un budget.
     */
    public void deleteBudget(String id) {
        try {
            budgetRepo.deleteById(id);
            System.out.println("✅ Budget eliminato: " + id);
        } catch (Exception e) {
            System.err.println("❌ Errore eliminazione budget: " + e.getMessage());
            throw new RuntimeException("Impossibile eliminare il budget", e);
        }
    }

}

/**
 * Record per le statistiche sui budget.
 */
record BudgetStatistics(
        int totalBudgets,
        int activeBudgets,
        double totalActiveAmount,
        double averageBudgetAmount,
        int periodsWithBudget,
        YearMonth topPeriod
) {}