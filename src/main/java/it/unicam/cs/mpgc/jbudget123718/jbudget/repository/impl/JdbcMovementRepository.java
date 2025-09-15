package it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Category;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Movement;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.MovementType;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.MovementRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMovementRepository implements MovementRepository {
    private final DataSource ds;

    public JdbcMovementRepository(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Movement save(Movement m) {
        if (m == null) {
            throw new IllegalArgumentException("Movement non può essere null");
        }

        String sql = "MERGE INTO movements KEY(id) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, m.id());
            ps.setString(2, m.name());
            ps.setDate(3, Date.valueOf(m.date()));
            ps.setDouble(4, m.amount());
            ps.setString(5, m.category().name());
            ps.setString(6, m.type().name());
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // created_at
            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis())); // updated_at

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Nessuna riga inserita/aggiornata");
            }

            System.out.println("✅ Movement salvato: " + m.id());
            return m;
        } catch (SQLException e) {
            System.err.println("❌ Errore salvataggio movimento: " + e.getMessage());
            throw new RuntimeException("Impossibile salvare il movimento", e);
        }
    }

    @Override
    public Optional<Movement> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM movements WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(mapResultSetToMovement(rs));
        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca movimento: " + e.getMessage());
            throw new RuntimeException("Impossibile trovare il movimento", e);
        }
    }

    @Override
    public List<Movement> findAll() {
        String sql = "SELECT * FROM movements ORDER BY date DESC, created_at DESC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Movement> movements = new ArrayList<>();
            while (rs.next()) {
                movements.add(mapResultSetToMovement(rs));
            }
            return movements;
        } catch (SQLException e) {
            System.err.println("❌ Errore recupero tutti i movimenti: " + e.getMessage());
            throw new RuntimeException("Impossibile recuperare i movimenti", e);
        }
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID movimento non può essere null o vuoto");
        }

        String sql = "DELETE FROM movements WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("⚠️ Nessun movimento trovato con ID: " + id);
            } else {
                System.out.println("✅ Movimento eliminato: " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Errore eliminazione movimento: " + e.getMessage());
            throw new RuntimeException("Impossibile eliminare il movimento", e);
        }
    }

    @Override
    public List<Movement> findByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Le date non possono essere null");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Data inizio deve essere <= data fine");
        }

        String sql = "SELECT * FROM movements WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();

            List<Movement> movements = new ArrayList<>();
            while (rs.next()) {
                movements.add(mapResultSetToMovement(rs));
            }
            return movements;
        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca movimenti per periodo: " + e.getMessage());
            throw new RuntimeException("Impossibile recuperare i movimenti per il periodo", e);
        }
    }

    /**
     * Metodo helper per mappare ResultSet a Movement
     */
    private Movement mapResultSetToMovement(ResultSet rs) throws SQLException {
        try {
            return new Movement(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getDate("date").toLocalDate(),
                    rs.getDouble("amount"),
                    MovementType.valueOf(rs.getString("type")),
                    Category.valueOf(rs.getString("category"))
            );
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Dati corrotti nel database per movimento: " + rs.getString("id"));
            throw new SQLException("Dati movimento non validi", e);
        }
    }
}