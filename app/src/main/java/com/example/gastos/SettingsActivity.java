package com.example.gastos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnLogout = findViewById(R.id.btnLogout);
        applyColor(btnLogout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // flecha atrás
        }

        // ---- Modo oscuro ----
        SwitchCompat swDark = findViewById(R.id.swDark);
        swDark.setChecked(prefs.isDark());
        swDark.setOnCheckedChangeListener((v, checked) -> {
            prefs.setDark(checked);
            AppCompatDelegate.setDefaultNightMode(checked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO); // recrea la pantalla solo
        });

        // ---- Color principal ----
        int[] colors = {0xFF3F51B5, 0xFF2E7D32, 0xFFC62828, 0xFFEF6C00, 0xFF6A1B9A};
        LinearLayout colorBox = findViewById(R.id.colorBox);
        for (int color : colors) {
            Button b = new Button(this);
            b.setBackgroundColor(color);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 120, 1f);
            lp.setMargins(8, 8, 8, 8);
            b.setLayoutParams(lp);
            b.setOnClickListener(v -> {
                prefs.setColor(color);
                applyColor(btnLogout);
            });
            colorBox.addView(b);
        }

        // ---- Almacenamiento: SQLite o CSV ----
        RadioGroup rgStorage = findViewById(R.id.rgStorage);
        RadioButton rbSqlite = findViewById(R.id.rbSqlite);
        RadioButton rbCsv = findViewById(R.id.rbCsv);
        if (prefs.getStorage().equals(Prefs.STORAGE_CSV)) rbCsv.setChecked(true);
        else rbSqlite.setChecked(true);
        rgStorage.setOnCheckedChangeListener((group, checkedId) ->
                prefs.setStorage(checkedId == R.id.rbCsv
                        ? Prefs.STORAGE_CSV : Prefs.STORAGE_SQLITE));

        // ---- Cerrar sesión ----
        btnLogout.setOnClickListener(v -> {
            prefs.logout();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
