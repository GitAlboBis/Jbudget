package it.unicam.cs.mpgc.jbudget123718.jbudget.service;

import java.time.YearMonth;
import java.util.Map;

/**
 * Record che rappresenta le statistiche mensili dei movimenti.
 * Utilizzato per aggregare e presentare i dati finanziari mensili.
 */
public record MonthlyStatistics(
        YearMonth period,
        double totalIncome,
        double totalExpenses,
        double balance,
        Map<String, Double> expensesByCategory
) {

    /**
     * Calcola il tasso di risparmio come percentuale del reddito.
     *
     * @return tasso di risparmio in percentuale (0-100)
     */
    public double getSavingsRate() {
        if (totalIncome <= 0) {
            return 0.0;
        }
        return (balance / totalIncome) * 100;
    }

    /**
     * Verifica se il mese è in perdita.
     *
     * @return true se le uscite superano le entrate
     */
    public boolean isInDeficit() {
        return balance < 0;
    }

    /**
     * Ottiene la categoria con maggiori spese.
     *
     * @return nome della categoria con spese maggiori, o null se non ci sono spese
     */
    public String getTopExpenseCategory() {
        return expensesByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Ottiene l'importo della categoria con maggiori spese.
     *
     * @return importo della categoria con spese maggiori
     */
    public double getTopExpenseAmount() {
        return expensesByCategory.values().stream()
                .max(Double::compareTo)
                .orElse(0.0);
    }

    /**
     * Calcola il numero di categorie con spese.
     *
     * @return numero di categorie che hanno registrato spese
     */
    public int getActiveCategoriesCount() {
        return (int) expensesByCategory.values().stream()
                .filter(amount -> amount > 0)
                .count();
    }
}