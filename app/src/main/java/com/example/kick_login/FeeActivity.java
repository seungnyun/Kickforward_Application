package com.example.kick_login;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;

public class FeeActivity extends AppCompatActivity {

    TextView textViewTime, textViewFee;

    Button endFeeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvitiy_fee);

        textViewTime = findViewById(R.id.time);
        textViewFee = findViewById(R.id.fee);
        endFeeButton = findViewById(R.id.endFee);


        //금액 콤마 넣기위해서 포맷 생성
        DecimalFormat formatter = new DecimalFormat("###,###");


        Intent receivedIntent = getIntent();
        String time = receivedIntent.getStringExtra("time");
        int fee = receivedIntent.getIntExtra("fee",0);

        textViewTime.setText(time);
        textViewFee.setText("￦"+formatter.format(fee));

        endFeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }



}
