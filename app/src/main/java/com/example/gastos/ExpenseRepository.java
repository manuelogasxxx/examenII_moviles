package com.example.gastos;

import java.util.List;

/** Contrato común: SQLite y CSV lo implementan igual. */
public interface ExpenseRepository {
    void add(long userId, String concept, double amount, String date);
    List<Expense> getByMonth(long userId, String month); // month = "yyyy-MM"
}
