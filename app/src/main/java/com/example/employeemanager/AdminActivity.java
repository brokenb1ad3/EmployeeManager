package com.example.employeemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanager.dal.DBHelper;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    EditText edtMaNV, edtTenNV, edtSdt, edtLuongGio;
    Button btnThem, btnDanhSach, btnSua, btnXoa, btnMoDanhSach, btnQuanLyLuong;
    ListView lvNhanVien;

    DBHelper dbHelper;
    ArrayList<String> danhSach;
    ArrayAdapter<String> adapter;

    String maNhanVienChon = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        edtMaNV = findViewById(R.id.edtMaNV);
        edtTenNV = findViewById(R.id.edtTenNV);
        edtSdt = findViewById(R.id.edtSdt);
        edtLuongGio = findViewById(R.id.edtLuongGio);

        btnThem = findViewById(R.id.btnThem);
        btnDanhSach = findViewById(R.id.btnDanhSach);
        btnSua = findViewById(R.id.btnSua);
        btnXoa = findViewById(R.id.btnXoa);
        btnMoDanhSach = findViewById(R.id.btnMoDanhSach);
        btnQuanLyLuong = findViewById(R.id.btnQuanLyLuong);


        lvNhanVien = findViewById(R.id.lvNhanVien);

        dbHelper = new DBHelper(this);
        danhSach = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                danhSach
        );

        lvNhanVien.setAdapter(adapter);

        btnThem.setOnClickListener(v -> themNhanVien());
        btnDanhSach.setOnClickListener(v -> hienThiDanhSach());
        btnSua.setOnClickListener(v -> suaNhanVien());
        btnXoa.setOnClickListener(v -> xoaNhanVien());

        btnMoDanhSach.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AdminActivity.this,
                    EmployeeListActivity.class
            );
            startActivity(intent);
        });

        btnQuanLyLuong.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, SalaryActivity.class));
        });

        lvNhanVien.setOnItemClickListener((parent, view, position, id) -> {
            String item = danhSach.get(position);

            String ma = "";
            String ten = "";
            String sdt = "";
            String luong = "";

            String[] lines = item.split("\n");

            for (String line : lines) {
                if (line.startsWith("Mã: ")) {
                    ma = line.substring(4);
                } else if (line.startsWith("Tên: ")) {
                    ten = line.substring(5);
                } else if (line.startsWith("SĐT: ")) {
                    sdt = line.substring(5);
                } else if (line.startsWith("Lương/giờ: ")) {
                    luong = line.substring(11);
                }
            }

            maNhanVienChon = ma;
            edtMaNV.setText(ma);
            edtTenNV.setText(ten);
            edtSdt.setText(sdt);
            edtLuongGio.setText(luong);

            Toast.makeText(
                    this,
                    "Đã chọn nhân viên " + ma,
                    Toast.LENGTH_SHORT
            ).show();
        });

        hienThiDanhSach();
    }

    private void themNhanVien() {
        String ma = edtMaNV.getText().toString().trim();
        String ten = edtTenNV.getText().toString().trim();
        String sdt = edtSdt.getText().toString().trim();
        String luongText = edtLuongGio.getText().toString().trim();

        if (ma.isEmpty() || ten.isEmpty()
                || luongText.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng nhập mã, tên và lương theo giờ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        double luongGio;

        try {
            luongGio = Double.parseDouble(luongText);
        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Mức lương không hợp lệ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (luongGio <= 0) {
            Toast.makeText(
                    this,
                    "Lương theo giờ phải lớn hơn 0",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        boolean ketQua = dbHelper.insertEmployee(
                ma,
                ten,
                "",
                sdt,
                luongGio
        );

        if (ketQua) {
            Toast.makeText(
                    this,
                    "Thêm nhân viên thành công. Tài khoản: "
                            + ma + " / Mật khẩu: 123",
                    Toast.LENGTH_LONG
            ).show();

            lamMoiForm();
            hienThiDanhSach();
        } else {
            Toast.makeText(
                    this,
                    "Thêm thất bại! Mã nhân viên hoặc tài khoản có thể đã tồn tại",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void hienThiDanhSach() {
        danhSach.clear();
        danhSach.addAll(dbHelper.getAllEmployees());
        adapter.notifyDataSetChanged();

        if (danhSach.isEmpty()) {
            Toast.makeText(
                    this,
                    "Chưa có nhân viên nào",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void suaNhanVien() {
        if (maNhanVienChon.isEmpty()) {
            Toast.makeText(
                    this,
                    "Hãy chọn nhân viên cần sửa trong danh sách",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String maMoi = edtMaNV.getText().toString().trim();
        String ten = edtTenNV.getText().toString().trim();
        String sdt = edtSdt.getText().toString().trim();
        String luongText = edtLuongGio.getText().toString().trim();

        if (maMoi.isEmpty() || ten.isEmpty()
                || luongText.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng nhập mã, tên và lương theo giờ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        double luongGio;

        try {
            luongGio = Double.parseDouble(luongText);
        } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    "Mức lương không hợp lệ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (luongGio <= 0) {
            Toast.makeText(
                    this,
                    "Lương theo giờ phải lớn hơn 0",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        boolean ketQua = dbHelper.updateEmployee(
                maNhanVienChon,
                maMoi,
                ten,
                sdt,
                luongGio
        );

        if (ketQua) {
            Toast.makeText(
                    this,
                    "Cập nhật nhân viên thành công",
                    Toast.LENGTH_SHORT
            ).show();

            lamMoiForm();
            hienThiDanhSach();
        } else {
            Toast.makeText(
                    this,
                    "Cập nhật thất bại! Hãy kiểm tra lại mã nhân viên",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void xoaNhanVien() {
        if (maNhanVienChon.isEmpty()) {
            Toast.makeText(
                    this,
                    "Hãy chọn nhân viên cần xóa trong danh sách",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        boolean ketQua = dbHelper.deleteEmployee(maNhanVienChon);

        if (ketQua) {
            Toast.makeText(
                    this,
                    "Xóa nhân viên thành công",
                    Toast.LENGTH_SHORT
            ).show();

            lamMoiForm();
            hienThiDanhSach();
        } else {
            Toast.makeText(
                    this,
                    "Xóa nhân viên thất bại",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void lamMoiForm() {
        edtMaNV.setText("");
        edtTenNV.setText("");
        edtSdt.setText("");
        edtLuongGio.setText("");
        maNhanVienChon = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        hienThiDanhSach();
    }
}
