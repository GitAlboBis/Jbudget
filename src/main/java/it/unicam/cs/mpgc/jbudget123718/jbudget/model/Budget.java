package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

import java.time.YearMonth;

public record Budget(String id, String name, YearMonth period, double amount, String currency, BudgetStatus status) {

}