package it.unicam.cs.mpgc.jbudget123718.jbudget.repository;


import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Budget;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.BudgetStatus;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Repository per la gestione dei budget.
 * Estende BaseRepository con query specifiche per i budget.
 */
public interface BudgetRepository extends BaseRepository<Budget, String> {

    /**
     * Trova budget per periodo specifico.
     *
     * @param period il periodo in formato YYYY-MM
     * @return lista dei budget per il periodo
     */
    List<Budget> findByPeriod(YearMonth period);

    /**
     * Trova budget per status.
     *
     * @param status lo status del budget
     * @return lista dei budget con lo status specificato
     */
    default List<Budget> findByStatus(BudgetStatus status) {
        return findAll().stream()
                .filter(b -> b.status() == status)
                .toList();
    }

    /**
     * Trova budget attivi per periodo.
     *
     * @param period il periodo
     * @return lista dei budget attivi
     */
    default List<Budget> findActiveByPeriod(YearMonth period) {
        return findByPeriod(period).stream()
                .filter(b -> b.status() == BudgetStatus.ACTIVE)
                .toList();
    }

    /**
     * Trova budget per ID categoria e periodo.
     * Utile per verificare se esiste già un budget per quella categoria/periodo.
     *
     * @param categoryId l'ID della categoria (che nel nostro caso è anche l'ID del budget)
     * @param period il periodo
     * @return Optional contenente il budget se trovato
     */
    default Optional<Budget> findByCategoryAndPeriod(String categoryId, YearMonth period) {
        return findByPeriod(period).stream()
                .filter(b -> b.id().equals(categoryId))
                .findFirst();
    }

    /**
     * Verifica se esiste un budget per il periodo.
     *
     * @param period il periodo da verificare
     * @return true se esistono budget per il periodo
     */
    default boolean existsByPeriod(YearMonth period) {
        return !findByPeriod(period).isEmpty();
    }

    /**
     * Calcola il budget totale per un periodo.
     *
     * @param period il periodo
     * @return somma di tutti i budget del periodo
     */
    default double getTotalBudgetForPeriod(YearMonth period) {
        return findByPeriod(period).stream()
                .mapToDouble(Budget::amount)
                .sum();
    }
}