import java.util.ArrayList;
import java.util.List;

public class Book {
    private long bookId;
    private String title;
    private String author;
    private int availableCopies;
    private double buyPrice;

    public Book(long bookId, String title, String author, int availableCopies, double buyPrice) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.availableCopies = availableCopies;
        this.buyPrice = buyPrice;
    }

    public long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getAvailableCopies() { return availableCopies; }
    public double getBuyPrice() { return buyPrice; }
    public boolean isAvailable() { return availableCopies > 0; }

    public void decrementCopies() { if (availableCopies > 0) availableCopies--; }
    public void incrementCopies() { availableCopies++; }

    /** A small in-memory catalog used by the GUI when no database is available. */
    public static List<Book> sampleCatalog() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(1, "To Kill a Mockingbird", "Harper Lee", 3, 12.99));
        books.add(new Book(2, "1984", "George Orwell", 2, 10.99));
        books.add(new Book(3, "Pride and Prejudice", "Jane Austen", 4, 9.99));
        books.add(new Book(4, "The Great Gatsby", "F. Scott Fitzgerald", 1, 11.50));
        books.add(new Book(5, "Moby-Dick", "Herman Melville", 2, 13.75));
        books.add(new Book(6, "The Catcher in the Rye", "J.D. Salinger", 3, 10.25));
        return books;
    }

    @Override
    public String toString() {
        return title + "  —  " + author + "   ($" + buyPrice + " to buy, " + availableCopies + " available)";
    }
}