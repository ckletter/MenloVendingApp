package com.example.menlovending;

import android.content.Intent;
import android.os.Bundle;
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
        String amount = getIntent().getStringExtra("PAYMENT_AMOUNT");
        amountTextView.setText("Amount Paid: $" + amount);

        // Start the checkmark animation
        LottieAnimationView animationView = findViewById(R.id.success_animation);
        animationView.playAnimation();
    }

    public void finish(View view) {
        finish(); // Close the activity when "Done" is clicked
    }
}
