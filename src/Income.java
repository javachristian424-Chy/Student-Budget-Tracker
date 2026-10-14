import java.time.LocalDate;

/** Money received (allowance, salary, gift). */
public class Income extends Transaction {
    public Income(String description, double amount, LocalDate date, Category category) {
        super(description, amount, date, category);
    }

    @Override public double applyToBalance(double balance) { return balance + getAmount(); }
    @Override public String getType() { return "Income"; }
    @Override public String getSignedAmount() { return String.format("+ P %,.2f", getAmount()); }
}