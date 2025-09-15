package it.unicam.cs.mpgc.jbudget123718.jbudget.repository;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.LoanPayment;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.LoanPaymentStatus;

import java.time.LocalDate;
import java.util.List; /**
 * Repository per la gestione delle rate dei prestiti.
 */
public interface LoanPaymentRepository extends BaseRepository<LoanPayment, String> {

    /**
     * Trova rate per prestito.
     */
    List<LoanPayment> findByLoanId(String loanId);

    /**
     * Trova rate per status.
     */
    default List<LoanPayment> findByStatus(LoanPaymentStatus status) {
        return findAll().stream()
                .filter(payment -> payment.status() == status)
                .toList();
    }

    /**
     * Trova rate in scadenza entro una data.
     */
    default List<LoanPayment> findDueBefore(LocalDate date) {
        return findAll().stream()
                .filter(payment -> payment.status() == LoanPaymentStatus.PENDING)
                .filter(payment -> !payment.dueDate().isAfter(date))
                .toList();
    }

    /**
     * Trova rate scadute.
     */
    default List<LoanPayment> findOverdue() {
        return findDueBefore(LocalDate.now()).stream()
                .filter(payment -> payment.dueDate().isBefore(LocalDate.now()))
                .toList();
    }

    /**
     * Trova prossime rate in scadenza.
     */
    default List<LoanPayment> findDueSoon(int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return findAll().stream()
                .filter(payment -> payment.status() == LoanPaymentStatus.PENDING)
                .filter(payment -> !payment.dueDate().isAfter(futureDate))
                .filter(payment -> !payment.dueDate().isBefore(LocalDate.now()))
                .toList();
    }
}
