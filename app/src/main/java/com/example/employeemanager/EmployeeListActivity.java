package com.example.employeemanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanager.dal.DBHelper;

import java.util.ArrayList;

public class EmployeeListActivity extends AppCompatActivity {

    EditText edtTimKiem;
    Button btnTimKiem, btnTatCa;
    ListView lvDanhSachNV;

    DBHelper dbHelper;
    ArrayList<String> danhSach;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        edtTimKiem = findViewById(R.id.edtTimKiem);
        btnTimKiem = findViewById(R.id.btnTimKiem);
        btnTatCa = findViewById(R.id.btnTatCa);
        lvDanhSachNV = findViewById(R.id.lvDanhSachNV);

        dbHelper = new DBHelper(this);
        danhSach = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                danhSach
        );

        lvDanhSachNV.setAdapter(adapter);

        btnTimKiem.setOnClickListener(v -> timKiem());

        btnTatCa.setOnClickListener(v -> hienThiTatCa());

        hienThiTatCa();
    }

    private void hienThiTatCa() {
        danhSach.clear();
        danhSach.addAll(dbHelper.getAllEmployees());
        adapter.notifyDataSetChanged();

        if (danhSach.isEmpty()) {
            Toast.makeText(this, "Chưa có nhân viên nào",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void timKiem() {
        String keyword = edtTimKiem.getText().toString().trim();

        if (keyword.isEmpty()) {
            hienThiTatCa();
            return;
        }

        danhSach.clear();
        danhSach.addAll(dbHelper.searchEmployees(keyword));
        adapter.notifyDataSetChanged();

        if (danhSach.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy nhân viên",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    "Tìm thấy " + danhSach.size() + " nhân viên",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
