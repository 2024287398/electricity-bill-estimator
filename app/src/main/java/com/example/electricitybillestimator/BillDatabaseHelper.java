package com.example.electricitybillestimator;

import android.content.ContentValues;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BillDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "electricity_bills.db";
    private static final int DATABASE_VERSION = 1;

    public BillDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE bills ("
                + "id INTEGER PRIMARY KEY, "
                + "month INTEGER NOT NULL, "
                + "units INTEGER NOT NULL, "
                + "rebate INTEGER NOT NULL, "
                + "total_charges REAL NOT NULL, "
                + "final_cost REAL NOT NULL"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 has no database upgrades.
    }

    public void insertBill(int month, int units, int rebate,
                           double totalCharges, double finalCost) {
        ContentValues values = new ContentValues();
        values.put("month", month);
        values.put("units", units);
        values.put("rebate", rebate);
        values.put("total_charges", totalCharges);
        values.put("final_cost", finalCost);

        getWritableDatabase().insertOrThrow("bills", null, values);
    }
}