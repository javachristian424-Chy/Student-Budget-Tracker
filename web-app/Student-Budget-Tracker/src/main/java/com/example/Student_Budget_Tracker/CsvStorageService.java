
package com.example.Student_Budget_Tracker;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvStorageService {

    private final Path filePath =
            Path.of("data", "budget_data.csv");

    public CsvStorageService() {
        try {
            Files.createDirectories(filePath.getParent());

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create CSV file.", e);
        }
    }

    public synchronized void save(
            String type,
            String description,
            double amount,
            String date) {

        try {
            description = description
                    .replace("|", "/")
                    .replace("\n", " ")
                    .replace("\r", " ");

            String line = type + "|"
                    + description + "|"
                    + amount + "|"
                    + date;

            List<String> lines =
                    new ArrayList<>(Files.readAllLines(filePath));

            lines.add(line);
            Files.write(filePath, lines);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not save transaction.", e);
        }
    }

    public synchronized List<String[]> loadAll() {
        List<String[]> transactions = new ArrayList<>();

        try {
            for (String line : Files.readAllLines(filePath)) {
                String[] parts = line.split("\\|", -1);

                if (parts.length == 4) {
                    transactions.add(parts);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read transactions.", e);
        }

        return transactions;
    }

    public synchronized void update(
            int index,
            String type,
            String description,
            double amount,
            String date) {

        List<String> lines;

        try {
            lines = new ArrayList<>(Files.readAllLines(filePath));

            if (index < 0 || index >= lines.size()) {
                throw new IllegalArgumentException(
                        "Transaction not found.");
            }

            description = description
                    .replace("|", "/")
                    .replace("\n", " ")
                    .replace("\r", " ");

            String updatedLine = type + "|"
                    + description + "|"
                    + amount + "|"
                    + date;

            lines.set(index, updatedLine);
            Files.write(filePath, lines);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not update transaction.", e);
        }
    }

    public synchronized void delete(int index) {
        try {
            List<String> lines =
                    new ArrayList<>(Files.readAllLines(filePath));

            if (index < 0 || index >= lines.size()) {
                throw new IllegalArgumentException(
                        "Transaction not found.");
            }

            lines.remove(index);
            Files.write(filePath, lines);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not delete transaction.", e);
        }
    }
}
