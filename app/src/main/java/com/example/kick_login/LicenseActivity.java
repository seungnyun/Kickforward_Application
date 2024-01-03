package com.example.kick_login;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.connection.HttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class LicenseActivity extends AppCompatActivity {



    private EditText editTextbirthDate,editTextRegionCode, editTextYear, editTextSixDigits, editTextTwoDigits, editTextserialNumber;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);



        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");

        editTextbirthDate = findViewById(R.id.birthDate);
        // 해당 EditText에 포커스를 주어 키보드를 자동으로 표시합니다.
        editTextbirthDate.requestFocus();
        // 자동으로 키보드가 올라오도록 설정합니다.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        editTextRegionCode = findViewById(R.id.regionCode);
        editTextYear = findViewById(R.id.year);
        editTextSixDigits = findViewById(R.id.sixDigits);
        editTextTwoDigits = findViewById(R.id.twoDigits);
        editTextserialNumber = findViewById(R.id.serialNumber);


        Button submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFormValid()){
                    return;
                }
                JSONObject joinJson = new JSONObject();
                try {
                    joinJson.put("id", id);
                    joinJson.put("userName", name);
                    joinJson.put("birthDate", editTextbirthDate.getText().toString());
                    joinJson.put("licenseNo01", editTextRegionCode.getText().toString());
                    joinJson.put("licenseNo02", editTextYear.getText().toString());
                    joinJson.put("licenseNo03", editTextSixDigits.getText().toString());
                    joinJson.put("licenseNo04", editTextTwoDigits.getText().toString());
                    joinJson.put("serialNo", editTextserialNumber.getText().toString());

                    String certificationData = joinJson.toString();

                    String path = getResources().getString(R.string.server_url) + "/certification";
                    HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
                        @Override
                        public void onResult(String data) {
                            if (data.trim().equals("1")) {
                                Toast.makeText(getApplicationContext(), "등록 완료", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LicenseActivity.this, MemberInfoActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(), "오류 발생 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    httpConnection.execute(path, certificationData);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isFormValid(){
        if (TextUtils.isEmpty(editTextserialNumber.getText())
                || TextUtils.isEmpty(editTextRegionCode.getText())
                || TextUtils.isEmpty(editTextYear.getText())
                || TextUtils.isEmpty(editTextSixDigits.getText())
                || TextUtils.isEmpty(editTextTwoDigits.getText())
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

