package com.example.gastos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private TextView tvTotal;
    private EditText etConcept, etAmount;
    private ListView lvExpenses;
    private Button btnAdd, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTotal = findViewById(R.id.tvTotal);
        etConcept = findViewById(R.id.etConcept);
        etAmount = findViewById(R.id.etAmount);
        lvExpenses = findViewById(R.id.lvExpenses);
        btnAdd = findViewById(R.id.btnAdd);
        btnSettings = findViewById(R.id.btnSettings);

        setTitle("Hola, " + prefs.getUsername());

        // Pedir permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        btnAdd.setOnClickListener(v -> addExpense());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    // Al volver de Ajustes se refresca el color y el origen de datos
    @Override
    protected void onResume() {
        super.onResume();
        applyColor(btnAdd, btnSettings);
        loadExpenses();
    }

    /** Elige SQLite o CSV según la preferencia guardada. */
    private ExpenseRepository repo() {
        if (prefs.getStorage().equals(Prefs.STORAGE_CSV)) {
            return new CsvExpenseRepository(this);
        }
        return new SqliteExpenseRepository(this);
    }

    private String currentMonth() {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
    }

    private void addExpense() {
        String concept = etConcept.getText().toString().trim();
        String amountText = etAmount.getText().toString().trim();
        if (concept.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Llena concepto y monto", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        repo().add(prefs.getUserId(), concept, amount, today);

        etConcept.setText("");
        etAmount.setText("");
        loadExpenses();
        NotificationHelper.show(this, "Gasto guardado",
                concept + ": $" + String.format(Locale.US, "%.2f", amount));
    }

    private void loadExpenses() {
        List<Expense> list = repo().getByMonth(prefs.getUserId(), currentMonth());
        double total = 0;
        List<String> rows = new ArrayList<>();
        for (Expense e : list) {
            total += e.amount;
            rows.add(e.date + "  |  " + e.concept + "  |  $"
                    + String.format(Locale.US, "%.2f", e.amount));
        }
        lvExpenses.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, rows));
        tvTotal.setText("Total del mes: $" + String.format(Locale.US, "%.2f", total));
    }
}
