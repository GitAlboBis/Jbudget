package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.dialogs;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Category;

import java.time.LocalDate;

/**
 * Record per trasferire le informazioni di pagamento dai dialog.
 */
public record PaymentInfo(
        double amount,
        Category category,
        LocalDate paymentDate
) {}
