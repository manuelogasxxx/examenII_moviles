package com.example.gastos;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda sesión y preferencias (color, modo oscuro, tipo de almacenamiento). */
public class Prefs {
    public static final String STORAGE_SQLITE = "sqlite";
    public static final String STORAGE_CSV = "csv";
    public static final int DEFAULT_COLOR = 0xFF3F51B5;

    private final SharedPreferences sp;

    public Prefs(Context c) {
        sp = c.getSharedPreferences("gastos_prefs", Context.MODE_PRIVATE);
    }

    // ---- Sesión ----
    public void saveSession(long userId, String username) {
        sp.edit().putLong("user_id", userId).putString("username", username).apply();
    }
    public long getUserId() { return sp.getLong("user_id", -1); }
    public String getUsername() { return sp.getString("username", ""); }
    public void logout() { sp.edit().remove("user_id").remove("username").apply(); }

    // ---- Preferencias ----
    public int getColor() { return sp.getInt("color", DEFAULT_COLOR); }
    public void setColor(int color) { sp.edit().putInt("color", color).apply(); }

    public boolean isDark() { return sp.getBoolean("dark", false); }
    public void setDark(boolean dark) { sp.edit().putBoolean("dark", dark).apply(); }

    public String getStorage() { return sp.getString("storage", STORAGE_SQLITE); }
    public void setStorage(String s) { sp.edit().putString("storage", s).apply(); }
}
