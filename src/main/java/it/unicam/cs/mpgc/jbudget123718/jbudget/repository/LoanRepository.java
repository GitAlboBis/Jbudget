package it.unicam.cs.mpgc.jbudget123718.jbudget.repository;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository per la gestione dei prestiti.
 */
public interface LoanRepository extends BaseRepository<Loan, String> {

    /**
     * Trova prestiti per status.
     */
    default List<Loan> findByStatus(LoanStatus status) {
        return findAll().stream()
                .filter(loan -> loan.status() == status)
                .toList();
    }

    /**
     * Trova prestiti attivi.
     */
    default List<Loan> findActiveLoans() {
        return findByStatus(LoanStatus.ACTIVE);
    }

    /**
     * Trova prestiti per tipo.
     */
    default List<Loan> findByType(LoanType type) {
        return findAll().stream()
                .filter(loan -> loan.type() == type)
                .toList();
    }

    /**
     * Trova prestiti con scadenza entro una data.
     */
    default List<Loan> findByStartDateBefore(LocalDate date) {
        return findAll().stream()
                .filter(loan -> loan.startDate().isBefore(date))
                .toList();
    }
}



