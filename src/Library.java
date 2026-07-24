/* Name: Mrudhula Malipeddi
 * Professor: Dr. Wang
 * Assignment: Final Project Library
 * Last edited date: 12-13-2024
 */
import java.time.LocalDate;
import java.util.List;

//Superclass Library
class Library {
    //data fields
    private String membershipType;
    private long membershipID;
    private String memberName;
    private LocalDate bookExpiry;
    private String password;

    private double amountOwed;
    private double amountPaid;

    protected static final int FREE_BOOKS_PER_MONTH = 5;
    protected static final double EXTRA_BOOK_FEE = 2.0;

    private CheckoutDAO checkoutDAO = new CheckoutDAO();
    private BookDAO bookDAO = new BookDAO();
    private MemberDAO memberDAO = new MemberDAO();

    //Constructor
    public Library(String membershipType, long membershipID, String memberName, LocalDate bookExpiry,
                   String password, double amountOwed, double amountPaid){
        this.membershipType = membershipType;
        this.membershipID = membershipID;
        this.memberName = memberName;
        this.bookExpiry = bookExpiry;
        this.password = password;
        this.amountOwed = amountOwed;
        this.amountPaid = amountPaid;
    }

    //methods
    public LocalDate getbookExpiry(){
        return this.bookExpiry;
    }

    public String membershipType(){
        return this.membershipType;
    }

    public long membershipID(){
        return this.membershipID;
    }

    public String memberName(){
        return this.memberName;
    }

    public boolean checkPassWord(String userInputpw) {
        return this.password.equals(userInputpw);
    }

    public double getAmountOwed() { return amountOwed; }
    public double getAmountPaid() { return amountPaid; }

    public List<BorrowedBook> getBorrowedBooks() throws java.sql.SQLException {
        return checkoutDAO.getActiveCheckouts(membershipID);
    }

    /** Borrow (rent) a book — checks the 5-free-per-month rule against real DB history */
    public String borrowBook(Book book) throws java.sql.SQLException {
        if (book == null || !book.isAvailable()) {
            return "Sorry, that book is not available right now.";
        }
        int rentedThisMonth = checkoutDAO.countRentalsThisMonth(membershipID);
        double fee = 0.0;
        if (rentedThisMonth >= FREE_BOOKS_PER_MONTH) {
            fee = EXTRA_BOOK_FEE;
            amountOwed += fee;
            memberDAO.updateBalances(membershipID, amountOwed, amountPaid);
        }
        checkoutDAO.rentBook(membershipID, book.getBookId(), fee);
        bookDAO.decrementCopies(book.getBookId());
        return fee == 0.0
            ? "Borrowed \"" + book.getTitle() + "\" — free (within your monthly allowance)."
            : "Borrowed \"" + book.getTitle() + "\" — $" + fee + " added to your balance (past your 5 free books).";
    }

    /** Buy a book outright */
    public String buyBook(Book book) throws java.sql.SQLException {
        if (book == null || !book.isAvailable()) {
            return "Sorry, that book is not available right now.";
        }
        checkoutDAO.buyBook(membershipID, book.getBookId(), book.getBuyPrice());
        bookDAO.decrementCopies(book.getBookId());
        amountPaid += book.getBuyPrice();
        memberDAO.updateBalances(membershipID, amountOwed, amountPaid);
        return "Purchased \"" + book.getTitle() + "\" for $" + book.getBuyPrice() + ".";
    }

    /** Return a previously rented book */
    public boolean returnBook(String title) throws java.sql.SQLException {
        Long bookId = checkoutDAO.getBookIdForActiveCheckout(membershipID, title);
        if (bookId == null) return false;
        boolean success = checkoutDAO.returnBook(membershipID, title);
        if (success) {
            bookDAO.incrementCopies(bookId);
        }
        return success;
    }

    public void payBalance(double amount) throws java.sql.SQLException {
        amountPaid += amount;
        amountOwed = Math.max(0, amountOwed - amount);
        memberDAO.updateBalances(membershipID, amountOwed, amountPaid);
    }
}

//Subclass Regular membership
class Regular extends Library{
    private int monthlyFee;

	public Regular(String membershipType, long membershipID, String memberName, LocalDate bookExpiry,
                   String password, int monthlyFee, double amountOwed, double amountPaid){
        super(membershipType, membershipID, memberName, bookExpiry, password, amountOwed, amountPaid);
        this.monthlyFee = monthlyFee;
    }

    public int getMonthlyFee() {
        return this.monthlyFee;
    }
}