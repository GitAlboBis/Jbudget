package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

/**
 * Record per le statistiche sui prestiti.
 * Contiene informazioni aggregate sui prestiti attivi e i pagamenti.
 */
public record LoanStatistics(
        int activeLoansCount,
        double totalOutstandingDebt,
        double monthlyPaymentTotal,
        int overduePaymentsCount,
        int upcomingPaymentsCount,
        double totalAmountPaid,
        double totalInterestPaid
) {


}