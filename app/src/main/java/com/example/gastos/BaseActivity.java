package com.example.gastos;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;

/** Todas las pantallas heredan de aquí: aplican modo oscuro y color principal. */
public abstract class BaseActivity extends AppCompatActivity {
    protected Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new Prefs(this);
        AppCompatDelegate.setDefaultNightMode(prefs.isDark()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
    }

    /** Pinta la barra superior y los botones con el color elegido. */
    protected void applyColor(Button... buttons) {
        int color = prefs.getColor();
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setBackgroundDrawable(new ColorDrawable(color));
        for (Button b : buttons) {
            ViewCompat.setBackgroundTintList(b, ColorStateList.valueOf(color));
        }
    }
}
