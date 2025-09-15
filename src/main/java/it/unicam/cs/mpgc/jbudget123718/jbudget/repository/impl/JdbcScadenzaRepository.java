package it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Scadenza;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.ScadenzaStatus;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.ScadenzaRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcScadenzaRepository implements ScadenzaRepository {
    private final DataSource ds;

    public JdbcScadenzaRepository(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Scadenza save(Scadenza s) {
        if (s == null) {
            throw new IllegalArgumentException("Scadenza non può essere null");
        }

        String sql = "MERGE INTO scadenze KEY(id) VALUES(?,?,?,?,?,?)";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, s.id());
            ps.setDate(2, Date.valueOf(s.dueDate()));
            ps.setString(3, s.description());
            ps.setString(4, s.status().name());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // created_at
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis())); // updated_at

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Nessuna riga inserita/aggiornata per la scadenza");
            }

            System.out.println("✅ Scadenza salvata: " + s.id());
            return s;

        } catch (SQLException e) {
            System.err.println("❌ Errore salvataggio scadenza: " + e.getMessage());
            throw new RuntimeException("Impossibile salvare la scadenza", e);
        }
    }

    @Override
    public Optional<Scadenza> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM scadenze WHERE id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(mapScadenza(rs));

        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca scadenza: " + e.getMessage());
            throw new RuntimeException("Impossibile trovare la scadenza", e);
        }
    }

    @Override
    public List<Scadenza> findAll() {
        String sql = "SELECT * FROM scadenze ORDER BY dueDate ASC";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Scadenza> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapScadenza(rs));
            }
            return list;

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero tutte le scadenze: " + e.getMessage());
            throw new RuntimeException("Impossibile recuperare le scadenze", e);
        }
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID scadenza non può essere null o vuoto");
        }

        String sql = "DELETE FROM scadenze WHERE id = ?";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("⚠️ Nessuna scadenza trovata con ID: " + id);
            } else {
                System.out.println("✅ Scadenza eliminata: " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore eliminazione scadenza: " + e.getMessage());
            throw new RuntimeException("Impossibile eliminare la scadenza", e);
        }
    }

    @Override
    public List<Scadenza> findByDueDateBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Data non può essere null");
        }

        String sql = "SELECT * FROM scadenze WHERE dueDate < ? ORDER BY dueDate ASC";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            List<Scadenza> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapScadenza(rs));
            }
            return list;

        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca scadenze scadute: " + e.getMessage());
            throw new RuntimeException("Impossibile trovare le scadenze scadute", e);
        }
    }

    /**
     * Metodo helper per mappare ResultSet a Scadenza.
     */
    private Scadenza mapScadenza(ResultSet rs) throws SQLException {
        try {
            return new Scadenza(
                    rs.getString("id"),
                    rs.getDate("dueDate").toLocalDate(),
                    rs.getString("description"),
                    ScadenzaStatus.valueOf(rs.getString("status"))
            );
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Dati corrotti nel database per scadenza: " + rs.getString("id"));
            throw new SQLException("Dati scadenza non validi", e);
        }
    }
}