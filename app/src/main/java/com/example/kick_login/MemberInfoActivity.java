package com.example.kick_login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.example.Service.TokenService;
import com.example.connection.HttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.bootpay.android.Bootpay;
import kr.co.bootpay.android.events.BootpayEventListener;
import kr.co.bootpay.android.models.BootExtra;
import kr.co.bootpay.android.models.BootItem;
import kr.co.bootpay.android.models.BootUser;
import kr.co.bootpay.android.models.Payload;

public class MemberInfoActivity extends AppCompatActivity {

    private String id, name, phone, email, license, billingKey;
    String receipt_id ="";

    private TokenService tokenService;

    TextView textViewId, textViewName, textViewPhone, textViewEmail, textViewLicense, textViewBillingKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);


        tokenService = new TokenService();

        SharedPreferences sharedPreferences = getSharedPreferences("KickForward", MODE_PRIVATE);
        String token = sharedPreferences.getString("token","");
        String data = sharedPreferences.getString("data","");


        tokenService.checkTokenExp(token, new TokenService.TokenCheckListener() {
            @Override
            public void onTokenCheckResult(boolean isTokenValid) {
                if (!isTokenValid) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("token");
                    editor.remove("data");
                    editor.apply();
                    Toast.makeText(MemberInfoActivity.this, "토큰만료. 다시 로그인 해주세요", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MemberInfoActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {

                    JSONObject memberInfoJson = new JSONObject();
                    try {
                        JSONObject tokenJsonObject = new JSONObject(data);
                        String userId = tokenJsonObject.optString("id", "");
                        String userPass = tokenJsonObject.optString("pass", "");
                        memberInfoJson.put("id",userId);
                        memberInfoJson.put("pass",userPass);
                        String MemberInfoData = memberInfoJson.toString();


                        String path = getResources().getString(R.string.server_url) + "/memberInfo";
                        HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
                            @Override
                            public void onResult(String data) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("data", data);
                                editor.commit();

                                try {
                                    JSONObject tokenJsonObject = new JSONObject(data);
                                    id = tokenJsonObject.optString("id","");
                                    name = tokenJsonObject.optString("name", "");
                                    phone = tokenJsonObject.optString("phone", "");
                                    email = tokenJsonObject.optString("email", "");
                                    license = tokenJsonObject.optString("license", "");
                                    billingKey = tokenJsonObject.optString("billingKey", "");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                textViewName = findViewById(R.id.name);
                                textViewPhone = findViewById(R.id.phone);
                                textViewEmail = findViewById(R.id.email);
                                textViewLicense = findViewById(R.id.license);
                                textViewBillingKey = findViewById(R.id.billingKey);
                                textViewBillingKey.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        BootExtra extra = new BootExtra()
                                                .setCardQuota("0"); // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)
                                        List<BootItem> items = new ArrayList<>();
                                        BootItem item1 = new BootItem().setName("킥보드 자동결제").setId("ITEM_CODE_Kickboard").setQty(1).setPrice(0d);
                                        items.add(item1);

                                        Payload payload = new Payload();
                                        payload.setApplicationId("645f193b3049c8001a96868f")
                                                .setOrderName("킥포워드 카드등록")
                                                .setPg("웰컴")
                                                .setMethod("카드자동")
                                                .setOrderId("1234")
                                                .setSubscriptionId("1234")
                                                .setPrice(0d)
                                                .setUser(getBootUser())
                                                .setExtra(extra)
                                                .setItems(items);

                                        Map<String, Object> map = new HashMap<>();
                                        map.put("1", "abcdef");
                                        map.put("2", "abcdef55");
                                        map.put("3", 1234);
                                        payload.setMetadata(map);

                                        Bootpay.init(getSupportFragmentManager(), getApplicationContext())
                                                .setPayload(payload)
                                                .setEventListener(new BootpayEventListener() {
                                                    @Override
                                                    public void onCancel(String data) {
                                                        Log.d("bootpay", "cancel: " + data);
                                                    }

                                                    @Override
                                                    public void onError(String data) {
                                                        Log.d("bootpay", "error: " + data);
                                                    }

                                                    @Override
                                                    public void onClose() {
                                                        Bootpay.removePaymentWindow();
                                                    }

                                                    @Override
                                                    public void onIssued(String data) {
                                                        Log.d("bootpay", "issued: " +data);
                                                    }

                                                    @Override
                                                    public boolean onConfirm(String data) {
                                                        Log.d("bootpay", "confirm: " + data);
                                                        return true;
                                                    }

                                                    @Override
                                                    public void onDone(String data) {
                                                        Log.d("done", data);
                                                        try {
                                                            JSONObject responseJson = new JSONObject(data);
                                                            JSONObject dataJson = responseJson.getJSONObject("data");
                                                            if (dataJson.has("receipt_id")) {
                                                                String receiptId = dataJson.getString("receipt_id");
                                                                receipt_id = receiptId;
                                                                sendReceipt_Id();
                                                            } else {
                                                                Log.d("done", "No receipt_id found in the response data.");
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).requestSubscription();
                                    }

                                    public BootUser getBootUser() {
                                        String userId = id;
                                        BootUser user = new BootUser();
                                        user.setId(userId);
                                        //user.setArea("서울");
                                        //user.setGender(1); //1: 남자, 0: 여자
                                        user.setEmail(email); // 추가된 부분
                                        user.setPhone(phone); // 추가된 부분
                                        //user.setBirth("1988-06-10");
                                        user.setUsername(name);
                                        return user;
                                    }

                                    public void sendReceipt_Id() {
                                        JSONObject joinJson = new JSONObject();
                                        try {
                                            joinJson.put("id", id);
                                            joinJson.put("receipt_id", receipt_id);
                                            String certificationData = joinJson.toString();

                                            String path = getResources().getString(R.string.server_url) + "/pay";
                                            HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
                                                @Override
                                                public void onResult(String data) {
                                                    if (data.trim().equals("1")) {
                                                        Toast.makeText(getApplicationContext(), "등록 완료", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(MemberInfoActivity.this, MainActivity.class);
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

                                textViewName.setText(name);
                                textViewPhone.setText(phone);
                                textViewEmail.setText(email);

                                if (license.equals("")) {
                                    textViewLicense.setText("인증하기");
                                    textViewLicense.setClickable(true);
                                }else if(license.equals("1")){
                                    textViewLicense.setText("인증완료");
                                    textViewLicense.setClickable(false);
                                }
                                if (billingKey.equals("")) {
                                    textViewBillingKey.setText("등록하기");
                                    textViewBillingKey.setClickable(true);
                                }else{
                                    textViewBillingKey.setText("등록완료");
                                    textViewBillingKey.setClickable(false);
                                    textViewBillingKey.setOnClickListener(null);
                                }




                            }
                        });
                        httpConnection.execute(path, MemberInfoData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void insertLicense(View view){
        Intent intent = new Intent(MemberInfoActivity.this, LicenseActivity.class);
        intent.putExtra("id",id);
        intent.putExtra("name",name);
        startActivity(intent);
    }

    public void backward(View view){
        finish();
    }

}
