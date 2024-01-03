package com.example.kick_login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Join1Activity extends AppCompatActivity {

    private EditText name, phone, email;
    private Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join1);

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);

        // 해당 EditText에 포커스를 주어 키보드를 자동으로 표시합니다.
        name.requestFocus();
        // 자동으로 키보드가 올라오도록 설정합니다.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFormValid()) return;
                Intent it = new Intent(getApplicationContext(),Join2Activity.class);
                it.putExtra("name",name.getText().toString());
                it.putExtra("phone",phone.getText().toString());
                it.putExtra("email",email.getText().toString());
                startActivity(it);
            }
        });



    }

    private boolean isFormValid(){
        if (TextUtils.isEmpty(name.getText())
                || TextUtils.isEmpty(phone.getText())
                || TextUtils.isEmpty(email.getText())
        ) {
            Toast.makeText(getApplicationContext(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void backward(View view){
        finish();
    }
}
