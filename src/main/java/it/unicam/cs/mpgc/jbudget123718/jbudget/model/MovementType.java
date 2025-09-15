package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

/**
 * Enum per i tipi di movimento finanziario.

 */
public enum MovementType {
    INCOME("📈", "Entrata", "Denaro ricevuto"),
    EXPENSE("📉", "Uscita", "Denaro speso"),
    LOAN("🏦", "Prestito", "Gestione prestiti");

    private final String emoji;
    private final String displayName;
    private final String description;

    MovementType(String emoji, String displayName, String description) {
        this.emoji = emoji;
        this.displayName = displayName;
        this.description = description;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Verifica se il tipo rappresenta un'uscita.
     */
    public boolean isExpense() {
        return this == EXPENSE;
    }

    /**
     * Verifica se il tipo rappresenta un prestito.
     */
    public boolean isLoan() {
        return this == LOAN;
    }
}