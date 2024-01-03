package com.example.kick_login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.connection.HttpConnectionWithHeader;
import com.example.connection.HttpConnectionWithHeader.Result;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText id, password;
    private ProgressBar loadingBar;

    Button loginButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        id = findViewById(R.id.id);
        // 해당 EditText에 포커스를 주어 키보드를 자동으로 표시합니다.
        id.requestFocus();
        // 자동으로 키보드가 올라오도록 설정합니다.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        password = findViewById(R.id.password);
        loadingBar = findViewById(R.id.loadingBar);

        loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 서버로 보낼 로그인 정보를 JSON 형식으로 만들기
                JSONObject loginJson = new JSONObject();
                try {
                    loginJson.put("id", id.getText().toString());
                    loginJson.put("pass", password.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String loginData = loginJson.toString();
                String urlStr = getResources().getString(R.string.server_url) + "/login";

                HttpConnectionWithHeader httpConnection = new HttpConnectionWithHeader(new HttpConnectionWithHeader.HttpConnectionListener() {
                    @Override
                    public void onResult(Result result) {
                        loadingBar.setVisibility(View.INVISIBLE);

                        // 헤더에서 받아온 JWT 토큰과 데이터를 처리
                        String jwtToken = result.jwtToken;
                        String data = result.data;

                        if (data != null) {
                            SharedPreferences sharedPreferences = getSharedPreferences("KickForward", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("token", jwtToken); // 토큰 저장
                            editor.putString("data", data);
                            editor.commit();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                httpConnection.execute(urlStr, loginData);
            }
        });
    }

    public void backward(View view){
        finish();
    }
}
