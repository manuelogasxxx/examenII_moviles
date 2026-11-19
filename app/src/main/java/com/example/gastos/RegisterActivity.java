package com.example.gastos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EditText etUser = findViewById(R.id.etUser);
        EditText etPass = findViewById(R.id.etPass);
        EditText etPass2 = findViewById(R.id.etPass2);
        Button btnRegister = findViewById(R.id.btnRegister);
        applyColor(btnRegister);

        btnRegister.setOnClickListener(v -> {
            String user = etUser.getText().toString().trim();
            String pass = etPass.getText().toString();
            String pass2 = etPass2.getText().toString();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(pass2)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            } else if (new DbHelper(this).registerUser(user, pass)) {
                Toast.makeText(this, "Usuario creado, inicia sesión", Toast.LENGTH_SHORT).show();
                finish(); // regresa al login
            } else {
                Toast.makeText(this, "Ese usuario ya existe", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
