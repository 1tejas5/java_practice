import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * TOPIC: JDBC end-to-end - DDL, batch insert, PreparedStatement, ResultSet, SQL injection demo,
 *        transactions with commit/rollback.
 *
 * The JDK has no built-in database, so this needs a JDBC driver on the classpath.
 * Easiest: the H2 in-memory database (a single jar, no install):
 *   1. Download h2-<version>.jar from https://h2database.com (or Maven Central: com.h2database:h2)
 *   2. java -cp h2-2.3.232.jar code/13_more_apis/JdbcDemo.java
 *
 * Without a driver, the program explains what to do and exits cleanly.
 * Other DBs: pass a URL, e.g.  java -cp mysql-connector-j.jar code/13_more_apis/JdbcDemo.java jdbc:mysql://localhost/test user pass
 */
public class JdbcDemo {

    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "jdbc:h2:mem:bank;DB_CLOSE_DELAY=-1";
        String user = args.length > 1 ? args[1] : "sa";
        String pass = args.length > 2 ? args[2] : "";

        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected to " + con.getMetaData().getDatabaseProductName());
            createSchema(con);
            batchInsert(con);
            query(con, 10_000);
            sqlInjection(con);
            transfer(con, 1, 2, 500);
            transfer(con, 1, 2, 999_999);   // fails -> rollback
            query(con, 0);
        } catch (SQLException e) {
            if (e.getMessage().contains("No suitable driver")) {
                System.out.println("""
                        No JDBC driver found for %s

                        To run this demo with the H2 in-memory database:
                          1. Download the H2 jar: https://h2database.com  (Maven: com.h2database:h2)
                          2. java -cp h2-<version>.jar code/13_more_apis/JdbcDemo.java

                        Meanwhile, read this file - the code shows every JDBC step with comments.
                        """.formatted(url));
            } else {
                throw e;
            }
        }
    }

    static void createSchema(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE accounts (id INT PRIMARY KEY, owner VARCHAR(50), balance DECIMAL(12,2))");
        }
        System.out.println("table created");
    }

    static void batchInsert(Connection con) throws SQLException {
        String sql = "INSERT INTO accounts (id, owner, balance) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            Object[][] rows = {{1, "Asha", 25_000}, {2, "Ravi", 8_000}, {3, "Meera", 52_000}};
            for (Object[] r : rows) {
                ps.setInt(1, (Integer) r[0]);          // parameter indexes are 1-based
                ps.setString(2, (String) r[1]);
                ps.setDouble(3, (Integer) r[2]);
                ps.addBatch();
            }
            int[] counts = ps.executeBatch();
            System.out.println("batch inserted " + counts.length + " rows");
        }
    }

    static void query(Connection con, double minBalance) throws SQLException {
        String sql = "SELECT id, owner, balance FROM accounts WHERE balance > ? ORDER BY id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, minBalance);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("accounts with balance > " + minBalance + ":");
                while (rs.next()) {                    // cursor starts before the first row
                    System.out.printf("  #%d %-6s %10.2f%n", rs.getInt("id"), rs.getString("owner"), rs.getDouble(3));
                }
            }
        }
    }

    static void sqlInjection(Connection con) throws SQLException {
        String evilInput = "' OR '1'='1";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM accounts WHERE owner = '" + evilInput + "'")) {
            rs.next();
            System.out.println("Statement + concatenation with " + evilInput + " -> matched " + rs.getInt(1) + " rows (INJECTED!)");
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM accounts WHERE owner = ?")) {
            ps.setString(1, evilInput);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("PreparedStatement with the same input -> matched " + rs.getInt(1) + " rows (safe)");
            }
        }
    }

    static void transfer(Connection con, int from, int to, double amount) throws SQLException {
        con.setAutoCommit(false);                       // begin a transaction
        try (PreparedStatement debit = con.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE id = ?");
             PreparedStatement credit = con.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id = ?");
             PreparedStatement check = con.prepareStatement("SELECT balance FROM accounts WHERE id = ?")) {
            debit.setDouble(1, amount);
            debit.setInt(2, from);
            debit.executeUpdate();
            check.setInt(1, from);
            try (ResultSet rs = check.executeQuery()) {
                rs.next();
                if (rs.getDouble(1) < 0) throw new SQLException("insufficient funds for transfer of " + amount);
            }
            credit.setDouble(1, amount);
            credit.setInt(2, to);
            credit.executeUpdate();
            con.commit();                               // both updates become permanent together
            System.out.println("transfer of " + amount + " committed");
        } catch (SQLException e) {
            con.rollback();                             // undo the debit
            System.out.println("transfer of " + amount + " ROLLED BACK: " + e.getMessage());
        } finally {
            con.setAutoCommit(true);
        }
    }
}
