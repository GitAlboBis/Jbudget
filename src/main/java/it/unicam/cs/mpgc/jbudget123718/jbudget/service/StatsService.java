package it.unicam.cs.mpgc.jbudget123718.jbudget.service;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Movement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsService {
    public Map<String, Double> sumByCategory(List<Movement> movements) {
        return movements.stream()
                .collect(Collectors.groupingBy(
                        m -> m.category().name(),
                        Collectors.summingDouble(Movement::amount)
                ));
    }
}