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
    public void add(long userId, String concept, double amount, String date) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("concept", concept);
        v.put("amount", amount);
        v.put("date", date);
        helper.getWritableDatabase().insert("expenses", null, v);
    }

    @Override
    public List<Expense> getByMonth(long userId, String month) {
        List<Expense> list = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, concept, amount, date FROM expenses "
                        + "WHERE user_id=? AND date LIKE ? ORDER BY id DESC",
                new String[]{String.valueOf(userId), month + "-%"});
        while (c.moveToNext()) {
            list.add(new Expense(c.getLong(0), c.getString(1), c.getDouble(2), c.getString(3)));
        }
        c.close();
        return list;
    }
}
