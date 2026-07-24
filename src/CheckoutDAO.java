import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CheckoutDAO {

    /** Active (not yet returned) rentals for a user */
    public List<BorrowedBook> getActiveCheckouts(long userId) throws SQLException {
        String sql = "SELECT c.\"CHECKOUT_ID\", b.\"TITLE\", c.\"DUE_DATE\", c.\"ACTION\", c.\"AMOUNT_CHARGED\" " +
                     "FROM \"CHECKOUT\" c JOIN \"BOOK\" b ON c.\"BOOK_ID\" = b.\"BOOK_ID\" " +
                     "WHERE c.\"USER_ID\" = ? AND c.\"RETURN_DATE\" IS NULL";
        List<BorrowedBook> result = new ArrayList<>();
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date due = rs.getDate("DUE_DATE");
                    boolean purchased = "buy".equals(rs.getString("action"));
                    result.add(new BorrowedBook(
                        rs.getString("TITLE"),
                        due == null ? null : due.toLocalDate(),
                        purchased,
                        rs.getDouble("amount_charged")
                    ));
                }
            }
        }
        return result;
    }

    /** Count rentals (not purchases) this calendar month — for the 5-free-books rule */
    public int countRentalsThisMonth(long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"CHECKOUT\" " +
                     "WHERE \"USER_ID\" = ? AND \"ACTION\" = 'rent' " +
                     "AND date_trunc('month', \"CHECKOUT_DATE\") = date_trunc('month', CURRENT_DATE)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void rentBook(long userId, long bookId, double amountCharged) throws SQLException {
        String sql = "INSERT INTO \"CHECKOUT\" (\"BOOK_ID\", \"USER_ID\", \"CHECKOUT_DATE\", \"DUE_DATE\", \"ACTION\", \"AMOUNT_CHARGED\") " +
                     "VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + INTERVAL '30 days', 'rent', ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.setLong(2, userId);
            stmt.setDouble(3, amountCharged);
            stmt.executeUpdate();
        }
    }

    public void buyBook(long userId, long bookId, double price) throws SQLException {
        String sql = "INSERT INTO \"CHECKOUT\" (\"BOOK_ID\", \"USER_ID\", \"CHECKOUT_DATE\", \"ACTION\", \"AMOUNT_CHARGED\") " +
                     "VALUES (?, ?, CURRENT_DATE, 'buy', ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.setLong(2, userId);
            stmt.setDouble(3, price);
            stmt.executeUpdate();
        }
    }

    public boolean returnBook(long userId, String title) throws SQLException {
        String sql = "UPDATE \"CHECKOUT\" c SET \"RETURN_DATE\" = CURRENT_DATE " +
                     "FROM \"BOOK\" b WHERE c.\"BOOK_ID\" = b.\"BOOK_ID\" " +
                     "AND c.\"USER_ID\" = ? AND b.\"TITLE\" = ? " +
                     "AND c.\"RETURN_DATE\" IS NULL AND c.\"ACTION\" = 'rent'";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, title);
            return stmt.executeUpdate() > 0;
        }
    }

    /** Needed so returnBook can tell BookDAO which book_id to increment */
    public Long getBookIdForActiveCheckout(long userId, String title) throws SQLException {
        String sql = "SELECT b.\"BOOK_ID\" FROM \"CHECKOUT\" c JOIN \"BOOK\" b ON c.\"BOOK_ID\" = b.\"BOOK_ID\" " +
                     "WHERE c.\"USER_ID\" = ? AND b.\"TITLE\" = ? AND c.\"RETURN_DATE\" IS NULL";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, title);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return null;
    }
}