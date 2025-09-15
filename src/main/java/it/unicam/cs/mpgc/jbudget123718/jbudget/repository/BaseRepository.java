package it.unicam.cs.mpgc.jbudget123718.jbudget.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfaccia base per tutti i repository.
 * Definisce le operazioni CRUD standard.
 *
 * @param <T> Tipo dell'entità
 * @param <ID> Tipo dell'identificatore
 */
public interface BaseRepository<T, ID> {

    /**
     * Salva un'entità (inserimento o aggiornamento).
     *
     * @param entity l'entità da salvare
     * @return l'entità salvata
     * @throws RuntimeException se il salvataggio fallisce
     */
    T save(T entity);

    /**
     * Trova un'entità per ID.
     *
     * @param id l'identificatore
     * @return Optional contenente l'entità se trovata
     */
    Optional<T> findById(ID id);

    /**
     * Trova tutte le entità.
     *
     * @return lista di tutte le entità
     */
    List<T> findAll();

    /**
     * Elimina un'entità per ID.
     *
     * @param id l'identificatore dell'entità da eliminare
     * @throws RuntimeException se l'eliminazione fallisce
     */
    void deleteById(ID id);
}
