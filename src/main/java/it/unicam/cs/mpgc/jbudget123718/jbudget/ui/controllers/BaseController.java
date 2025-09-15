package it.unicam.cs.mpgc.jbudget123718.jbudget.ui.controllers;

import javafx.scene.control.Alert;

/**
 * Classe base per tutti i controller dell'applicazione.
 * Fornisce funzionalità comuni come dialog e utility.
 */
public abstract class BaseController {

    /**
     * Mostra un dialog di informazione.
     */
    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setPrefWidth(400);
        alert.showAndWait();
    }

    /**
     * Mostra un dialog di errore.
     */
    protected void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Si è verificato un errore");
        alert.setContentText(message);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    /**
     * Mostra un dialog di avvertimento.
     */
    protected void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Attenzione");
        alert.setContentText(message);
        alert.getDialogPane().setPrefWidth(400);
        alert.showAndWait();
    }

    /**
     * Mostra un dialog di conferma.
     */
    protected boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Conferma");
        alert.setContentText(message);
        alert.getDialogPane().setPrefWidth(400);

        return alert.showAndWait()
                .filter(response -> response == javafx.scene.control.ButtonType.OK)
                .isPresent();
    }

    /**
     * Metodo di inizializzazione chiamato dopo il caricamento FXML.
     */
    public abstract void initialize();
}