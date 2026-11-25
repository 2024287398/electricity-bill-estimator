package com.example.electricitybillestimator;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class BillDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_BILL_ID = "bill_id";

    private long billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bill_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        billId = getIntent().getLongExtra(EXTRA_BILL_ID, -1);

        findViewById(R.id.buttonEditBill).setOnClickListener(view -> {
            Intent intent = new Intent(this, EditBillActivity.class);
            intent.putExtra(EXTRA_BILL_ID, billId);
            startActivity(intent);
        });

        findViewById(R.id.buttonDeleteBill).setOnClickListener(view -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showBill();
    }

    private void showBill() {
        BillDatabaseHelper databaseHelper = new BillDatabaseHelper(this);

        try (Cursor cursor = databaseHelper.getReadableDatabase().query(
                "bills",
                new String[]{"month", "units", "total_charges", "rebate", "final_cost"},
                "id = ?",
                new String[]{String.valueOf(billId)},
                null, null, null)) {

            if (!cursor.moveToFirst()) {
                Toast.makeText(this, "Bill could not be found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String[] months = getResources().getStringArray(R.array.months);
            String details = String.format(
                    Locale.ROOT,
                    "Month: %s\nUsage: %d kWh\nTotal Charges: RM%.2f\n"
                            + "Rebate: %d%%\nFinal Cost: RM%.2f",
                    months[cursor.getInt(0) - 1],
                    cursor.getInt(1),
                    cursor.getDouble(2),
                    cursor.getInt(3),
                    cursor.getDouble(4));

            TextView textBillDetails = findViewById(R.id.textBillDetails);
            textBillDetails.setText(details);
        } finally {
            databaseHelper.close();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete this bill?")
                .setMessage("This will remove the bill from your history.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteBill())
                .show();
    }

    private void deleteBill() {
        BillDatabaseHelper databaseHelper = new BillDatabaseHelper(this);
        int deletedRows;

        try {
            deletedRows = databaseHelper.getWritableDatabase().delete(
                    "bills", "id = ?", new String[]{String.valueOf(billId)});
        } catch (SQLiteException e) {
            Toast.makeText(this, "Could not delete the bill", Toast.LENGTH_LONG).show();
            return;
        } finally {
            databaseHelper.close();
        }

        String message = deletedRows > 0 ? "Bill deleted" : "Bill could not be found";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}