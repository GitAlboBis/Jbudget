package it.unicam.cs.mpgc.jbudget123718.jbudget.service;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.*;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.ScadenzaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ScadenzaService {
    private final ScadenzaRepository scadenzaRepository;
    private final MovementService movementService;

    public ScadenzaService(ScadenzaRepository scadenzaRepository, MovementService movementService) {
        this.scadenzaRepository = scadenzaRepository;
        this.movementService = movementService;
    }

    public Scadenza addScadenza(Scadenza scadenza) {
        return scadenzaRepository.save(scadenza);
    }

    public List<Scadenza> getAllScadenze() {
        return scadenzaRepository.findAll();
    }

    public List<Scadenza> getOverdueScadenze() {
        return scadenzaRepository.findByDueDateBefore(LocalDate.now());
    }

    /**
     * Paga una scadenza creando automaticamente un movimento di uscita.
     */
    public void payScadenza(String scadenzaId, double amount, Category category) {
        var scadenza = scadenzaRepository.findById(scadenzaId)
                .orElseThrow(() -> new IllegalArgumentException("Scadenza non trovata"));

        // Crea movimento di uscita con nome descrittivo
        Movement movement = new Movement(
                UUID.randomUUID().toString(),
                "Pagamento: " + scadenza.description(), // Nome descrittivo
                LocalDate.now(),
                -Math.abs(amount), // Sempre negativo per le uscite
                MovementType.EXPENSE,
                category
        );

        movementService.addMovement(movement);

        // Aggiorna stato scadenza
        Scadenza updatedScadenza = new Scadenza(
                scadenza.id(),
                scadenza.dueDate(),
                scadenza.description(),
                ScadenzaStatus.COMPLETED
        );

        scadenzaRepository.save(updatedScadenza);
    }

    public void deleteScadenza(String id) {
        scadenzaRepository.deleteById(id);
    }

    /**
     * Aggiorna automaticamente lo status delle scadenze scadute.
     */
    public void updateOverdueStatus() {
        List<Scadenza> all = scadenzaRepository.findAll();
        LocalDate now = LocalDate.now();

        for (Scadenza s : all) {
            if (s.dueDate().isBefore(now) && s.status() == ScadenzaStatus.PENDING) {
                Scadenza updated = new Scadenza(s.id(), s.dueDate(), s.description(), ScadenzaStatus.OVERDUE);
                scadenzaRepository.save(updated);
            }
        }
    }
}