package it.unicam.cs.mpgc.jbudget123718.jbudget.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init(DataSource ds) {
        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {

            // Test connessione
            if (!conn.isValid(5)) {
                throw new SQLException("Connessione database non valida");
            }

            System.out.println("🔄 Inizializzazione database in corso...");

            // Creazione tabelle
            createTablesIfNotExists(st);

            // Creazione indici
            createIndexesIfNotExists(st);

            // Validazione struttura
            validateDatabaseStructure(st);

            System.out.println("✅ Database inizializzato correttamente");

        } catch (SQLException e) {
            System.err.println("❌ Errore inizializzazione database: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossibile inizializzare il database", e);
        }
    }

    private static void createTablesIfNotExists(Statement st) throws SQLException {

        // Tabella movimenti (invariata)
        st.execute("""
            CREATE TABLE IF NOT EXISTS movements (
                id VARCHAR(255) PRIMARY KEY, 
                name VARCHAR(255) NOT NULL,
                date DATE NOT NULL, 
                amount DOUBLE NOT NULL, 
                category VARCHAR(255) NOT NULL CHECK (category IN ('FOOD', 'TRANSPORT', 'UTILITIES', 'OTHER')), 
                type VARCHAR(255) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Tabella budget (invariata)
        st.execute("""
            CREATE TABLE IF NOT EXISTS budgets (
                id VARCHAR(255) PRIMARY KEY, 
                name VARCHAR(255) NOT NULL,
                period VARCHAR(7) NOT NULL, 
                amount DOUBLE NOT NULL CHECK (amount > 0), 
                currency VARCHAR(3) NOT NULL DEFAULT 'EUR', 
                status VARCHAR(20) NOT NULL CHECK (status IN ('PLANNED', 'ACTIVE', 'CLOSED')),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(period, name)
            )
        """);

        // Tabella scadenze (invariata)
        st.execute("""
            CREATE TABLE IF NOT EXISTS scadenze (
                id VARCHAR(255) PRIMARY KEY, 
                dueDate DATE NOT NULL, 
                description VARCHAR(255) NOT NULL, 
                status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'OVERDUE')),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // NUOVA: Tabella prestiti
        st.execute("""
            CREATE TABLE IF NOT EXISTS loans (
                id VARCHAR(255) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                totalAmount DOUBLE NOT NULL CHECK (totalAmount > 0),
                interestRate DOUBLE NOT NULL CHECK (interestRate >= 0),
                totalPayments INTEGER NOT NULL CHECK (totalPayments > 0),
                startDate DATE NOT NULL,
                type VARCHAR(50) NOT NULL CHECK (type IN ('MORTGAGE', 'CAR_LOAN', 'PERSONAL_LOAN', 'STUDENT_LOAN', 'BUSINESS_LOAN', 'OTHER')),
                category VARCHAR(255) NOT NULL CHECK (category IN ('FOOD', 'TRANSPORT', 'UTILITIES', 'OTHER')),
                status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETED', 'SUSPENDED', 'DEFAULTED')),
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // NUOVA: Tabella rate prestiti
        st.execute("""
            CREATE TABLE IF NOT EXISTS loan_payments (
                id VARCHAR(255) PRIMARY KEY,
                loanId VARCHAR(255) NOT NULL,
                paymentNumber INTEGER NOT NULL,
                dueDate DATE NOT NULL,
                totalAmount DOUBLE NOT NULL CHECK (totalAmount > 0),
                principalAmount DOUBLE NOT NULL CHECK (principalAmount >= 0),
                interestAmount DOUBLE NOT NULL CHECK (interestAmount >= 0),
                remainingBalance DOUBLE NOT NULL CHECK (remainingBalance >= 0),
                status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (loanId) REFERENCES loans(id) ON DELETE CASCADE,
                UNIQUE(loanId, paymentNumber)
            )
        """);

        System.out.println("📋 Tabelle create/verificate (incluse tabelle prestiti)");
    }

    private static void createIndexesIfNotExists(Statement st) throws SQLException {

        // Indici esistenti per movements
        st.execute("CREATE INDEX IF NOT EXISTS idx_movements_date ON movements(date)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_movements_category ON movements(category)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_movements_type ON movements(type)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_movements_date_category ON movements(date, category)");

        // Indici esistenti per budgets
        st.execute("CREATE INDEX IF NOT EXISTS idx_budgets_period ON budgets(period)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_budgets_status ON budgets(status)");

        // Indici esistenti per scadenze
        st.execute("CREATE INDEX IF NOT EXISTS idx_scadenze_date ON scadenze(dueDate)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_scadenze_status ON scadenze(status)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_scadenze_date_status ON scadenze(dueDate, status)");

        //  Indici per prestiti
        st.execute("CREATE INDEX IF NOT EXISTS idx_loans_status ON loans(status)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_loans_type ON loans(type)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_loans_start_date ON loans(startDate)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_loans_category ON loans(category)");

        //  Indici per rate prestiti
        st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_loan_id ON loan_payments(loanId)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_due_date ON loan_payments(dueDate)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_status ON loan_payments(status)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_due_status ON loan_payments(dueDate, status)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_loan_payment_num ON loan_payments(loanId, paymentNumber)");

        System.out.println("🚀 Indici creati per ottimizzare le performance (inclusi prestiti)");
    }

    private static void validateDatabaseStructure(Statement st) throws SQLException {

        // Verifica esistenza tabelle
        var rs = st.executeQuery("""
            SELECT table_name FROM information_schema.tables 
            WHERE table_schema = 'PUBLIC' AND table_name IN ('MOVEMENTS', 'BUDGETS', 'SCADENZE', 'LOANS', 'LOAN_PAYMENTS')
        """);

        int tableCount = 0;
        while (rs.next()) {
            tableCount++;
        }

        if (tableCount != 5) {
            throw new SQLException("Non tutte le tabelle sono state create correttamente. Trovate: " + tableCount + "/5");
        }

        // Test inserimento/lettura movements
        try {
            st.execute("INSERT INTO movements VALUES('test', 'Test Movement', CURRENT_DATE, 100.0, 'OTHER', 'INCOME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
            var testRs = st.executeQuery("SELECT COUNT(*) FROM movements WHERE id = 'test'");
            testRs.next();

            if (testRs.getInt(1) != 1) {
                throw new SQLException("Test di scrittura movements fallito");
            }

            st.execute("DELETE FROM movements WHERE id = 'test'");

        } catch (Exception e) {
            System.err.println("⚠️ Attenzione: Test validazione movements fallito: " + e.getMessage());
        }

        // Test inserimento/lettura loans
        try {
            st.execute("""
                INSERT INTO loans VALUES('test-loan', 'Test Loan', 10000.0, 5.0, 12, CURRENT_DATE, 
                'PERSONAL_LOAN', 'OTHER', 'ACTIVE', 'Test Description', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """);

            var testLoanRs = st.executeQuery("SELECT COUNT(*) FROM loans WHERE id = 'test-loan'");
            testLoanRs.next();

            if (testLoanRs.getInt(1) != 1) {
                throw new SQLException("Test di scrittura loans fallito");
            }

            // Test rate prestito
            st.execute("""
                INSERT INTO loan_payments VALUES('test-payment', 'test-loan', 1, CURRENT_DATE, 
                500.0, 450.0, 50.0, 9500.0, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """);

            var testPaymentRs = st.executeQuery("SELECT COUNT(*) FROM loan_payments WHERE id = 'test-payment'");
            testPaymentRs.next();

            if (testPaymentRs.getInt(1) != 1) {
                throw new SQLException("Test di scrittura loan_payments fallito");
            }

            // Cleanup test data
            st.execute("DELETE FROM loan_payments WHERE id = 'test-payment'");
            st.execute("DELETE FROM loans WHERE id = 'test-loan'");

        } catch (Exception e) {
            System.err.println("⚠️ Attenzione: Test validazione prestiti fallito: " + e.getMessage());
        }

        System.out.println("🔍 Struttura database validata (incluse nuove tabelle prestiti)");
    }

    /**
     * Metodo per reset completo del database
     */
    public static void resetDatabase(DataSource ds) {
        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {

            System.out.println("⚠️ ATTENZIONE: Reset database in corso...");

            // Drop tabelle in ordine inverso per rispettare le dipendenze
            st.execute("DROP TABLE IF EXISTS loan_payments");
            st.execute("DROP TABLE IF EXISTS loans");
            st.execute("DROP TABLE IF EXISTS scadenze");
            st.execute("DROP TABLE IF EXISTS budgets");
            st.execute("DROP TABLE IF EXISTS movements");

            System.out.println("🗑️ Tabelle eliminate");

            // Ricrea tutto
            init(ds);

            System.out.println("✅ Database resettato e ricreato");

        } catch (SQLException e) {
            System.err.println("❌ Errore reset database: " + e.getMessage());
            throw new RuntimeException("Impossibile resettare il database", e);
        }
    }

    /**
     * Metodo per ottenere statistiche database
     */
    public static void printDatabaseStats(DataSource ds) {
        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {

            System.out.println("\n📊 STATISTICHE DATABASE:");

            // Conta records per tabella esistente
            var rsMovements = st.executeQuery("SELECT COUNT(*) FROM movements");
            rsMovements.next();
            System.out.println("💳 Movimenti: " + rsMovements.getInt(1));

            var rsBudgets = st.executeQuery("SELECT COUNT(*) FROM budgets");
            rsBudgets.next();
            System.out.println("📊 Budget: " + rsBudgets.getInt(1));

            var rsScadenze = st.executeQuery("SELECT COUNT(*) FROM scadenze");
            rsScadenze.next();
            System.out.println("📅 Scadenze: " + rsScadenze.getInt(1));

            //  Conta records tabelle prestiti
            var rsLoans = st.executeQuery("SELECT COUNT(*) FROM loans");
            rsLoans.next();
            System.out.println("🏦 Prestiti: " + rsLoans.getInt(1));

            var rsLoanPayments = st.executeQuery("SELECT COUNT(*) FROM loan_payments");
            rsLoanPayments.next();
            System.out.println("💰 Rate prestiti: " + rsLoanPayments.getInt(1));

            // Verifica integrità
            var rsIntegrity = st.executeQuery("""
                SELECT 
                    (SELECT COUNT(*) FROM movements WHERE category NOT IN ('FOOD', 'TRANSPORT', 'UTILITIES', 'OTHER')) as invalid_categories,
                    (SELECT COUNT(*) FROM movements WHERE type NOT IN ('INCOME', 'EXPENSE')) as invalid_types,
                    (SELECT COUNT(*) FROM budgets WHERE amount <= 0) as invalid_budgets,
                    (SELECT COUNT(*) FROM loans WHERE totalAmount <= 0) as invalid_loans,
                    (SELECT COUNT(*) FROM loan_payments WHERE totalAmount <= 0) as invalid_payments
            """);
            rsIntegrity.next();

            int invalidCategories = rsIntegrity.getInt(1);
            int invalidTypes = rsIntegrity.getInt(2);
            int invalidBudgets = rsIntegrity.getInt(3);
            int invalidLoans = rsIntegrity.getInt(4);
            int invalidPayments = rsIntegrity.getInt(5);

            if (invalidCategories == 0 && invalidTypes == 0 && invalidBudgets == 0 &&
                    invalidLoans == 0 && invalidPayments == 0) {
                System.out.println("✅ Integrità dati: OK");
            } else {
                System.out.println("⚠️ Problemi integrità trovati:");
                if (invalidCategories > 0) System.out.println("  - Categorie non valide: " + invalidCategories);
                if (invalidTypes > 0) System.out.println("  - Tipi non validi: " + invalidTypes);
                if (invalidBudgets > 0) System.out.println("  - Budget non validi: " + invalidBudgets);
                if (invalidLoans > 0) System.out.println("  - Prestiti non validi: " + invalidLoans);
                if (invalidPayments > 0) System.out.println("  - Rate non valide: " + invalidPayments);
            }

            //  Statistiche prestiti
            try {
                var rsLoanStats = st.executeQuery("""
                    SELECT 
                        COUNT(*) as total_loans,
                        COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_loans,
                        SUM(CASE WHEN status = 'ACTIVE' THEN totalAmount ELSE 0 END) as total_debt,
                        COUNT(CASE WHEN lp.status = 'OVERDUE' THEN 1 END) as overdue_payments
                    FROM loans l 
                    LEFT JOIN loan_payments lp ON l.id = lp.loanId
                """);
                rsLoanStats.next();

                int totalLoans = rsLoanStats.getInt("total_loans");
                int activeLoans = rsLoanStats.getInt("active_loans");
                double totalDebt = rsLoanStats.getDouble("total_debt");
                int overduePayments = rsLoanStats.getInt("overdue_payments");

                System.out.println("\n🏦 STATISTICHE PRESTITI:");
                System.out.println("  - Prestiti totali: " + totalLoans);
                System.out.println("  - Prestiti attivi: " + activeLoans);
                System.out.println("  - Debito totale: €" + String.format("%.2f", totalDebt));
                System.out.println("  - Rate scadute: " + overduePayments);

            } catch (Exception e) {
                System.err.println("⚠️ Errore calcolo statistiche prestiti: " + e.getMessage());
            }

            System.out.println();

        } catch (SQLException e) {
            System.err.println("❌ Errore lettura statistiche: " + e.getMessage());
        }
    }

    /**
     *  Metodo per aggiornare il database esistente aggiungendo le nuove tabelle
     */
    public static void upgradeDatabase(DataSource ds) {
        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {

            System.out.println("🔄 Aggiornamento database in corso...");

            // Controlla se le tabelle prestiti esistono già
            var rs = st.executeQuery("""
                SELECT table_name FROM information_schema.tables 
                WHERE table_schema = 'PUBLIC' AND table_name IN ('LOANS', 'LOAN_PAYMENTS')
            """);

            boolean hasLoansTable = false;
            boolean hasPaymentsTable = false;

            while (rs.next()) {
                String tableName = rs.getString("table_name");
                if ("LOANS".equals(tableName)) hasLoansTable = true;
                if ("LOAN_PAYMENTS".equals(tableName)) hasPaymentsTable = true;
            }

            if (!hasLoansTable) {
                System.out.println("➕ Creazione tabella loans...");
                st.execute("""
                    CREATE TABLE loans (
                        id VARCHAR(255) PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        totalAmount DOUBLE NOT NULL CHECK (totalAmount > 0),
                        interestRate DOUBLE NOT NULL CHECK (interestRate >= 0),
                        totalPayments INTEGER NOT NULL CHECK (totalPayments > 0),
                        startDate DATE NOT NULL,
                        type VARCHAR(50) NOT NULL CHECK (type IN ('MORTGAGE', 'CAR_LOAN', 'PERSONAL_LOAN', 'STUDENT_LOAN', 'BUSINESS_LOAN', 'OTHER')),
                        category VARCHAR(255) NOT NULL CHECK (category IN ('FOOD', 'TRANSPORT', 'UTILITIES', 'OTHER')),
                        status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETED', 'SUSPENDED', 'DEFAULTED')),
                        description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);
            }

            if (!hasPaymentsTable) {
                System.out.println("➕ Creazione tabella loan_payments...");
                st.execute("""
                    CREATE TABLE loan_payments (
                        id VARCHAR(255) PRIMARY KEY,
                        loanId VARCHAR(255) NOT NULL,
                        paymentNumber INTEGER NOT NULL,
                        dueDate DATE NOT NULL,
                        totalAmount DOUBLE NOT NULL CHECK (totalAmount > 0),
                        principalAmount DOUBLE NOT NULL CHECK (principalAmount >= 0),
                        interestAmount DOUBLE NOT NULL CHECK (interestAmount >= 0),
                        remainingBalance DOUBLE NOT NULL CHECK (remainingBalance >= 0),
                        status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (loanId) REFERENCES loans(id) ON DELETE CASCADE,
                        UNIQUE(loanId, paymentNumber)
                    )
                """);
            }

            // Crea indici per le nuove tabelle
            if (!hasLoansTable || !hasPaymentsTable) {
                System.out.println("🚀 Creazione indici prestiti...");
                st.execute("CREATE INDEX IF NOT EXISTS idx_loans_status ON loans(status)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_loans_type ON loans(type)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_loans_start_date ON loans(startDate)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_loans_category ON loans(category)");

                st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_loan_id ON loan_payments(loanId)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_due_date ON loan_payments(dueDate)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_status ON loan_payments(status)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_due_status ON loan_payments(dueDate, status)");
                st.execute("CREATE INDEX IF NOT EXISTS idx_loan_payments_loan_payment_num ON loan_payments(loanId, paymentNumber)");
            }

            System.out.println("✅ Aggiornamento database completato");

        } catch (SQLException e) {
            System.err.println("❌ Errore aggiornamento database: " + e.getMessage());
            throw new RuntimeException("Impossibile aggiornare il database", e);
        }
    }
}