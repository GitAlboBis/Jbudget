package it.unicam.cs.mpgc.jbudget123718.jbudget.repository.impl;

import it.unicam.cs.mpgc.jbudget123718.jbudget.model.LoanPayment;
import it.unicam.cs.mpgc.jbudget123718.jbudget.model.LoanPaymentStatus;
import it.unicam.cs.mpgc.jbudget123718.jbudget.repository.LoanPaymentRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; /**
 * Implementazione JDBC del repository per le rate dei prestiti.
 */
public class JdbcLoanPaymentRepository implements LoanPaymentRepository {

    private final DataSource dataSource;

    public JdbcLoanPaymentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public LoanPayment save(LoanPayment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("LoanPayment non può essere null");
        }

        String sql = "MERGE INTO loan_payments KEY(id) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, payment.id());
            statement.setString(2, payment.loanId());
            statement.setInt(3, payment.paymentNumber());
            statement.setDate(4, Date.valueOf(payment.dueDate()));
            statement.setDouble(5, payment.totalAmount());
            statement.setDouble(6, payment.principalAmount());
            statement.setDouble(7, payment.interestAmount());
            statement.setDouble(8, payment.remainingBalance());
            statement.setString(9, payment.status().name());
            statement.setTimestamp(10, new Timestamp(System.currentTimeMillis())); // created_at
            statement.setTimestamp(11, new Timestamp(System.currentTimeMillis())); // updated_at

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Nessuna riga inserita/aggiornata per la rata");
            }

            System.out.println("✅ Rata salvata: " + payment.id());
            return payment;

        } catch (SQLException e) {
            System.err.println("❌ Errore salvataggio rata: " + e.getMessage());
            throw new RuntimeException("Impossibile salvare la rata", e);
        }
    }

    @Override
    public Optional<LoanPayment> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM loan_payments WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(mapResultSetToLoanPayment(resultSet));

        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca rata: " + e.getMessage());
            throw new RuntimeException("Impossibile trovare la rata", e);
        }
    }

    @Override
    public List<LoanPayment> findAll() {
        String sql = "SELECT * FROM loan_payments ORDER BY dueDate ASC, paymentNumber ASC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<LoanPayment> payments = new ArrayList<>();
            while (resultSet.next()) {
                payments.add(mapResultSetToLoanPayment(resultSet));
            }

            return payments;

        } catch (SQLException e) {
            System.err.println("❌ Errore recupero tutte le rate: " + e.getMessage());
            throw new RuntimeException("Impossibile recuperare le rate", e);
        }
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID rata non può essere null o vuoto");
        }

        String sql = "DELETE FROM loan_payments WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("⚠️ Nessuna rata trovata con ID: " + id);
            } else {
                System.out.println("✅ Rata eliminata: " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore eliminazione rata: " + e.getMessage());
            throw new RuntimeException("Impossibile eliminare la rata", e);
        }
    }

    @Override
    public List<LoanPayment> findByLoanId(String loanId) {
        if (loanId == null || loanId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "SELECT * FROM loan_payments WHERE loanId = ? ORDER BY paymentNumber ASC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, loanId);
            ResultSet resultSet = statement.executeQuery();

            List<LoanPayment> payments = new ArrayList<>();
            while (resultSet.next()) {
                payments.add(mapResultSetToLoanPayment(resultSet));
            }

            return payments;

        } catch (SQLException e) {
            System.err.println("❌ Errore ricerca rate per prestito: " + e.getMessage());
            throw new RuntimeException("Impossibile trovare le rate per il prestito", e);
        }
    }

    private LoanPayment mapResultSetToLoanPayment(ResultSet resultSet) throws SQLException {
        try {
            return new LoanPayment(
                    resultSet.getString("id"),
                    resultSet.getString("loanId"),
                    resultSet.getInt("paymentNumber"),
                    resultSet.getDate("dueDate").toLocalDate(),
                    resultSet.getDouble("totalAmount"),
                    resultSet.getDouble("principalAmount"),
                    resultSet.getDouble("interestAmount"),
                    resultSet.getDouble("remainingBalance"),
                    LoanPaymentStatus.valueOf(resultSet.getString("status"))
            );
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Dati corrotti nel database per rata: " + resultSet.getString("id"));
            throw new SQLException("Dati rata non validi", e);
        }
    }
}
