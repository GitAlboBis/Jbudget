package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

import java.time.LocalDate;
import java.util.List; /**
 * Rappresenta un prestito con piano di ammortamento.
 */
public record Loan(
        String id,
        String name,
        double totalAmount,
        double interestRate,
        int totalPayments,
        LocalDate startDate,
        LoanType type,
        Category category,
        LoanStatus status,
        String description
) {

    /**
     * Calcola l'importo della rata mensile usando la formula del prestito.
     */
    public double calculateMonthlyPayment() {
        if (interestRate == 0) {
            return totalAmount / totalPayments;
        }

        double monthlyRate = interestRate / 100 / 12;
        double factor = Math.pow(1 + monthlyRate, totalPayments);
        return totalAmount * (monthlyRate * factor) / (factor - 1);
    }

    /**
     * Genera il piano di ammortamento completo.
     */
    public List<LoanPayment> generateAmortizationSchedule() {
        List<LoanPayment> payments = new java.util.ArrayList<>();
        double monthlyPayment = calculateMonthlyPayment();
        double remainingBalance = totalAmount;
        double monthlyRate = interestRate / 100 / 12;

        for (int i = 1; i <= totalPayments; i++) {
            double interestAmount = remainingBalance * monthlyRate;
            double principalAmount = monthlyPayment - interestAmount;
            remainingBalance -= principalAmount;

            // Aggiusta l'ultima rata per eventuali arrotondamenti
            if (i == totalPayments) {
                principalAmount += remainingBalance;
                remainingBalance = 0;
            }

            LocalDate dueDate = startDate.plusMonths(i - 1);

            LoanPayment payment = new LoanPayment(
                    java.util.UUID.randomUUID().toString(),
                    this.id,
                    i,
                    dueDate,
                    monthlyPayment,
                    principalAmount,
                    interestAmount,
                    Math.max(0, remainingBalance),
                    LoanPaymentStatus.PENDING
            );

            payments.add(payment);
        }

        return payments;
    }


}
