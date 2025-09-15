// === LoanPayment.java ===
package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Rappresenta una singola rata del piano di ammortamento.
 */
public record LoanPayment(
        String id,
        String loanId,
        int paymentNumber,
        LocalDate dueDate,
        double totalAmount,
        double principalAmount,
        double interestAmount,
        double remainingBalance,
        LoanPaymentStatus status
) {

}

