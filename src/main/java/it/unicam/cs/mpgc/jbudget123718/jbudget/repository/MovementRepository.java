package it.unicam.cs.mpgc.jbudget123718.jbudget.repository;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Category;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Movement;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.MovementType;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository per la gestione dei movimenti finanziari.
 * Estende BaseRepository con query specifiche per i movimenti.
 */
public interface MovementRepository extends BaseRepository<Movement, String> {

    /**
     * Trova movimenti in un intervallo di date.
     *
     * @param from data di inizio (inclusa)
     * @param to data di fine (inclusa)
     * @return lista dei movimenti nel periodo
     * @throws IllegalArgumentException se le date sono null o from > to
     */
    List<Movement> findByDateRange(LocalDate from, LocalDate to);

    /**
     * Trova movimenti per categoria.
     *
     * @param category la categoria da cercare
     * @return lista dei movimenti della categoria
     */
    default List<Movement> findByCategory(Category category) {
        return findAll().stream()
                .filter(m -> m.category() == category)
                .toList();
    }

    /**
     * Trova movimenti per tipo (entrata/uscita).
     *
     * @param type il tipo di movimento
     * @return lista dei movimenti del tipo specificato
     */
    default List<Movement> findByType(MovementType type) {
        return findAll().stream()
                .filter(m -> m.type() == type)
                .toList();
    }

    /**
     * Trova movimenti per categoria in un periodo.
     *
     * @param category la categoria
     * @param from data di inizio
     * @param to data di fine
     * @return lista dei movimenti filtrati
     */
    default List<Movement> findByCategoryAndDateRange(Category category, LocalDate from, LocalDate to) {
        return findByDateRange(from, to).stream()
                .filter(m -> m.category() == category)
                .toList();
    }

    /**
     * Calcola il totale per tipo in un periodo.
     *
     * @param type tipo di movimento
     * @param from data di inizio
     * @param to data di fine
     * @return somma degli importi
     */
    default double getTotalByTypeAndDateRange(MovementType type, LocalDate from, LocalDate to) {
        return findByDateRange(from, to).stream()
                .filter(m -> m.type() == type)
                .mapToDouble(Movement::absoluteAmount)
                .sum();
    }
}
