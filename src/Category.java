/** A spending group such as Food or Transport, with an optional budget limit. */
public class Category {
    private String name;
    private double limit; // 0 means "no limit"

    public Category(String name, double limit) {
        setName(name);
        setLimit(limit);
    }

    public String getName() { return name; }
    public double getLimit() { return limit; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Category name cannot be empty.");
        this.name = name.trim();
    }

    public void setLimit(double limit) {
        if (limit < 0)
            throw new IllegalArgumentException("Limit cannot be negative.");
        this.limit = limit;
    }

    public boolean hasLimit() { return limit > 0; }

    @Override
    public String toString() { return name; }
}