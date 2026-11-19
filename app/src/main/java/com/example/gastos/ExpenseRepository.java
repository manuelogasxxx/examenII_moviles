package com.example.gastos;

import java.util.List;

/** Contrato común: SQLite y CSV lo implementan igual. */
public interface ExpenseRepository {
    void add(long userId, String concept, double amount, String category, String date);
    void update(long userId, Expense e);          // busca por e.id
    void delete(long userId, long expenseId);
    List<Expense> getByMonth(long userId, String month); // month = "yyyy-MM"
}
