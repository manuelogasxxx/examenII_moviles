package com.example.gastos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;

/** Base de datos SQLite: tablas users y expenses. */
public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context c) {
        super(c, "gastos.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, password TEXT NOT NULL)");
        db.execSQL("CREATE TABLE expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, concept TEXT NOT NULL, "
                + "amount REAL NOT NULL, date TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS expenses");
        onCreate(db);
    }

    /** Devuelve false si el usuario ya existe. */
    public boolean registerUser(String username, String password) {
        ContentValues v = new ContentValues();
        v.put("username", username);
        v.put("password", hash(password));
        return getWritableDatabase().insert("users", null, v) != -1;
    }

    /** Devuelve el id del usuario, o -1 si los datos son incorrectos. */
    public long login(String username, String password) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM users WHERE username=? AND password=?",
                new String[]{username, hash(password)});
        long id = c.moveToFirst() ? c.getLong(0) : -1;
        c.close();
        return id;
    }

    /** SHA-256 en hexadecimal (la contraseña nunca se guarda en texto plano). */
    public static String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
