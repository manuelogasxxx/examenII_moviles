package com.example.gastos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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
    private Spinner spCategory;
    private ListView lvExpenses;
    private Button btnAdd, btnSettings;

    // Gastos que se están mostrando (misma posición que las filas de la lista)
    private List<Expense> current = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTotal = findViewById(R.id.tvTotal);
        etConcept = findViewById(R.id.etConcept);
        etAmount = findViewById(R.id.etAmount);
        spCategory = findViewById(R.id.spCategory);
        lvExpenses = findViewById(R.id.lvExpenses);
        btnAdd = findViewById(R.id.btnAdd);
        btnSettings = findViewById(R.id.btnSettings);

        setTitle("Hola, " + prefs.getUsername());
        spCategory.setAdapter(categoryAdapter());

        // Pedir permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        btnAdd.setOnClickListener(v -> addExpense());
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // Tocar un gasto abre el diálogo de edición / eliminación
        lvExpenses.setOnItemClickListener((parent, view, position, id) ->
                showEditDialog(current.get(position)));
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

    private ArrayAdapter<String> categoryAdapter() {
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Categories.ALL);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private String currentMonth() {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
    }

    /** Devuelve el monto o null si el texto no es un número válido. */
    private Double parseAmount(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ---------- Agregar ----------
    private void addExpense() {
        String concept = etConcept.getText().toString().trim();
        Double amount = parseAmount(etAmount.getText().toString());
        if (concept.isEmpty() || amount == null) {
            Toast.makeText(this, "Concepto y monto válidos, por favor", Toast.LENGTH_SHORT).show();
            return;
        }
        String category = Categories.ALL[spCategory.getSelectedItemPosition()];
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        repo().add(prefs.getUserId(), concept, amount, category, today);

        etConcept.setText("");
        etAmount.setText("");
        loadExpenses();
        NotificationHelper.show(this, "Gasto guardado",
                category + " - " + concept + ": $" + String.format(Locale.US, "%.2f", amount));
    }

    // ---------- Editar ----------
    private void showEditDialog(Expense e) {
        View view = getLayoutInflater().inflate(R.layout.dialog_expense, null);
        EditText etC = view.findViewById(R.id.etConcept);
        EditText etA = view.findViewById(R.id.etAmount);
        Spinner sp = view.findViewById(R.id.spCategory);
        sp.setAdapter(categoryAdapter());
        etC.setText(e.concept);
        etA.setText(String.valueOf(e.amount));
        sp.setSelection(Categories.indexOf(e.category));

        new AlertDialog.Builder(this)
                .setTitle("Editar gasto")
                .setView(view)
                .setPositiveButton("Guardar", (d, w) -> {
                    String concept = etC.getText().toString().trim();
                    Double amount = parseAmount(etA.getText().toString());
                    if (concept.isEmpty() || amount == null) {
                        Toast.makeText(this, "Datos inválidos, no se guardó", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String category = Categories.ALL[sp.getSelectedItemPosition()];
                    repo().update(prefs.getUserId(),
                            new Expense(e.id, concept, amount, category, e.date));
                    loadExpenses();
                })
                .setNeutralButton("Eliminar", (d, w) -> confirmDelete(e))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- Eliminar ----------
    private void confirmDelete(Expense e) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar gasto")
                .setMessage("¿Eliminar \"" + e.concept + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repo().delete(prefs.getUserId(), e.id);
                    loadExpenses();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- Listar ----------
    private void loadExpenses() {
        current = repo().getByMonth(prefs.getUserId(), currentMonth());
        double total = 0;
        List<String> rows = new ArrayList<>();
        for (Expense e : current) {
            total += e.amount;
            rows.add(e.date + "  |  " + e.category + "\n" + e.concept + "  -  $"
                    + String.format(Locale.US, "%.2f", e.amount));
        }
        lvExpenses.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, rows));
        tvTotal.setText("Total del mes: $" + String.format(Locale.US, "%.2f", total));
    }
}
