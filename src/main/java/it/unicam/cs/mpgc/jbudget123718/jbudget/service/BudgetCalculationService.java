package it.unicam.cs.mpgc.jbudget123718.jbudget.service;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Movement;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.MovementType;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.MovementRepository;

import java.time.YearMonth;

/**
 * Service dedicato ai calcoli dei budget
 */
public class BudgetCalculationService {

    private final MovementRepository movementRepository;

    public BudgetCalculationService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    /**
     * Calcola l'importo speso per una categoria in un periodo specifico.
     * @param categoryName il nome della categoria da analizzare
     * @param period il periodo in formato "YYYY-MM"
     * @return l'importo totale speso (sempre positivo)
     */
    public double calculateSpentAmount(String categoryName, String period) {
        try {
            YearMonth yearMonth = YearMonth.parse(period);
            return movementRepository.findByDateRange(
                            yearMonth.atDay(1),
                            yearMonth.atEndOfMonth()
                    ).stream()
                    .filter(m -> categoryName.equals(m.category().name()) && m.type() == MovementType.EXPENSE)
                    .mapToDouble(Movement::absoluteAmount)
                    .sum();
        } catch (Exception e) {
            System.err.println("❌ Errore calcolo spesa categoria " + categoryName + ": " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calcola l'importo netto per una categoria (entrate - uscite).
     * Utile per categorie che possono avere sia entrate che uscite.
     *
     * @param categoryName il nome della categoria da analizzare
     * @param period il periodo in formato "YYYY-MM"
     * @return l'importo netto (può essere negativo)
     */
    public double calculateNetAmount(String categoryName, String period) {
        try {
            YearMonth yearMonth = YearMonth.parse(period);
            return movementRepository.findByDateRange(
                            yearMonth.atDay(1),
                            yearMonth.atEndOfMonth()
                    ).stream()
                    .filter(m -> categoryName.equals(m.category().name()))
                    .mapToDouble(Movement::amount)
                    .sum();
        } catch (Exception e) {
            System.err.println("❌ Errore calcolo netto categoria " + categoryName + ": " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calcola la percentuale di utilizzo del budget.
     */
    public double calculateUsagePercentage(double spentAmount, double budgetAmount) {
        if (budgetAmount <= 0) {
            return 0.0;
        }
        return Math.min((spentAmount / budgetAmount) * 100, 100.0);
    }

    /**
     * Verifica se un budget è stato superato.
     */
    public boolean isBudgetExceeded(double spentAmount, double budgetAmount) {
        return spentAmount > budgetAmount;
    }

    /**
     * Calcola l'importo rimanente nel budget.
     */
    public double calculateRemainingAmount(double spentAmount, double budgetAmount) {
        return budgetAmount - spentAmount;
    }

    /**
     * Calcola il livello di rischio del budget basato sulla percentuale di utilizzo.
     */
    public String calculateRiskLevel(double usagePercentage) {
        if (usagePercentage > 100) {
            return "EXCEEDED";
        } else if (usagePercentage > 80) {
            return "HIGH";
        } else if (usagePercentage > 60) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Calcola la proiezione di spesa per fine mese basata sui movimenti correnti.
     */
    public double calculateMonthlyProjection(String categoryName, String period) {
        try {
            YearMonth yearMonth = YearMonth.parse(period);
            int daysInMonth = yearMonth.lengthOfMonth();
            int currentDay = java.time.LocalDate.now().getDayOfMonth();

            double spentSoFar = calculateSpentAmount(categoryName, period);

            if (currentDay <= 0) {
                return spentSoFar;
            }

            // Proiezione lineare basata sui giorni trascorsi
            double dailyAverage = spentSoFar / currentDay;
            return dailyAverage * daysInMonth;

        } catch (Exception e) {
            System.err.println("❌ Errore calcolo proiezione categoria " + categoryName + ": " + e.getMessage());
            return 0.0;
        }
    }
}