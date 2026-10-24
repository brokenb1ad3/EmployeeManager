package com.example.employeemanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanager.dal.DBHelper;

import java.util.ArrayList;

public class EmployeeActivity extends AppCompatActivity {

    TextView tvMaNhanVien;
    Button btnCheckIn, btnCheckOut, btnLichSu;
    ListView lvLichSu;

    DBHelper dbHelper;
    ArrayList<String> danhSach;
    ArrayAdapter<String> adapter;
    String maNhanVien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        SharedPreferences preferences = getSharedPreferences(
                "EmployeeManager", MODE_PRIVATE
        );

        maNhanVien = preferences.getString("username", "");

        dbHelper = new DBHelper(this);

        tvMaNhanVien = findViewById(R.id.tvMaNhanVien);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnLichSu = findViewById(R.id.btnLichSu);
        lvLichSu = findViewById(R.id.lvLichSu);

        tvMaNhanVien.setText("Mã nhân viên: " + maNhanVien);

        danhSach = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                danhSach
        );

        lvLichSu.setAdapter(adapter);

        btnCheckIn.setOnClickListener(v -> checkIn());
        btnCheckOut.setOnClickListener(v -> checkOut());
        btnLichSu.setOnClickListener(v -> hienThiLichSu());

        hienThiLichSu();
    }

    private void checkIn() {
        if (maNhanVien.isEmpty()) {
            thongBao("Không tìm thấy tài khoản nhân viên");
            return;
        }

        if (dbHelper.checkIn(maNhanVien)) {
            thongBao("Check-in thành công!");
            hienThiLichSu();
        } else {
            thongBao("Check-in thất bại! Có thể mã nhân viên không tồn tại hoặc đã check-in hôm nay.");
        }
    }

    private void checkOut() {
        if (maNhanVien.isEmpty()) {
            thongBao("Không tìm thấy tài khoản nhân viên");
            return;
        }

        if (dbHelper.checkOut(maNhanVien)) {
            thongBao("Check-out thành công!");
            hienThiLichSu();
        } else {
            thongBao("Check-out thất bại! Hãy kiểm tra xem bạn đã check-in chưa.");
        }
    }

    private void hienThiLichSu() {
        danhSach.clear();
        danhSach.addAll(dbHelper.getWorkHistory(maNhanVien));
        adapter.notifyDataSetChanged();
    }

    private void thongBao(String noiDung) {
        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_LONG
        ).show();
    }
}
