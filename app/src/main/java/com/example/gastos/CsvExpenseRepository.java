package com.example.gastos;

import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Guarda cada gasto como una línea: userId,fecha,monto,concepto */
public class CsvExpenseRepository implements ExpenseRepository {
    private final File file;

    public CsvExpenseRepository(Context c) {
        file = new File(c.getFilesDir(), "gastos.csv");
    }

    @Override
    public void add(long userId, String concept, double amount, String date) {
        String clean = concept.replace(",", " ").replace("\n", " ");
        try (FileWriter w = new FileWriter(file, true)) { // true = agregar al final
            w.write(userId + "," + date + "," + amount + "," + clean + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Expense> getByMonth(long userId, String month) {
        List<Expense> list = new ArrayList<>();
        if (!file.exists()) return list;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",", 4);
                if (p.length < 4) continue;
                if (Long.parseLong(p[0]) == userId && p[1].startsWith(month)) {
                    list.add(new Expense(0, p[3], Double.parseDouble(p[2]), p[1]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        Collections.reverse(list); // más recientes primero
        return list;
    }
}
