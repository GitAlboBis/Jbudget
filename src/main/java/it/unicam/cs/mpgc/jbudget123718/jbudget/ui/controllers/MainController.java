package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.controllers;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.ScadenzaStatus;
import it.unicam.cs.mpgc.jbudget123718.jbudget.service.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;

import java.time.LocalDate;

/**
 * Controller principale dell'applicazione JBudget.
 * Con FXML multipli, gestisce SOLO la logica di coordinamento generale.
 */
public class MainController extends BaseController {

    @FXML private TabPane mainTabPane;
    @FXML private Label notificationsLabel;

    private final ScadenzaService scadenzaService;
    private final BudgetCalculationService budgetCalculationService;

    public MainController(MovementService movementService,
                          BudgetService budgetService,
                          ScadenzaService scadenzaService,
                          BudgetCalculationService budgetCalculationService,
                          StatsService statsService) {
        // Memorizziamo solo i servizi che ci servono per le notifiche
        this.scadenzaService = scadenzaService;
        this.budgetCalculationService = budgetCalculationService;
    }

    @Override
    public void initialize() {
        System.out.println("🎯 Inizializzazione MainController...");

        try {

            // Aggiorna automaticamente le scadenze all'avvio
            scadenzaService.updateOverdueStatus();

            // Controlla notifiche urgenti
            checkUrgentNotifications();

            System.out.println("✅ MainController inizializzato con successo");

        } catch (Exception e) {
            System.err.println("❌ Errore inizializzazione MainController: " + e.getMessage());
            e.printStackTrace();
            showError("Errore Inizializzazione", "Impossibile inizializzare l'interfaccia: " + e.getMessage());
        }
    }

    /**
     * Controlla e mostra notifiche urgenti.
     */
    private void checkUrgentNotifications() {
        try {
            var scadenze = scadenzaService.getAllScadenze();
            LocalDate today = LocalDate.now();

            long urgentCount = scadenze.stream()
                    .filter(s -> s.status() == ScadenzaStatus.PENDING)
                    .filter(s -> !s.dueDate().isAfter(today))
                    .count();

            if (urgentCount > 0) {
                String message = urgentCount == 1 ?
                        "🚨 1 scadenza urgente" :
                        "🚨 " + urgentCount + " scadenze urgenti";

                notificationsLabel.setText(message);
                notificationsLabel.setVisible(true);
                notificationsLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");

                System.out.println("⚠️ Trovate " + urgentCount + " scadenze urgenti");
            } else {
                notificationsLabel.setVisible(false);
            }

        } catch (Exception e) {
            System.err.println("❌ Errore controllo notifiche: " + e.getMessage());
            notificationsLabel.setVisible(false);
        }
    }



}