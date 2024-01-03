package com.example.kick_login;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.Service.TokenService;
import com.example.connection.HttpConnection;
import com.example.connection.HttpConnectionWithHeader;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback  {
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView tvId, tvName;

    private TokenService tokenService;

    TMapView tMapView;
    TMapGpsManager gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenService = new TokenService();


        try{
            SharedPreferences sharedPreferences = getSharedPreferences("KickForward", MODE_PRIVATE);
            String token = sharedPreferences.getString("token","");
            String data = sharedPreferences.getString("data","");


            JSONObject tokenJsonObject = null;
            try {
                tokenJsonObject = new JSONObject(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            if(tokenJsonObject != null){
                String id = tokenJsonObject.optString("id", "");
                // 서버로 보낼 로그인 정보를 JSON 형식으로 만들기
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (!token.isEmpty()) {
                    tokenService.checkTokenExp(token, new TokenService.TokenCheckListener() {
                        @Override
                        public void onTokenCheckResult(boolean isTokenValid) {
                            if (isTokenValid) {

                                String jsonData = jsonObject.toString();
                                String urlStr = getResources().getString(R.string.server_url) + "/checkInuse";
                                HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
                                    @Override
                                    public void onResult(String data) {
                                        if (!data.trim().equals("N")) {
                                            try {
                                                JSONObject JsonObject = new JSONObject(data);
                                                String markerName = JsonObject.optString("kickboardId");
                                                long rentalDateTime = JsonObject.optLong("returnDatetime");
                                                double rentalLatitude = JsonObject.optDouble("rentalLatitude");
                                                double rentalLongitude = JsonObject.optDouble("rentalLongitude");
                                                String usageRecordId = JsonObject.optString("usageRecordId");

                                                Intent intent = new Intent(getApplicationContext(), RentalActivity.class);
                                                intent.putExtra("markerName", markerName);
                                                intent.putExtra("timeStamp", rentalDateTime);
                                                intent.putExtra("latitude",rentalLatitude);
                                                intent.putExtra("longitude",rentalLongitude);
                                                intent.putExtra("usageRecordId",usageRecordId);
                                                startActivity(intent);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                                httpConnection.execute(urlStr, jsonData);
                            }
                        }
                    });
                }
            }


        }finally {
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

            FloatingActionButton gpsButton = findViewById(R.id.gps_btn);
            gpsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tMapView.setTrackingMode(true);
                }
            });

            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar); // 툴바를 액션바로 설정

            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 왼쪽 상단 버튼 만들기
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_dehaze_24); // 왼쪽 상단 버튼 아이콘 지정

            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.navigation_view);
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    SharedPreferences sharedPreferences = getSharedPreferences("KickForward", MODE_PRIVATE);

                    switch (item.getItemId()){
                        case R.id.item_info:
                            Intent intentInfo = new Intent(MainActivity.this, MemberInfoActivity.class);
                            startActivity(intentInfo);
                            break;
                        case R.id.item_logout:
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("token"); // "token" 키에 해당하는 데이터 삭제
                            editor.remove("data");
                            editor.apply();
                            drawerLayout.closeDrawer(GravityCompat.START);
                            Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.item_report:
                            Intent intentReport = new Intent(MainActivity.this, ReportActivity.class);
                            startActivity(intentReport);
                            break;

                    }
                    return false;
                }
            });

            View headerView = navigationView.getHeaderView(0); // 헤더 뷰 가져오기
            tvId = headerView.findViewById(R.id.tv_id); // 헤더 뷰에서 TextView 찾기
            tvName = headerView.findViewById(R.id.tv_name); // 헤더 뷰에서 TextView 찾기


            // 킥보드 리스트 받아서 지도에 마커 찍기
            String path = getResources().getString(R.string.server_url) + "/getKickBoardList";
            HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {

                @Override
                public void onResult(String data){
                    try {
                        JSONArray jsonArray = new JSONArray(data);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String serialNumber = jsonObject.getString("serialNumber");
                            double latitude = Double.parseDouble(jsonObject.getString("latitude"));
                            double longitude = Double.parseDouble(jsonObject.getString("longitude"));

                            // TMapMarker를 생성하고 위치 설정
                            TMapMarkerItem marker = new TMapMarkerItem();
                            TMapPoint point = new TMapPoint(latitude, longitude);
                            marker.setTMapPoint(point);
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.scooter);
                            marker.setIcon(bitmap);
                            marker.setName(serialNumber);

                            // 마커를 지도에 추가
                            tMapView.addMarkerItem(serialNumber, marker);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            httpConnection.execute(path,"");

            // 킥보드 마커 클릭시
            tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
                @Override
                public boolean onPressUpEvent(ArrayList markerlist, ArrayList poilist, TMapPoint point, PointF pointf) {
                    if(!markerlist.isEmpty()) {
                        // markerlist가 비어있지 않을 때 (마커가 터치되었을 때)
                        TMapMarkerItem markerItem = (TMapMarkerItem) markerlist.get(0); // 첫 번째 마커 아이템을 가져옴

                        // 마커 아이템에서 필요한 정보 추출
                        TMapPoint markerPoint = markerItem.getTMapPoint(); // 마커의 위치 정보
                        String markerName = markerItem.getName(); // 마커의 이름


//                        tMapView.setLocationPoint(markerPoint.getLongitude(),markerPoint.getLatitude());
//                        tMapView.setTrackingMode(true);
//                        tMapView.setCompassMode(false);
//                        tMapView.setIconVisibility(false);
//                        tMapView.setSightVisible(false);

                        //Toast.makeText(MainActivity.this, markerName, Toast.LENGTH_SHORT).show();


                        BottomSheet bottomSheet = new BottomSheet();
//                      bottomSheet.setTMapView(tMapView);
                        bottomSheet.setMarkerInfo(markerName,markerPoint);
                        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());

                    }

                    return false;
                }

                @Override
                public boolean onPressEvent(ArrayList markerlist,ArrayList poilist, TMapPoint point, PointF pointf) {

                    return false;
                }

            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { // 왼쪽 상단 버튼 눌렀을 때
                SharedPreferences sharedPreferences = getSharedPreferences("KickForward", MODE_PRIVATE);
                String token = sharedPreferences.getString("token","");
                String data = sharedPreferences.getString("data","");
                if (token.isEmpty()) {
                    Intent intent = new Intent(this, LoginOrJoinActivity.class);
                    startActivity(intent);
                }else{
                    tokenService.checkTokenExp(token, new TokenService.TokenCheckListener() {
                        @Override
                        public void onTokenCheckResult(boolean isTokenValid) {
                            if (!isTokenValid) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("token");
                                editor.remove("data");
                                editor.apply();
                                drawerLayout.closeDrawer(GravityCompat.START);
                                Toast.makeText(MainActivity.this, "토큰만료. 다시 로그인 해주세요", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                try {
                                    JSONObject tokenJsonObject = new JSONObject(data);
                                    String userId = tokenJsonObject.optString("id", "");
                                    String userName = tokenJsonObject.optString("name", "");
                                    tvId.setText(userId);
                                    tvName.setText(userName);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                drawerLayout.openDrawer(GravityCompat.START);
                            }
                        }
                    });
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() { // 뒤로가기 했을 때
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
    }
}