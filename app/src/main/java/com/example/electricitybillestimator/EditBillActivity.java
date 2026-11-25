package com.example.electricitybillestimator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditBillActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnits;
    private SeekBar seekBarRebate;
    private long billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_bill);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        billId = getIntent().getLongExtra(BillDetailsActivity.EXTRA_BILL_ID, -1);
        spinnerMonth = findViewById(R.id.spinnerEditMonth);
        editTextUnits = findViewById(R.id.editTextEditUnits);
        seekBarRebate = findViewById(R.id.seekBarEditRebate);
        TextView textRebate = findViewById(R.id.textEditRebate);

        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textRebate.setText("Rebate " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        findViewById(R.id.buttonSaveChanges).setOnClickListener(view -> saveChanges());

        if (savedInstanceState == null) {
            loadBill();
        }
    }

    private void loadBill() {
        BillDatabaseHelper databaseHelper = new BillDatabaseHelper(this);

        try (Cursor cursor = databaseHelper.getReadableDatabase().query(
                "bills",
                new String[]{"month", "units", "rebate"},
                "id = ?",
                new String[]{String.valueOf(billId)},
                null, null, null)) {

            if (!cursor.moveToFirst()) {
                Toast.makeText(this, "Bill could not be found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            spinnerMonth.setSelection(cursor.getInt(0) - 1);
            editTextUnits.setText(String.valueOf(cursor.getInt(1)));
            seekBarRebate.setProgress(cursor.getInt(2));
        } finally {
            databaseHelper.close();
        }
    }

    private void saveChanges() {
        String input = editTextUnits.getText().toString().trim();
        int units;

        try {
            units = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            editTextUnits.setError("Enter a whole number from 1 to 1000.");
            return;
        }

        if (units < 1 || units > 1000) {
            editTextUnits.setError("Enter a value from 1 to 1000 kWh.");
            return;
        }

        editTextUnits.setError(null);

        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int rebate = seekBarRebate.getProgress();
        BillAmounts amounts = BillAmounts.calculate(units, rebate);

        ContentValues values = new ContentValues();
        values.put("month", month);
        values.put("units", units);
        values.put("rebate", rebate);
        values.put("total_charges", amounts.totalCharges);
        values.put("final_cost", amounts.finalCost);

        BillDatabaseHelper databaseHelper = new BillDatabaseHelper(this);
        int updatedRows;

        try {
            updatedRows = databaseHelper.getWritableDatabase().update(
                    "bills", values, "id = ?", new String[]{String.valueOf(billId)});
        } catch (SQLiteException e) {
            Toast.makeText(this, "Could not save the changes", Toast.LENGTH_LONG).show();
            return;
        } finally {
            databaseHelper.close();
        }

        if (updatedRows == 0) {
            Toast.makeText(this, "Bill could not be found", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Bill updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}