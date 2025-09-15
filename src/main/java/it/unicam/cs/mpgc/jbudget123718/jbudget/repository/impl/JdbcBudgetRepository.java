package it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.Budget;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.BudgetStatus;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.BudgetRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione JDBC del repository per la gestione dei budget.
 * Gestisce la persistenza dei budget nel database H2.
 */
public class JdbcBudgetRepository implements BudgetRepository {

    private final DataSource dataSource;

    public JdbcBudgetRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Budget save(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget non può essere null");
        }


        String sql = "MERGE INTO budgets KEY(id) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, budget.id());
            statement.setString(2, budget.name());
            statement.setString(3, budget.period().toString());
            statement.setDouble(4, budget.amount());
            statement.setString(5, budget.currency());
            statement.setString(6, budget.status().name());
            statement.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // created_at
            statement.setTimestamp(8, new Timestamp(System.currentTimeMillis())); // updated_at

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Nessuna riga inserita/aggiornata per il budget");
            }

            System.out.println("✅ Budget salvato: " + budget.id());
            return budget;

        } catch (SQLException e) {
            System.err.println("❌ Errore salvataggio budget: " + e.getMessage());
            throw new RuntimeException("Impossibile salvare il budget", e);
        }
    }

    @Override
    public Optional<Budget> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM budgets WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(mapResultSetToBudget(resultSet));

        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca budget: " + e.getMessage());
            throw new RuntimeException("Impossibile trovare il budget", e);
        }
    }

    @Override
    public List<Budget> findAll() {
        String sql = "SELECT * FROM budgets ORDER BY period DESC, id";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<Budget> budgets = new ArrayList<>();
            while (resultSet.next()) {
                budgets.add(mapResultSetToBudget(resultSet));
            }

            return budgets;

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero tutti i budget: " + e.getMessage());
            throw new RuntimeException("Impossibile recuperare i budget", e);
        }
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID budget non può essere null o vuoto");
        }

        String sql = "DELETE FROM budgets WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("⚠️ Nessun budget trovato con ID: " + id);
            } else {
                System.out.println("✅ Budget eliminato: " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore eliminazione budget: " + e.getMessage());
            throw new RuntimeException("Impossibile eliminare il budget", e);
        }
    }

    @Override
    public List<Budget> findByPeriod(YearMonth period) {
        if (period == null) {
            throw new IllegalArgumentException("Periodo non può essere null");
        }

        String sql = "SELECT * FROM budgets WHERE period = ? ORDER BY id";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, period.toString());
            ResultSet resultSet = statement.executeQuery();

            List<Budget> budgets = new ArrayList<>();
            while (resultSet.next()) {
                budgets.add(mapResultSetToBudget(resultSet));
            }

            return budgets;

        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca budget per periodo: " + e.getMessage());
            throw new RuntimeException("Impossibile trovare i budget per il periodo", e);
        }
    }

    /**
     * Metodo helper per mappare ResultSet a Budget.
     */
    private Budget mapResultSetToBudget(ResultSet resultSet) throws SQLException {
        try {
            return new Budget(
                    resultSet.getString("id"),
                    resultSet.getString("name"),
                    YearMonth.parse(resultSet.getString("period")),
                    resultSet.getDouble("amount"),
                    resultSet.getString("currency"),
                    BudgetStatus.valueOf(resultSet.getString("status"))
            );
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Dati corrotti nel database per budget: " + resultSet.getString("id"));
            throw new SQLException("Dati budget non validi", e);
        }
    }
}