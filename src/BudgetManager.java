import java.io.*;
import java.time.LocalDate;
import java.util.*;

/** Keeps all transactions and categories, and does the budget computations. */
public class BudgetManager {
    private static final String DATA_FILE = "budget_data.csv";

    private Student student;
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Category> categories = new LinkedHashMap<>();

    public BudgetManager(Student student) {
        this.student = student;
        addCategory(new Category("Income", 0));
        addCategory(new Category("Food", 1500));
        addCategory(new Category("Transport", 800));
        addCategory(new Category("School", 1000));
        addCategory(new Category("Utilities", 500));
        addCategory(new Category("Others", 0));
    }

    public Student getStudent() { return student; }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public Collection<Category> getCategories() { return categories.values(); }
    public Category getCategory(String name) { return categories.get(name); }

    public void addCategory(Category c) { categories.put(c.getName(), c); }

    public void addTransaction(Transaction t) { transactions.add(t); }

    public boolean removeTransaction(int index) {
        if (index < 0 || index >= transactions.size()) return false;
        transactions.remove(index);
        return true;
    }

    public double getRemainingBalance() {
        double balance = student.getAllowance();
        for (Transaction t : transactions) balance = t.applyToBalance(balance);
        return balance;
    }

    public double getTotalIncome() { return total("Income"); }
    public double getTotalExpenses() { return total("Expense"); }

    private double total(String type) {
        double sum = 0;
        for (Transaction t : transactions) if (t.getType().equals(type)) sum += t.getAmount();
        return sum;
    }

    public double getSpentInCategory(Category c) {
        double sum = 0;
        for (Transaction t : transactions)
            if (t.getType().equals("Expense") && t.getCategory().getName().equals(c.getName()))
                sum += t.getAmount();
        return sum;
    }

    /** Percentage of the total money available (allowance + income) that has been spent. */
    public int getPercentUsed() {
        double available = student.getAllowance() + getTotalIncome();
        if (available <= 0) return 0;
        return (int) Math.min(100, Math.round(getTotalExpenses() / available * 100));
    }

    /** Returns a warning message if the category limit is reached, otherwise null. */
    public String checkLimit(Category c) {
        if (!c.hasLimit()) return null;
        double spent = getSpentInCategory(c);
        if (spent > c.getLimit())
            return String.format("You are over your %s budget by P %,.2f.", c.getName(), spent - c.getLimit());
        if (spent >= c.getLimit() * 0.8)
            return String.format("You have used %d%% of your %s budget.",
                    Math.round(spent / c.getLimit() * 100), c.getName());
        return null;
    }

    // ---------- saving and loading ----------
    public void save() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(DATA_FILE))) {
            out.println(student.getName().replace(",", " ") + "," + student.getStudentId().replace(",", " ")
                    + "," + student.getAllowance());
            for (Transaction t : transactions)
                out.println(t.getType() + "," + t.getDescription() + "," + t.getAmount() + ","
                        + t.getDate() + "," + t.getCategory().getName());
        }
    }

    /** Loads saved data, or returns null if no save file exists. */
    public static BudgetManager load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return null;
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            String[] s = in.readLine().split(",");
            BudgetManager m = new BudgetManager(new Student(s[0], s[1], Double.parseDouble(s[2])));
            String line;
            while ((line = in.readLine()) != null) {
                String[] p = line.split(",");
                Category c = m.getCategory(p[4]);
                if (c == null) { c = new Category(p[4], 0); m.addCategory(c); }
                double amt = Double.parseDouble(p[2]);
                LocalDate d = LocalDate.parse(p[3]);
                m.addTransaction(p[0].equals("Income") ? new Income(p[1], amt, d, c)
                                                       : new Expense(p[1], amt, d, c));
            }
            return m;
        } catch (Exception e) {
            return null; // corrupted file: start fresh
        }
    }
}