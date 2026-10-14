import java.time.LocalDate;

/** Abstract parent of Income and Expense (Abstraction + Inheritance). */
public abstract class Transaction {
    private String description;
    private double amount;
    private LocalDate date;
    private Category category;

    protected Transaction(String description, double amount, LocalDate date, Category category) {
        setDescription(description);
        setAmount(amount);
        setDate(date);
        setCategory(category);
    }

    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public Category getCategory() { return category; }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("Description cannot be empty.");
        this.description = description.trim().replace(",", " ");
    }

    public void setAmount(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");
        this.amount = amount;
    }

    public void setDate(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date is required.");
        this.date = date;
    }

    public void setCategory(Category category) {
        if (category == null) throw new IllegalArgumentException("Category is required.");
        this.category = category;
    }

    /** Polymorphic method: returns the new balance after this transaction. */
    public abstract double applyToBalance(double balance);

    /** "Income" or "Expense". */
    public abstract String getType();

    /** Amount with a sign, used for display. */
    public abstract String getSignedAmount();
}