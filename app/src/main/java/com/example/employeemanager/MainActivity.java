package com.example.employeemanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanager.dal.DBHelper;

public class MainActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnLogin;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.login(username, password);

            if (cursor.moveToFirst()) {
                String role = cursor.getString(
                        cursor.getColumnIndexOrThrow("role"));

                SharedPreferences preferences =
                        getSharedPreferences("EmployeeManager", MODE_PRIVATE);

                preferences.edit()
                        .putString("username", username)
                        .putString("role", role)
                        .apply();

                cursor.close();

                if (role.equals("Admin")) {
                    Intent intent = new Intent(
                            MainActivity.this, AdminActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(
                            MainActivity.this, EmployeeActivity.class);
                    startActivity(intent);
                }
            } else {
                cursor.close();
                Toast.makeText(this,
                        "Sai tên đăng nhập hoặc mật khẩu",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
