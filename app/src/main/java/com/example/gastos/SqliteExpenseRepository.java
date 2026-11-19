package com.example.gastos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class SqliteExpenseRepository implements ExpenseRepository {
    private final DbHelper helper;

    public SqliteExpenseRepository(Context c) {
        helper = new DbHelper(c);
    }

    @Override
    public void add(long userId, String concept, double amount, String category, String date) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("concept", concept);
        v.put("amount", amount);
        v.put("category", category);
        v.put("date", date);
        helper.getWritableDatabase().insert("expenses", null, v);
    }

    @Override
    public void update(long userId, Expense e) {
        ContentValues v = new ContentValues();
        v.put("concept", e.concept);
        v.put("amount", e.amount);
        v.put("category", e.category);
        helper.getWritableDatabase().update("expenses", v,
                "id=? AND user_id=?",
                new String[]{String.valueOf(e.id), String.valueOf(userId)});
    }

    @Override
    public void delete(long userId, long expenseId) {
        helper.getWritableDatabase().delete("expenses",
                "id=? AND user_id=?",
                new String[]{String.valueOf(expenseId), String.valueOf(userId)});
    }

    @Override
    public List<Expense> getByMonth(long userId, String month) {
        List<Expense> list = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, concept, amount, category, date FROM expenses "
                        + "WHERE user_id=? AND date LIKE ? ORDER BY id DESC",
                new String[]{String.valueOf(userId), month + "-%"});
        while (c.moveToNext()) {
            list.add(new Expense(c.getLong(0), c.getString(1), c.getDouble(2),
                    c.getString(3), c.getString(4)));
        }
        c.close();
        return list;
    }
}
