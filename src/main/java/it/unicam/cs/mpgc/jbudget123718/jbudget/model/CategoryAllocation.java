package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

/**
 * Classe separata per rappresentare l'allocazione di budget per categoria.
 */
public record CategoryAllocation(
        String category,
        double allocatedAmount,
        double percentage,
        double spentAmount,
        double remainingAmount
) {
    /**
     * Verifica se il budget per questa categoria è stato superato.
     */
    public boolean isExceeded() {
        return spentAmount > allocatedAmount;
    }

    /**
     * Calcola la percentuale di utilizzo del budget.
     */
    public double getUsagePercentage() {
        return allocatedAmount > 0 ? (spentAmount / allocatedAmount) * 100 : 0;
    }

    /**
     * Ottiene lo stato del budget per questa categoria.
     */
    public String getStatus() {
        if (isExceeded()) {
            return "SUPERATO";
        } else if (getUsagePercentage() > 90) {
            return "CRITICO";
        } else if (getUsagePercentage() > 70) {
            return "ATTENZIONE";
        } else {
            return "OK";
        }
    }


}
