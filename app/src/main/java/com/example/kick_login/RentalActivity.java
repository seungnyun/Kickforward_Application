package com.example.kick_login;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.connection.HttpConnection;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RentalActivity extends Activity {

    String userNo,billingKey,phone;
    Timestamp rentalStamp,returnStamp;

    private OutputStream outputStream = null;

    int rentalFee = 0;

    TMapView tMapView;
    TMapGpsManager gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental);

        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey( "l7xx9c8429f435ea47f0a1dee9e82d35884e" );
        linearLayoutTmap.addView(tMapView);

        /* 현재 보는 방향 */
        tMapView.setCompassMode(true);
        /* 현위치 아이콘표시 */
        tMapView.setIconVisibility(true);

        gps = new TMapGpsManager(this);
        gps.setMinTime(1000);
        gps.setMinDistance(5);
        gps.setProvider(gps.NETWORK_PROVIDER); //연결된 인터넷으로 현 위치를 받습니다.
        //gps.setProvider(gps.GPS_PROVIDER); //gps로 현 위치를 잡습니다.
        gps.OpenGps();

        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);


        // 빌링키 및 아이디 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("KickForward", Context.MODE_PRIVATE);
        String userData = sharedPreferences.getString("data","");
        try {
            // userData 문자열을 JSON 객체로 파싱
            JSONObject jsonUserData = new JSONObject(userData);

            userNo = jsonUserData.optString("no", "");
            billingKey = jsonUserData.optString("billingKey", "");
            phone = jsonUserData.optString("phone","");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // 대여 시작 직후의 데이터 받아옴
        String markerName = getIntent().getStringExtra("markerName");
        long rentalDateTime = getIntent().getLongExtra("timeStamp", 0);
        String usageRecordId = getIntent().getStringExtra("usageRecordId");

        // "대여 종료" 버튼 가져오기
        Button stopButton = findViewById(R.id.stopButton);
        // "대여 종료" 버튼에 클릭 리스너 설정
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //@SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                String returnLatitude = String.valueOf(location.getLatitude());
                String returnLongitude = String.valueOf(location.getLongitude());




                long rentalTime = rentalDateTime - System.currentTimeMillis();
                long rentalTimeInMinutes = rentalTime / (1000 * 60);

                if(rentalTimeInMinutes > 5){
                    rentalFee = 1000 + (int) ((rentalTimeInMinutes-5) * 150);
                }else rentalFee = 1000;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String rentalDatetimeString = sdf.format(new Date(rentalDateTime));
                String returnDatetimeString = sdf.format(new Date(System.currentTimeMillis()));
                try {
                    rentalStamp = Timestamp.valueOf(rentalDatetimeString);
                    returnStamp = Timestamp.valueOf(returnDatetimeString);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    // Timestamp로의 변환이 실패한 경우 에러 처리
                }



                JSONObject rentJson = new JSONObject();
                try {
                    rentJson.put("usageRecordId",usageRecordId);
                    rentJson.put("userId",userNo);
                    rentJson.put("kickboardId",markerName);
                    rentJson.put("returnDatetime", returnStamp);
                    rentJson.put("returnLatitude",returnLatitude);
                    rentJson.put("returnLongitude",returnLongitude);
                    rentJson.put("billingKey",billingKey);
                    rentJson.put("phone",phone);
                    rentJson.put("rentalFee",rentalFee);
                    rentJson.put("rentalStatus","사용완료");

                    String rentData = rentJson.toString();
                    String path = getResources().getString(R.string.server_url) + "/endRentKickboard";
                    HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
                        @Override
                        public void onResult(String data) {
                            if (data.trim().equals("1")) {

                                // returnStamp와 rentalStamp의 시간차를 구합니다.
                                long timeDifference = returnStamp.getTime() - rentalStamp.getTime();

                                // 시간차를 시, 분, 초로 변환
                                long seconds = timeDifference / 1000;
                                long minutes = seconds / 60;
                                long hours = minutes / 60;
                                seconds %= 60;
                                minutes %= 60;

                                // 시, 분, 초를 하나의 문자열로 합칩니다.
                                String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                                Intent intent = new Intent(RentalActivity.this,FeeActivity.class);
                                intent.putExtra("time", time);
                                intent.putExtra("fee",rentalFee);
                                intent.putExtra("rentalDatetime",rentalDateTime);
                                intent.putExtra("returnDatetime",returnStamp);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                    httpConnection.execute(path, rentData);
                    sendDataToArduino("B");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        // 뒤로 가기 버튼막음
    }

    private void sendDataToArduino(String data) {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

