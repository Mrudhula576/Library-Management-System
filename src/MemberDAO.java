import java.sql.*;

public class MemberDAO {

    /** Returns a populated Library object if credentials match, else null */
    public Library login(String username, String password) throws SQLException {
        String sql = "SELECT \"USER_ID\", \"FNAME\", \"LNAME\", \"MEM_TYPE\", \"DUE_DATE\", " +
                     "\"AMOUNT_OWED\", \"AMOUNT_PAID\", \"MONTHLY_FEE\" " +
                     "FROM \"USER\" WHERE \"USERNAME\" = ? AND \"PASSWORD\" = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date dueDate = rs.getDate("DUE_DATE");
                    return new Regular(
                        rs.getString("MEM_TYPE"),
                        rs.getLong("USER_ID"),
                        rs.getString("FNAME") + " " + (rs.getString("LNAME") == null ? "" : rs.getString("LNAME")),
                        dueDate == null ? null : dueDate.toLocalDate(),
                        password,
                        (int) rs.getDouble("monthly_fee"),
                        rs.getDouble("amount_owed"),
                        rs.getDouble("amount_paid")
                    );
                }
            }
        }
        return null;
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM \"USER\" WHERE \"USERNAME\" = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void signup(String fname, String lname, String memType, String username, String password) throws SQLException {
        String sql = "INSERT INTO \"USER\" (\"FNAME\", \"LNAME\", \"MEM_TYPE\", \"USER_ID\", \"USERNAME\", \"PASSWORD\", \"MONTHLY_FEE\") " +
                     "VALUES (?, ?, ?, ?, ?, ?, 5)";
        long newId = (long) (Math.random() * 1_000_000);
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fname);
            stmt.setString(2, lname);
            stmt.setString(3, memType);
            stmt.setLong(4, newId);
            stmt.setString(5, username);
            stmt.setString(6, password);
            stmt.executeUpdate();
        }
    }

    public void updateBalances(long userId, double owed, double paid) throws SQLException {
        String sql = "UPDATE \"USER\" SET \"AMOUNT_OWED\" = ?, \"AMOUNT_PAID\" = ? WHERE \"USER_ID\" = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, owed);
            stmt.setDouble(2, paid);
            stmt.setLong(3, userId);
            stmt.executeUpdate();
        }
    }
}