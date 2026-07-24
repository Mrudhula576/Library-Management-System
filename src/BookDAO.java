import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<Book> getAvailableBooks() throws SQLException {
        String sql = "SELECT \"BOOK_ID\", \"TITLE\", \"AUTHOR\", \"AVAILABLE_COPIES\", \"BUY_PRICE\" " +
                     "FROM \"BOOK\" WHERE \"AVAILABLE_COPIES\" > 0 ORDER BY \"TITLE\"";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                books.add(new Book(
                    rs.getLong("BOOK_ID"),
                    rs.getString("TITLE"),
                    rs.getString("AUTHOR"),
                    rs.getInt("available_copies"),
                    rs.getDouble("buy_price")
                ));
            }
        }
        return books;
    }

    public Book getBookById(long bookId) throws SQLException {
        String sql = "SELECT \"BOOK_ID\", \"TITLE\", \"AUTHOR\", \"AVAILABLE_COPIES\", \"BUY_PRICE\" " +
                     "FROM \"BOOK\" WHERE \"BOOK_ID\" = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                        rs.getLong("BOOK_ID"),
                        rs.getString("TITLE"),
                        rs.getString("AUTHOR"),
                        rs.getInt("available_copies"),
                        rs.getDouble("buy_price")
                    );
                }
            }
        }
        return null;
    }

    public void decrementCopies(long bookId) throws SQLException {
        String sql = "UPDATE \"BOOK\" SET \"AVAILABLE_COPIES\" = \"AVAILABLE_COPIES\" - 1 " +
                     "WHERE \"BOOK_ID\" = ? AND \"AVAILABLE_COPIES\" > 0";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.executeUpdate();
        }
    }

    public void incrementCopies(long bookId) throws SQLException {
        String sql = "UPDATE \"BOOK\" SET \"AVAILABLE_COPIES\" = \"AVAILABLE_COPIES\" + 1 WHERE \"BOOK_ID\" = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.executeUpdate();
        }
    }
}