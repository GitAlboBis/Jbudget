package it.unicam.cs.mpgc.jbudget123718.jbudget.config;

import org.h2.jdbcx.JdbcDataSource;
import javax.sql.DataSource;

/**
 * Factory per la creazione del DataSource H2.
 * Configurazione centralizzata per la connessione al database.
 */
public class DataSourceFactory {

    private static final String DATABASE_URL = "jdbc:h2:./data/jbudget;AUTO_SERVER=TRUE";
    private static final String DATABASE_USER = "sa";
    private static final String DATABASE_PASSWORD = "";

    /**
     * Crea e configura il DataSource H2 per l'applicazione.
     *
     * @return DataSource configurato per H2
     */
    public static DataSource create() {
        try {
            JdbcDataSource dataSource = new JdbcDataSource();

            // Configurazione connessione
            dataSource.setURL(DATABASE_URL);
            dataSource.setUser(DATABASE_USER);
            dataSource.setPassword(DATABASE_PASSWORD);

            // Configurazioni aggiuntive per performance e sicurezza
            dataSource.setDescription("JBudget H2 Database");

            System.out.println("✅ DataSource H2 creato: " + DATABASE_URL);

            return dataSource;

        } catch (Exception e) {
            System.err.println("❌ Errore creazione DataSource: " + e.getMessage());
            throw new RuntimeException("Impossibile creare il DataSource H2", e);
        }
    }

    /**
     * Crea un DataSource per i test con database in memoria.
     *
     * @return DataSource per test
     */
    public static DataSource createTestDataSource() {
        try {
            JdbcDataSource dataSource = new JdbcDataSource();

            // Database in memoria per test
            dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            dataSource.setUser("sa");
            dataSource.setPassword("");

            System.out.println("✅ DataSource H2 Test creato (in memoria)");

            return dataSource;

        } catch (Exception e) {
            System.err.println("❌ Errore creazione DataSource test: " + e.getMessage());
            throw new RuntimeException("Impossibile creare il DataSource H2 per test", e);
        }
    }

    /**
     * Crea un DataSource con configurazioni personalizzate.
     *
     * @param databasePath il percorso del database
     * @param user l'username
     * @param password la password
     * @return DataSource configurato
     */
    public static DataSource createCustom(String databasePath, String user, String password) {
        try {
            JdbcDataSource dataSource = new JdbcDataSource();

            String url = "jdbc:h2:" + databasePath + ";AUTO_SERVER=TRUE";
            dataSource.setURL(url);
            dataSource.setUser(user);
            dataSource.setPassword(password);

            System.out.println("✅ DataSource H2 personalizzato creato: " + url);

            return dataSource;

        } catch (Exception e) {
            System.err.println("❌ Errore creazione DataSource personalizzato: " + e.getMessage());
            throw new RuntimeException("Impossibile creare il DataSource H2 personalizzato", e);
        }
    }
}