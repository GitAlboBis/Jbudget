package it.unicam.cs.mpgc.jbudget123718.jbudget.repository;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Scadenza;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.ScadenzaStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository per la gestione delle scadenze.
 * Estende BaseRepository con query specifiche per le scadenze.
 */
public interface ScadenzaRepository extends BaseRepository<Scadenza, String> {

    /**
     * Trova scadenze con data di scadenza precedente alla data specificata.
     * Utilizzato per trovare scadenze scadute.
     *
     * @param date la data di riferimento
     * @return lista delle scadenze scadute
     */
    List<Scadenza> findByDueDateBefore(LocalDate date);

    /**
     * Trova scadenze per status.
     *
     * @param status lo status delle scadenze
     * @return lista delle scadenze con lo status specificato
     */
    default List<Scadenza> findByStatus(ScadenzaStatus status) {
        return findAll().stream()
                .filter(s -> s.status() == status)
                .toList();
    }

    /**
     * Trova scadenze in scadenza entro i prossimi giorni.
     *
     * @param days numero di giorni da oggi
     * @return lista delle scadenze in scadenza
     */
    default List<Scadenza> findDueSoon(int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return findAll().stream()
                .filter(s -> s.status() == ScadenzaStatus.PENDING)
                .filter(s -> !s.dueDate().isAfter(futureDate))
                .filter(s -> !s.dueDate().isBefore(LocalDate.now()))
                .toList();
    }

    /**
     * Trova scadenze scadute (status PENDING con data passata).
     *
     * @return lista delle scadenze scadute
     */
    default List<Scadenza> findOverdue() {
        return findByDueDateBefore(LocalDate.now()).stream()
                .filter(s -> s.status() == ScadenzaStatus.PENDING)
                .toList();
    }

    /**
     * Trova scadenze in un intervallo di date.
     *
     * @param from data di inizio
     * @param to data di fine
     * @return lista delle scadenze nel periodo
     */
    default List<Scadenza> findByDueDateRange(LocalDate from, LocalDate to) {
        return findAll().stream()
                .filter(s -> !s.dueDate().isBefore(from) && !s.dueDate().isAfter(to))
                .toList();
    }

    /**
     * Trova scadenze pending (da pagare).
     *
     * @return lista delle scadenze in attesa di pagamento
     */
    default List<Scadenza> findPending() {
        return findByStatus(ScadenzaStatus.PENDING);
    }

    /**
     * Trova scadenze completate.
     *
     * @return lista delle scadenze pagate
     */
    default List<Scadenza> findCompleted() {
        return findByStatus(ScadenzaStatus.COMPLETED);
    }

    /**
     * Conta scadenze per status.
     *
     * @param status lo status da contare
     * @return numero di scadenze con quello status
     */
    default long countByStatus(ScadenzaStatus status) {
        return findByStatus(status).size();
    }

    /**
     * Verifica se ci sono scadenze urgenti (scadute o in scadenza oggi).
     *
     * @return true se ci sono scadenze urgenti
     */
    default boolean hasUrgentScadenze() {
        LocalDate today = LocalDate.now();
        return findAll().stream()
                .anyMatch(s -> s.status() == ScadenzaStatus.PENDING &&
                        !s.dueDate().isAfter(today));
    }
}