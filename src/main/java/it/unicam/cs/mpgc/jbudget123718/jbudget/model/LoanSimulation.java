package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

/**
 * Record per la simulazione di scenari di pagamento prestiti.
 * Confronta il piano standard con un piano accelerato.
 */
public record LoanSimulation(
        int standardMonths,
        int acceleratedMonths,
        int monthsSaved,
        double standardTotalInterest,
        double acceleratedTotalInterest,
        double interestSaved,
        double extraMonthlyPayment
) {



}