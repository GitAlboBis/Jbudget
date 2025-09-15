package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.controllers;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager semplice per notificare gli aggiornamenti dati tra controller.
 */
public class DataRefreshManager {

    private static final DataRefreshManager INSTANCE = new DataRefreshManager();
    private final List<Runnable> refreshListeners = new ArrayList<>();

    private DataRefreshManager() {}

    public static DataRefreshManager getInstance() {
        return INSTANCE;
    }

    /**
     * Registra un listener per gli aggiornamenti.
     */
    public void addRefreshListener(Runnable listener) {
        refreshListeners.add(listener);
    }

    /**
     * Notifica tutti i controller che i dati sono cambiati.
     */
    public void notifyDataChanged(String source) {
        System.out.println("📢 Dati cambiati da: " + source + " - Aggiornamento controller...");
        refreshListeners.forEach(listener -> {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("❌ Errore aggiornamento controller: " + e.getMessage());
            }
        });
    }}

