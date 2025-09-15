package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

import java.time.LocalDate;

public record Movement(String id, String name, LocalDate date, double amount, MovementType type, Category category) {

    /**
     * Restituisce il valore assoluto dell'importo.
     * Utile per calcoli che necessitano sempre valori positivi.
     */
    public double absoluteAmount() {
        return Math.abs(amount);
    }

    /**
     * Verifica se il movimento è un'entrata.
     */
    public boolean isIncome() {
        return type == MovementType.INCOME;
    }

    /**
     * Verifica se il movimento è una spesa.
     */
    public boolean isExpense() {
        return type == MovementType.EXPENSE;
    }


}