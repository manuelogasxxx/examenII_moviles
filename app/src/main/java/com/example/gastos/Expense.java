package com.example.gastos;

public class Expense {
    public long id;
    public String concept;
    public double amount;
    public String category;
    public String date; // yyyy-MM-dd

    public Expense(long id, String concept, double amount, String category, String date) {
        this.id = id;
        this.concept = concept;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }
}
