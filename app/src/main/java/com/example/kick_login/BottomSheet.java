package com.example.kick_login;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Service.TokenService;
import com.example.connection.HttpConnection;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class BottomSheet extends BottomSheetDialogFragment {

    TextView clickMaker;
    String markerName;
    TMapPoint markerPoint;

    TMapView tMapView;

    TMapGpsManager gps;

    private TokenService tokenService;

    Timestamp rentalStamp;




    // 현재 선택한 킥보드 정보 받아오는 메소드
    public void setMarkerInfo(String marker, TMapPoint point) {
        markerName = marker;
        markerPoint = point;
    }

    public void setTMapView(TMapView tMapView) {
        this.tMapView = tMapView;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clickMaker = view.findViewById(R.id.clickMaker);

        tokenService = new TokenService();


//        view.findViewById(R.id.btn_rent).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("KickForward", Context.MODE_PRIVATE);
//                String token = sharedPreferences.getString("token","");
//                String data = sharedPreferences.getString("data","");
//
//                long rentalDateTime = System.currentTimeMillis();
//
//                if (token.isEmpty()) {
//                    // SharedPreferences에 값이 없으면 로그인을 유도하는 토스트 메시지 띄우기
//                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
//                    // 로그인 화면으로 이동하도록 인텐트 생성 및 startActivity 호출
//                    Intent intent = new Intent(requireContext(), LoginActivity.class);
//                    startActivity(intent);
//                }else{
//                    tokenService.checkTokenExp(token, new TokenService.TokenCheckListener() {
//                        @Override
//                        public void onTokenCheckResult(boolean isTokenValid) {
//                            if (!isTokenValid) {
//                                SharedPreferences.Editor editor = sharedPreferences.edit();
//                                editor.remove("token");
//                                editor.remove("data");
//                                editor.apply();
//                                // SharedPreferences에 값이 없으면 로그인을 유도하는 토스트 메시지 띄우기
//                                Toast.makeText(requireContext(), "토큰만료. 다시 로그인 해주세요", Toast.LENGTH_SHORT).show();
//                                // 로그인 화면으로 이동하도록 인텐트 생성 및 startActivity 호출
//                                Intent intent = new Intent(requireContext(), LoginActivity.class);
//                                startActivity(intent);
//                            } else {
//                                try {
//                                    // 저장된 JSON 문자열을 가져옴
//                                    String data = sharedPreferences.getString("data", "");
//
//                                    // JSON 문자열을 JSON 객체로 파싱
//                                    JSONObject jsonObject = new JSONObject(data);
//
//                                    // "라이센스" 필드의 값을 가져옴
//                                    String license = jsonObject.getString("license");
//                                    String billingKey = jsonObject.getString("billingKey");
//                                    String id = jsonObject.getString("id");
//                                    String userNo  = jsonObject.optString("no", "");
//
//
//                                    //시간 저장을 위한 변환
//                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//                                    String rentalDatetimeString = sdf.format(new Date(rentalDateTime));
//                                    try {
//                                        rentalStamp = Timestamp.valueOf(rentalDatetimeString);
//                                    } catch (IllegalArgumentException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    if(!license.isEmpty() && !billingKey.isEmpty()){
//                                        JSONObject rentJson = new JSONObject();
//                                        try {
//                                            rentJson.put("kickboardId",markerName);
//                                            rentJson.put("userId",userNo);
//                                            rentJson.put("rentalDatetime",rentalStamp);
//                                            rentJson.put("rentalLatitude",markerPoint.getLatitude());
//                                            rentJson.put("rentalLongitude",markerPoint.getLongitude());
//                                            rentJson.put("rentalStatus","사용중");
//
//                                            String rentData = rentJson.toString();
//                                            String path = getResources().getString(R.string.server_url) + "/rentKickboard";
//                                            HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
//                                                @Override
//                                                public void onResult(String data) {
//                                                    Log.d("데이터",data);
//                                                    Toast.makeText(requireContext(), "대여시작!", Toast.LENGTH_SHORT).show();
//                                                    Intent intent = new Intent(requireContext(), RentalActivity.class);
//                                                    intent.putExtra("markerName", markerName);
//                                                    intent.putExtra("timeStamp", rentalDateTime);intent.putExtra("latitude",markerPoint.getLatitude());
//                                                    intent.putExtra("longitude",markerPoint.getLongitude());
//                                                    intent.putExtra("usageRecordId",data);
//                                                    startActivity(intent);
//                                                    dismiss();
//                                                }
//                                            });
//
//                                            httpConnection.execute(path, rentData);
//
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    }else{
//                                        //렌트 킥보드 안되었을시 작성해야댐.
//                                    }
//
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    });
//                }
//            }
//        });

        view.findViewById(R.id.btn_bRent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("KickForward", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token","");
                String data = sharedPreferences.getString("data","");

                long rentalDateTime = System.currentTimeMillis();

                if (token.isEmpty()) {
                    Intent intent = new Intent(getContext(), LoginOrJoinActivity.class);
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
                                // SharedPreferences에 값이 없으면 로그인을 유도하는 토스트 메시지 띄우기
                                Toast.makeText(requireContext(), "토큰만료. 다시 로그인 해주세요", Toast.LENGTH_SHORT).show();
                                // 로그인 화면으로 이동하도록 인텐트 생성 및 startActivity 호출
                                Intent intent = new Intent(requireContext(), LoginActivity.class);
                                startActivity(intent);
                            } else {
                                try {
                                    // 저장된 JSON 문자열을 가져옴
                                    String data = sharedPreferences.getString("data", "");

                                    // JSON 문자열을 JSON 객체로 파싱
                                    JSONObject jsonObject = new JSONObject(data);

                                    // "라이센스" 필드의 값을 가져옴
                                    String license = jsonObject.getString("license");
                                    String billingKey = jsonObject.getString("billingKey");
                                    String id = jsonObject.getString("id");
                                    String userNo  = jsonObject.optString("no", "");


                                    //시간 저장을 위한 변환
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                    String rentalDatetimeString = sdf.format(new Date(rentalDateTime));
                                    try {
                                        rentalStamp = Timestamp.valueOf(rentalDatetimeString);
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    }

                                    if(!license.isEmpty() && !billingKey.isEmpty()){
                                        JSONObject rentJson = new JSONObject();
                                        try {
                                            rentJson.put("kickboardId",markerName);
                                            rentJson.put("userId",userNo);
                                            rentJson.put("rentalDatetime",rentalStamp);
                                            rentJson.put("rentalLatitude",markerPoint.getLatitude());
                                            rentJson.put("rentalLongitude",markerPoint.getLongitude());
                                            rentJson.put("rentalStatus","사용중");

                                            String rentData = rentJson.toString();
                                            String path = getResources().getString(R.string.server_url) + "/rentKickboard";
                                            HttpConnection httpConnection = new HttpConnection(new HttpConnection.HttpConnectionListener() {
                                                @Override
                                                public void onResult(String data) {
                                                    Toast.makeText(requireContext(), "대여시작!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(requireContext(), ValidationActivity.class);
                                                    intent.putExtra("markerName", markerName);
                                                    intent.putExtra("timeStamp", rentalDateTime);
                                                    intent.putExtra("latitude",markerPoint.getLatitude());
                                                    intent.putExtra("usageRecordId",data);
                                                    startActivity(intent);
                                                    dismiss();
                                                }
                                            });

                                            httpConnection.execute(path, rentData);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(requireContext(), "운전면허 및 카드등록이 필요합니다!", Toast.LENGTH_SHORT).show();
                                        Intent it = new Intent(requireContext(),MemberInfoActivity.class);
                                        startActivity(it);
                                        dismiss();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });



//        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                if (tMapView != null) {
////                    gps = new TMapGpsManager(getContext());
////                    gps.setMinTime(1000);
////                    gps.setMinDistance(5);
////                    //gps.setProvider(gps.NETWORK_PROVIDER); //연결된 인터넷으로 현 위치를 받습니다.
////                    gps.setProvider(gps.GPS_PROVIDER); //gps로 현 위치를 잡습니다.
////                    gps.OpenGps();
////
////                    tMapView.setCompassMode(true);
////                    tMapView.setIconVisibility(true);
////                    tMapView.setSightVisible(true);
////                    tMapView.setTrackingMode(true);
////                }
//                dismiss();
//            }
//        });

        clickMaker.setText(markerName);

    }
}
