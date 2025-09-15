package it.unicam.cs.mpgc.jbudget123718.jbudget.service;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Movement;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.MovementRepository;

import java.time.LocalDate;
import java.util.List;

public class MovementService {
    private final MovementRepository movementRepo;

    public MovementService(MovementRepository movementRepo) {
        this.movementRepo = movementRepo;
    }

    public Movement addMovement(Movement movement) {
        return movementRepo.save(movement);
    }

    public List<Movement> getMovementsInPeriod(LocalDate from, LocalDate to) {
        return movementRepo.findByDateRange(from, to);
    }

    public void removeMovement(String id) {
        movementRepo.deleteById(id);
    }
}