package com.example.employeemanager.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EmployeeManager.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE Account (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL)");

        db.execSQL("CREATE TABLE Employee (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT UNIQUE NOT NULL, " +
                "fullname TEXT NOT NULL, " +
                "email TEXT, " +
                "phone TEXT, " +
                "hourlyRate REAL NOT NULL, " +
                "accountId INTEGER, " +
                "FOREIGN KEY(accountId) REFERENCES Account(id))");

        db.execSQL("CREATE TABLE WorkLog (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "employeeId INTEGER NOT NULL, " +
                "workDate TEXT NOT NULL, " +
                "checkInTime TEXT, " +
                "checkOutTime TEXT, " +
                "totalHours REAL DEFAULT 0, " +
                "status TEXT DEFAULT 'Working', " +
                "FOREIGN KEY(employeeId) REFERENCES Employee(id))");

        db.execSQL("CREATE TABLE Salary (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "employeeId INTEGER NOT NULL, " +
                "month INTEGER NOT NULL, " +
                "year INTEGER NOT NULL, " +
                "totalHours REAL DEFAULT 0, " +
                "overtimeHours REAL DEFAULT 0, " +
                "totalSalary REAL DEFAULT 0, " +
                "status TEXT DEFAULT 'Unpaid', " +
                "FOREIGN KEY(employeeId) REFERENCES Employee(id))");

        // Tai khoan Admin mac dinh
        ContentValues admin = new ContentValues();
        admin.put("username", "admin");
        admin.put("password", "123");
        admin.put("role", "Admin");
        db.insert("Account", null, admin);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Salary");
        db.execSQL("DROP TABLE IF EXISTS WorkLog");
        db.execSQL("DROP TABLE IF EXISTS Employee");
        db.execSQL("DROP TABLE IF EXISTS Account");
        onCreate(db);
    }

    // Dang nhap
    public Cursor login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM Account WHERE username=? AND password=?",
                new String[]{username, password}
        );
    }

    // Them tai khoan
    public long insertAccount(String username,
                              String password,
                              String role) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("role", role);

        return db.insert("Account", null, values);
    }



    public boolean insertEmployee(String code, String fullname,
                                  String email, String phone,
                                  double hourlyRate) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            ContentValues account = new ContentValues();
            account.put("username", code);
            account.put("password", "123");
            account.put("role", "Employee");

            long accountId = db.insert("Account", null, account);

            if (accountId == -1) {
                return false;
            }

            ContentValues employee = new ContentValues();
            employee.put("code", code);
            employee.put("fullname", fullname);
            employee.put("email", email);
            employee.put("phone", phone);
            employee.put("hourlyRate", hourlyRate);
            employee.put("accountId", accountId);

            long employeeId = db.insert("Employee", null, employee);

            if (employeeId == -1) {
                return false;
            }

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }


    public ArrayList<String> getAllEmployees() {
        ArrayList<String> list = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT code, fullname, phone, hourlyRate " +
                        "FROM Employee ORDER BY id DESC",
                null
        );

        while (cursor.moveToNext()) {
            String code = cursor.getString(0);
            String name = cursor.getString(1);
            String phone = cursor.getString(2);
            double hourlyRate = cursor.getDouble(3);

            list.add(
                    "Mã: " + code +
                            "\nTên: " + name +
                            "\nSĐT: " + (phone == null ? "" : phone) +
                            "\nLương/giờ: " + hourlyRate
            );
        }

        cursor.close();
        return list;
    }

    public boolean updateEmployee(
            String oldCode,
            String newCode,
            String fullname,
            String phone,
            double hourlyRate) {

        ContentValues values = new ContentValues();
        values.put("code", newCode);
        values.put("fullname", fullname);
        values.put("phone", phone);
        values.put("hourlyRate", hourlyRate);

        return getWritableDatabase().update(
                "Employee",
                values,
                "code = ?",
                new String[]{oldCode}
        ) > 0;
    }



    public boolean deleteEmployee(String code) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                "Employee",
                "code = ?",
                new String[]{code}
        );

        return result > 0;
    }


    public ArrayList<String> searchEmployees(String keyword) {
        ArrayList<String> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT code, fullname, phone FROM Employee " +
                        "WHERE code LIKE ? OR fullname LIKE ? " +
                        "ORDER BY id DESC",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"}
        );

        if (cursor.moveToFirst()) {
            do {
                String ma = cursor.getString(0);
                String ten = cursor.getString(1);
                String sdt = cursor.getString(2);

                list.add("Mã: " + ma
                        + "\nTên: " + ten
                        + "\nSĐT: " + (sdt == null ? "" : sdt));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }


    public int getEmployeeIdByCode(String code) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM Employee WHERE code = ?",
                new String[]{code}
        );

        int employeeId = -1;

        if (cursor.moveToFirst()) {
            employeeId = cursor.getInt(0);
        }

        cursor.close();
        return employeeId;
    }

    public boolean checkIn(String employeeCode) {
        int employeeId = getEmployeeIdByCode(employeeCode);

        if (employeeId == -1) {
            return false;
        }

        String today = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(new Date());

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM WorkLog WHERE employeeId = ? " +
                        "AND workDate = ?",
                new String[]{
                        String.valueOf(employeeId),
                        today
                }
        );

        boolean daChamCong = cursor.moveToFirst();
        cursor.close();

        if (daChamCong) {
            return false;
        }

        String now = new SimpleDateFormat(
                "HH:mm:ss", Locale.getDefault()
        ).format(new Date());

        ContentValues values = new ContentValues();
        values.put("employeeId", employeeId);
        values.put("workDate", today);
        values.put("checkInTime", now);
        values.put("totalHours", 0);
        values.put("status", "Working");

        return getWritableDatabase().insert(
                "WorkLog", null, values
        ) != -1;
    }

    public boolean checkOut(String employeeCode) {
        int employeeId = getEmployeeIdByCode(employeeCode);

        if (employeeId == -1) {
            return false;
        }

        String today = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(new Date());

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id, checkInTime FROM WorkLog " +
                        "WHERE employeeId = ? AND workDate = ? " +
                        "AND checkOutTime IS NULL",
                new String[]{
                        String.valueOf(employeeId),
                        today
                }
        );

        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }

        int workLogId = cursor.getInt(0);
        String checkInTime = cursor.getString(1);
        cursor.close();

        String checkOutTime = new SimpleDateFormat(
                "HH:mm:ss", Locale.getDefault()
        ).format(new Date());

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "HH:mm:ss", Locale.getDefault()
            );

            Date inTime = sdf.parse(checkInTime);
            Date outTime = sdf.parse(checkOutTime);

            if (inTime == null || outTime == null) {
                return false;
            }

            long diff = outTime.getTime() - inTime.getTime();

            if (diff < 0) {
                return false;
            }

            double totalHours = diff / (1000.0 * 60 * 60);

            ContentValues values = new ContentValues();
            values.put("checkOutTime", checkOutTime);
            values.put("totalHours", totalHours);
            values.put("status", "Completed");

            int result = getWritableDatabase().update(
                    "WorkLog",
                    values,
                    "id = ?",
                    new String[]{String.valueOf(workLogId)}
            );

            return result > 0;

        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    public ArrayList<String> getWorkHistory(String employeeCode) {
        ArrayList<String> history = new ArrayList<>();

        int employeeId = getEmployeeIdByCode(employeeCode);

        if (employeeId == -1) {
            return history;
        }

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT workDate, checkInTime, checkOutTime, " +
                        "totalHours, status FROM WorkLog " +
                        "WHERE employeeId = ? ORDER BY workDate DESC",
                new String[]{String.valueOf(employeeId)}
        );

        while (cursor.moveToNext()) {
            String date = cursor.getString(0);
            String checkIn = cursor.getString(1);
            String checkOut = cursor.getString(2);
            double hours = cursor.getDouble(3);
            String status = cursor.getString(4);

            if (checkOut == null) {
                checkOut = "Chưa check-out";
            }

            history.add(
                    "Ngày: " + date +
                            "\nGiờ vào: " + checkIn +
                            "\nGiờ ra: " + checkOut +
                            "\nTổng giờ: " + String.format(
                            Locale.getDefault(), "%.2f", hours
                    ) +
                            "\nTrạng thái: " + status
            );
        }

        cursor.close();
        return history;
    }


    public boolean calculateMonthlySalary(
            String employeeCode, int month, int year) {

        SQLiteDatabase db = getWritableDatabase();

        Cursor employeeCursor = db.rawQuery(
                "SELECT id, hourlyRate FROM Employee WHERE code = ?",
                new String[]{employeeCode}
        );

        if (!employeeCursor.moveToFirst()) {
            employeeCursor.close();
            return false;
        }

        int employeeId = employeeCursor.getInt(0);
        double hourlyRate = employeeCursor.getDouble(1);
        employeeCursor.close();

        Cursor workCursor = db.rawQuery(
                "SELECT workDate, SUM(totalHours) FROM WorkLog " +
                        "WHERE employeeId = ? " +
                        "AND substr(workDate, 1, 4) = ? " +
                        "AND substr(workDate, 6, 2) = ? " +
                        "AND checkOutTime IS NOT NULL " +
                        "GROUP BY workDate",
                new String[]{
                        String.valueOf(employeeId),
                        String.format(Locale.US, "%04d", year),
                        String.format(Locale.US, "%02d", month)
                }
        );

        double totalHours = 0;
        double overtimeHours = 0;
        double normalHours = 0;

        while (workCursor.moveToNext()) {
            double hours = workCursor.getDouble(1);

            totalHours += hours;
            normalHours += Math.min(hours, 8);
            overtimeHours += Math.max(hours - 8, 0);
        }

        workCursor.close();

        double totalSalary =
                normalHours * hourlyRate
                        + overtimeHours * hourlyRate * 1.5;

        ContentValues values = new ContentValues();
        values.put("employeeId", employeeId);
        values.put("month", month);
        values.put("year", year);
        values.put("totalHours", totalHours);
        values.put("overtimeHours", overtimeHours);
        values.put("totalSalary", totalSalary);
        values.put("status", "Unpaid");

        Cursor salaryCursor = db.rawQuery(
                "SELECT id FROM Salary " +
                        "WHERE employeeId = ? AND month = ? AND year = ?",
                new String[]{
                        String.valueOf(employeeId),
                        String.valueOf(month),
                        String.valueOf(year)
                }
        );

        boolean exists = salaryCursor.moveToFirst();
        salaryCursor.close();

        if (exists) {
            return db.update(
                    "Salary",
                    values,
                    "employeeId = ? AND month = ? AND year = ?",
                    new String[]{
                            String.valueOf(employeeId),
                            String.valueOf(month),
                            String.valueOf(year)
                    }
            ) > 0;
        }

        return db.insert("Salary", null, values) != -1;
    }


    public ArrayList<String> getMonthlySalary(int month, int year) {
        ArrayList<String> list = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT E.code, E.fullname, S.totalHours, " +
                        "S.overtimeHours, S.totalSalary, S.status " +
                        "FROM Salary S " +
                        "JOIN Employee E ON S.employeeId = E.id " +
                        "WHERE S.month = ? AND S.year = ? " +
                        "ORDER BY E.code",
                new String[]{
                        String.valueOf(month),
                        String.valueOf(year)
                }
        );

        while (cursor.moveToNext()) {
            list.add(
                    "Mã NV: " + cursor.getString(0) +
                            "\nHọ tên: " + cursor.getString(1) +
                            "\nTổng giờ: " + cursor.getDouble(2) +
                            "\nGiờ tăng ca: " + cursor.getDouble(3) +
                            "\nTổng lương: " + String.format(
                            Locale.getDefault(), "%,.0f VNĐ",
                            cursor.getDouble(4)
                    ) +
                            "\nTrạng thái: " + cursor.getString(5)
            );
        }

        cursor.close();
        return list;
    }

    public ArrayList<String> getAllEmployeeCodes() {
        ArrayList<String> list = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT code FROM Employee ORDER BY code",
                null
        );

        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }

        cursor.close();
        return list;
    }
    public boolean updateSalaryStatus(String employeeCode, int month, int year,
                                      String status) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("status", status);

        return db.update(
                "Salary",
                values,
                "employeeId = (SELECT id FROM Employee WHERE code = ?) " +
                        "AND month = ? AND year = ?",
                new String[]{
                        employeeCode,
                        String.valueOf(month),
                        String.valueOf(year)
                }
        ) > 0;
    }
}
