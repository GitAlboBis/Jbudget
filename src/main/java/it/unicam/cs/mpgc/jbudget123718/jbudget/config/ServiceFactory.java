package it.unicam.cs.mpgc.jbudget123718.jbudget.config;

import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.BudgetRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.MovementRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.ScadenzaRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.LoanRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.LoanPaymentRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl.JdbcBudgetRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl.JdbcMovementRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl.JdbcScadenzaRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl.JdbcLoanRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl.JdbcLoanPaymentRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.*;
import javax.sql.DataSource;

/**
 * Factory che implementa il Dependency Inversion Principle.
 * Centralizza la creazione e configurazione di tutti i servizi inclusi i prestiti.
 */
public class ServiceFactory {
    private final DataSource dataSource;

    // Repository
    private MovementRepository movementRepository;
    private BudgetRepository budgetRepository;
    private ScadenzaRepository scadenzaRepository;
    private LoanRepository loanRepository;
    private LoanPaymentRepository loanPaymentRepository;

    // Services
    private MovementService movementService;
    private BudgetService budgetService;
    private ScadenzaService scadenzaService;
    private LoanService loanService;
    private BudgetCalculationService budgetCalculationService;
    private StatsService statsService;

    public ServiceFactory(DataSource dataSource) {
        this.dataSource = dataSource;
        initializeRepositories();
        initializeServices();
    }

    private void initializeRepositories() {
        this.movementRepository = new JdbcMovementRepository(dataSource);
        this.budgetRepository = new JdbcBudgetRepository(dataSource);
        this.scadenzaRepository = new JdbcScadenzaRepository(dataSource);
        this.loanRepository = new JdbcLoanRepository(dataSource);
        this.loanPaymentRepository = new JdbcLoanPaymentRepository(dataSource);

        System.out.println("✅ Repository inizializzati (inclusi prestiti)");
    }

    private void initializeServices() {
        this.movementService = new MovementService(movementRepository);
        this.budgetService = new BudgetService(budgetRepository);
        this.scadenzaService = new ScadenzaService(scadenzaRepository, movementService);
        this.loanService = new LoanService(loanRepository, loanPaymentRepository, movementService, scadenzaService);
        this.budgetCalculationService = new BudgetCalculationService(movementRepository);
        this.statsService = new StatsService();

        System.out.println("✅ Servizi inizializzati (incluso LoanService)");
    }

    // Getters
    public MovementService getMovementService() { return movementService; }
    public BudgetService getBudgetService() { return budgetService; }
    public ScadenzaService getScadenzaService() { return scadenzaService; }
    public LoanService getLoanService() { return loanService; }
    public BudgetCalculationService getBudgetCalculationService() { return budgetCalculationService; }
    public StatsService getStatsService() { return statsService; }
}