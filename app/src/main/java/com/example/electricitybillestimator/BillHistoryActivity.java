package com.example.electricitybillestimator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class BillHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bill_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills();
    }

    private void loadBills() {
        String[] months = getResources().getStringArray(R.array.months);
        ArrayList<String> rows = new ArrayList<>();
        ArrayList<Long> billIds = new ArrayList<>();
        BillDatabaseHelper databaseHelper = new BillDatabaseHelper(this);

        try (Cursor cursor = databaseHelper.getReadableDatabase().query(
                "bills",
                new String[]{"id", "month", "final_cost"},
                null, null, null, null,
                "month ASC, id DESC")) {

            int idColumn = cursor.getColumnIndexOrThrow("id");
            int monthColumn = cursor.getColumnIndexOrThrow("month");
            int costColumn = cursor.getColumnIndexOrThrow("final_cost");

            while (cursor.moveToNext()) {
                billIds.add(cursor.getLong(idColumn));
                int month = cursor.getInt(monthColumn);
                double finalCost = cursor.getDouble(costColumn);
                rows.add(String.format(
                        Locale.ROOT, "%s - RM%.2f", months[month - 1], finalCost));
            }
        } finally {
            databaseHelper.close();
        }

        ListView listBills = findViewById(R.id.listBills);
        listBills.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, rows));

        listBills.setOnItemClickListener((parent, view, position, id) -> {
            long billId = billIds.get(position);
            Intent intent = new Intent(this, BillDetailsActivity.class);
            intent.putExtra(BillDetailsActivity.EXTRA_BILL_ID, billId);
            startActivity(intent);
        });
    }
}