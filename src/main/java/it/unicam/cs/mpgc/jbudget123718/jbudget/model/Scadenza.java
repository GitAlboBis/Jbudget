package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

import java.time.LocalDate;
public record Scadenza(String id, LocalDate dueDate, String description, ScadenzaStatus status) {}