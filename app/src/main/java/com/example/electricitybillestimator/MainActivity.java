package com.example.electricitybillestimator;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.Button;
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

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUnits;
    private SeekBar seekBarRebate;
    private TextView textTotalCharges;
    private TextView textFinalCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextUnits = findViewById(R.id.editTextUnits);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        textTotalCharges = findViewById(R.id.textTotalCharges);
        textFinalCost = findViewById(R.id.textFinalCost);
        clearResults();

        TextView textRebate = findViewById(R.id.textRebate);
        textRebate.setText("Rebate " + seekBarRebate.getProgress() + "%");

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

        Button buttonCalculate = findViewById(R.id.buttonCalculate);
        buttonCalculate.setOnClickListener(view -> calculateBill());

        Button buttonHistory = findViewById(R.id.buttonHistory);
        buttonHistory.setOnClickListener(view ->
                startActivity(new Intent(this, BillHistoryActivity.class)));

        Button buttonAbout = findViewById(R.id.buttonAbout);
        buttonAbout.setOnClickListener(view ->
                startActivity(new Intent(this, AboutActivity.class)));
    }

    private void clearResults() {
        textTotalCharges.setText("Total Charges:");
        textFinalCost.setText("Final Cost After Rebate:");
    }

    private void calculateBill() {
        String input = editTextUnits.getText().toString().trim();
        int units;

        clearResults();

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

        int rebate = seekBarRebate.getProgress();
        BillAmounts amounts = BillAmounts.calculate(units, rebate);

        textTotalCharges.setText(String.format(
                Locale.ROOT, "Total Charges: RM%.2f", amounts.totalCharges));
        textFinalCost.setText(String.format(
                Locale.ROOT, "Final Cost After Rebate: RM%.2f", amounts.finalCost));

        Spinner spinnerMonth = findViewById(R.id.spinnerMonth);
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        BillDatabaseHelper databaseHelper = new BillDatabaseHelper(this);

        try {
            databaseHelper.insertBill(
                    month, units, rebate, amounts.totalCharges, amounts.finalCost);
            Toast.makeText(this, "Bill saved", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Could not save the bill", Toast.LENGTH_LONG).show();
        } finally {
            databaseHelper.close();
        }
    }
}