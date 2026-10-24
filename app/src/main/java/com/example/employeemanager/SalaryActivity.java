package com.example.employeemanager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.employeemanager.dal.DBHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class SalaryActivity extends Activity {

    EditText edtThang, edtNam;
    Button btnTinhLuong;
    ListView lvBangLuong;
    DBHelper dbHelper;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);

        dbHelper = new DBHelper(this);

        edtThang = findViewById(R.id.edtThang);
        edtNam = findViewById(R.id.edtNam);
        btnTinhLuong = findViewById(R.id.btnTinhLuong);
        lvBangLuong = findViewById(R.id.lvBangLuong);

        Calendar calendar = Calendar.getInstance();
        edtThang.setText(String.valueOf(calendar.get(Calendar.MONTH) + 1));
        edtNam.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );
        lvBangLuong.setAdapter(adapter);

        btnTinhLuong.setOnClickListener(v -> tinhLuong());
    }

    private void tinhLuong() {
        try {
            int thang = Integer.parseInt(edtThang.getText().toString().trim());
            int nam = Integer.parseInt(edtNam.getText().toString().trim());

            if (thang < 1 || thang > 12 || nam < 2000) {
                Toast.makeText(this, "Tháng hoặc năm không hợp lệ",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> maNhanViens = dbHelper.getAllEmployeeCodes();

            for (String maNV : maNhanViens) {
                dbHelper.calculateMonthlySalary(maNV, thang, nam);
            }

            ArrayList<String> bangLuong =
                    dbHelper.getMonthlySalary(thang, nam);

            adapter.clear();
            adapter.addAll(bangLuong);
            adapter.notifyDataSetChanged();

            if (bangLuong.isEmpty()) {
                Toast.makeText(this, "Chưa có nhân viên để tính lương",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập tháng và năm",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
