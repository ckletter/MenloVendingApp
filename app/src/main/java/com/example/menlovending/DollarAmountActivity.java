package com.example.menlovending;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DollarAmountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dollar_amount);

        // Get the dollar amount from the Intent
        String dollarAmount = getIntent().getStringExtra("DOLLAR_AMOUNT");

        // Find the TextView and set the dollar amount
        TextView amountTextView = findViewById(R.id.dollar_amount_text_view);
        amountTextView.setText("$" + dollarAmount);
    }
}
