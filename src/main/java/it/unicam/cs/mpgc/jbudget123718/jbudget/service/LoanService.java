package it.unicam.cs.mpgc.jbudget123718.jbudget.service;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.*;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.LoanPaymentRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.LoanRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Servizio per la gestione dei prestiti e piani di ammortamento.
 * Integra con le logiche esistenti di movimenti e scadenze.
 */
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final MovementService movementService;
    private final ScadenzaService scadenzaService;

    public LoanService(LoanRepository loanRepository,
                       LoanPaymentRepository loanPaymentRepository,
                       MovementService movementService,
                       ScadenzaService scadenzaService) {
        this.loanRepository = loanRepository;
        this.loanPaymentRepository = loanPaymentRepository;
        this.movementService = movementService;
        this.scadenzaService = scadenzaService;
    }

    /**
     * Crea un nuovo prestito e genera automaticamente il piano di ammortamento.
     */
    public Loan createLoan(Loan loan) {
        try {
            // Salva il prestito
            Loan savedLoan = loanRepository.save(loan);

            // Genera e salva il piano di ammortamento
            List<LoanPayment> payments = loan.generateAmortizationSchedule();
            for (LoanPayment payment : payments) {
                loanPaymentRepository.save(payment);

                // Crea automaticamente una scadenza per ogni rata
                createPaymentReminder(payment, loan.name());
            }

            System.out.println("✅ Prestito creato con " + payments.size() + " rate");
            return savedLoan;

        } catch (Exception e) {
            System.err.println("❌ Errore creazione prestito: " + e.getMessage());
            throw new RuntimeException("Impossibile creare il prestito", e);
        }
    }

    /**
     * Paga una rata del prestito, creando automaticamente i movimenti appropriati.
     */
    public void payLoanInstallment(String paymentId, LocalDate paymentDate) {
        try {
            var payment = loanPaymentRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Rata non trovata"));

            var loan = loanRepository.findById(payment.loanId())
                    .orElseThrow(() -> new IllegalArgumentException("Prestito non trovato"));

            // Crea movimento per la quota capitale (uscita per rimborso prestito)
            if (payment.principalAmount() > 0) {
                Movement principalMovement = new Movement(
                        UUID.randomUUID().toString(),
                        "Rimborso capitale - " + loan.name() + " (Rata " + payment.paymentNumber() + ")",
                        paymentDate,
                        -payment.principalAmount(), // Negativo perché è un'uscita
                        MovementType.EXPENSE,
                        loan.category()
                );
                movementService.addMovement(principalMovement);
            }

            // Crea movimento per gli interessi (uscita per interessi)
            if (payment.interestAmount() > 0) {
                Movement interestMovement = new Movement(
                        UUID.randomUUID().toString(),
                        "Interessi prestito - " + loan.name() + " (Rata " + payment.paymentNumber() + ")",
                        paymentDate,
                        -payment.interestAmount(), // Negativo perché è un'uscita
                        MovementType.EXPENSE,
                        loan.category()
                );
                movementService.addMovement(interestMovement);
            }

            // Aggiorna lo status della rata
            LoanPayment paidPayment = new LoanPayment(
                    payment.id(),
                    payment.loanId(),
                    payment.paymentNumber(),
                    payment.dueDate(),
                    payment.totalAmount(),
                    payment.principalAmount(),
                    payment.interestAmount(),
                    payment.remainingBalance(),
                    LoanPaymentStatus.PAID
            );

            loanPaymentRepository.save(paidPayment);

            // Verifica se il prestito è completato
            checkLoanCompletion(loan.id());

            System.out.println("✅ Rata pagata: " + payment.paymentNumber() + "/" + loan.totalPayments());

        } catch (Exception e) {
            System.err.println("❌ Errore pagamento rata: " + e.getMessage());
            throw new RuntimeException("Impossibile pagare la rata", e);
        }
    }

    /**
     * Crea una scadenza per ogni rata del prestito.
     */
    private void createPaymentReminder(LoanPayment payment, String loanName) {
        try {
            Scadenza reminder = new Scadenza(
                    UUID.randomUUID().toString(),
                    payment.dueDate(),
                    String.format("Rata prestito %s - %d (€%.2f)",
                            loanName, payment.paymentNumber(), payment.totalAmount()),
                    ScadenzaStatus.PENDING
            );

            scadenzaService.addScadenza(reminder);

        } catch (Exception e) {
            System.err.println("⚠️ Errore creazione promemoria rata: " + e.getMessage());
            // Non bloccare l'operazione principale per un errore di promemoria
        }
    }

    /**
     * Verifica se tutte le rate del prestito sono state pagate.
     */
    private void checkLoanCompletion(String loanId) {
        try {
            var allPayments = loanPaymentRepository.findByLoanId(loanId);
            boolean allPaid = allPayments.stream()
                    .allMatch(p -> p.status() == LoanPaymentStatus.PAID);

            if (allPaid) {
                var loan = loanRepository.findById(loanId).orElse(null);
                if (loan != null) {
                    Loan completedLoan = new Loan(
                            loan.id(),
                            loan.name(),
                            loan.totalAmount(),
                            loan.interestRate(),
                            loan.totalPayments(),
                            loan.startDate(),
                            loan.type(),
                            loan.category(),
                            LoanStatus.COMPLETED,
                            loan.description()
                    );

                    loanRepository.save(completedLoan);
                    System.out.println("🎉 Prestito completato: " + loan.name());
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Errore verifica completamento prestito: " + e.getMessage());
        }
    }

    /**
     * Ottiene tutti i prestiti attivi.
     */
    public List<Loan> getActiveLoans() {
        return loanRepository.findActiveLoans();
    }

    /**
     * Ottiene il piano di ammortamento di un prestito.
     */
    public List<LoanPayment> getLoanPayments(String loanId) {
        return loanPaymentRepository.findByLoanId(loanId);
    }

    /**
     * Ottiene le rate in scadenza nei prossimi giorni.
     */
    public List<LoanPayment> getUpcomingPayments(int days) {
        return loanPaymentRepository.findDueSoon(days);
    }

    /**
     * Ottiene le rate scadute non pagate.
     */
    public List<LoanPayment> getOverduePayments() {
        return loanPaymentRepository.findOverdue();
    }

    /**
     * Calcola il debito residuo totale di tutti i prestiti attivi.
     */
    public double getTotalOutstandingDebt() {
        return getActiveLoans().stream()
                .mapToDouble(this::calculateRemainingDebt)
                .sum();
    }

    /**
     * Calcola il debito residuo di un prestito specifico.
     */
    public double calculateRemainingDebt(Loan loan) {
        var payments = getLoanPayments(loan.id());
        return payments.stream()
                .filter(p -> p.status() == LoanPaymentStatus.PENDING)
                .mapToDouble(LoanPayment::principalAmount)
                .sum();
    }

    /**
     * Calcola gli interessi totali rimanenti di un prestito.
     */
    public double calculateRemainingInterest(Loan loan) {
        var payments = getLoanPayments(loan.id());
        return payments.stream()
                .filter(p -> p.status() == LoanPaymentStatus.PENDING)
                .mapToDouble(LoanPayment::interestAmount)
                .sum();
    }

    /**
     * Calcola l'importo totale mensile delle rate di tutti i prestiti attivi.
     */
    public double getMonthlyPaymentTotal() {
        return getActiveLoans().stream()
                .mapToDouble(Loan::calculateMonthlyPayment)
                .sum();
    }

    /**
     * Ottiene statistiche sui prestiti.
     */
    public LoanStatistics getLoanStatistics() {
        var activeLoans = getActiveLoans();
        var allPayments = loanPaymentRepository.findAll();

        double totalDebt = getTotalOutstandingDebt();
        double monthlyPayments = getMonthlyPaymentTotal();
        int overdueCount = getOverduePayments().size();
        int upcomingCount = getUpcomingPayments(30).size();

        double totalPaid = allPayments.stream()
                .filter(p -> p.status() == LoanPaymentStatus.PAID)
                .mapToDouble(LoanPayment::totalAmount)
                .sum();

        double totalInterestPaid = allPayments.stream()
                .filter(p -> p.status() == LoanPaymentStatus.PAID)
                .mapToDouble(LoanPayment::interestAmount)
                .sum();

        return new LoanStatistics(
                activeLoans.size(),
                totalDebt,
                monthlyPayments,
                overdueCount,
                upcomingCount,
                totalPaid,
                totalInterestPaid
        );
    }

    /**
     * Elimina un prestito e tutte le sue rate associate.
     */
    public void deleteLoan(String loanId) {
        try {
            // Elimina tutte le rate del prestito
            var payments = getLoanPayments(loanId);
            for (LoanPayment payment : payments) {
                loanPaymentRepository.deleteById(payment.id());
            }

            // Elimina il prestito
            loanRepository.deleteById(loanId);

            System.out.println("✅ Prestito eliminato con " + payments.size() + " rate");

        } catch (Exception e) {
            System.err.println("❌ Errore eliminazione prestito: " + e.getMessage());
            throw new RuntimeException("Impossibile eliminare il prestito", e);
        }
    }

    /**
     * Simula diversi scenari di pagamento per un prestito.
     */
    public LoanSimulation simulatePaymentScenarios(Loan loan, double extraMonthlyPayment) {
        var standardPayments = loan.generateAmortizationSchedule();

        // Scenario con pagamenti extra
        var acceleratedPayments = simulateAcceleratedPayment(loan, extraMonthlyPayment);

        double standardTotalInterest = standardPayments.stream()
                .mapToDouble(LoanPayment::interestAmount)
                .sum();

        double acceleratedTotalInterest = acceleratedPayments.stream()
                .mapToDouble(LoanPayment::interestAmount)
                .sum();

        int monthsSaved = standardPayments.size() - acceleratedPayments.size();
        double interestSaved = standardTotalInterest - acceleratedTotalInterest;

        return new LoanSimulation(
                standardPayments.size(),
                acceleratedPayments.size(),
                monthsSaved,
                standardTotalInterest,
                acceleratedTotalInterest,
                interestSaved,
                extraMonthlyPayment
        );
    }

    /**
     * Simula un piano di ammortamento con pagamenti accelerati.
     */
    private List<LoanPayment> simulateAcceleratedPayment(Loan loan, double extraPayment) {
        List<LoanPayment> payments = new java.util.ArrayList<>();
        double monthlyPayment = loan.calculateMonthlyPayment() + extraPayment;
        double remainingBalance = loan.totalAmount();
        double monthlyRate = loan.interestRate() / 100 / 12;
        int paymentNumber = 1;

        while (remainingBalance > 0.01 && paymentNumber <= loan.totalPayments() * 2) {
            double interestAmount = remainingBalance * monthlyRate;
            double principalAmount = Math.min(monthlyPayment - interestAmount, remainingBalance);
            remainingBalance -= principalAmount;

            LocalDate dueDate = loan.startDate().plusMonths(paymentNumber - 1);

            LoanPayment payment = new LoanPayment(
                    UUID.randomUUID().toString(),
                    loan.id(),
                    paymentNumber,
                    dueDate,
                    principalAmount + interestAmount,
                    principalAmount,
                    interestAmount,
                    Math.max(0, remainingBalance),
                    LoanPaymentStatus.PENDING
            );

            payments.add(payment);
            paymentNumber++;
        }

        return payments;
    }

    /**
     * Aggiorna automaticamente lo status delle rate scadute.
     */
    public void updateOverduePayments() {
        try {
            var allPayments = loanPaymentRepository.findAll();
            LocalDate today = LocalDate.now();

            for (LoanPayment payment : allPayments) {
                if (payment.status() == LoanPaymentStatus.PENDING &&
                        payment.dueDate().isBefore(today)) {

                    LoanPayment overduePayment = new LoanPayment(
                            payment.id(),
                            payment.loanId(),
                            payment.paymentNumber(),
                            payment.dueDate(),
                            payment.totalAmount(),
                            payment.principalAmount(),
                            payment.interestAmount(),
                            payment.remainingBalance(),
                            LoanPaymentStatus.OVERDUE
                    );

                    loanPaymentRepository.save(overduePayment);
                }
            }

            System.out.println("✅ Status rate aggiornato");

        } catch (Exception e) {
            System.err.println("❌ Errore aggiornamento status rate: " + e.getMessage());
        }}}