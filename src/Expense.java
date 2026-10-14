import java.time.LocalDate;

/** Money spent. */
public class Expense extends Transaction {
    public Expense(String description, double amount, LocalDate date, Category category) {
        super(description, amount, date, category);
    }

    @Override public double applyToBalance(double balance) { return balance - getAmount(); }
    @Override public String getType() { return "Expense"; }
    @Override public String getSignedAmount() { return String.format("- P %,.2f", getAmount()); }
}