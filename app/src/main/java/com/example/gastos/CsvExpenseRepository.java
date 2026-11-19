package com.example.gastos;

import android.content.Context;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Cada gasto es una línea: id,userId,fecha,monto,categoria,concepto
 * Para editar o borrar se lee todo el archivo, se modifica la lista y se reescribe.
 */
public class CsvExpenseRepository implements ExpenseRepository {
    private final File file;

    public CsvExpenseRepository(Context c) {
        file = new File(c.getFilesDir(), "gastos.csv");
    }

    private String clean(String s) {
        return s.replace(",", " ").replace("\n", " ");
    }

    /** Lee el archivo; las líneas con formato inválido (p. ej. del formato viejo) se ignoran. */
    private List<String[]> readAll() {
        List<String[]> rows = new ArrayList<>();
        if (!file.exists()) return rows;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",", 6);
                if (p.length < 6) continue;
                try {
                    Long.parseLong(p[0]);
                    Long.parseLong(p[1]);
                    Double.parseDouble(p[3]);
                    rows.add(p);
                } catch (NumberFormatException ignored) { }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private void writeAll(List<String[]> rows) {
        try (FileWriter w = new FileWriter(file, false)) { // false = sobrescribir
            for (String[] p : rows) {
                w.write(TextUtils.join(",", p) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(long userId, String concept, double amount, String category, String date) {
        List<String[]> rows = readAll();
        long next = 1;
        for (String[] p : rows) next = Math.max(next, Long.parseLong(p[0]) + 1);
        rows.add(new String[]{String.valueOf(next), String.valueOf(userId), date,
                String.valueOf(amount), clean(category), clean(concept)});
        writeAll(rows);
    }

    @Override
    public void update(long userId, Expense e) {
        List<String[]> rows = readAll();
        for (String[] p : rows) {
            if (p[0].equals(String.valueOf(e.id)) && p[1].equals(String.valueOf(userId))) {
                p[3] = String.valueOf(e.amount);
                p[4] = clean(e.category);
                p[5] = clean(e.concept);
            }
        }
        writeAll(rows);
    }

    @Override
    public void delete(long userId, long expenseId) {
        List<String[]> rows = readAll();
        Iterator<String[]> it = rows.iterator();
        while (it.hasNext()) {
            String[] p = it.next();
            if (p[0].equals(String.valueOf(expenseId)) && p[1].equals(String.valueOf(userId))) {
                it.remove();
            }
        }
        writeAll(rows);
    }

    @Override
    public List<Expense> getByMonth(long userId, String month) {
        List<Expense> list = new ArrayList<>();
        for (String[] p : readAll()) {
            if (p[1].equals(String.valueOf(userId)) && p[2].startsWith(month)) {
                list.add(new Expense(Long.parseLong(p[0]), p[5],
                        Double.parseDouble(p[3]), p[4], p[2]));
            }
        }
        Collections.reverse(list); // más recientes primero
        return list;
    }
}
