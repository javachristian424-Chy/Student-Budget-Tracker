import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;

/** Dashboard window. Shows the data and passes user actions to BudgetManager. */
public class MainGUI extends JFrame {
    private static final Color NAVY = new Color(31, 56, 100);
    private static final Color RED = new Color(176, 58, 46);
    private static final Color GREEN = new Color(39, 125, 69);

    private final BudgetManager manager;
    private final JLabel budgetLabel = new JLabel();
    private final JLabel spentLabel = new JLabel();
    private final JLabel remainingLabel = new JLabel();
    private final JProgressBar usedBar = new JProgressBar(0, 100);
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"Date", "Description", "Category", "Amount"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable table = new JTable(tableModel);

    public MainGUI(BudgetManager manager) {
        super("Student Budget Tracker");
        this.manager = manager;
        buildUI();
        refresh();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { saveAndExit(); }
        });
        setSize(820, 560);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("  Student Budget Tracker  -  " + manager.getStudent().getName()
                + " (" + manager.getStudent().getStudentId() + ")");
        title.setOpaque(true);
        title.setBackground(NAVY);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.add(makeCard("Total Budget", budgetLabel, NAVY));
        cards.add(makeCard("Total Spent", spentLabel, RED));
        cards.add(makeCard("Remaining", remainingLabel, GREEN));
        center.add(cards, BorderLayout.NORTH);

        table.setRowHeight(24);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        usedBar.setStringPainted(true);
        center.add(usedBar, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        JButton addIncome = new JButton("+ Add Income");
        JButton addExpense = new JButton("+ Add Expense");
        JButton delete = new JButton("Delete Selected");
        addIncome.addActionListener(e -> openTransactionDialog(true));
        addExpense.addActionListener(e -> openTransactionDialog(false));
        delete.addActionListener(e -> deleteSelected());
        buttons.add(addIncome);
        buttons.add(addExpense);
        buttons.add(delete);
        add(buttons, BorderLayout.SOUTH);
    }

    private JPanel makeCard(String caption, JLabel value, Color color) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 215)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel c = new JLabel(caption);
        c.setForeground(Color.GRAY);
        value.setForeground(color);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));
        p.add(c);
        p.add(value);
        return p;
    }

    private void refresh() {
        budgetLabel.setText(String.format("P %,.2f", manager.getStudent().getAllowance() + manager.getTotalIncome()));
        spentLabel.setText(String.format("P %,.2f", manager.getTotalExpenses()));
        remainingLabel.setText(String.format("P %,.2f", manager.getRemainingBalance()));
        usedBar.setValue(manager.getPercentUsed());
        usedBar.setString("Budget used: " + manager.getPercentUsed() + "%");

        tableModel.setRowCount(0);
        for (Transaction t : manager.getTransactions())
            tableModel.addRow(new Object[]{t.getDate(), t.getDescription(),
                    t.getCategory().getName(), t.getSignedAmount()});
    }

    private void openTransactionDialog(boolean isIncome) {
        JTextField desc = new JTextField(15);
        JTextField amount = new JTextField(15);
        JTextField date = new JTextField(LocalDate.now().toString(), 15);
        JComboBox<Category> cat = new JComboBox<>();
        for (Category c : manager.getCategories()) {
            boolean incomeCat = c.getName().equals("Income");
            if (incomeCat == isIncome) cat.addItem(c);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Description:")); form.add(desc);
        form.add(new JLabel("Category:"));    form.add(cat);
        form.add(new JLabel("Amount (PHP):")); form.add(amount);
        form.add(new JLabel("Date (YYYY-MM-DD):")); form.add(date);

        int result = JOptionPane.showConfirmDialog(this, form,
                isIncome ? "Add Income" : "Add Expense",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            double amt = Double.parseDouble(amount.getText().trim());
            LocalDate d = LocalDate.parse(date.getText().trim());
            Category c = (Category) cat.getSelectedItem();
            Transaction t = isIncome ? new Income(desc.getText(), amt, d, c)
                                     : new Expense(desc.getText(), amt, d, c);
            manager.addTransaction(t);
            refresh();
            if (!isIncome) {
                String warning = manager.checkLimit(c);
                if (warning != null)
                    JOptionPane.showMessageDialog(this, warning, "Budget Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            error("Please enter a valid number for the amount.");
        } catch (java.time.format.DateTimeParseException ex) {
            error("Please enter the date as YYYY-MM-DD.");
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { error("Select a transaction to delete."); return; }
        manager.removeTransaction(row);
        refresh();
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void saveAndExit() {
        try { manager.save(); }
        catch (IOException e) { error("Could not save data: " + e.getMessage()); }
        dispose();
        System.exit(0);
    }

    // ---------- program entry point ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BudgetManager manager = BudgetManager.load();
            if (manager == null) {
                JTextField name = new JTextField(15);
                JTextField id = new JTextField(15);
                JTextField allowance = new JTextField("5000", 15);
                JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
                form.add(new JLabel("Name:")); form.add(name);
                form.add(new JLabel("Student ID:")); form.add(id);
                form.add(new JLabel("Starting allowance (PHP):")); form.add(allowance);
                while (manager == null) {
                    int r = JOptionPane.showConfirmDialog(null, form, "Student Setup",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (r != JOptionPane.OK_OPTION) System.exit(0);
                    try {
                        manager = new BudgetManager(new Student(name.getText(), id.getText(),
                                Double.parseDouble(allowance.getText().trim())));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Invalid input: " + ex.getMessage());
                    }
                }
            }
            new MainGUI(manager).setVisible(true);
        });
    }
}