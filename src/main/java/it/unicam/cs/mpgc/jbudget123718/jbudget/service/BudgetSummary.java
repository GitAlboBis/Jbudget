package it.unicam.cs.mpgc.jbudget123718.jbudget.service;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Budget;

import java.time.YearMonth;
import java.util.List; /**
 * Record per il riepilogo dei budget di un periodo.
 */
public record BudgetSummary(
        YearMonth period,
        List<Budget> activeBudgets,
        double totalAmount,
        int totalBudgetsCount,
        BudgetSummaryStatus status
) {

    /**
     * Verifica se ci sono budget multipli attivi.
     */
    public boolean hasMultipleBudgets() {
        return activeBudgets.size() > 1;
    }

    /**
     * Ottiene il budget principale
     */
    public Budget getMainBudget() {
        return activeBudgets.stream()
                .max((b1, b2) -> Double.compare(b1.amount(), b2.amount()))
                .orElse(null);
    }

    /**
     * Ottiene una descrizione testuale del riepilogo.
     */
    public String getDescription() {
        return switch (status) {
            case NO_BUDGET -> "Nessun budget configurato";
            case INACTIVE -> "Budget presenti ma non attivi";
            case SINGLE -> "Un budget attivo: " + getMainBudget().name();
            case MULTIPLE -> activeBudgets.size() + " budget attivi (totale: €" + String.format("%.2f", totalAmount) + ")";
        };
    }
}
