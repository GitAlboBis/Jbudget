package it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.*;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.LoanPaymentRepository;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.LoanRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione JDBC del repository per i prestiti.
 */
public class JdbcLoanRepository implements LoanRepository {

    private final DataSource dataSource;

    public JdbcLoanRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Loan save(Loan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("Loan non può essere null");
        }

        String sql = "MERGE INTO loans KEY(id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, loan.id());
            statement.setString(2, loan.name());
            statement.setDouble(3, loan.totalAmount());
            statement.setDouble(4, loan.interestRate());
            statement.setInt(5, loan.totalPayments());
            statement.setDate(6, Date.valueOf(loan.startDate()));
            statement.setString(7, loan.type().name());
            statement.setString(8, loan.category().name());
            statement.setString(9, loan.status().name());
            statement.setString(10, loan.description());
            statement.setTimestamp(11, new Timestamp(System.currentTimeMillis())); // created_at
            statement.setTimestamp(12, new Timestamp(System.currentTimeMillis())); // updated_at

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Nessuna riga inserita/aggiornata per il prestito");
            }

            System.out.println("✅ Prestito salvato: " + loan.id());
            return loan;

        } catch (SQLException e) {
            System.err.println("❌ Errore salvataggio prestito: " + e.getMessage());
            throw new RuntimeException("Impossibile salvare il prestito", e);
        }
    }

    @Override
    public Optional<Loan> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM loans WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(mapResultSetToLoan(resultSet));

        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca prestito: " + e.getMessage());
            throw new RuntimeException("Impossibile trovare il prestito", e);
        }
    }

    @Override
    public List<Loan> findAll() {
        String sql = "SELECT * FROM loans ORDER BY startDate DESC, id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<Loan> loans = new ArrayList<>();
            while (resultSet.next()) {
                loans.add(mapResultSetToLoan(resultSet));
            }

            return loans;

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero tutti i prestiti: " + e.getMessage());
            throw new RuntimeException("Impossibile recuperare i prestiti", e);
        }
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID prestito non può essere null o vuoto");
        }

        String sql = "DELETE FROM loans WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("⚠️ Nessun prestito trovato con ID: " + id);
            } else {
                System.out.println("✅ Prestito eliminato: " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore eliminazione prestito: " + e.getMessage());
            throw new RuntimeException("Impossibile eliminare il prestito", e);
        }
    }

    private Loan mapResultSetToLoan(ResultSet resultSet) throws SQLException {
        try {
            return new Loan(
                    resultSet.getString("id"),
                    resultSet.getString("name"),
                    resultSet.getDouble("totalAmount"),
                    resultSet.getDouble("interestRate"),
                    resultSet.getInt("totalPayments"),
                    resultSet.getDate("startDate").toLocalDate(),
                    LoanType.valueOf(resultSet.getString("type")),
                    Category.valueOf(resultSet.getString("category")),
                    LoanStatus.valueOf(resultSet.getString("status")),
                    resultSet.getString("description")
            );
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Dati corrotti nel database per prestito: " + resultSet.getString("id"));
            throw new SQLException("Dati prestito non validi", e);
        }
    }
}


