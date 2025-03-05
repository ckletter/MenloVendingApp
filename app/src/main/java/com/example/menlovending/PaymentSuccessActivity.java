package com.example.menlovending;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;

public class PaymentSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        // Get the amount from the intent
        TextView amountTextView = findViewById(R.id.payment_amount);
        double amount = getIntent().getDoubleExtra("PAYMENT_AMOUNT", 0.0);
        amountTextView.setText("Amount Paid: $%.2f" + amount);

        Button doneButton = findViewById(R.id.done_button);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to return to MainActivity
                Intent intent = new Intent(PaymentSuccessActivity.this, MainActivity.class);

                // Clear top and create a new task to ensure MainActivity is at the top
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);

                // Finish the current activity
                finish();
            }
        });
        // Start the checkmark animation
        LottieAnimationView animationView = findViewById(R.id.success_animation);
        animationView.playAnimation();
    }

    public void finish(View view) {
        finish(); // Close the activity when "Done" is clicked
    }
}
