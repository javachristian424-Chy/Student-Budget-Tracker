
package com.example.Student_Budget_Tracker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    private final CsvStorageService storage;
    private static final double ALLOWANCE = 5000.00;

    public HomeController(CsvStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<String[]> transactions = storage.loadAll();

        double income = 0;
        double expenses = 0;

        for (String[] row : transactions) {
            try {
                double amount = Double.parseDouble(row[2]);

                if (row[0].equals("INCOME")) {
                    income += amount;
                } else if (row[0].equals("EXPENSE")) {
                    expenses += amount;
                }
            } catch (NumberFormatException e) {
                // Ignore invalid amounts.
            }
        }

        model.addAttribute("allowance", ALLOWANCE);
        model.addAttribute("income", income);
        model.addAttribute("expenses", expenses);
        model.addAttribute(
                "balance", ALLOWANCE + income - expenses);
        model.addAttribute("transactions", transactions);

        return "index";
    }

    @PostMapping("/add")
    public String addTransaction(
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam double amount,
            @RequestParam String date,
            RedirectAttributes redirectAttributes) {

        if (!validTransaction(type, description, amount, date)) {
            redirectAttributes.addFlashAttribute(
                    "error", "Please enter valid transaction details.");
            return "redirect:/";
        }

        storage.save(type, description.trim(), amount, date);

        redirectAttributes.addFlashAttribute(
                "success", "Transaction added successfully!");

        return "redirect:/";
    }

    @GetMapping("/edit/{index}")
    public String showEditForm(
            @PathVariable int index, Model model,
            RedirectAttributes redirectAttributes) {

        List<String[]> transactions = storage.loadAll();

        if (index < 0 || index >= transactions.size()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Transaction not found.");
            return "redirect:/";
        }

        model.addAttribute("transaction", transactions.get(index));
        model.addAttribute("index", index);

        return "edit";
    }

    @PostMapping("/edit/{index}")
    public String updateTransaction(
            @PathVariable int index,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam double amount,
            @RequestParam String date,
            RedirectAttributes redirectAttributes) {

        if (!validTransaction(type, description, amount, date)) {
            redirectAttributes.addFlashAttribute(
                    "error", "Please enter valid transaction details.");
            return "redirect:/edit/" + index;
        }

        try {
            storage.update(index, type, description.trim(), amount, date);

            redirectAttributes.addFlashAttribute(
                    "success", "Transaction updated successfully!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Transaction not found.");
        }

        return "redirect:/";
    }

    @PostMapping("/delete/{index}")
    public String deleteTransaction(
            @PathVariable int index,
            RedirectAttributes redirectAttributes) {

        try {
            storage.delete(index);

            redirectAttributes.addFlashAttribute(
                    "success", "Transaction deleted successfully!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Transaction not found.");
        }

        return "redirect:/";
    }

    private boolean validTransaction(
            String type, String description,
            double amount, String date) {

        if ((!type.equals("INCOME") && !type.equals("EXPENSE"))
                || description == null
                || description.isBlank()
                || description.length() > 200
                || !Double.isFinite(amount)
                || amount <= 0
                || date == null) {
            return false;
        }

        try {
            LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
