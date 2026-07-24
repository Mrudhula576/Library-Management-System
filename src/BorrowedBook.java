import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BorrowedBook {
    private String title;
    private LocalDate dueDate;      // null if purchased outright
    private boolean purchased;
    private double amountCharged;

    public BorrowedBook(String title, LocalDate dueDate, boolean purchased, double amountCharged) {
        this.title = title;
        this.dueDate = dueDate;
        this.purchased = purchased;
        this.amountCharged = amountCharged;
    }

    public String getTitle() { return title; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isPurchased() { return purchased; }
    public double getAmountCharged() { return amountCharged; }

    public long daysRemaining() {
        if (dueDate == null) return -1;
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    @Override
    public String toString() {
        if (purchased) {
            return title + "  (purchased — yours to keep)";
        }
        long days = daysRemaining();
        return title + "  — due " + dueDate + "  (" + days + (days == 1 ? " day" : " days") + " left)";
    }
}