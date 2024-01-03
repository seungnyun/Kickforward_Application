package com.example.kick_login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connection.HttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class Join2Activity extends AppCompatActivity {
    private EditText id, password, confirmPassword;
    private Button join;
    private String name, phone, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join2);

        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        email = getIntent().getStringExtra("email");

        id = findViewById(R.id.id);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);

        // 해당 EditText에 포커스를 주어 키보드를 자동으로 표시합니다.
        id.requestFocus();
        // 자동으로 키보드가 올라오도록 설정합니다.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        join = findViewById(R.id.join);
        join.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFormValid()){
                    return;
                }
                JSONObject joinJson = new JSONObject();
                try {
                    joinJson.put("id", id.getText().toString());
                    joinJson.put("pass", password.getText().toString());
                    joinJson.put("name", name);
                    joinJson.put("phone", phone);
                    joinJson.put("email", email);

                    String joinData = joinJson.toString();

                    String path = getResources().getString(R.string.server_url) + "/join";
                    HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
                        @Override
                        public void onResult(String data) {
                            //loadingBar.setVisibility(View.INVISIBLE);
                            if (data.trim().equals("1")) {
                                Toast.makeText(Join2Activity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Join2Activity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    });

                    httpConnection.execute(path, joinData);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean isFormValid(){
        if (TextUtils.isEmpty(id.getText())
                || TextUtils.isEmpty(password.getText())
                || TextUtils.isEmpty(confirmPassword.getText())
        ) {
            Toast.makeText(getApplicationContext(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void backward(View view){
        finish();
    }
}
