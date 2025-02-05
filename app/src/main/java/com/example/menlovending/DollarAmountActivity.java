package com.example.menlovending;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DollarAmountActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dollar_amount);

        // Retrieve the extra from the intent
        double dollarAmount = getIntent().getDoubleExtra("DOLLAR_AMOUNT", 0.0);

        // Display the dollarAmount
        TextView amountTextView = findViewById(R.id.dollar_amount_text_view);
        amountTextView.setText(String.format("$%.2f", dollarAmount));

        // Handle the Back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish()); // Ends current activity and returns to MainActivity
    }
}
